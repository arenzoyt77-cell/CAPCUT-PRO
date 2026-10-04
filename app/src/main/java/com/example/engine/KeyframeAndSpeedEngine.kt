package com.example.engine

import com.example.model.Keyframe
import com.example.model.KeyframeInterpolation
import com.example.model.KeyframeProperty
import com.example.model.SpeedPoint
import com.example.model.TimelineClip
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Real Keyframe Interpolation Engine & Variable Speed Curve Engine.
 * Evaluates multi-property keyframe curves and piecewise speed ramps at millisecond precision.
 */
object KeyframeAndSpeedEngine {

    data class SpeedPresetDefinition(
        val id: String,
        val name: String,
        val description: String,
        val points: List<SpeedPoint>
    )

    val speedPresets: List<SpeedPresetDefinition> = listOf(
        SpeedPresetDefinition(
            id = "normal",
            name = "Normal",
            description = "Constant 1.0x speed across clip",
            points = listOf(
                SpeedPoint(0.0f, 1.0f),
                SpeedPoint(0.25f, 1.0f),
                SpeedPoint(0.5f, 1.0f),
                SpeedPoint(0.75f, 1.0f),
                SpeedPoint(1.0f, 1.0f)
            )
        ),
        SpeedPresetDefinition(
            id = "montage",
            name = "Montage",
            description = "Fast entry ramp into dramatic slow-mo peak then whip exit",
            points = listOf(
                SpeedPoint(0.0f, 2.4f),
                SpeedPoint(0.25f, 3.2f),
                SpeedPoint(0.50f, 0.35f),
                SpeedPoint(0.78f, 0.45f),
                SpeedPoint(1.0f, 2.8f)
            )
        ),
        SpeedPresetDefinition(
            id = "jump",
            name = "Jump Cut",
            description = "Rhythmic high-velocity beat jump with sudden freeze-slow drop",
            points = listOf(
                SpeedPoint(0.0f, 1.0f),
                SpeedPoint(0.30f, 4.5f),
                SpeedPoint(0.52f, 0.30f),
                SpeedPoint(0.80f, 3.8f),
                SpeedPoint(1.0f, 1.0f)
            )
        ),
        SpeedPresetDefinition(
            id = "flash",
            name = "Flash In",
            description = "Ultra-fast 5x burst into smooth 0.5x reveal",
            points = listOf(
                SpeedPoint(0.0f, 5.0f),
                SpeedPoint(0.20f, 3.5f),
                SpeedPoint(0.45f, 0.5f),
                SpeedPoint(0.75f, 0.75f),
                SpeedPoint(1.0f, 1.0f)
            )
        ),
        SpeedPresetDefinition(
            id = "hero",
            name = "Hero Time",
            description = "Normal lead-in dropping to 0.25x cinematic hero slow-motion",
            points = listOf(
                SpeedPoint(0.0f, 1.2f),
                SpeedPoint(0.28f, 1.4f),
                SpeedPoint(0.48f, 0.25f),
                SpeedPoint(0.76f, 0.25f),
                SpeedPoint(1.0f, 1.6f)
            )
        ),
        SpeedPresetDefinition(
            id = "slow",
            name = "Smooth Slow",
            description = "Silky 0.4x slow motion curve with gentle entry and exit",
            points = listOf(
                SpeedPoint(0.0f, 0.8f),
                SpeedPoint(0.25f, 0.4f),
                SpeedPoint(0.50f, 0.4f),
                SpeedPoint(0.75f, 0.4f),
                SpeedPoint(1.0f, 0.8f)
            )
        ),
        SpeedPresetDefinition(
            id = "custom",
            name = "Custom Curve",
            description = "User-defined multi-point velocity curve",
            points = listOf(
                SpeedPoint(0.0f, 1.0f),
                SpeedPoint(0.25f, 1.8f),
                SpeedPoint(0.50f, 0.5f),
                SpeedPoint(0.75f, 2.2f),
                SpeedPoint(1.0f, 1.0f)
            )
        )
    )

    /**
     * Evaluates instantaneous speed multiplier at normalized clip progress [0f..1f].
     */
    fun evaluateSpeedAtFraction(clip: TimelineClip, fraction: Float): Float {
        val clamped = fraction.coerceIn(0f, 1f)
        val pts = clip.speedPoints.sortedBy { it.positionFraction }
        if (pts.size < 2) {
            return clip.speedMultiplier.coerceIn(0.1f, 10f)
        }
        for (i in 0 until pts.size - 1) {
            val left = pts[i]
            val right = pts[i + 1]
            if (clamped >= left.positionFraction && clamped <= right.positionFraction) {
                val span = (right.positionFraction - left.positionFraction).coerceAtLeast(0.0001f)
                val localT = (clamped - left.positionFraction) / span
                val smoothT = localT * localT * (3f - 2f * localT)
                val curveSpeed = left.speedMultiplier + (right.speedMultiplier - left.speedMultiplier) * smoothT
                return (curveSpeed * clip.speedMultiplier).coerceIn(0.1f, 10f)
            }
        }
        return (pts.last().speedMultiplier * clip.speedMultiplier).coerceIn(0.1f, 10f)
    }

    /**
     * Computes effective timeline duration of a clip in milliseconds,
     * integrating both constant speedMultiplier and any variable speed curve points.
     */
    fun computeEffectiveClipDurationMs(clip: TimelineClip): Long {
        val rawTrimmedDuration = (clip.trimEndMs - clip.trimStartMs).coerceAtLeast(100L)
        if (clip.isFrozen) {
            return rawTrimmedDuration
        }
        val pts = clip.speedPoints.sortedBy { it.positionFraction }
        if (pts.size < 2) {
            val speed = clip.speedMultiplier.coerceIn(0.1f, 10f)
            return (rawTrimmedDuration / speed).toLong().coerceAtLeast(100L)
        }
        // Numerical integration across 40 segments: dt_timeline = ds_source / v(s)
        val steps = 40
        var totalFactor = 0.0
        for (i in 0 until steps) {
            val midFraction = (i + 0.5f) / steps.toFloat()
            val v = evaluateSpeedAtFraction(clip, midFraction).coerceIn(0.1f, 10f)
            totalFactor += (1.0 / v) / steps
        }
        return (rawTrimmedDuration * totalFactor).toLong().coerceAtLeast(100L)
    }

    /**
     * Maps elapsed time inside a clip on the timeline (0..effectiveDurationMs)
     * to the exact source media timestamp (trimStartMs..trimEndMs), accounting for
     * speed curves, constant speed, reverse playback, and freeze-frame.
     */
    fun mapTimelineOffsetToSourceMs(clip: TimelineClip, clipOffsetMs: Long): Long {
        val rawTrimmedDuration = (clip.trimEndMs - clip.trimStartMs).coerceAtLeast(100L)
        if (clip.isFrozen) {
            return clip.trimStartMs
        }
        val effectiveDuration = computeEffectiveClipDurationMs(clip).coerceAtLeast(1L)
        val normalizedTimeline = (clipOffsetMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)

        val sourceFraction = if (clip.speedPoints.size < 2) {
            normalizedTimeline
        } else {
            // Integrate velocity over timeline fraction to obtain source fraction
            val steps = 32
            var unnormalizedDistance = 0f
            var currentNormDistance = 0f
            val targetStep = (normalizedTimeline * steps).toInt()
            for (i in 0 until steps) {
                val frac = (i + 0.5f) / steps.toFloat()
                val v = evaluateSpeedAtFraction(clip, frac)
                unnormalizedDistance += v
                if (i < targetStep) {
                    currentNormDistance += v
                } else if (i == targetStep) {
                    val subProgress = (normalizedTimeline * steps) - targetStep
                    currentNormDistance += v * subProgress
                }
            }
            if (unnormalizedDistance > 0.0001f) {
                (currentNormDistance / unnormalizedDistance).coerceIn(0f, 1f)
            } else {
                normalizedTimeline
            }
        }

        val finalFraction = if (clip.isReversed) (1f - sourceFraction) else sourceFraction
        return (clip.trimStartMs + (rawTrimmedDuration * finalFraction).toLong())
            .coerceIn(clip.trimStartMs, clip.trimEndMs)
    }

    /**
     * Evaluates a keyframed property at [clipLocalMs] (relative to the start of the clip/item).
     * If no keyframes exist for [property], returns [fallbackValue].
     */
    fun evaluateProperty(
        keyframes: List<Keyframe>,
        property: KeyframeProperty,
        clipLocalMs: Long,
        fallbackValue: Float
    ): Float {
        val propFrames = keyframes
            .filter { it.property == property }
            .sortedBy { it.timestampMs }

        if (propFrames.isEmpty()) return fallbackValue
        if (propFrames.size == 1) return propFrames.first().value
        if (clipLocalMs <= propFrames.first().timestampMs) return propFrames.first().value
        if (clipLocalMs >= propFrames.last().timestampMs) return propFrames.last().value

        for (i in 0 until propFrames.size - 1) {
            val startKf = propFrames[i]
            val endKf = propFrames[i + 1]
            if (clipLocalMs in startKf.timestampMs..endKf.timestampMs) {
                val span = (endKf.timestampMs - startKf.timestampMs).coerceAtLeast(1L)
                val rawT = ((clipLocalMs - startKf.timestampMs).toFloat() / span.toFloat()).coerceIn(0f, 1f)
                val easedT = applyInterpolation(rawT, startKf.interpolation)
                return startKf.value + (endKf.value - startKf.value) * easedT
            }
        }
        return propFrames.last().value
    }

    /**
     * Mathematical interpolation curve evaluator.
     */
    fun applyInterpolation(t: Float, mode: KeyframeInterpolation): Float {
        val x = t.coerceIn(0f, 1f)
        return when (mode) {
            KeyframeInterpolation.LINEAR -> x
            KeyframeInterpolation.SMOOTH -> x * x * (3f - 2f * x)
            KeyframeInterpolation.EASE_IN -> x * x * x
            KeyframeInterpolation.EASE_OUT -> 1f - (1f - x).pow(3)
            KeyframeInterpolation.EASE_IN_OUT -> {
                if (x < 0.5f) 4f * x * x * x else 1f - (-2f * x + 2f).pow(3) / 2f
            }
            KeyframeInterpolation.CUBIC -> {
                // Cubic Bezier approximation with dramatic cinematic S-curve (0.65, 0.0, 0.35, 1.0)
                val u = 1f - x
                val tt = x * x
                val uu = u * u
                val ttt = tt * x
                (3f * uu * x * 0.05f) + (3f * u * tt * 0.95f) + ttt
            }
        }.coerceIn(0f, 1f)
    }

    /**
     * Inserts or updates a keyframe at [timestampMs] for [property] with [value].
     * Snap threshold is 90ms so nearby edits update the existing keyframe cleanly.
     */
    fun upsertKeyframe(
        existing: List<Keyframe>,
        timestampMs: Long,
        property: KeyframeProperty,
        value: Float,
        interpolation: KeyframeInterpolation = KeyframeInterpolation.EASE_IN_OUT,
        snapThresholdMs: Long = 90L
    ): List<Keyframe> {
        val mutable = existing.toMutableList()
        val index = mutable.indexOfFirst {
            it.property == property && abs(it.timestampMs - timestampMs) <= snapThresholdMs
        }
        if (index >= 0) {
            mutable[index] = mutable[index].copy(
                timestampMs = timestampMs,
                value = value,
                interpolation = interpolation
            )
        } else {
            mutable.add(
                Keyframe(
                    timestampMs = max(0L, timestampMs),
                    property = property,
                    value = value,
                    interpolation = interpolation
                )
            )
        }
        return mutable.sortedBy { it.timestampMs }
    }

    /**
     * Checks if there is any keyframe near [timestampMs] (within [thresholdMs]).
     */
    fun hasKeyframeNear(
        keyframes: List<Keyframe>,
        timestampMs: Long,
        property: KeyframeProperty? = null,
        thresholdMs: Long = 90L
    ): Boolean {
        return keyframes.any {
            (property == null || it.property == property) &&
                abs(it.timestampMs - timestampMs) <= thresholdMs
        }
    }

    /**
     * Removes all keyframes near [timestampMs] (or for a specific property).
     */
    fun removeKeyframesNear(
        keyframes: List<Keyframe>,
        timestampMs: Long,
        property: KeyframeProperty? = null,
        thresholdMs: Long = 100L
    ): List<Keyframe> {
        return keyframes.filterNot {
            (property == null || it.property == property) &&
                abs(it.timestampMs - timestampMs) <= thresholdMs
        }
    }

    data class EvaluatedClipState(
        val posX: Float,
        val posY: Float,
        val scale: Float,
        val rotation: Float,
        val opacity: Float,
        val cropLeft: Float,
        val cropTop: Float,
        val cropRight: Float,
        val cropBottom: Float,
        val volume: Float,
        val filterIntensity: Float
    )

    fun evaluateClipStateAt(clip: TimelineClip, clipLocalMs: Long): EvaluatedClipState {
        return EvaluatedClipState(
            posX = evaluateProperty(clip.keyframes, KeyframeProperty.POS_X, clipLocalMs, clip.transformX),
            posY = evaluateProperty(clip.keyframes, KeyframeProperty.POS_Y, clipLocalMs, clip.transformY),
            scale = evaluateProperty(clip.keyframes, KeyframeProperty.SCALE, clipLocalMs, clip.scale).coerceIn(0.1f, 5f),
            rotation = evaluateProperty(clip.keyframes, KeyframeProperty.ROTATION, clipLocalMs, clip.rotation),
            opacity = evaluateProperty(clip.keyframes, KeyframeProperty.OPACITY, clipLocalMs, clip.opacity).coerceIn(0f, 1f),
            cropLeft = evaluateProperty(clip.keyframes, KeyframeProperty.CROP_LEFT, clipLocalMs, clip.cropLeft).coerceIn(0f, 0.45f),
            cropTop = evaluateProperty(clip.keyframes, KeyframeProperty.CROP_TOP, clipLocalMs, clip.cropTop).coerceIn(0f, 0.45f),
            cropRight = evaluateProperty(clip.keyframes, KeyframeProperty.CROP_RIGHT, clipLocalMs, clip.cropRight).coerceIn(0f, 0.45f),
            cropBottom = evaluateProperty(clip.keyframes, KeyframeProperty.CROP_BOTTOM, clipLocalMs, clip.cropBottom).coerceIn(0f, 0.45f),
            volume = evaluateProperty(clip.keyframes, KeyframeProperty.VOLUME, clipLocalMs, clip.volume).coerceIn(0f, 2f),
            filterIntensity = evaluateProperty(clip.keyframes, KeyframeProperty.FILTER_INTENSITY, clipLocalMs, clip.filterIntensity).coerceIn(0f, 1f)
        )
    }
}
