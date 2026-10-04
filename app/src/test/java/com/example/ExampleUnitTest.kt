package com.example

import com.example.engine.EffectFilterTransitionCatalog
import com.example.engine.KeyframeAndSpeedEngine
import com.example.engine.VideoRenderEngine
import com.example.model.Keyframe
import com.example.model.KeyframeInterpolation
import com.example.model.KeyframeProperty
import com.example.model.TimelineClip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun keyframeEngine_interpolatesLinearAndSmoothCorrectly() {
        val kfs = listOf(
            Keyframe(timestampMs = 0L, property = KeyframeProperty.SCALE, value = 1.0f, interpolation = KeyframeInterpolation.LINEAR),
            Keyframe(timestampMs = 1000L, property = KeyframeProperty.SCALE, value = 2.0f, interpolation = KeyframeInterpolation.LINEAR)
        )
        val startVal = KeyframeAndSpeedEngine.evaluateProperty(kfs, KeyframeProperty.SCALE, 0L, 1.0f)
        val midVal = KeyframeAndSpeedEngine.evaluateProperty(kfs, KeyframeProperty.SCALE, 500L, 1.0f)
        val endVal = KeyframeAndSpeedEngine.evaluateProperty(kfs, KeyframeProperty.SCALE, 1000L, 1.0f)

        assertEquals(1.0f, startVal, 0.001f)
        assertEquals(1.5f, midVal, 0.001f)
        assertEquals(2.0f, endVal, 0.001f)
    }

    @Test
    fun speedEngine_handlesShortMediumAndLongVideosAndSpeedCurves() {
        // 5 second clip at 2x speed -> 2.5s effective duration
        val clip5s = TimelineClip(title = "5s Clip", sourceDurationMs = 5000L, trimStartMs = 0L, trimEndMs = 5000L, speedMultiplier = 2.0f)
        assertEquals(2500L, KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip5s))

        // 1 minute clip at 0.5x slow motion -> 2 minutes effective duration
        val clip1m = TimelineClip(title = "1m Clip", sourceDurationMs = 60_000L, trimStartMs = 0L, trimEndMs = 60_000L, speedMultiplier = 0.5f)
        assertEquals(120_000L, KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip1m))

        // 10 minute clip with Montage speed curve
        val montagePoints = KeyframeAndSpeedEngine.speedPresets.first { it.id == "montage" }.points
        val clip10m = TimelineClip(
            title = "10m Clip",
            sourceDurationMs = 600_000L,
            trimStartMs = 0L,
            trimEndMs = 600_000L,
            speedPoints = montagePoints
        )
        val effective10m = KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip10m)
        assertTrue(effective10m > 100_000L)

        // Reverse clip time mapping
        val revClip = TimelineClip(title = "Reverse", sourceDurationMs = 4000L, trimStartMs = 0L, trimEndMs = 4000L, isReversed = true)
        val mappedStart = KeyframeAndSpeedEngine.mapTimelineOffsetToSourceMs(revClip, 0L)
        val mappedEnd = KeyframeAndSpeedEngine.mapTimelineOffsetToSourceMs(revClip, 4000L)
        assertEquals(4000L, mappedStart)
        assertEquals(0L, mappedEnd)
    }

    @Test
    fun catalogsAndTemplates_arePopulatedAndSearchable() {
        assertTrue(EffectFilterTransitionCatalog.filters.size >= 40)
        assertTrue(EffectFilterTransitionCatalog.effects.size >= 35)
        assertTrue(EffectFilterTransitionCatalog.transitions.size >= 20)
        assertTrue(EffectFilterTransitionCatalog.templates.size >= 15)

        val searchGlitch = EffectFilterTransitionCatalog.searchAll("Glitch")
        assertTrue(searchGlitch.isNotEmpty())

        val templateProject = EffectFilterTransitionCatalog.buildProjectFromTemplate(
            EffectFilterTransitionCatalog.templates.first()
        )
        assertTrue(templateProject.primaryClips.isNotEmpty())
        assertTrue(VideoRenderEngine.computeProjectTotalDurationMs(templateProject) > 3000L)
    }
}
