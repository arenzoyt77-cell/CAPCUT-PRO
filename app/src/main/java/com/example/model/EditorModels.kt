package com.example.model

import com.example.R
import java.util.UUID

enum class AspectRatioMode(val label: String, val ratioWidth: Float, val ratioHeight: Float) {
    RATIO_9_16("9:16", 9f, 16f),
    RATIO_16_9("16:9", 16f, 9f),
    RATIO_1_1("1:1", 1f, 1f),
    RATIO_4_5("4:5", 4f, 5f),
    RATIO_21_9("21:9", 21f, 9f),
    RATIO_4_3("4:3", 4f, 3f);

    val aspectValue: Float
        get() = ratioWidth / ratioHeight

    companion object {
        fun fromDimensions(width: Int, height: Int): AspectRatioMode {
            if (width <= 0 || height <= 0) return RATIO_9_16
            val ratio = width.toFloat() / height.toFloat()
            return entries.minByOrNull { kotlin.math.abs(it.aspectValue - ratio) } ?: if (height > width) RATIO_9_16 else RATIO_16_9
        }
    }
}

/**
 * Canonical representation of the source video geometry.
 * Rotation metadata is normalized and applied ONCE to compute [displayWidth] and [displayHeight].
 */
data class SourceVideoGeometry(
    val rawWidth: Int = 1080,
    val rawHeight: Int = 1920,
    val rotationDegrees: Int = 0, // Normalized to 0, 90, 180, or 270
    val pixelAspectRatio: Float = 1.0f,
    val frameRate: Float = 30f,
    val durationMs: Long = 8000L,
    val hasAudio: Boolean = true,
    val bitrateBps: Long = 16_000_000L,
    val fileSizeMb: Float = 14.2f
) {
    val normalizedRotation: Int
        get() = ((rotationDegrees % 360) + 360) % 360

    val isRotationSwapped: Boolean
        get() = normalizedRotation == 90 || normalizedRotation == 270

    val displayWidth: Int
        get() = (((if (isRotationSwapped) rawHeight else rawWidth).coerceAtLeast(1)) * pixelAspectRatio.coerceIn(0.5f, 2.0f)).toInt().coerceAtLeast(1)

    val displayHeight: Int
        get() = (if (isRotationSwapped) rawWidth else rawHeight).coerceAtLeast(1)

    val displayAspectRatio: Float
        get() = displayWidth.toFloat() / displayHeight.toFloat()

    val isPortrait: Boolean
        get() = displayHeight > displayWidth

    val canonicalAspectRatioMode: AspectRatioMode
        get() = AspectRatioMode.fromDimensions(displayWidth, displayHeight)
}

/**
 * Canonical uniform-only transform state for a video frame.
 * Guarantees rotation = 0, skewX = 0, skewY = 0, and scaleX == scaleY for automatic edits.
 */
data class TransformData(
    val uniformScale: Float = 1.0f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val posX: Float = 0.0f,
    val posY: Float = 0.0f,
    val rotationDegrees: Float = 0.0f,
    val skewX: Float = 0.0f,
    val skewY: Float = 0.0f
) {
    val isGeometrySacredCompliant: Boolean
        get() = kotlin.math.abs(scaleX - scaleY) < 0.0001f &&
            kotlin.math.abs(rotationDegrees) < 0.0001f &&
            kotlin.math.abs(skewX) < 0.0001f &&
            kotlin.math.abs(skewY) < 0.0001f &&
            uniformScale >= 0.1f
}

enum class AutomaticMotionMode(val label: String, val description: String) {
    SIMPLE("Simple Motion", "Every clip: 100% start keyframe → uniform subtle zoom end keyframe"),
    SMART("Smart Motion", "Context-aware zoom in/out & crop-protected subject framing per clip")
}

enum class ZoomDirection(val label: String) {
    ZOOM_IN("Zoom In"),
    ZOOM_OUT("Zoom Out"),
    SUBTLE_DRIFT_IN("Subtle Move + Zoom"),
    STATIC_100("Static 100%")
}

/**
 * Millisecond-accurate word timestamp from SpeechAnalyzer / WordTimestampAnalyzer.
 */
data class WordTimestamp(
    val word: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val confidence: Float = 0.95f,
    val isSentenceEnd: Boolean = false,
    val isClausePause: Boolean = false
)

/**
 * Canonical speech segment detected by SpeechSegmentDetector.
 */
data class SpeechSegment(
    val id: String = UUID.randomUUID().toString(),
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String,
    val confidence: Float = 0.94f,
    val words: List<WordTimestamp> = emptyList(),
    val pauseAfterMs: Long = 0L,
    val semanticContinuityScore: Float = 0.0f
)

/**
 * Snapshot of the initial AI-generated automatic edit state so RESET AUTOMATIC EDIT
 * restores original detected splits, automatic keyframes, and motion values without re-importing.
 */
data class AutoEditSnapshot(
    val sourceGeometry: SourceVideoGeometry,
    val motionMode: AutomaticMotionMode,
    val targetZoomFactor: Float,
    val detectedSegments: List<SpeechSegment>,
    val splitPointsMs: List<Long>,
    val generatedClips: List<TimelineClip>,
    val generatedCaptions: List<TextClip>
)

data class CapCutDraftExportResult(
    val success: Boolean,
    val draftDirectoryPath: String = "",
    val draftZipFilePath: String = "",
    val fallbackMp4Path: String = "",
    val exportedSegmentsCount: Int = 0,
    val exportedKeyframesCount: Int = 0,
    val usedMp4ShareFallback: Boolean = false,
    val message: String = ""
)

enum class CanvasBackgroundType(val label: String) {
    SOLID_COLOR("Color"),
    BLUR("Blur"),
    GRADIENT("Gradient")
}

data class CanvasConfig(
    val type: CanvasBackgroundType = CanvasBackgroundType.SOLID_COLOR,
    val solidColorHex: Long = 0xFF06080CL,
    val gradientStartHex: Long = 0xFF0F172AL,
    val gradientEndHex: Long = 0xFF1E1B4BL,
    val blurRadius: Float = 28f
)

enum class KeyframeInterpolation(val label: String) {
    LINEAR("Linear"),
    SMOOTH("Smooth"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In-Out"),
    CUBIC("Cubic Bezier")
}

enum class KeyframeProperty(
    val label: String,
    val defaultValue: Float,
    val minValue: Float,
    val maxValue: Float
) {
    POS_X("Position X", 0f, -1f, 1f),
    POS_Y("Position Y", 0f, -1f, 1f),
    SCALE("Scale", 1f, 0.1f, 5f),
    ROTATION("Rotation", 0f, -360f, 360f),
    OPACITY("Opacity", 1f, 0f, 1f),
    CROP_LEFT("Crop Left", 0f, 0f, 0.45f),
    CROP_TOP("Crop Top", 0f, 0f, 0.45f),
    CROP_RIGHT("Crop Right", 0f, 0f, 0.45f),
    CROP_BOTTOM("Crop Bottom", 0f, 0f, 0.45f),
    VOLUME("Volume", 1f, 0f, 2f),
    FILTER_INTENSITY("Filter Intensity", 1f, 0f, 1f),
    EFFECT_INTENSITY("Effect Intensity", 0.8f, 0f, 1f)
}

data class Keyframe(
    val id: String = UUID.randomUUID().toString(),
    val timestampMs: Long, // Relative to clip start (0..clipEffectiveDurationMs)
    val property: KeyframeProperty,
    val value: Float,
    val interpolation: KeyframeInterpolation = KeyframeInterpolation.EASE_IN_OUT
)

data class SpeedPoint(
    val positionFraction: Float, // 0.0f .. 1.0f across clip
    val speedMultiplier: Float   // 0.1f .. 10.0f
)

data class HslAdjustment(
    val bandName: String,
    val hueShift: Float = 0f,       // -180f..180f
    val saturationShift: Float = 0f, // -1f..1f
    val luminanceShift: Float = 0f   // -1f..1f
)

data class CurveControlPoint(
    val input: Float,  // 0f..1f
    val output: Float  // 0f..1f
)

data class ColorAdjustment(
    val brightness: Float = 0f,    // -1f..1f
    val contrast: Float = 0f,      // -1f..1f
    val saturation: Float = 0f,    // -1f..1f
    val exposure: Float = 0f,      // -1f..1f
    val highlights: Float = 0f,    // -1f..1f
    val shadows: Float = 0f,       // -1f..1f
    val temperature: Float = 0f,   // -1f..1f (Cool to Warm)
    val tint: Float = 0f,          // -1f..1f (Green to Magenta)
    val fade: Float = 0f,          // 0f..1f
    val sharpen: Float = 0f,       // 0f..1f
    val vignette: Float = 0f,      // -1f..1f
    val grain: Float = 0f,         // 0f..1f
    val redGain: Float = 1f,       // 0.5f..1.5f (RGB controls)
    val greenGain: Float = 1f,     // 0.5f..1.5f
    val blueGain: Float = 1f,      // 0.5f..1.5f
    val hslBands: List<HslAdjustment> = defaultHslBands(),
    val lumaCurvePoints: List<CurveControlPoint> = listOf(
        CurveControlPoint(0f, 0f),
        CurveControlPoint(0.25f, 0.25f),
        CurveControlPoint(0.5f, 0.5f),
        CurveControlPoint(0.75f, 0.75f),
        CurveControlPoint(1f, 1f)
    )
) {
    fun isDefault(): Boolean {
        return brightness == 0f && contrast == 0f && saturation == 0f &&
            exposure == 0f && highlights == 0f && shadows == 0f &&
            temperature == 0f && tint == 0f && fade == 0f &&
            sharpen == 0f && vignette == 0f && grain == 0f &&
            redGain == 1f && greenGain == 1f && blueGain == 1f &&
            hslBands.all { it.hueShift == 0f && it.saturationShift == 0f && it.luminanceShift == 0f } &&
            lumaCurvePoints.all { kotlin.math.abs(it.input - it.output) < 0.01f }
    }

    companion object {
        fun defaultHslBands(): List<HslAdjustment> = listOf(
            HslAdjustment("Red"),
            HslAdjustment("Orange"),
            HslAdjustment("Yellow"),
            HslAdjustment("Green"),
            HslAdjustment("Aqua"),
            HslAdjustment("Blue"),
            HslAdjustment("Purple"),
            HslAdjustment("Magenta")
        )
    }
}

enum class MaskType(val label: String) {
    NONE("None"),
    RECTANGLE("Rectangle"),
    CIRCLE("Circle"),
    LINEAR("Linear Split"),
    MIRROR("Mirror Band"),
    STAR("Star"),
    HEART("Heart")
}

data class MaskConfig(
    val type: MaskType = MaskType.NONE,
    val centerX: Float = 0.5f,
    val centerY: Float = 0.5f,
    val radiusOrWidth: Float = 0.38f,
    val heightRatio: Float = 0.38f,
    val rotationDeg: Float = 0f,
    val feather: Float = 0.08f,
    val invert: Boolean = false
)

enum class BlendModeType(val label: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    SOFT_LIGHT("Soft Light"),
    HARD_LIGHT("Hard Light"),
    LIGHTEN("Lighten"),
    DARKEN("Darken"),
    DIFFERENCE("Difference"),
    ADD("Linear Dodge (Add)")
}

data class CutoutConfig(
    val autoCutoutEnabled: Boolean = false,
    val chromaKeyEnabled: Boolean = false,
    val chromaColorHex: Long = 0xFF00FF00L,
    val tolerance: Float = 0.32f,
    val feather: Float = 0.15f,
    val spillSuppression: Float = 0.25f,
    val edgeRefinement: Float = 0.20f,
    val neonStrokeEnabled: Boolean = false,
    val neonStrokeColorHex: Long = 0xFF00E5FFL
)

data class ClipAnimation(
    val inAnimId: String = "none",
    val inDurationMs: Long = 500L,
    val outAnimId: String = "none",
    val outDurationMs: Long = 500L,
    val loopAnimId: String = "none",
    val loopCycleMs: Long = 1200L
)

data class TransitionConfig(
    val transitionId: String = "none",
    val durationMs: Long = 600L,
    val intensity: Float = 1f
)

data class TimelineClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val mediaUri: String = "",
    val sampleDrawableRes: Int = R.drawable.img_sample_cyberpunk,
    val isPhoto: Boolean = false,
    val isOverlay: Boolean = false,
    val overlayStartMs: Long = 0L, // Used when isOverlay == true
    val sourceDurationMs: Long = 6000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 6000L,
    val speedMultiplier: Float = 1.0f,
    val speedPresetId: String = "normal",
    val speedPoints: List<SpeedPoint> = emptyList(),
    val isReversed: Boolean = false,
    val isFrozen: Boolean = false,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val transformX: Float = 0f, // -1f..1f
    val transformY: Float = 0f, // -1f..1f
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f,
    val mirrorH: Boolean = false,
    val mirrorV: Boolean = false,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 0f,
    val cropBottom: Float = 0f,
    val opacity: Float = 1.0f,
    val filterId: String = "none",
    val filterIntensity: Float = 0.85f,
    val adjustments: ColorAdjustment = ColorAdjustment(),
    val mask: MaskConfig = MaskConfig(),
    val blendMode: BlendModeType = BlendModeType.NORMAL,
    val cutout: CutoutConfig = CutoutConfig(),
    val animation: ClipAnimation = ClipAnimation(),
    val transitionAfter: TransitionConfig = TransitionConfig(),
    val keyframes: List<Keyframe> = emptyList(),
    val accentColorHex: Long = 0xFF00E5FFL,
    val speechText: String = "",
    val zoomDirection: ZoomDirection = ZoomDirection.ZOOM_IN,
    val isAutoSplitClip: Boolean = false,
    val sourceGeometry: SourceVideoGeometry? = null
)

enum class AudioCategory(val label: String) {
    MUSIC("Music"),
    SFX("Sound FX"),
    EXTRACTED("Extracted"),
    VOICEOVER("Voiceover")
}

data class AudioClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artistOrSource: String = "NovaCut Studio",
    val audioUri: String = "",
    val builtInSynthId: String = "synth_cyber_pulse",
    val category: AudioCategory = AudioCategory.MUSIC,
    val timelineStartMs: Long = 0L,
    val trimStartMs: Long = 0L,
    val durationMs: Long = 8000L,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val fadeInMs: Long = 300L,
    val fadeOutMs: Long = 500L,
    val speed: Float = 1.0f,
    val waveform: List<Float> = listOf(0.3f, 0.7f, 0.9f, 0.5f, 0.8f, 0.4f, 0.95f, 0.6f, 0.75f, 0.45f, 0.85f, 0.5f),
    val beatMarkersMs: List<Long> = listOf(500L, 1500L, 2500L, 3500L, 4500L, 5500L, 6500L, 7500L)
)

data class CaptionWordTiming(
    val word: String,
    val startOffsetMs: Long,
    val endOffsetMs: Long
)

data class TextClip(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCaption: Boolean = false,
    val timelineStartMs: Long = 0L,
    val durationMs: Long = 3000L,
    val fontFamilyId: String = "space_grotesk",
    val fontSizeSp: Float = 26f,
    val textColorHex: Long = 0xFFFFFFFFL,
    val strokeColorHex: Long = 0xFF000000L,
    val strokeWidth: Float = 2f,
    val shadowColorHex: Long = 0xAA000000L,
    val shadowBlur: Float = 8f,
    val shadowOffsetX: Float = 2f,
    val shadowOffsetY: Float = 3f,
    val backgroundColorHex: Long = 0xFF000000L,
    val backgroundAlpha: Float = 0.0f,
    val backgroundCornerRadius: Float = 10f,
    val alignment: String = "CENTER", // LEFT, CENTER, RIGHT
    val letterSpacing: Float = 0.5f,
    val lineHeightMultiplier: Float = 1.15f,
    val opacity: Float = 1.0f,
    val posX: Float = 0f, // -1f..1f from center
    val posY: Float = 0.55f, // -1f..1f from center
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val inAnimId: String = "pop_up",
    val outAnimId: String = "fade_out",
    val loopAnimId: String = "none",
    val stylePresetId: String = "clean_cinema",
    val wordTimings: List<CaptionWordTiming> = emptyList(),
    val keyframes: List<Keyframe> = emptyList()
)

data class EffectTrackItem(
    val id: String = UUID.randomUUID().toString(),
    val effectDefId: String,
    val effectName: String,
    val category: String,
    val timelineStartMs: Long = 0L,
    val durationMs: Long = 3500L,
    val intensity: Float = 0.75f,
    val speed: Float = 0.6f,
    val secondaryParam: Float = 0.5f,
    val targetLayer: String = "ALL", // ALL, PRIMARY, OVERLAY
    val keyframes: List<Keyframe> = emptyList()
)

data class StickerClip(
    val id: String = UUID.randomUUID().toString(),
    val stickerDefId: String,
    val label: String,
    val symbol: String,
    val accentHex: Long = 0xFF00E5FFL,
    val timelineStartMs: Long = 0L,
    val durationMs: Long = 3000L,
    val posX: Float = 0.35f,
    val posY: Float = -0.35f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f,
    val loopAnimId: String = "pulse",
    val keyframes: List<Keyframe> = emptyList()
)

data class VideoProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis(),
    val aspectRatio: AspectRatioMode = AspectRatioMode.RATIO_9_16,
    val canvasConfig: CanvasConfig = CanvasConfig(),
    val primaryClips: List<TimelineClip> = emptyList(),
    val overlayClips: List<TimelineClip> = emptyList(),
    val audioClips: List<AudioClip> = emptyList(),
    val textClips: List<TextClip> = emptyList(),
    val effectItems: List<EffectTrackItem> = emptyList(),
    val stickerClips: List<StickerClip> = emptyList(),
    val globalFilterId: String = "none",
    val globalFilterIntensity: Float = 0.8f,
    val isMainTrackMuted: Boolean = false,
    val exportResolution: String = "1080p",
    val exportFps: Int = 30,
    val exportBitrateMode: String = "High",
    val customBitrateMbps: Float = 24f,
    val coverDrawableRes: Int = R.drawable.img_sample_cyberpunk,
    val coverTitleText: String = "",
    val templateSourceId: String? = null,
    val sourceGeometry: SourceVideoGeometry = SourceVideoGeometry(),
    val automaticMotionMode: AutomaticMotionMode = AutomaticMotionMode.SIMPLE,
    val autoZoomTargetFactor: Float = 1.10f,
    val detectedSpeechSegments: List<SpeechSegment> = emptyList(),
    val autoEditSnapshot: AutoEditSnapshot? = null
)

enum class MediaAlbumCategory(val label: String) {
    ALBUMS("Albums"),
    GENERATED("Generated"),
    SPACES("Spaces"),
    LIBRARY("Library")
}

enum class MediaTypeFilter(val label: String) {
    ALL("All"),
    VIDEOS("Videos"),
    PHOTOS("Photos")
}

data class MediaItemModel(
    val id: String,
    val title: String,
    val albumName: String,
    val tabCategory: MediaAlbumCategory,
    val isVideo: Boolean,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val uriString: String = "",
    val drawableRes: Int = R.drawable.img_sample_cyberpunk,
    val accentHex: Long = 0xFF00E5FFL,
    val isHd: Boolean = true,
    val fileSizeMb: Float = 14.2f
)

data class ExportConfig(
    val resolution: String = "1080p", // 480p, 720p, 1080p, 1440p, 2160p
    val fps: Int = 30,                // 24, 25, 30, 50, 60
    val qualityPreset: String = "High", // Lower, Standard, High, Custom
    val customBitrateMbps: Float = 24f,
    val codec: String = "H.264 / AVC",  // H.264 / AVC, H.265 / HEVC
    val includeWatermark: Boolean = false,
    val exportAudioOnly: Boolean = false
)

data class ExportProgressState(
    val isExporting: Boolean = false,
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false,
    val errorMessage: String? = null,
    val progressPercent: Int = 0,
    val currentStage: String = "Idle",
    val renderedFrames: Int = 0,
    val totalFrames: Int = 0,
    val outputFilePath: String = "",
    val savedToGalleryUri: String = "",
    val estimatedSizeMb: Float = 18.4f
)

data class StudioNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: String,
    val timestampLabel: String,
    val isUnread: Boolean = true,
    val actionTarget: String = "EDITOR"
)
