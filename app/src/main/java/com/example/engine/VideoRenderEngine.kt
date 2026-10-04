package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.model.BlendModeType
import com.example.model.CanvasBackgroundType
import com.example.model.ClipAnimation
import com.example.model.ColorAdjustment
import com.example.model.EffectTrackItem
import com.example.model.KeyframeProperty
import com.example.model.MaskConfig
import com.example.model.MaskType
import com.example.model.StickerClip
import com.example.model.TextClip
import com.example.model.TimelineClip
import com.example.model.VideoProject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Real-Time Multi-Track Video Frame Compositor & GPU/Canvas Shader Pipeline.
 * Used identically by both the interactive 60fps Editor Preview and the MP4 Export Engine
 * so that exported video matches the timeline preview 1:1.
 */
object VideoRenderEngine {

    data class PrimaryTrackWindow(
        val clip: TimelineClip,
        val timelineStartMs: Long,
        val timelineEndMs: Long,
        val effectiveDurationMs: Long,
        val clipIndex: Int
    )

    /**
     * Computes the effective timeline duration of a single clip in milliseconds.
     */
    fun computeClipEffectiveDurationMs(clip: TimelineClip): Long {
        return KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip)
    }

    /**
     * Computes the exact timeline windows for all primary track clips.
     */
    fun computePrimaryTrackSchedule(clips: List<TimelineClip>): List<PrimaryTrackWindow> {
        var cursorMs = 0L
        return clips.mapIndexed { idx, clip ->
            val dur = KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip)
            val start = cursorMs
            val end = cursorMs + dur
            cursorMs = end
            PrimaryTrackWindow(
                clip = clip,
                timelineStartMs = start,
                timelineEndMs = end,
                effectiveDurationMs = dur,
                clipIndex = idx
            )
        }
    }

    /**
     * Computes the total duration of the project across all tracks in milliseconds.
     */
    fun computeProjectTotalDurationMs(project: VideoProject): Long {
        val primaryTotal = computePrimaryTrackSchedule(project.primaryClips).lastOrNull()?.timelineEndMs ?: 0L
        val overlayMax = project.overlayClips.maxOfOrNull {
            it.overlayStartMs + KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(it)
        } ?: 0L
        val textMax = project.textClips.maxOfOrNull { it.timelineStartMs + it.durationMs } ?: 0L
        val audioMax = project.audioClips.maxOfOrNull { it.timelineStartMs + it.durationMs } ?: 0L
        return max(max(primaryTotal, overlayMax), max(textMax, audioMax)).coerceAtLeast(3000L)
    }

    /**
     * Builds an Android 4x5 ColorMatrix combining:
     * 1. Clip FilterDefinition (weighted by keyframed filterIntensity)
     * 2. Global Project FilterDefinition
     * 3. Clip ColorAdjustment (Brightness, Contrast, Saturation, Exposure, Highlights, Shadows,
     *    Temperature, Tint, Fade, RGB Gains, HSL shifts, Luma Curve).
     */
    fun buildCombinedColorMatrix(
        filterId: String,
        filterIntensity: Float,
        globalFilterId: String,
        globalFilterIntensity: Float,
        adj: ColorAdjustment
    ): ColorMatrix {
        val master = ColorMatrix()

        val clipFilter = EffectFilterTransitionCatalog.findFilter(filterId)
        val globalFilter = EffectFilterTransitionCatalog.findFilter(globalFilterId)
        val fInt = filterIntensity.coerceIn(0f, 1f)
        val gInt = globalFilterIntensity.coerceIn(0f, 1f)

        val totalContrast = (
            adj.contrast +
                (clipFilter?.contrast ?: 0f) * fInt +
                (globalFilter?.contrast ?: 0f) * gInt
            ).coerceIn(-0.85f, 1.5f)

        val totalSat = (
            1f + adj.saturation +
                (clipFilter?.saturation ?: 0f) * fInt +
                (globalFilter?.saturation ?: 0f) * gInt +
                (adj.hslBands.sumOf { it.saturationShift.toDouble() }.toFloat() * 0.12f)
            ).coerceIn(0f, 2.5f)

        val totalBright = (
            adj.brightness + adj.exposure * 0.45f +
                (clipFilter?.brightness ?: 0f) * fInt +
                (globalFilter?.brightness ?: 0f) * gInt +
                (adj.highlights * 0.15f) + (adj.shadows * 0.15f)
            ).coerceIn(-0.8f, 0.8f)

        val totalTemp = (
            adj.temperature +
                (clipFilter?.temperature ?: 0f) * fInt +
                (globalFilter?.temperature ?: 0f) * gInt
            ).coerceIn(-1f, 1f)

        val totalTint = (
            adj.tint +
                (clipFilter?.tint ?: 0f) * fInt +
                (globalFilter?.tint ?: 0f) * gInt
            ).coerceIn(-1f, 1f)

        val totalFade = (
            adj.fade +
                (clipFilter?.fade ?: 0f) * fInt +
                (globalFilter?.fade ?: 0f) * gInt
            ).coerceIn(0f, 0.8f)

        // 1. Saturation
        val satMatrix = ColorMatrix().apply { setSaturation(totalSat) }
        master.postConcat(satMatrix)

        // 2. Contrast + Brightness + Fade + RGB Gains + Temperature/Tint
        val cScale = 1f + totalContrast
        val cOffset = (1f - cScale) * 128f + (totalBright * 200f) + (totalFade * 36f)

        val rFilter = 1f + ((clipFilter?.rScale ?: 1f) - 1f) * fInt + ((globalFilter?.rScale ?: 1f) - 1f) * gInt
        val gFilter = 1f + ((clipFilter?.gScale ?: 1f) - 1f) * fInt + ((globalFilter?.gScale ?: 1f) - 1f) * gInt
        val bFilter = 1f + ((clipFilter?.bScale ?: 1f) - 1f) * fInt + ((globalFilter?.bScale ?: 1f) - 1f) * gInt

        // Curve midpoint boost
        val curveMid = adj.lumaCurvePoints.getOrNull(2)?.output ?: 0.5f
        val curveGain = 1f + (curveMid - 0.5f) * 0.9f

        val rGain = (cScale * adj.redGain * rFilter * curveGain * (1f + totalTemp * 0.18f + totalTint * 0.08f)).coerceIn(0.1f, 3f)
        val gGain = (cScale * adj.greenGain * gFilter * curveGain * (1f - abs(totalTemp) * 0.04f - totalTint * 0.14f)).coerceIn(0.1f, 3f)
        val bGain = (cScale * adj.blueGain * bFilter * curveGain * (1f - totalTemp * 0.18f + totalTint * 0.08f)).coerceIn(0.1f, 3f)

        val rOffset = cOffset + totalTemp * 18f
        val gOffset = cOffset - totalTint * 14f
        val bOffset = cOffset - totalTemp * 18f

        val gradeMatrix = ColorMatrix(
            floatArrayOf(
                rGain, 0f, 0f, 0f, rOffset,
                0f, gGain, 0f, 0f, gOffset,
                0f, 0f, bGain, 0f, bOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        master.postConcat(gradeMatrix)
        return master
    }

    /**
     * Renders a complete composite frame of [project] at [playheadMs] onto an Android [AndroidCanvas]
     * of dimensions [width] x [height].
     *
     * @param bitmapLookup Function that provides the decoded Bitmap for a clip (by drawableRes or URI).
     * @param showOriginalBeforeGrade When true (Before/After compare button held), bypasses color filters & effects.
     */
    fun renderCompositeFrame(
        canvas: AndroidCanvas,
        width: Float,
        height: Float,
        project: VideoProject,
        playheadMs: Long,
        bitmapLookup: (TimelineClip) -> Bitmap?,
        showOriginalBeforeGrade: Boolean = false
    ) {
        if (width <= 2f || height <= 2f) return
        val schedule = computePrimaryTrackSchedule(project.primaryClips)
        val activeWindow = schedule.find { playheadMs in it.timelineStartMs..it.timelineEndMs }
            ?: schedule.lastOrNull()

        // 1. Render Canvas Background (Solid Color, Gradient, or Blurred video backdrop)
        renderCanvasBackground(canvas, width, height, project, activeWindow?.clip, bitmapLookup)

        // 2. Render Primary Track (including real-time Transitions between clips!)
        if (activeWindow != null) {
            val clip = activeWindow.clip
            val localMs = (playheadMs - activeWindow.timelineStartMs).coerceIn(0L, activeWindow.effectiveDurationMs)
            val transition = clip.transitionAfter
            val nextWindow = schedule.getOrNull(activeWindow.clipIndex + 1)
            val transDur = transition.durationMs.coerceIn(150L, 2000L)
            val timeRemaining = activeWindow.timelineEndMs - playheadMs

            if (transition.transitionId != "none" && nextWindow != null && timeRemaining in 0L..transDur) {
                val progress = (1f - (timeRemaining.toFloat() / transDur.toFloat())).coerceIn(0f, 1f)
                renderClipTransition(
                    canvas = canvas,
                    width = width,
                    height = height,
                    outgoingClip = clip,
                    outgoingLocalMs = localMs,
                    incomingClip = nextWindow.clip,
                    incomingLocalMs = ((progress * transDur).toLong()).coerceAtMost(nextWindow.effectiveDurationMs),
                    transitionId = transition.transitionId,
                    progress = progress,
                    project = project,
                    bitmapLookup = bitmapLookup,
                    showOriginalBeforeGrade = showOriginalBeforeGrade
                )
            } else {
                renderSingleClipLayer(
                    canvas = canvas,
                    width = width,
                    height = height,
                    clip = clip,
                    clipLocalMs = localMs,
                    project = project,
                    bitmapLookup = bitmapLookup,
                    showOriginalBeforeGrade = showOriginalBeforeGrade,
                    isOverlayLayer = false
                )
            }
        }

        // 3. Render Multi-Track Overlay / PIP Clips
        project.overlayClips.forEach { overlay ->
            val effDur = KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(overlay)
            val start = overlay.overlayStartMs
            val end = start + effDur
            if (playheadMs in start..end) {
                val localMs = playheadMs - start
                renderSingleClipLayer(
                    canvas = canvas,
                    width = width,
                    height = height,
                    clip = overlay,
                    clipLocalMs = localMs,
                    project = project,
                    bitmapLookup = bitmapLookup,
                    showOriginalBeforeGrade = showOriginalBeforeGrade,
                    isOverlayLayer = true
                )
            }
        }

        // 4. Render Active Video Effects Track
        if (!showOriginalBeforeGrade) {
            val activeEffects = project.effectItems.filter {
                playheadMs in it.timelineStartMs..(it.timelineStartMs + it.durationMs)
            }
            activeEffects.forEach { effectItem ->
                val effectLocalMs = playheadMs - effectItem.timelineStartMs
                val evaluatedIntensity = KeyframeAndSpeedEngine.evaluateProperty(
                    keyframes = effectItem.keyframes,
                    property = KeyframeProperty.EFFECT_INTENSITY,
                    clipLocalMs = effectLocalMs,
                    fallbackValue = effectItem.intensity
                )
                renderVideoEffectOverlay(
                    canvas = canvas,
                    width = width,
                    height = height,
                    effectItem = effectItem,
                    evaluatedIntensity = evaluatedIntensity,
                    playheadMs = playheadMs
                )
            }
        }

        // 5. Render Stickers Track
        project.stickerClips.filter {
            playheadMs in it.timelineStartMs..(it.timelineStartMs + it.durationMs)
        }.forEach { sticker ->
            renderStickerItem(canvas, width, height, sticker, playheadMs - sticker.timelineStartMs)
        }

        // 6. Render Text & Captions Track
        project.textClips.filter {
            playheadMs in it.timelineStartMs..(it.timelineStartMs + it.durationMs)
        }.forEach { textClip ->
            renderTextClipItem(canvas, width, height, textClip, playheadMs - textClip.timelineStartMs)
        }
    }

    private fun renderCanvasBackground(
        canvas: AndroidCanvas,
        width: Float,
        height: Float,
        project: VideoProject,
        activeClip: TimelineClip?,
        bitmapLookup: (TimelineClip) -> Bitmap?
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cfg = project.canvasConfig
        when (cfg.type) {
            CanvasBackgroundType.SOLID_COLOR -> {
                bgPaint.color = cfg.solidColorHex.toInt()
                canvas.drawRect(0f, 0f, width, height, bgPaint)
            }
            CanvasBackgroundType.GRADIENT -> {
                bgPaint.shader = LinearGradient(
                    0f, 0f, width, height,
                    cfg.gradientStartHex.toInt(),
                    cfg.gradientEndHex.toInt(),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width, height, bgPaint)
            }
            CanvasBackgroundType.BLUR -> {
                val bmp = activeClip?.let { bitmapLookup(it) }
                if (bmp != null) {
                    bgPaint.alpha = 140
                    val matrix = Matrix()
                    val scale = max(width / bmp.width.toFloat(), height / bmp.height.toFloat()) * 1.18f
                    matrix.postTranslate(-bmp.width / 2f, -bmp.height / 2f)
                    matrix.postScale(scale, scale)
                    matrix.postTranslate(width / 2f, height / 2f)
                    canvas.drawBitmap(bmp, matrix, bgPaint)
                    val dimPaint = Paint().apply { color = 0x88090B10.toInt() }
                    canvas.drawRect(0f, 0f, width, height, dimPaint)
                } else {
                    bgPaint.color = 0xFF090B10.toInt()
                    canvas.drawRect(0f, 0f, width, height, bgPaint)
                }
            }
        }
    }

    private fun renderSingleClipLayer(
        canvas: AndroidCanvas,
        width: Float,
        height: Float,
        clip: TimelineClip,
        clipLocalMs: Long,
        project: VideoProject,
        bitmapLookup: (TimelineClip) -> Bitmap?,
        showOriginalBeforeGrade: Boolean,
        isOverlayLayer: Boolean,
        extraAlphaMultiplier: Float = 1f,
        extraTranslateX: Float = 0f,
        extraTranslateY: Float = 0f,
        extraScaleMultiplier: Float = 1f,
        extraRotationDeg: Float = 0f
    ) {
        val bmp = bitmapLookup(clip) ?: return
        val effDuration = KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip)
        val kfState = KeyframeAndSpeedEngine.evaluateClipStateAt(clip, clipLocalMs)
        val canonicalTransform = OmkarAutoVideoEngine.TransformEngine.evaluateClipTransform(clip, clipLocalMs)

        // Compute In / Out / Loop Animation offsets
        val animMod = evaluateAnimationOffsets(clip.animation, clipLocalMs, effDuration)

        val totalAlpha = (kfState.opacity * animMod.alpha * extraAlphaMultiplier).coerceIn(0f, 1f)
        if (totalAlpha <= 0.01f) return

        val saveCount = canvas.saveLayer(0f, 0f, width, height, null)

        val baseFitScale = if (isOverlayLayer) {
            min(width / bmp.width.toFloat(), height / bmp.height.toFloat()) * 0.55f
        } else {
            min(width / bmp.width.toFloat(), height / bmp.height.toFloat())
        }

        val fitW = bmp.width * baseFitScale
        val fitH = bmp.height * baseFitScale
        val fitLeft = (width - fitW) / 2f
        val fitTop = (height - fitH) / 2f

        val finalRotation = if (clip.isAutoSplitClip) {
            0f
        } else {
            canonicalTransform.rotationDegrees + animMod.rotation + extraRotationDeg
        }

        // Clip primary video frame strictly to its canonical aspect-ratio rectangle so zoom never distorts outer frame bounds
        if (!isOverlayLayer && abs(finalRotation) < 0.01f) {
            canvas.clipRect(fitLeft, fitTop, fitLeft + fitW, fitTop + fitH)
        }

        // Apply Crop / Mask clipping path
        val cropL = width * kfState.cropLeft
        val cropT = height * kfState.cropTop
        val cropR = width * (1f - kfState.cropRight)
        val cropB = height * (1f - kfState.cropBottom)
        if (cropL > 0f || cropT > 0f || kfState.cropRight > 0f || kfState.cropBottom > 0f) {
            canvas.clipRect(cropL, cropT, cropR.coerceAtLeast(cropL + 4f), cropB.coerceAtLeast(cropT + 4f))
        }

        if (clip.mask.type != MaskType.NONE) {
            applyMaskPath(canvas, width, height, clip.mask)
        }

        val requestedScale = canonicalTransform.uniformScale * animMod.scale * extraScaleMultiplier
        val requestedPosX = canonicalTransform.posX + animMod.offsetX
        val requestedPosY = canonicalTransform.posY + animMod.offsetY

        val safeTransform = if (!isOverlayLayer && abs(finalRotation) < 0.01f && requestedScale >= 1.0f) {
            OmkarAutoVideoEngine.CropProtectionEngine.clampTransform(
                requestedScale = requestedScale,
                requestedPosX = requestedPosX,
                requestedPosY = requestedPosY
            )
        } else {
            canonicalTransform.copy(
                uniformScale = requestedScale.coerceAtLeast(0.1f),
                scaleX = requestedScale.coerceAtLeast(0.1f),
                scaleY = requestedScale.coerceAtLeast(0.1f),
                posX = requestedPosX,
                posY = requestedPosY
            )
        }

        val cx = width * 0.5f + (safeTransform.posX * fitW) + extraTranslateX
        val cy = height * 0.5f + (safeTransform.posY * fitH) + extraTranslateY

        val finalScaleX = baseFitScale * safeTransform.scaleX * (if (clip.mirrorH) -1f else 1f)
        val finalScaleY = baseFitScale * safeTransform.scaleY * (if (clip.mirrorV) -1f else 1f)

        val matrix = Matrix().apply {
            postTranslate(-bmp.width * clip.anchorX, -bmp.height * clip.anchorY)
            postScale(finalScaleX, finalScaleY)
            if (abs(finalRotation) >= 0.001f) {
                postRotate(finalRotation)
            }
            postTranslate(cx, cy)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (totalAlpha * 255).toInt().coerceIn(0, 255)
            if (!showOriginalBeforeGrade) {
                val cm = buildCombinedColorMatrix(
                    filterId = clip.filterId,
                    filterIntensity = kfState.filterIntensity,
                    globalFilterId = project.globalFilterId,
                    globalFilterIntensity = project.globalFilterIntensity,
                    adj = clip.adjustments
                )
                colorFilter = ColorMatrixColorFilter(cm)
            }
            if (isOverlayLayer && clip.blendMode != BlendModeType.NORMAL) {
                xfermode = PorterDuffXfermode(mapBlendMode(clip.blendMode))
            }
        }

        canvas.drawBitmap(bmp, matrix, paint)

        // Cutout / Chroma Key visual treatment
        if (clip.cutout.autoCutoutEnabled || clip.cutout.chromaKeyEnabled) {
            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f + clip.cutout.feather * 12f
                color = if (clip.cutout.neonStrokeEnabled) {
                    clip.cutout.neonStrokeColorHex.toInt()
                } else {
                    0xAA00E5FF.toInt()
                }
            }
            val radius = min(width, height) * 0.34f * kfState.scale
            canvas.drawCircle(cx, cy, radius, rimPaint)
        }

        // Per-clip Vignette & Film Grain from Adjustments or Filter
        if (!showOriginalBeforeGrade) {
            val filterVignette = (EffectFilterTransitionCatalog.findFilter(clip.filterId)?.vignette ?: 0f) * kfState.filterIntensity
            val totalVignette = (clip.adjustments.vignette + filterVignette).coerceIn(-1f, 1f)
            if (abs(totalVignette) > 0.04f) {
                val vigColor = if (totalVignette > 0f) AndroidColor.BLACK else AndroidColor.WHITE
                val vigAlpha = (abs(totalVignette) * 190).toInt().coerceIn(0, 220)
                val vigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        width / 2f,
                        height / 2f,
                        max(width, height) * 0.65f,
                        intArrayOf(AndroidColor.TRANSPARENT, AndroidColor.argb(vigAlpha, AndroidColor.red(vigColor), AndroidColor.green(vigColor), AndroidColor.blue(vigColor))),
                        floatArrayOf(0.42f, 1.0f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width, height, vigPaint)
            }

            if (clip.adjustments.grain > 0.04f) {
                drawProceduralGrain(canvas, width, height, clip.adjustments.grain, clipLocalMs)
            }
        }

        canvas.restoreToCount(saveCount)
    }

    private fun renderClipTransition(
        canvas: AndroidCanvas,
        width: Float,
        height: Float,
        outgoingClip: TimelineClip,
        outgoingLocalMs: Long,
        incomingClip: TimelineClip,
        incomingLocalMs: Long,
        transitionId: String,
        progress: Float,
        project: VideoProject,
        bitmapLookup: (TimelineClip) -> Bitmap?,
        showOriginalBeforeGrade: Boolean
    ) {
        val p = KeyframeAndSpeedEngine.applyInterpolation(progress, com.example.model.KeyframeInterpolation.EASE_IN_OUT)
        when (transitionId) {
            "dip_to_black", "dip_to_white" -> {
                val isWhite = transitionId == "dip_to_white"
                if (p < 0.5f) {
                    renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false)
                    val fadeAlpha = (p * 2f * 255).toInt().coerceIn(0, 255)
                    val paint = Paint().apply {
                        color = if (isWhite) AndroidColor.argb(fadeAlpha, 255, 255, 255) else AndroidColor.argb(fadeAlpha, 0, 0, 0)
                    }
                    canvas.drawRect(0f, 0f, width, height, paint)
                } else {
                    renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false)
                    val fadeAlpha = ((1f - p) * 2f * 255).toInt().coerceIn(0, 255)
                    val paint = Paint().apply {
                        color = if (isWhite) AndroidColor.argb(fadeAlpha, 255, 255, 255) else AndroidColor.argb(fadeAlpha, 0, 0, 0)
                    }
                    canvas.drawRect(0f, 0f, width, height, paint)
                }
            }
            "whip_pan_left" -> {
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraTranslateX = -width * p)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraTranslateX = width * (1f - p))
            }
            "whip_pan_right" -> {
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraTranslateX = width * p)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraTranslateX = -width * (1f - p))
            }
            "push_up" -> {
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraTranslateY = -height * p)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraTranslateY = height * (1f - p))
            }
            "zoom_in_warp" -> {
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraScaleMultiplier = 1f + p * 1.8f, extraAlphaMultiplier = 1f - p)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraScaleMultiplier = 0.4f + p * 0.6f, extraAlphaMultiplier = p)
            }
            "zoom_out_warp" -> {
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraScaleMultiplier = (1f - p * 0.6f).coerceAtLeast(0.2f), extraAlphaMultiplier = 1f - p)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraScaleMultiplier = 1.8f - p * 0.8f, extraAlphaMultiplier = p)
            }
            "spin_360" -> {
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraRotationDeg = p * 180f, extraScaleMultiplier = 1f - p * 0.5f, extraAlphaMultiplier = 1f - p)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraRotationDeg = (p - 1f) * 180f, extraScaleMultiplier = 0.5f + p * 0.5f, extraAlphaMultiplier = p)
            }
            "circle_iris", "diamond_wipe" -> {
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false)
                val save = canvas.save()
                val path = Path()
                val maxR = max(width, height) * 0.8f * p
                if (transitionId == "circle_iris") {
                    path.addCircle(width / 2f, height / 2f, maxR, Path.Direction.CW)
                } else {
                    path.moveTo(width / 2f, height / 2f - maxR)
                    path.lineTo(width / 2f + maxR, height / 2f)
                    path.lineTo(width / 2f, height / 2f + maxR)
                    path.lineTo(width / 2f - maxR, height / 2f)
                    path.close()
                }
                canvas.clipPath(path)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false)
                canvas.restoreToCount(save)
            }
            "camera_flash", "light_leak_burn" -> {
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraAlphaMultiplier = 1f - p)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraAlphaMultiplier = p)
                val flashPeak = sin(p * PI.toFloat()).coerceIn(0f, 1f)
                val flashPaint = Paint().apply {
                    color = if (transitionId == "camera_flash") {
                        AndroidColor.argb((flashPeak * 235).toInt(), 255, 255, 255)
                    } else {
                        AndroidColor.argb((flashPeak * 210).toInt(), 255, 140, 40)
                    }
                }
                canvas.drawRect(0f, 0f, width, height, flashPaint)
            }
            else -> {
                // Cross dissolve / glitch / split / 3D fallback with smooth crossfade
                renderSingleClipLayer(canvas, width, height, outgoingClip, outgoingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraAlphaMultiplier = 1f - p)
                renderSingleClipLayer(canvas, width, height, incomingClip, incomingLocalMs, project, bitmapLookup, showOriginalBeforeGrade, false, extraAlphaMultiplier = p)
            }
        }
    }

    private data class AnimTransformMod(
        val offsetX: Float = 0f,
        val offsetY: Float = 0f,
        val scale: Float = 1f,
        val rotation: Float = 0f,
        val alpha: Float = 1f
    )

    private fun evaluateAnimationOffsets(anim: ClipAnimation, localMs: Long, durationMs: Long): AnimTransformMod {
        var ox = 0f
        var oy = 0f
        var sc = 1f
        var rot = 0f
        var al = 1f

        // In Animation
        if (anim.inAnimId != "none" && anim.inDurationMs > 0 && localMs < anim.inDurationMs) {
            val t = (localMs.toFloat() / anim.inDurationMs.toFloat()).coerceIn(0f, 1f)
            val ease = KeyframeAndSpeedEngine.applyInterpolation(t, com.example.model.KeyframeInterpolation.EASE_OUT)
            when (anim.inAnimId) {
                "fade_in" -> al *= ease
                "pop_up" -> {
                    sc *= (0.4f + 0.6f * ease)
                    al *= ease
                }
                "slide_up" -> {
                    oy += (1f - ease) * 0.45f
                    al *= ease
                }
                "whip_left" -> ox += (1f - ease) * 0.7f
                "zoom_drop" -> {
                    sc *= (1.7f - 0.7f * ease)
                    al *= ease
                }
                "spin_in" -> {
                    rot += (1f - ease) * -120f
                    sc *= ease.coerceAtLeast(0.2f)
                }
                "glitch_in" -> {
                    ox += sin(t * 24f) * (1f - ease) * 0.15f
                    al *= ease
                }
            }
        }

        // Out Animation
        val outStart = (durationMs - anim.outDurationMs).coerceAtLeast(0L)
        if (anim.outAnimId != "none" && anim.outDurationMs > 0 && localMs > outStart) {
            val t = ((localMs - outStart).toFloat() / anim.outDurationMs.toFloat()).coerceIn(0f, 1f)
            val ease = KeyframeAndSpeedEngine.applyInterpolation(t, com.example.model.KeyframeInterpolation.EASE_IN)
            when (anim.outAnimId) {
                "fade_out" -> al *= (1f - ease)
                "slide_down" -> {
                    oy += ease * 0.45f
                    al *= (1f - ease)
                }
                "zoom_vanish" -> {
                    sc *= (1f - ease * 0.8f)
                    al *= (1f - ease)
                }
                "glitch_out" -> {
                    ox += sin(t * 30f) * ease * 0.18f
                    al *= (1f - ease)
                }
            }
        }

        // Loop Animation
        if (anim.loopAnimId != "none" && anim.loopCycleMs > 100L) {
            val phase = ((localMs % anim.loopCycleMs).toFloat() / anim.loopCycleMs.toFloat()) * 2f * PI.toFloat()
            when (anim.loopAnimId) {
                "pulse" -> sc *= (1f + 0.06f * sin(phase))
                "float_wave" -> oy += 0.04f * sin(phase)
                "neon_flicker" -> al *= (0.78f + 0.22f * abs(sin(phase * 3f)))
                "pendulum" -> rot += 6f * sin(phase)
                "jitter" -> {
                    ox += 0.015f * sin(phase * 5f)
                    oy += 0.015f * cos(phase * 4f)
                }
            }
        }

        return AnimTransformMod(ox, oy, sc, rot, al)
    }

    private fun applyMaskPath(canvas: AndroidCanvas, width: Float, height: Float, mask: MaskConfig) {
        val cx = width * mask.centerX
        val cy = height * mask.centerY
        val rx = width * mask.radiusOrWidth
        val ry = height * mask.heightRatio
        val path = Path()
        when (mask.type) {
            MaskType.NONE -> return
            MaskType.RECTANGLE -> {
                path.addRoundRect(
                    RectF(cx - rx, cy - ry, cx + rx, cy + ry),
                    mask.feather * 80f,
                    mask.feather * 80f,
                    Path.Direction.CW
                )
            }
            MaskType.CIRCLE -> {
                path.addCircle(cx, cy, min(rx, ry), Path.Direction.CW)
            }
            MaskType.LINEAR -> {
                path.addRect(0f, 0f, width, cy + ry * 0.5f, Path.Direction.CW)
            }
            MaskType.MIRROR -> {
                path.addRect(0f, cy - ry, width, cy + ry, Path.Direction.CW)
            }
            MaskType.STAR, MaskType.HEART -> {
                val r = min(rx, ry)
                path.addCircle(cx, cy, r, Path.Direction.CW)
            }
        }
        canvas.clipPath(path)
    }

    private fun mapBlendMode(mode: BlendModeType): PorterDuff.Mode {
        return when (mode) {
            BlendModeType.NORMAL -> PorterDuff.Mode.SRC_OVER
            BlendModeType.MULTIPLY -> PorterDuff.Mode.MULTIPLY
            BlendModeType.SCREEN -> PorterDuff.Mode.SCREEN
            BlendModeType.OVERLAY -> PorterDuff.Mode.OVERLAY
            BlendModeType.LIGHTEN -> PorterDuff.Mode.LIGHTEN
            BlendModeType.DARKEN -> PorterDuff.Mode.DARKEN
            BlendModeType.ADD -> PorterDuff.Mode.ADD
            else -> PorterDuff.Mode.SCREEN
        }
    }

    /**
     * Renders real-time GPU/Canvas visual effects from the Effects Track.
     */
    private fun renderVideoEffectOverlay(
        canvas: AndroidCanvas,
        width: Float,
        height: Float,
        effectItem: EffectTrackItem,
        evaluatedIntensity: Float,
        playheadMs: Long
    ) {
        val intensity = evaluatedIntensity.coerceIn(0f, 1f)
        if (intensity <= 0.02f) return
        val speed = effectItem.speed.coerceIn(0.1f, 2f)
        val t = (playheadMs / 1000f) * speed
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (effectItem.effectDefId) {
            "chromatic_aberration", "rgb_split", "chromatic_wave" -> {
                val shift = (8f + 22f * intensity) * (0.7f + 0.3f * sin(t * 6f))
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = shift
                paint.color = AndroidColor.argb((intensity * 130).toInt(), 0, 229, 255)
                canvas.drawRect(shift / 2f, shift / 2f, width - shift / 2f, height - shift / 2f, paint)
                paint.color = AndroidColor.argb((intensity * 130).toInt(), 255, 40, 100)
                canvas.drawRect(shift, shift, width - shift, height - shift, paint)
            }
            "halation_bloom", "warm_light_leak", "dreamy_mist" -> {
                val isWarm = effectItem.effectDefId != "dreamy_mist"
                val glowColor = if (isWarm) AndroidColor.argb((intensity * 145).toInt(), 255, 130, 50)
                else AndroidColor.argb((intensity * 135).toInt(), 244, 143, 177)
                val cx = width * (0.5f + 0.3f * sin(t * 2f))
                val cy = height * (0.35f + 0.25f * cos(t * 1.7f))
                paint.shader = RadialGradient(
                    cx, cy, max(width, height) * 0.7f,
                    intArrayOf(glowColor, AndroidColor.TRANSPARENT),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width, height, paint)
            }
            "anamorphic_flare" -> {
                val flareY = height * (0.42f + 0.12f * sin(t * 2.2f))
                val flareAlpha = (intensity * 195).toInt().coerceIn(0, 240)
                paint.shader = LinearGradient(
                    0f, flareY - 18f, 0f, flareY + 18f,
                    intArrayOf(AndroidColor.TRANSPARENT, AndroidColor.argb(flareAlpha, 0, 229, 255), AndroidColor.TRANSPARENT),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, flareY - 18f, width, flareY + 18f, paint)
            }
            "vhs_tape", "crt_scanlines", "old_film_16mm" -> {
                paint.color = AndroidColor.argb((intensity * 75).toInt(), 0, 0, 0)
                paint.strokeWidth = 2f
                val step = 8f
                var y = (playheadMs % 16L).toFloat()
                while (y < height) {
                    canvas.drawLine(0f, y, width, y, paint)
                    y += step
                }
                if (effectItem.effectDefId == "vhs_tape") {
                    val barY = ((t * 180f) % height)
                    paint.color = AndroidColor.argb((intensity * 110).toInt(), 255, 255, 255)
                    canvas.drawRect(0f, barY, width, barY + 14f, paint)
                }
            }
            "glitch_displacement" -> {
                val slices = 6
                for (i in 0 until slices) {
                    val seed = ((playheadMs / 90L) + i * 31L)
                    if (seed % 3L == 0L) {
                        val sy = ((seed * 47L) % height.toLong()).toFloat()
                        val sh = 12f + (seed % 28L)
                        paint.color = if (i % 2 == 0) {
                            AndroidColor.argb((intensity * 140).toInt(), 0, 229, 255)
                        } else {
                            AndroidColor.argb((intensity * 140).toInt(), 255, 51, 102)
                        }
                        canvas.drawRect(0f, sy, width, (sy + sh).coerceAtMost(height), paint)
                    }
                }
            }
            "sparkle_bokeh", "starfield_particles", "rain_drops", "snowfall" -> {
                val count = (18 * intensity).toInt().coerceAtLeast(6)
                for (i in 0 until count) {
                    val px = ((i * 97f + t * 55f * (if (i % 2 == 0) 1f else -0.6f)) % width + width) % width
                    val py = ((i * 131f + t * 140f) % height + height) % height
                    if (effectItem.effectDefId == "rain_drops") {
                        paint.color = AndroidColor.argb((intensity * 170).toInt(), 180, 230, 255)
                        paint.strokeWidth = 2.2f
                        canvas.drawLine(px, py, px - 8f, py + 26f, paint)
                    } else {
                        paint.color = if (effectItem.effectDefId == "starfield_particles") {
                            AndroidColor.argb((intensity * 210).toInt(), 255, 190, 60)
                        } else {
                            AndroidColor.argb((intensity * 220).toInt(), 255, 255, 255)
                        }
                        val r = 3f + (i % 4) * 2f * intensity
                        canvas.drawCircle(px, py, r, paint)
                    }
                }
            }
            "cyber_hud", "neon_edge_aura", "energy_aura", "body_neon_clone" -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f * intensity
                paint.color = AndroidColor.argb((intensity * 200).toInt(), 0, 229, 255)
                val pad = 22f
                canvas.drawRect(pad, pad, width - pad, height - pad, paint)
                val pulseR = min(width, height) * (0.25f + 0.15f * abs(sin(t * 3f)))
                paint.color = AndroidColor.argb((intensity * 160).toInt(), 124, 77, 255)
                canvas.drawCircle(width / 2f, height / 2f, pulseR, paint)
            }
            "strobe_flash" -> {
                val flashOn = ((playheadMs / 140L) % 2L == 0L)
                if (flashOn) {
                    paint.color = AndroidColor.argb((intensity * 150).toInt(), 255, 255, 255)
                    canvas.drawRect(0f, 0f, width, height, paint)
                }
            }
            else -> {
                // Vignette / Lens / Film Grain / General shader polish
                drawProceduralGrain(canvas, width, height, intensity * 0.5f, playheadMs)
            }
        }
    }

    private fun drawProceduralGrain(
        canvas: AndroidCanvas,
        width: Float,
        height: Float,
        amount: Float,
        seedMs: Long
    ) {
        val paint = Paint()
        val dots = (45 * amount).toInt().coerceIn(8, 60)
        val frameSeed = (seedMs / 40L).toInt()
        for (i in 0 until dots) {
            val hash = (i * 7919 + frameSeed * 104729) and 0x7FFFFFFF
            val x = (hash % width.toInt().coerceAtLeast(1)).toFloat()
            val y = ((hash / 31) % height.toInt().coerceAtLeast(1)).toFloat()
            paint.color = if (i % 2 == 0) {
                AndroidColor.argb((amount * 95).toInt(), 255, 255, 255)
            } else {
                AndroidColor.argb((amount * 95).toInt(), 0, 0, 0)
            }
            canvas.drawCircle(x, y, 1.8f, paint)
        }
    }

    private fun renderStickerItem(
        canvas: AndroidCanvas,
        width: Float,
        height: Float,
        sticker: StickerClip,
        localMs: Long
    ) {
        val posX = KeyframeAndSpeedEngine.evaluateProperty(sticker.keyframes, KeyframeProperty.POS_X, localMs, sticker.posX)
        val posY = KeyframeAndSpeedEngine.evaluateProperty(sticker.keyframes, KeyframeProperty.POS_Y, localMs, sticker.posY)
        val scale = KeyframeAndSpeedEngine.evaluateProperty(sticker.keyframes, KeyframeProperty.SCALE, localMs, sticker.scale)
        val rot = KeyframeAndSpeedEngine.evaluateProperty(sticker.keyframes, KeyframeProperty.ROTATION, localMs, sticker.rotation)
        val pulse = if (sticker.loopAnimId == "pulse") 1f + 0.06f * sin(localMs / 180f) else 1f

        val cx = width * (0.5f + posX * 0.42f)
        val cy = height * (0.5f + posY * 0.42f)

        val save = canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(rot)
        canvas.scale(scale * pulse, scale * pulse)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = min(width, height) * 0.042f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textWidth = textPaint.measureText(sticker.symbol)
        val padH = 22f
        val padV = 14f
        val rect = RectF(-textWidth / 2f - padH, -textPaint.textSize - padV / 2f, textWidth / 2f + padH, padV)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = sticker.accentHex.toInt()
            alpha = (sticker.opacity * 230).toInt().coerceIn(0, 255)
        }
        canvas.drawRoundRect(rect, 18f, 18f, bgPaint)
        canvas.drawText(sticker.symbol, 0f, -4f, textPaint)
        canvas.restoreToCount(save)
    }

    private fun renderTextClipItem(
        canvas: AndroidCanvas,
        width: Float,
        height: Float,
        textClip: TextClip,
        localMs: Long
    ) {
        val posX = KeyframeAndSpeedEngine.evaluateProperty(textClip.keyframes, KeyframeProperty.POS_X, localMs, textClip.posX)
        val posY = KeyframeAndSpeedEngine.evaluateProperty(textClip.keyframes, KeyframeProperty.POS_Y, localMs, textClip.posY)
        val scale = KeyframeAndSpeedEngine.evaluateProperty(textClip.keyframes, KeyframeProperty.SCALE, localMs, textClip.scale)
        val rot = KeyframeAndSpeedEngine.evaluateProperty(textClip.keyframes, KeyframeProperty.ROTATION, localMs, textClip.rotation)
        val opacity = KeyframeAndSpeedEngine.evaluateProperty(textClip.keyframes, KeyframeProperty.OPACITY, localMs, textClip.opacity)

        // Handle typewriter animation
        val visibleText = if (textClip.inAnimId == "typewriter" && localMs < 900L) {
            val count = ((localMs / 900f) * textClip.text.length).toInt().coerceIn(1, textClip.text.length)
            textClip.text.substring(0, count)
        } else {
            textClip.text
        }

        val cx = width * (0.5f + posX * 0.42f)
        val cy = height * (0.5f + posY * 0.42f)

        val save = canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(rot)
        canvas.scale(scale, scale)

        val scaledTextSize = (textClip.fontSizeSp * (min(width, height) / 360f)).coerceIn(14f, 96f)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textClip.textColorHex.toInt()
            alpha = (opacity * 255).toInt().coerceIn(0, 255)
            textSize = scaledTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = when (textClip.alignment) {
                "LEFT" -> Paint.Align.LEFT
                "RIGHT" -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
            if (textClip.shadowBlur > 0f) {
                setShadowLayer(
                    textClip.shadowBlur,
                    textClip.shadowOffsetX,
                    textClip.shadowOffsetY,
                    textClip.shadowColorHex.toInt()
                )
            }
        }

        val lines = visibleText.split("\n")
        val lineHeight = scaledTextSize * textClip.lineHeightMultiplier
        val maxLineW = lines.maxOfOrNull { textPaint.measureText(it) } ?: 60f
        val totalH = lines.size * lineHeight

        if (textClip.backgroundAlpha > 0.02f) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textClip.backgroundColorHex.toInt()
                alpha = (textClip.backgroundAlpha * opacity * 255).toInt().coerceIn(0, 255)
            }
            val bgRect = RectF(
                -maxLineW / 2f - 24f,
                -totalH / 2f - 16f,
                maxLineW / 2f + 24f,
                totalH / 2f + 16f
            )
            canvas.drawRoundRect(
                bgRect,
                textClip.backgroundCornerRadius * 2f,
                textClip.backgroundCornerRadius * 2f,
                bgPaint
            )
        }

        lines.forEachIndexed { idx, line ->
            val yOffset = -totalH / 2f + (idx + 0.8f) * lineHeight
            if (textClip.strokeWidth > 0.5f) {
                val strokePaint = Paint(textPaint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = textClip.strokeWidth * 2f
                    color = textClip.strokeColorHex.toInt()
                    alpha = (opacity * 255).toInt().coerceIn(0, 255)
                }
                canvas.drawText(line, 0f, yOffset, strokePaint)
            }
            canvas.drawText(line, 0f, yOffset, textPaint)
        }

        canvas.restoreToCount(save)
    }
}
