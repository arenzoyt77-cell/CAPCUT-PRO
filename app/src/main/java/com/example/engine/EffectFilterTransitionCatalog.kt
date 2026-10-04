package com.example.engine

import com.example.R
import com.example.model.AspectRatioMode
import com.example.model.AudioCategory
import com.example.model.AudioClip
import com.example.model.ClipAnimation
import com.example.model.ColorAdjustment
import com.example.model.EffectTrackItem
import com.example.model.Keyframe
import com.example.model.KeyframeInterpolation
import com.example.model.KeyframeProperty
import com.example.model.SpeedPoint
import com.example.model.StickerClip
import com.example.model.TextClip
import com.example.model.TimelineClip
import com.example.model.TransitionConfig
import com.example.model.VideoProject

data class FilterDefinition(
    val id: String,
    val name: String,
    val category: String,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val brightness: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val fade: Float = 0f,
    val vignette: Float = 0f,
    val rScale: Float = 1f,
    val gScale: Float = 1f,
    val bScale: Float = 1f,
    val accentHex: Long = 0xFF00E5FFL
)

data class EffectDefinition(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val param1Name: String = "Intensity",
    val param2Name: String = "Speed / Rate",
    val param3Name: String = "Spread / Radius",
    val defaultIntensity: Float = 0.75f,
    val defaultSpeed: Float = 0.55f,
    val defaultSecondary: Float = 0.50f,
    val accentHex: Long = 0xFFB388FFL,
    val isPro: Boolean = false
)

data class TransitionDefinition(
    val id: String,
    val name: String,
    val category: String,
    val defaultDurationMs: Long = 600L,
    val accentHex: Long = 0xFF00E5FFL
)

data class TemplateDefinition(
    val id: String,
    val title: String,
    val category: String,
    val author: String,
    val aspectRatio: AspectRatioMode,
    val clipSlotsCount: Int,
    val durationSec: Float,
    val usesCountLabel: String,
    val previewDrawableRes: Int,
    val accentHex: Long,
    val defaultFilterId: String,
    val defaultEffectId: String,
    val defaultTransitionId: String,
    val titleOverlayText: String,
    val subtitleOverlayText: String,
    val audioPresetId: String
)

data class AudioPresetDefinition(
    val id: String,
    val title: String,
    val artist: String,
    val category: AudioCategory,
    val durationMs: Long,
    val bpm: Int,
    val baseFreqHz: Float,
    val waveform: List<Float>,
    val beatMarkersMs: List<Long>
)

data class TextStylePreset(
    val id: String,
    val name: String,
    val fontFamilyId: String,
    val textColorHex: Long,
    val strokeColorHex: Long,
    val strokeWidth: Float,
    val bgHex: Long,
    val bgAlpha: Float,
    val shadowHex: Long
)

data class AnimationPreset(
    val id: String,
    val name: String,
    val type: String, // IN, OUT, LOOP
    val defaultDurationMs: Long = 500L
)

data class StickerDefinition(
    val id: String,
    val label: String,
    val symbol: String,
    val category: String,
    val accentHex: Long
)

data class GlobalSearchResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String, // EFFECT, FILTER, TRANSITION, TEMPLATE, FONT, STICKER, SOUND
    val accentHex: Long
)

object EffectFilterTransitionCatalog {

    val filterCategories = listOf(
        "Featured", "Life", "Movies", "Retro", "Vintage", "B&W",
        "Food", "Nature", "Night Scene", "Warm", "Cool", "Moody",
        "Film", "HDR", "Social", "Style"
    )

    val filters: List<FilterDefinition> = listOf(
        // Featured / Basic
        FilterDefinition("natural_boost", "Clear", "Featured", contrast = 0.12f, saturation = 0.18f, brightness = 0.04f, accentHex = 0xFF00E5FFL),
        FilterDefinition("vivid_pop", "Vivid", "Featured", contrast = 0.22f, saturation = 0.38f, brightness = 0.05f, accentHex = 0xFF00E676L),
        FilterDefinition("crisp_clean", "Cold Brew", "Featured", contrast = 0.18f, saturation = 0.08f, temperature = -0.08f, accentHex = 0xFF40C4FFL),
        // Life / Portrait
        FilterDefinition("velvet_skin", "Velvet Skin", "Life", contrast = -0.06f, saturation = 0.10f, brightness = 0.08f, temperature = 0.14f, fade = 0.08f, accentHex = 0xFFFF80ABL),
        FilterDefinition("peach_glow", "Peach", "Life", saturation = 0.16f, brightness = 0.07f, temperature = 0.22f, tint = 0.15f, rScale = 1.08f, accentHex = 0xFFFFAB91L),
        FilterDefinition("studio_soft", "Creamy", "Life", contrast = 0.08f, saturation = -0.08f, brightness = 0.06f, fade = 0.12f, accentHex = 0xFFF48FB1L),
        // Movies / Cinema
        FilterDefinition("teal_orange", "Teal & Orange", "Movies", contrast = 0.28f, saturation = 0.20f, temperature = -0.15f, rScale = 1.12f, gScale = 1.02f, bScale = 1.14f, vignette = 0.28f, accentHex = 0xFF00BFA5L),
        FilterDefinition("anamorphic_gold", "Oppenheimer", "Movies", contrast = 0.24f, saturation = 0.14f, temperature = 0.32f, rScale = 1.14f, gScale = 1.06f, bScale = 0.88f, vignette = 0.22f, accentHex = 0xFFFFD54FL),
        FilterDefinition("imax_noir", "Gotham Steel", "Movies", contrast = 0.34f, saturation = -0.22f, temperature = -0.26f, bScale = 1.15f, vignette = 0.35f, accentHex = 0xFF546E7AL),
        // Retro
        FilterDefinition("retro_80s", "1980s", "Retro", contrast = 0.15f, saturation = 0.28f, tint = 0.28f, fade = 0.22f, rScale = 1.10f, bScale = 1.16f, accentHex = 0xFFE040FBL),
        FilterDefinition("polaroid_fade", "Miami", "Retro", contrast = -0.10f, saturation = -0.14f, temperature = 0.24f, fade = 0.30f, rScale = 1.08f, gScale = 1.04f, bScale = 0.90f, accentHex = 0xFFFFCC80L),
        FilterDefinition("vhs_amber", "VHS", "Retro", contrast = 0.12f, saturation = -0.18f, temperature = 0.36f, fade = 0.24f, accentHex = 0xFFFF8A65L),
        // Vintage
        FilterDefinition("sepia_heirloom", "Carmel", "Vintage", contrast = 0.14f, saturation = -0.55f, temperature = 0.45f, rScale = 1.18f, gScale = 1.04f, bScale = 0.82f, vignette = 0.30f, accentHex = 0xFFA1887FL),
        FilterDefinition("aged_kodachrome", "Kodak Gold", "Vintage", contrast = 0.26f, saturation = 0.18f, temperature = 0.20f, rScale = 1.12f, gScale = 0.98f, bScale = 0.88f, accentHex = 0xFFD4E157L),
        FilterDefinition("dusty_vinyl", "Vintage 1970", "Vintage", contrast = -0.08f, saturation = -0.28f, fade = 0.34f, vignette = 0.25f, accentHex = 0xFF8D6E63L),
        // B&W
        FilterDefinition("silver_nitrate", "Mono Classic", "B&W", contrast = 0.36f, saturation = -1.0f, brightness = 0.02f, vignette = 0.25f, accentHex = 0xFFCFD8DCL),
        FilterDefinition("noir_shadow", "Noir B&W", "B&W", contrast = 0.55f, saturation = -1.0f, brightness = -0.06f, vignette = 0.42f, accentHex = 0xFF90A4AEL),
        FilterDefinition("matte_mono", "Fade B&W", "B&W", contrast = 0.10f, saturation = -1.0f, fade = 0.28f, accentHex = 0xFFB0BEC5L),
        // Food
        FilterDefinition("gourmet_warm", "Delicious", "Food", contrast = 0.20f, saturation = 0.36f, temperature = 0.28f, rScale = 1.12f, gScale = 1.05f, accentHex = 0xFFFF7043L),
        FilterDefinition("fresh_citrus", "Sweetness", "Food", contrast = 0.16f, saturation = 0.42f, brightness = 0.07f, gScale = 1.10f, accentHex = 0xFFFFCA28L),
        FilterDefinition("espresso_roast", "Latte", "Food", contrast = 0.28f, saturation = 0.12f, temperature = 0.22f, vignette = 0.20f, accentHex = 0xFF6D4C41L),
        // Nature
        FilterDefinition("emerald_forest", "Green Orange", "Nature", contrast = 0.18f, saturation = 0.26f, gScale = 1.15f, bScale = 1.04f, accentHex = 0xFF00C853L),
        FilterDefinition("alpine_air", "Sky Blue", "Nature", contrast = 0.22f, saturation = 0.20f, temperature = -0.18f, bScale = 1.12f, accentHex = 0xFF00B0FFL),
        FilterDefinition("autumn_foliage", "Maple", "Nature", contrast = 0.20f, saturation = 0.30f, temperature = 0.34f, rScale = 1.14f, accentHex = 0xFFFF6D00L),
        // Night Scene
        FilterDefinition("cyber_neon", "Cyberpunk", "Night Scene", contrast = 0.32f, saturation = 0.34f, temperature = -0.24f, tint = 0.28f, rScale = 1.08f, bScale = 1.22f, accentHex = 0xFF00E5FFL),
        FilterDefinition("tokyo_midnight", "Tokyo Night", "Night Scene", contrast = 0.28f, saturation = 0.22f, temperature = -0.32f, gScale = 1.06f, bScale = 1.20f, vignette = 0.28f, accentHex = 0xFF7C4DFFL),
        FilterDefinition("tungsten_street", "Neon Street", "Night Scene", contrast = 0.26f, saturation = 0.18f, temperature = 0.38f, rScale = 1.16f, gScale = 1.06f, bScale = 0.84f, accentHex = 0xFFFFAB00L),
        // Warm
        FilterDefinition("golden_hour", "Warm Sunset", "Warm", contrast = 0.16f, saturation = 0.24f, temperature = 0.42f, rScale = 1.16f, gScale = 1.06f, bScale = 0.86f, accentHex = 0xFFFFB300L),
        FilterDefinition("sahara_sun", "Sunkissed", "Warm", contrast = 0.22f, saturation = 0.18f, temperature = 0.48f, brightness = 0.04f, accentHex = 0xFFFF8F00L),
        FilterDefinition("amber_honey", "Honey", "Warm", contrast = 0.12f, saturation = 0.20f, temperature = 0.30f, fade = 0.14f, accentHex = 0xFFFFC107L),
        // Cool
        FilterDefinition("arctic_blue", "Iceberg", "Cool", contrast = 0.20f, saturation = 0.10f, temperature = -0.42f, bScale = 1.20f, accentHex = 0xFF18FFFFL),
        FilterDefinition("nordic_fjord", "Glacier", "Cool", contrast = 0.18f, saturation = -0.20f, temperature = -0.30f, gScale = 1.04f, bScale = 1.14f, accentHex = 0xFF4FC3F7L),
        FilterDefinition("cobalt_glass", "Sapphire", "Cool", contrast = 0.26f, saturation = 0.16f, temperature = -0.36f, bScale = 1.24f, accentHex = 0xFF2979FFL),
        // Moody
        FilterDefinition("dark_emerald", "Dark Forest", "Moody", contrast = 0.24f, saturation = -0.28f, brightness = -0.06f, gScale = 1.05f, fade = 0.18f, vignette = 0.36f, accentHex = 0xFF26A69AL),
        FilterDefinition("rainy_slate", "Rainday", "Moody", contrast = 0.18f, saturation = -0.38f, temperature = -0.20f, fade = 0.22f, vignette = 0.32f, accentHex = 0xFF78909CL),
        FilterDefinition("obsidian_fade", "Shadow Matte", "Moody", contrast = 0.30f, saturation = -0.45f, brightness = -0.08f, fade = 0.24f, vignette = 0.40f, accentHex = 0xFF455A64L),
        // Film
        FilterDefinition("portra_400", "Fuji Pro", "Film", contrast = 0.14f, saturation = 0.12f, temperature = 0.18f, fade = 0.12f, rScale = 1.06f, gScale = 1.03f, accentHex = 0xFFFFCC80L),
        FilterDefinition("cinestill_800t", "CineStill 800T", "Film", contrast = 0.28f, saturation = 0.18f, temperature = -0.28f, tint = 0.12f, rScale = 1.06f, bScale = 1.18f, accentHex = 0xFF00E5FFL),
        FilterDefinition("fuji_velvia", "Negative Film", "Film", contrast = 0.32f, saturation = 0.44f, gScale = 1.08f, bScale = 1.08f, accentHex = 0xFF00E676L),
        // HDR
        FilterDefinition("hdr_clarity", "HDR Clarity", "HDR", contrast = 0.42f, saturation = 0.28f, brightness = 0.03f, vignette = 0.15f, accentHex = 0xFF00E5FFL),
        FilterDefinition("dynamic_range", "High Contrast", "HDR", contrast = 0.36f, saturation = 0.34f, temperature = 0.06f, accentHex = 0xFF7C4DFFL),
        // Social
        FilterDefinition("aesthetic_cream", "Brighten", "Social", contrast = -0.04f, saturation = -0.10f, brightness = 0.08f, temperature = 0.16f, fade = 0.16f, accentHex = 0xFFFFE082L),
        FilterDefinition("reel_punch", "Viral Pop", "Social", contrast = 0.26f, saturation = 0.32f, brightness = 0.04f, accentHex = 0xFFFF4081L),
        // Style
        FilterDefinition("ultraviolet", "Purple Neon", "Style", contrast = 0.24f, saturation = 0.30f, tint = 0.45f, rScale = 1.14f, gScale = 0.88f, bScale = 1.24f, accentHex = 0xFFD500F9L),
        FilterDefinition("infrared_false", "Red Punk", "Style", contrast = 0.30f, saturation = 0.35f, rScale = 1.32f, gScale = 0.82f, bScale = 1.06f, accentHex = 0xFFFF1744L),
        FilterDefinition("matrix_code", "Matrix Green", "Style", contrast = 0.34f, saturation = -0.20f, rScale = 0.78f, gScale = 1.28f, bScale = 0.84f, accentHex = 0xFF00E676L)
    )

    val effectCategories = listOf(
        "Trending", "Nightclub", "Lens", "Retro", "Glitch", "Distortion",
        "Light", "Spark", "Particles", "Nature", "Comic", "3D",
        "Split", "Shake", "Blur", "Neon", "Dream", "Film",
        "Noise", "Chromatic", "VHS", "RGB", "Cyber", "Energy", "Body"
    )

    val effects: List<EffectDefinition> = listOf(
        EffectDefinition("chromatic_aberration", "Chromatic Blur", "Trending", "CapCut signature red/cyan chromatic split & edge fringe", "Range", "Speed", "Filter", 0.80f, 0.50f, 0.65f, 0xFF00E5FFL),
        EffectDefinition("halation_bloom", "Halo Blur", "Trending", "CapCut viral dreamy opening halo blur & soft glow", "Intensity", "Speed", "Warmth", 0.75f, 0.40f, 0.70f, 0xFFFF8A65L),
        EffectDefinition("bass_impact_shake", "Shake", "Trending", "CapCut beat-synced camera punch zoom and shake", "Strength", "Speed", "Zoom", 0.85f, 0.75f, 0.60f, 0xFFFF3366L),
        EffectDefinition("gaussian_blur", "Oblique Blur", "Blur", "Smooth CapCut optical defocus across the frame", "Blur", "Horizontal", "Rotate", 0.65f, 0.30f, 0.50f, 0xFF64B5F6L),
        EffectDefinition("motion_blur", "Motion Blur", "Blur", "High-velocity directional streak blur", "Blur", "Horizontal", "Mix", 0.75f, 0.60f, 0.50f, 0xFF4FC3F7L),
        EffectDefinition("zoom_blur", "Diamond Zoom", "Trending", "CapCut radial zoom burst with specular glints", "Strength", "Horizontal", "Vertical", 0.80f, 0.50f, 0.50f, 0xFF29B6F6L),
        EffectDefinition("radial_spin_blur", "Vortex Spin", "Nightclub", "Rotational vortex blur around focal point", "Spin", "Range", "Smooth", 0.70f, 0.65f, 0.50f, 0xFF00E5FFL),
        EffectDefinition("cinema_vignette", "Vignette", "Lens", "Natural optical lens falloff darkening frame corners", "Texture", "Range", "Feather", 0.75f, 0.50f, 0.60f, 0xFF90A4AEL),
        EffectDefinition("edge_sharpen", "Smart Sharpen", "Lens", "CapCut micro-contrast detail & clarity enhancer", "Sharpen", "Filter", "Range", 0.70f, 0.40f, 0.50f, 0xFF81C784L),
        EffectDefinition("fisheye_lens", "Fisheye", "Lens", "CapCut wide-angle fisheye barrel distortion", "Twist", "Size", "Range", 0.80f, 0.50f, 0.60f, 0xFF4DB6ACL),
        EffectDefinition("anamorphic_flare", "Laser Beam", "Nightclub", "Horizontal sapphire streak lens flare across highlights", "Atmosphere", "Speed", "Range", 0.85f, 0.45f, 0.75f, 0xFF00B0FFL),
        EffectDefinition("vhs_tape", "1998 VHS", "VHS", "CapCut magnetic tape tracking lines, timecode & color bleed", "Jitter", "Noise", "Filter", 0.80f, 0.65f, 0.70f, 0xFFFFB74DL),
        EffectDefinition("crt_scanlines", "DV Camcorder", "Retro", "Retro camcorder frame with REC indicator & scanlines", "Density", "Speed", "Filter", 0.75f, 0.55f, 0.60f, 0xFFAED581L),
        EffectDefinition("old_film_16mm", "Super 8mm", "Film", "Scratches, dust gate weave, and flickering film roll", "Dust", "Speed", "Filter", 0.75f, 0.60f, 0.55f, 0xFFDCE775L),
        EffectDefinition("film_grain_35mm", "Nostalgia Grain", "Film", "Organic silver-halide celluloid grain texture", "Grain", "Roughness", "Filter", 0.65f, 0.70f, 0.50f, 0xFFFFD54FL),
        EffectDefinition("glitch_displacement", "Glitch Lines", "Glitch", "Horizontal RGB block slicing and glitch tear", "Glitch", "Speed", "Range", 0.80f, 0.75f, 0.65f, 0xFFFF4081L),
        EffectDefinition("rgb_split", "RGB Split", "RGB", "Independent Red, Green, and Blue channel separation", "Offset", "Speed", "Twist", 0.80f, 0.60f, 0.50f, 0xFFE040FBL),
        EffectDefinition("pixel_mosaic", "Pixelate", "Distortion", "Retro square mosaic pixelation grid", "Size", "Filter", "Range", 0.70f, 0.40f, 0.50f, 0xFF7C4DFFL),
        EffectDefinition("water_ripple", "Ripple Distortion", "Distortion", "Concentric refractive wave ripples across frame", "Twist", "Speed", "Size", 0.75f, 0.65f, 0.60f, 0xFF00E5FFL),
        EffectDefinition("sine_wave", "Rebound Swing", "Nightclub", "Rhythmic pendulum wave distortion warp", "Swing", "Speed", "Amplitude", 0.70f, 0.60f, 0.55f, 0xFFFF8A65L),
        EffectDefinition("warm_light_leak", "Sunset Light", "Light", "Organic amber and crimson film edge light burns", "Light", "Speed", "Filter", 0.80f, 0.45f, 0.75f, 0xFFFFAB40L),
        EffectDefinition("strobe_flash", "Black Flash", "Trending", "CapCut viral high-energy rhythmic black/white strobe flash", "Speed", "Intensity", "Glow", 0.75f, 0.80f, 0.50f, 0xFFFFFFFFL),
        EffectDefinition("sparkle_bokeh", "Kira Sparkle", "Spark", "Multi-point starlight glints on bright specular areas", "Size", "Speed", "Number", 0.80f, 0.60f, 0.70f, 0xFFFFF176L),
        EffectDefinition("starfield_particles", "Fireflies", "Particles", "Glowing bokeh dust and rising firefly particles", "Number", "Speed", "Glow", 0.85f, 0.55f, 0.65f, 0xFFFFB300L),
        EffectDefinition("rain_drops", "Rainy Night", "Nature", "Diagonal atmospheric rain streaks with mist", "Number", "Angle", "Speed", 0.80f, 0.75f, 0.60f, 0xFF4FC3F7L),
        EffectDefinition("snowfall", "Snowflakes", "Nature", "Soft layered foreground and background snowflakes", "Number", "Speed", "Size", 0.75f, 0.50f, 0.55f, 0xFFE1F5FEL),
        EffectDefinition("comic_halftone", "Manga Comic", "Comic", "CMYK halftone dot matrix and bold ink contours", "Size", "Filter", "Color", 0.80f, 0.50f, 0.70f, 0xFFFF5252L),
        EffectDefinition("perspective_3d", "3D Zoom Pro", "3D", "Dynamic 3D card pitch/yaw floating camera rig", "Tilt", "Speed", "Range", 0.75f, 0.55f, 0.60f, 0xFF7C4DFFL),
        EffectDefinition("quad_mirror", "Four Screens", "Split", "CapCut 4-screen grid split reflection", "Split", "Speed", "Zoom", 0.85f, 0.50f, 0.50f, 0xFF1DE9B6L),
        EffectDefinition("kaleidoscope", "Kaleidoscope", "Split", "Multi-faceted hexagonal mirror prism rotation", "Count", "Speed", "Range", 0.85f, 0.60f, 0.70f, 0xFFE040FBL),
        EffectDefinition("handheld_shake", "Camera Shake", "Shake", "Organic handheld camera sway & motion", "Range", "Speed", "Rotate", 0.65f, 0.50f, 0.45f, 0xFFFFCA28L),
        EffectDefinition("neon_edge_aura", "Edge Glow", "Trending", "CapCut electric neon contour glow around edges", "Glow", "Speed", "Color", 0.85f, 0.65f, 0.75f, 0xFF00E5FFL),
        EffectDefinition("dreamy_mist", "Dreamy Glow", "Dream", "Pastel highlight halo and soft contrast diffusion", "Glow", "Range", "Filter", 0.75f, 0.40f, 0.65f, 0xFFF48FB1L),
        EffectDefinition("digital_noise", "Film Noise", "Noise", "High-frequency chroma and luminance sensor noise", "Noise", "Color", "Speed", 0.70f, 0.80f, 0.50f, 0xFFB0BEC5L),
        EffectDefinition("chromatic_wave", "Astral Soul Out", "Trending", "CapCut ghosting astral projection zoom wave", "Range", "Speed", "Alpha", 0.80f, 0.60f, 0.65f, 0xFF00E5FFL),
        EffectDefinition("cyber_hud", "Cyber Frame", "Cyber", "Futuristic telemetry reticle, scan grid, and data readout", "Alpha", "Speed", "Size", 0.85f, 0.65f, 0.70f, 0xFF00E676L),
        EffectDefinition("energy_aura", "Lightning Aura", "Energy", "High-voltage plasma arcs and radial shockwave rings", "Strength", "Speed", "Size", 0.85f, 0.75f, 0.70f, 0xFF00E5FFL),
        EffectDefinition("body_neon_clone", "Neon Outline", "Body", "Glowing dual-tone silhouette rim around subject", "Width", "Speed", "Color", 0.80f, 0.60f, 0.65f, 0xFFFF3366L),
        EffectDefinition("hue_cycle", "Flash Warning", "Nightclub", "Continuous 360-degree neon club color strobe", "Color", "Speed", "Glow", 0.80f, 0.70f, 0.75f, 0xFFD500F9L),
        EffectDefinition("thermal_vision", "Negative", "Trending", "Inverted color heat signature palette mapping", "Contrast", "Color", "Glow", 0.85f, 0.50f, 0.65f, 0xFFFF6D00L)
    )

    val transitionCategories = listOf(
        "Overlay", "Camera", "Blur", "Basic", "Light Effect", "Glitch", "Slide",
        "Split", "Mask", "MG", "Social"
    )

    val transitions: List<TransitionDefinition> = listOf(
        TransitionDefinition("none", "None", "Overlay", 0L, 0xFF636E82L),
        TransitionDefinition("cross_dissolve", "Mix", "Overlay", 600L, 0xFF00E5FFL),
        TransitionDefinition("dip_to_black", "Black Fade", "Overlay", 700L, 0xFF90A4AEL),
        TransitionDefinition("dip_to_white", "White Flash", "Overlay", 550L, 0xFFFFFFFFL),
        TransitionDefinition("blur_dissolve", "Vertical Blur", "Blur", 600L, 0xFF64B5F6L),
        TransitionDefinition("whip_pan_left", "Left", "Camera", 450L, 0xFF00E676L),
        TransitionDefinition("whip_pan_right", "Right", "Camera", 450L, 0xFF00E676L),
        TransitionDefinition("push_up", "Up", "Slide", 500L, 0xFF1DE9B6L),
        TransitionDefinition("split_horizontal", "Split", "Split", 550L, 0xFFFFB300L),
        TransitionDefinition("split_vertical", "Blinds", "Split", 550L, 0xFFFFB300L),
        TransitionDefinition("zoom_in_warp", "Pull In", "Camera", 500L, 0xFFFF3366L),
        TransitionDefinition("zoom_out_warp", "Pull Out", "Camera", 500L, 0xFFFF3366L),
        TransitionDefinition("spin_360", "CW Swirl", "Camera", 550L, 0xFF7C4DFFL),
        TransitionDefinition("camera_flash", "Glare", "Light Effect", 400L, 0xFFFFF176L),
        TransitionDefinition("glitch_tear", "Glitch", "Glitch", 450L, 0xFFFF4081L),
        TransitionDefinition("light_leak_burn", "Burn", "Light Effect", 700L, 0xFFFF8A65L),
        TransitionDefinition("circle_iris", "Circle", "Mask", 600L, 0xFF00E5FFL),
        TransitionDefinition("diamond_wipe", "Diamond", "Mask", 600L, 0xFFB388FFL),
        TransitionDefinition("motion_shutter", "Film Roll", "Slide", 500L, 0xFF4DB6ACL),
        TransitionDefinition("cube_3d", "Cube", "MG", 650L, 0xFF7C4DFFL),
        TransitionDefinition("kaleido_morph", "Distortion", "Glitch", 700L, 0xFFE040FBL)
    )

    val templateCategories = listOf(
        "Trending", "Beat", "Cinematic", "Reels", "Shorts", "Travel",
        "Gaming", "Photo", "Lyrics", "Birthday", "Love", "Vlog",
        "Intro", "Outro", "Social"
    )

    val templates: List<TemplateDefinition> = listOf(
        TemplateDefinition(
            id = "tpl_cyber_beat",
            title = "Neon Velocity Beat Sync",
            category = "Trending",
            author = "@capcut_velocity",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 14.5f,
            usesCountLabel = "1.4M uses",
            previewDrawableRes = R.drawable.img_sample_cyberpunk,
            accentHex = 0xFF00E5FFL,
            defaultFilterId = "cyber_neon",
            defaultEffectId = "chromatic_aberration",
            defaultTransitionId = "zoom_in_warp",
            titleOverlayText = "NIGHT VELOCITY",
            subtitleOverlayText = "CAPCUT VELOCITY EDIT",
            audioPresetId = "synth_cyber_pulse"
        ),
        TemplateDefinition(
            id = "tpl_alpine_cinema",
            title = "Dolomites 21:9 Cinema Log",
            category = "Cinematic",
            author = "@capcut_cinema",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            clipSlotsCount = 3,
            durationSec = 18.0f,
            usesCountLabel = "898.4K uses",
            previewDrawableRes = R.drawable.img_sample_alpine,
            accentHex = 0xFFFFB300L,
            defaultFilterId = "teal_orange",
            defaultEffectId = "halation_bloom",
            defaultTransitionId = "cross_dissolve",
            titleOverlayText = "BEYOND THE PEAKS",
            subtitleOverlayText = "OPPENHEIMER & TEAL LUT",
            audioPresetId = "synth_orchestral_horizon"
        ),
        TemplateDefinition(
            id = "tpl_portrait_reel",
            title = "Flash Cut Fashion Reel",
            category = "Reels",
            author = "@ical_capcut",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 4,
            durationSec = 12.0f,
            usesCountLabel = "2.1M uses",
            previewDrawableRes = R.drawable.img_sample_portrait,
            accentHex = 0xFFFF3366L,
            defaultFilterId = "velvet_skin",
            defaultEffectId = "sparkle_bokeh",
            defaultTransitionId = "camera_flash",
            titleOverlayText = "ICAL CAPCUT TREND",
            subtitleOverlayText = "HALO BLUR + DIAMOND ZOOM",
            audioPresetId = "synth_trap_beat"
        ),
        TemplateDefinition(
            id = "tpl_beat_phoonk",
            title = "Bass Shake Montage",
            category = "Beat",
            author = "@phonk_capcut",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 4,
            durationSec = 11.2f,
            usesCountLabel = "3.4M uses",
            previewDrawableRes = R.drawable.img_hero_studio,
            accentHex = 0xFF7C4DFFL,
            defaultFilterId = "hdr_clarity",
            defaultEffectId = "bass_impact_shake",
            defaultTransitionId = "glitch_tear",
            titleOverlayText = "PHONK DRIFT SHAKE",
            subtitleOverlayText = "AUTO VELOCITY + BLACK FLASH",
            audioPresetId = "synth_phonk_drive"
        ),
        TemplateDefinition(
            id = "tpl_shorts_hook",
            title = "High-Retention Creator Hook",
            category = "Shorts",
            author = "@capcut_creators",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 15.0f,
            usesCountLabel = "689.2K uses",
            previewDrawableRes = R.drawable.img_sample_cyberpunk,
            accentHex = 0xFF00E676L,
            defaultFilterId = "vivid_pop",
            defaultEffectId = "edge_sharpen",
            defaultTransitionId = "whip_pan_left",
            titleOverlayText = "AUTO CAPTIONS HOOK",
            subtitleOverlayText = "WATCH UNTIL THE END",
            audioPresetId = "synth_lofi_chill"
        ),
        TemplateDefinition(
            id = "tpl_travel_diary",
            title = "Kodak Gold Travel Postcard",
            category = "Travel",
            author = "@nomad_capcut",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 16.0f,
            usesCountLabel = "574.9K uses",
            previewDrawableRes = R.drawable.img_sample_alpine,
            accentHex = 0xFFFF8A65L,
            defaultFilterId = "aged_kodachrome",
            defaultEffectId = "film_grain_35mm",
            defaultTransitionId = "light_leak_burn",
            titleOverlayText = "SUMMER IN THE ALPS",
            subtitleOverlayText = "1998 VHS • FILM ROLL",
            audioPresetId = "synth_acoustic_sun"
        ),
        TemplateDefinition(
            id = "tpl_gaming_frag",
            title = "240FPS Cyber Frag Highlight",
            category = "Gaming",
            author = "@capcut_esports",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            clipSlotsCount = 3,
            durationSec = 13.5f,
            usesCountLabel = "928.0K uses",
            previewDrawableRes = R.drawable.img_hero_studio,
            accentHex = 0xFF00E5FFL,
            defaultFilterId = "cyber_neon",
            defaultEffectId = "cyber_hud",
            defaultTransitionId = "spin_360",
            titleOverlayText = "ACE CLUTCH // 4K",
            subtitleOverlayText = "SPEED CURVE + RGB SPLIT",
            audioPresetId = "synth_phonk_drive"
        ),
        TemplateDefinition(
            id = "tpl_photo_3d",
            title = "3D Zoom Pro Photo Dump",
            category = "Photo",
            author = "@capcut_trends",
            aspectRatio = AspectRatioMode.RATIO_4_5,
            clipSlotsCount = 4,
            durationSec = 10.0f,
            usesCountLabel = "1.8M uses",
            previewDrawableRes = R.drawable.img_sample_portrait,
            accentHex = 0xFFE040FBL,
            defaultFilterId = "polaroid_fade",
            defaultEffectId = "perspective_3d",
            defaultTransitionId = "camera_flash",
            titleOverlayText = "3D PHOTO DUMP",
            subtitleOverlayText = "CAPCUT 3D ZOOM PRO",
            audioPresetId = "synth_lofi_chill"
        ),
        TemplateDefinition(
            id = "tpl_lyrics_glow",
            title = "Edge Glow Kinetic Lyrics",
            category = "Lyrics",
            author = "@lyrics_capcut",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 2,
            durationSec = 14.0f,
            usesCountLabel = "1.1M uses",
            previewDrawableRes = R.drawable.img_sample_cyberpunk,
            accentHex = 0xFFB388FFL,
            defaultFilterId = "tokyo_midnight",
            defaultEffectId = "neon_edge_aura",
            defaultTransitionId = "blur_dissolve",
            titleOverlayText = "LOST IN THE NEON LIGHTS",
            subtitleOverlayText = "AUTO-SYNCED WORD TIMING",
            audioPresetId = "synth_cyber_pulse"
        ),
        TemplateDefinition(
            id = "tpl_birthday_spark",
            title = "Kira Sparkle Birthday Bash",
            category = "Birthday",
            author = "@moment_capcut",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 12.5f,
            usesCountLabel = "453.7K uses",
            previewDrawableRes = R.drawable.img_sample_portrait,
            accentHex = 0xFFFFD54FL,
            defaultFilterId = "golden_hour",
            defaultEffectId = "sparkle_bokeh",
            defaultTransitionId = "circle_iris",
            titleOverlayText = "HAPPY BIRTHDAY",
            subtitleOverlayText = "KIRA SPARKLE + GOLDEN HOUR",
            audioPresetId = "synth_acoustic_sun"
        ),
        TemplateDefinition(
            id = "tpl_love_dream",
            title = "Dreamy Glow Soft Romance",
            category = "Love",
            author = "@velvet_capcut",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 15.0f,
            usesCountLabel = "791.8K uses",
            previewDrawableRes = R.drawable.img_sample_alpine,
            accentHex = 0xFFF48FB1L,
            defaultFilterId = "peach_glow",
            defaultEffectId = "dreamy_mist",
            defaultTransitionId = "light_leak_burn",
            titleOverlayText = "FOREVER GOLDEN",
            subtitleOverlayText = "PEACH FILTER + DREAMY GLOW",
            audioPresetId = "synth_orchestral_horizon"
        ),
        TemplateDefinition(
            id = "tpl_daily_vlog",
            title = "Fuji Pro Aesthetic Day Vlog",
            category = "Vlog",
            author = "@vlog_capcut",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 4,
            durationSec = 16.5f,
            usesCountLabel = "904.2K uses",
            previewDrawableRes = R.drawable.img_hero_studio,
            accentHex = 0xFF4DB6ACL,
            defaultFilterId = "portra_400",
            defaultEffectId = "cinema_vignette",
            defaultTransitionId = "cross_dissolve",
            titleOverlayText = "07:30 AM // DAILY VLOG",
            subtitleOverlayText = "A DAY IN THE LIFE",
            audioPresetId = "synth_lofi_chill"
        ),
        TemplateDefinition(
            id = "tpl_youtube_intro",
            title = "CapCut Creator Channel Opener",
            category = "Intro",
            author = "@capcut_official",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            clipSlotsCount = 2,
            durationSec = 8.0f,
            usesCountLabel = "679.5K uses",
            previewDrawableRes = R.drawable.img_hero_studio,
            accentHex = 0xFF00E5FFL,
            defaultFilterId = "crisp_clean",
            defaultEffectId = "anamorphic_flare",
            defaultTransitionId = "whip_pan_right",
            titleOverlayText = "CAPCUT ORIGINALS",
            subtitleOverlayText = "SUBSCRIBE • NEW EPISODE",
            audioPresetId = "synth_cyber_pulse"
        ),
        TemplateDefinition(
            id = "tpl_cinema_outro",
            title = "End Credits & Social Callout",
            category = "Outro",
            author = "@capcut_official",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            clipSlotsCount = 2,
            durationSec = 9.0f,
            usesCountLabel = "344.1K uses",
            previewDrawableRes = R.drawable.img_sample_cyberpunk,
            accentHex = 0xFF7C4DFFL,
            defaultFilterId = "imax_noir",
            defaultEffectId = "starfield_particles",
            defaultTransitionId = "dip_to_black",
            titleOverlayText = "THANKS FOR WATCHING",
            subtitleOverlayText = "MADE WITH CAPCUT PRO",
            audioPresetId = "synth_orchestral_horizon"
        ),
        TemplateDefinition(
            id = "tpl_social_promo",
            title = "Viral TikTok & Reels Drop",
            category = "Social",
            author = "@viral_capcut",
            aspectRatio = AspectRatioMode.RATIO_1_1,
            clipSlotsCount = 3,
            durationSec = 10.5f,
            usesCountLabel = "861.9K uses",
            previewDrawableRes = R.drawable.img_sample_portrait,
            accentHex = 0xFFFF6E40L,
            defaultFilterId = "reel_punch",
            defaultEffectId = "rgb_split",
            defaultTransitionId = "split_horizontal",
            titleOverlayText = "LIMITED EDITION DROP",
            subtitleOverlayText = "AVAILABLE NOW WORLDWIDE",
            audioPresetId = "synth_trap_beat"
        )
    )

    val audioPresets: List<AudioPresetDefinition> = listOf(
        AudioPresetDefinition(
            id = "synth_cyber_pulse",
            title = "Cybernetic Horizon (128 BPM)",
            artist = "CapCut Sound Studio",
            category = AudioCategory.MUSIC,
            durationMs = 16000L,
            bpm = 128,
            baseFreqHz = 110f,
            waveform = listOf(0.35f, 0.85f, 0.55f, 0.95f, 0.40f, 0.88f, 0.62f, 0.92f, 0.45f, 0.90f, 0.50f, 0.96f, 0.42f, 0.84f, 0.58f, 0.91f),
            beatMarkersMs = (468L..15500L step 468L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_orchestral_horizon",
            title = "Dolomites Sunrise Score",
            artist = "CapCut Cinema Audio",
            category = AudioCategory.MUSIC,
            durationMs = 18000L,
            bpm = 96,
            baseFreqHz = 146.8f,
            waveform = listOf(0.20f, 0.35f, 0.50f, 0.68f, 0.82f, 0.95f, 0.88f, 0.75f, 0.60f, 0.78f, 0.92f, 0.85f, 0.64f, 0.48f, 0.32f, 0.22f),
            beatMarkersMs = (625L..17500L step 625L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_phonk_drive",
            title = "Tokyo Drift Phonk (Beat Sync)",
            artist = "CapCut Viral Beats",
            category = AudioCategory.MUSIC,
            durationMs = 14000L,
            bpm = 140,
            baseFreqHz = 82.4f,
            waveform = listOf(0.92f, 0.45f, 0.98f, 0.52f, 0.95f, 0.40f, 0.99f, 0.60f, 0.94f, 0.48f, 0.97f, 0.50f, 0.96f, 0.44f, 0.93f, 0.55f),
            beatMarkersMs = (428L..13500L step 428L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_lofi_chill",
            title = "Midnight Aesthetic Lofi",
            artist = "CapCut Vlog Sounds",
            category = AudioCategory.MUSIC,
            durationMs = 16000L,
            bpm = 85,
            baseFreqHz = 196.0f,
            waveform = listOf(0.42f, 0.65f, 0.50f, 0.72f, 0.48f, 0.68f, 0.54f, 0.75f, 0.46f, 0.62f, 0.52f, 0.70f, 0.44f, 0.66f, 0.50f, 0.60f),
            beatMarkersMs = (705L..15500L step 705L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_trap_beat",
            title = "ICAL Velocity 808 Beat",
            artist = "CapCut Trend Lab",
            category = AudioCategory.MUSIC,
            durationMs = 15000L,
            bpm = 150,
            baseFreqHz = 65.4f,
            waveform = listOf(0.90f, 0.30f, 0.85f, 0.40f, 0.96f, 0.35f, 0.88f, 0.45f, 0.92f, 0.32f, 0.95f, 0.38f, 0.89f, 0.42f, 0.94f, 0.36f),
            beatMarkersMs = (400L..14600L step 400L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_acoustic_sun",
            title = "Golden Hour Acoustic",
            artist = "CapCut Originals",
            category = AudioCategory.MUSIC,
            durationMs = 16000L,
            bpm = 110,
            baseFreqHz = 220.0f,
            waveform = listOf(0.38f, 0.62f, 0.74f, 0.56f, 0.80f, 0.64f, 0.72f, 0.52f, 0.78f, 0.60f, 0.70f, 0.54f, 0.76f, 0.58f, 0.68f, 0.46f),
            beatMarkersMs = (545L..15500L step 545L).toList()
        ),
        AudioPresetDefinition(
            id = "sfx_whoosh_cinematic",
            title = "Transition Whoosh Impact",
            artist = "CapCut SFX",
            category = AudioCategory.SFX,
            durationMs = 2000L,
            bpm = 120,
            baseFreqHz = 310f,
            waveform = listOf(0.15f, 0.45f, 0.98f, 0.75f, 0.35f, 0.12f),
            beatMarkersMs = listOf(450L)
        ),
        AudioPresetDefinition(
            id = "sfx_sub_drop",
            title = "Bass Shake Boom 808",
            artist = "CapCut SFX",
            category = AudioCategory.SFX,
            durationMs = 2500L,
            bpm = 120,
            baseFreqHz = 55f,
            waveform = listOf(0.99f, 0.85f, 0.65f, 0.45f, 0.25f, 0.10f),
            beatMarkersMs = listOf(100L)
        ),
        AudioPresetDefinition(
            id = "sfx_camera_shutter",
            title = "Flash Shutter Click",
            artist = "CapCut SFX",
            category = AudioCategory.SFX,
            durationMs = 1200L,
            bpm = 120,
            baseFreqHz = 520f,
            waveform = listOf(0.20f, 0.95f, 0.30f, 0.85f, 0.15f),
            beatMarkersMs = listOf(200L)
        ),
        AudioPresetDefinition(
            id = "sfx_glitch_riser",
            title = "Cyber Glitch Riser",
            artist = "CapCut SFX",
            category = AudioCategory.SFX,
            durationMs = 2200L,
            bpm = 128,
            baseFreqHz = 440f,
            waveform = listOf(0.20f, 0.35f, 0.55f, 0.75f, 0.92f, 0.99f),
            beatMarkersMs = listOf(1800L)
        )
    )

    val textStylePresets: List<TextStylePreset> = listOf(
        TextStylePreset("clean_cinema", "Cinema Subtitle", "plus_jakarta_sans", 0xFFFFFFFFL, 0xFF000000L, 3f, 0xFF000000L, 0f, 0xCC000000L),
        TextStylePreset("neon_cyber", "Cyber Glow", "space_grotesk", 0xFF00E5FFL, 0xFF003844L, 4f, 0xFF090B10L, 0.55f, 0xFF00E5FFL),
        TextStylePreset("bold_headline", "Impact Poster", "bebas_neue", 0xFFFFEA00L, 0xFF000000L, 5f, 0xFF000000L, 0f, 0xEE000000L),
        TextStylePreset("editorial_serif", "Vogue Editorial", "playfair_display", 0xFFFDFBF7L, 0xFF1A1A1AL, 1.5f, 0xFF000000L, 0f, 0x99000000L),
        TextStylePreset("creator_pill", "Creator Pill", "plus_jakarta_sans", 0xFF090B10L, 0xFF000000L, 0f, 0xFF00E5FFL, 0.95f, 0x44000000L),
        TextStylePreset("crimson_badge", "Breaking Badge", "space_grotesk", 0xFFFFFFFFL, 0xFF000000L, 0f, 0xFFFF3366L, 0.92f, 0x66000000L),
        TextStylePreset("mono_timecode", "HUD Telemetry", "jetbrains_mono", 0xFF00E676L, 0xFF002211L, 2f, 0xFF050A08L, 0.72f, 0xAA00E676L),
        TextStylePreset("script_neon", "Neon Script", "pacifico", 0xFFFF80ABL, 0xFF4A0028L, 3f, 0xFF000000L, 0f, 0xFFFF4081L)
    )

    val fontFamiliesCatalog: List<Pair<String, String>> = listOf(
        "space_grotesk" to "Space Grotesk",
        "plus_jakarta_sans" to "Plus Jakarta",
        "bebas_neue" to "Bebas Neue",
        "playfair_display" to "Playfair Serif",
        "jetbrains_mono" to "JetBrains Mono",
        "pacifico" to "Pacifico Script"
    )

    val animationsCatalog: List<AnimationPreset> = listOf(
        AnimationPreset("none", "None", "IN", 0L),
        AnimationPreset("fade_in", "Fade In", "IN", 500L),
        AnimationPreset("pop_up", "Rock Vertically", "IN", 450L),
        AnimationPreset("slide_up", "Swing Bottom", "IN", 500L),
        AnimationPreset("whip_left", "Rock Horizontally", "IN", 400L),
        AnimationPreset("zoom_drop", "Zoom 1", "IN", 500L),
        AnimationPreset("spin_in", "Spin", "IN", 550L),
        AnimationPreset("glitch_in", "Mini Zoom", "IN", 450L),
        AnimationPreset("typewriter", "Typewriter", "IN", 800L),

        AnimationPreset("none", "None", "OUT", 0L),
        AnimationPreset("fade_out", "Fade Out", "OUT", 500L),
        AnimationPreset("slide_down", "Slide Down", "OUT", 450L),
        AnimationPreset("zoom_vanish", "Zoom Out", "OUT", 450L),
        AnimationPreset("glitch_out", "Spin Out", "OUT", 450L),

        AnimationPreset("none", "None", "LOOP", 0L),
        AnimationPreset("pulse", "Pendulum 1", "LOOP", 1000L),
        AnimationPreset("float_wave", "Pendulum 2", "LOOP", 1600L),
        AnimationPreset("neon_flicker", "Distort Left", "LOOP", 800L),
        AnimationPreset("pendulum", "Bounce", "LOOP", 1400L),
        AnimationPreset("jitter", "Wobble", "LOOP", 600L)
    )

    val stickersCatalog: List<StickerDefinition> = listOf(
        StickerDefinition("stk_rec", "REC Indicator", "● REC", "Studio", 0xFFFF3366L),
        StickerDefinition("stk_4k", "4K UHD Badge", "4K UHD", "Studio", 0xFF00E5FFL),
        StickerDefinition("stk_fire", "Trending Fire", "🔥 HOT", "Social", 0xFFFF6E40L),
        StickerDefinition("stk_star", "CapCut Choice", "★ FEATURED", "Cinema", 0xFFFFD54FL),
        StickerDefinition("stk_bolt", "High Voltage", "⚡ FLASH", "Energy", 0xFFFFEA00L),
        StickerDefinition("stk_sound", "Audio On", "♪ SOUND ON", "Social", 0xFF00E676L),
        StickerDefinition("stk_frame", "Crop Crosshair", "⌖ FOCUS", "Studio", 0xFFFFFFFFL),
        StickerDefinition("stk_heart", "Like Burst", "♥ LIKE", "Social", 0xFFFF4081L),
        StickerDefinition("stk_new", "New Drop", "✦ NEW DROP", "Social", 0xFFB388FFL),
        StickerDefinition("stk_cinema", "Anamorphic 2.39", "2.39:1 SCOPE", "Cinema", 0xFF40C4FFL),
        StickerDefinition("stk_arrow", "Swipe Up", "↑ WATCH FULL", "Social", 0xFF1DE9B6L),
        StickerDefinition("stk_spark", "Diamond Glint", "✧ SPARK", "Energy", 0xFFE040FBL)
    )

    fun findFilter(id: String): FilterDefinition? = filters.find { it.id == id }
    fun findEffect(id: String): EffectDefinition? = effects.find { it.id == id }
    fun findTransition(id: String): TransitionDefinition? = transitions.find { it.id == id }
    fun findTemplate(id: String): TemplateDefinition? = templates.find { it.id == id }

    fun safeDrawableRes(resId: Int): Int {
        return when (resId) {
            R.drawable.img_hero_studio,
            R.drawable.img_sample_cyberpunk,
            R.drawable.img_sample_alpine,
            R.drawable.img_sample_portrait,
            R.drawable.img_app_icon,
            R.drawable.img_capcut_icon -> resId
            else -> R.drawable.img_sample_cyberpunk
        }
    }

    /**
     * Instant unified search across Effects, Filters, Transitions, Templates, Fonts, Stickers, and Sounds.
     */
    fun searchAll(query: String): List<GlobalSearchResult> {
        val q = query.trim().lowercase()
        val results = mutableListOf<GlobalSearchResult>()

        effects.filter { q.isEmpty() || it.name.lowercase().contains(q) || it.category.lowercase().contains(q) || it.description.lowercase().contains(q) }
            .forEach {
                results.add(
                    GlobalSearchResult(
                        id = it.id,
                        title = it.name,
                        subtitle = "Video Effect • ${it.category}",
                        type = "EFFECT",
                        accentHex = it.accentHex
                    )
                )
            }

        filters.filter { q.isEmpty() || it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .forEach {
                results.add(
                    GlobalSearchResult(
                        id = it.id,
                        title = it.name,
                        subtitle = "Color Filter • ${it.category}",
                        type = "FILTER",
                        accentHex = it.accentHex
                    )
                )
            }

        transitions.filter { it.id != "none" && (q.isEmpty() || it.name.lowercase().contains(q) || it.category.lowercase().contains(q)) }
            .forEach {
                results.add(
                    GlobalSearchResult(
                        id = it.id,
                        title = it.name,
                        subtitle = "Transition • ${it.category}",
                        type = "TRANSITION",
                        accentHex = it.accentHex
                    )
                )
            }

        templates.filter { q.isEmpty() || it.title.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .forEach {
                results.add(
                    GlobalSearchResult(
                        id = it.id,
                        title = it.title,
                        subtitle = "Template • ${it.category} • ${it.aspectRatio.label}",
                        type = "TEMPLATE",
                        accentHex = it.accentHex
                    )
                )
            }

        fontFamiliesCatalog.filter { q.isEmpty() || it.second.lowercase().contains(q) }
            .forEach {
                results.add(
                    GlobalSearchResult(
                        id = it.first,
                        title = it.second,
                        subtitle = "Typography Font Family",
                        type = "FONT",
                        accentHex = 0xFFFFB300L
                    )
                )
            }

        stickersCatalog.filter { q.isEmpty() || it.label.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .forEach {
                results.add(
                    GlobalSearchResult(
                        id = it.id,
                        title = "${it.symbol}  ${it.label}",
                        subtitle = "Overlay Sticker • ${it.category}",
                        type = "STICKER",
                        accentHex = it.accentHex
                    )
                )
            }

        audioPresets.filter { q.isEmpty() || it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.category.label.lowercase().contains(q) }
            .forEach {
                results.add(
                    GlobalSearchResult(
                        id = it.id,
                        title = it.title,
                        subtitle = "${it.category.label} • ${it.artist} (${it.bpm} BPM)",
                        type = "SOUND",
                        accentHex = 0xFF00E676L
                    )
                )
            }

        return results.take(60)
    }

    /**
     * Builds a complete, editable multi-track VideoProject from any TemplateDefinition.
     */
    fun buildProjectFromTemplate(template: TemplateDefinition, customMediaDrawables: List<Int> = emptyList()): VideoProject {
        val drawables = if (customMediaDrawables.isNotEmpty()) {
            customMediaDrawables
        } else {
            listOf(
                template.previewDrawableRes,
                R.drawable.img_sample_cyberpunk,
                R.drawable.img_sample_alpine,
                R.drawable.img_sample_portrait,
                R.drawable.img_hero_studio
            )
        }
        val slotDurationMs = ((template.durationSec * 1000L) / template.clipSlotsCount.coerceAtLeast(1)).toLong().coerceAtLeast(2500L)
        val clips = (0 until template.clipSlotsCount).map { idx ->
            val drawable = drawables[idx % drawables.size]
            TimelineClip(
                title = "${template.category} Slot ${idx + 1}",
                sampleDrawableRes = drawable,
                sourceDurationMs = slotDurationMs + 1500L,
                trimStartMs = 0L,
                trimEndMs = slotDurationMs,
                filterId = template.defaultFilterId,
                filterIntensity = 0.85f,
                speedPresetId = if (idx == 1 && template.category in listOf("Beat", "Trending", "Gaming")) "montage" else "normal",
                speedPoints = if (idx == 1 && template.category in listOf("Beat", "Trending", "Gaming")) {
                    KeyframeAndSpeedEngine.speedPresets.first { it.id == "montage" }.points
                } else emptyList(),
                transitionAfter = if (idx < template.clipSlotsCount - 1) {
                    TransitionConfig(transitionId = template.defaultTransitionId, durationMs = 550L)
                } else TransitionConfig(),
                keyframes = listOf(
                    Keyframe(
                        timestampMs = 0L,
                        property = KeyframeProperty.SCALE,
                        value = 1.0f,
                        interpolation = KeyframeInterpolation.EASE_IN_OUT
                    ),
                    Keyframe(
                        timestampMs = slotDurationMs,
                        property = KeyframeProperty.SCALE,
                        value = 1.14f,
                        interpolation = KeyframeInterpolation.EASE_IN_OUT
                    )
                ),
                accentColorHex = template.accentHex
            )
        }

        val audioPreset = audioPresets.find { it.id == template.audioPresetId } ?: audioPresets.first()
        val totalDurationMs = clips.sumOf { KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(it) }

        return VideoProject(
            name = template.title,
            aspectRatio = template.aspectRatio,
            primaryClips = clips,
            audioClips = listOf(
                AudioClip(
                    title = audioPreset.title,
                    artistOrSource = audioPreset.artist,
                    builtInSynthId = audioPreset.id,
                    category = audioPreset.category,
                    timelineStartMs = 0L,
                    durationMs = totalDurationMs,
                    waveform = audioPreset.waveform,
                    beatMarkersMs = audioPreset.beatMarkersMs.filter { it < totalDurationMs }
                )
            ),
            textClips = listOf(
                TextClip(
                    text = template.titleOverlayText,
                    timelineStartMs = 200L,
                    durationMs = (totalDurationMs * 0.65f).toLong().coerceAtLeast(2500L),
                    fontFamilyId = "space_grotesk",
                    fontSizeSp = 28f,
                    textColorHex = 0xFFFFFFFFL,
                    strokeColorHex = 0xFF090B10L,
                    strokeWidth = 3f,
                    posY = -0.52f,
                    inAnimId = "pop_up",
                    outAnimId = "fade_out",
                    stylePresetId = "neon_cyber"
                ),
                TextClip(
                    text = template.subtitleOverlayText,
                    isCaption = true,
                    timelineStartMs = 600L,
                    durationMs = (totalDurationMs * 0.8f).toLong().coerceAtLeast(3000L),
                    fontFamilyId = "jetbrains_mono",
                    fontSizeSp = 15f,
                    textColorHex = template.accentHex,
                    backgroundColorHex = 0xFF090B10L,
                    backgroundAlpha = 0.72f,
                    posY = 0.64f,
                    inAnimId = "slide_up",
                    outAnimId = "fade_out",
                    stylePresetId = "mono_timecode"
                )
            ),
            effectItems = listOf(
                EffectTrackItem(
                    effectDefId = template.defaultEffectId,
                    effectName = findEffect(template.defaultEffectId)?.name ?: "Chromatic Split",
                    category = template.category,
                    timelineStartMs = 0L,
                    durationMs = totalDurationMs.coerceAtMost(6000L),
                    intensity = 0.78f,
                    speed = 0.6f
                )
            ),
            stickerClips = listOf(
                StickerClip(
                    stickerDefId = "stk_4k",
                    label = "4K UHD Badge",
                    symbol = "4K UHD",
                    accentHex = template.accentHex,
                    timelineStartMs = 0L,
                    durationMs = 3500L,
                    posX = 0.65f,
                    posY = -0.78f
                )
            ),
            globalFilterId = template.defaultFilterId,
            coverDrawableRes = template.previewDrawableRes,
            coverTitleText = template.titleOverlayText,
            templateSourceId = template.id
        )
    }

    /**
     * Builds the 6 default studio projects shown in the Home screen recent project strip and Projects tab.
     */
    fun buildDefaultSeedProjects(): List<VideoProject> {
        val now = System.currentTimeMillis()
        val projOct03 = VideoProject(
            id = "seed_proj_oct03",
            name = "Project Oct 03 20:11",
            createdAtMs = now - 120_000L,
            updatedAtMs = now - 30_000L,
            aspectRatio = AspectRatioMode.RATIO_16_9,
            exportResolution = "1080p",
            exportFps = 30,
            primaryClips = listOf(
                TimelineClip(
                    id = "seed_clip_oct03_1",
                    title = "Tokyo_Neon_Anamorphic_4K.mp4",
                    sampleDrawableRes = R.drawable.img_sample_cyberpunk,
                    sourceDurationMs = 5500L,
                    trimStartMs = 0L,
                    trimEndMs = 4000L,
                    filterId = "cyber_neon",
                    filterIntensity = 0.85f,
                    transitionAfter = TransitionConfig(transitionId = "cross_dissolve", durationMs = 500L),
                    keyframes = listOf(
                        Keyframe(
                            timestampMs = 0L,
                            property = KeyframeProperty.SCALE,
                            value = 1.0f,
                            interpolation = KeyframeInterpolation.EASE_IN_OUT
                        ),
                        Keyframe(
                            timestampMs = 4000L,
                            property = KeyframeProperty.SCALE,
                            value = 1.12f,
                            interpolation = KeyframeInterpolation.EASE_IN_OUT
                        )
                    )
                ),
                TimelineClip(
                    id = "seed_clip_oct03_2",
                    title = "Dolomites_FPV_Sunrise.mp4",
                    sampleDrawableRes = R.drawable.img_sample_alpine,
                    sourceDurationMs = 6000L,
                    trimStartMs = 0L,
                    trimEndMs = 4500L,
                    filterId = "teal_orange",
                    filterIntensity = 0.80f
                )
            ),
            audioClips = listOf(
                AudioClip(
                    title = "Cybernetic Horizon (128 BPM)",
                    artistOrSource = "NovaCut Sound Studio",
                    builtInSynthId = "synth_cyber_pulse",
                    category = AudioCategory.MUSIC,
                    timelineStartMs = 0L,
                    durationMs = 8000L,
                    waveform = audioPresets.first().waveform,
                    beatMarkersMs = audioPresets.first().beatMarkersMs.filter { it < 8000L }
                )
            ),
            textClips = listOf(
                TextClip(
                    text = "NIGHT VELOCITY // 4K",
                    timelineStartMs = 200L,
                    durationMs = 4200L,
                    fontFamilyId = "space_grotesk",
                    fontSizeSp = 26f,
                    textColorHex = 0xFFFFFFFFL,
                    stylePresetId = "neon_cyber"
                ),
                TextClip(
                    text = "ANAMORPHIC MASTER GRADE",
                    isCaption = true,
                    timelineStartMs = 600L,
                    durationMs = 5000L,
                    fontFamilyId = "jetbrains_mono",
                    fontSizeSp = 15f,
                    textColorHex = 0xFF00E5FFL,
                    stylePresetId = "mono_timecode"
                )
            ),
            effectItems = listOf(
                EffectTrackItem(
                    effectDefId = "chromatic_aberration",
                    effectName = "Chromatic Blur",
                    category = "Trending",
                    timelineStartMs = 0L,
                    durationMs = 5000L,
                    intensity = 0.78f,
                    speed = 0.55f
                )
            ),
            coverDrawableRes = R.drawable.img_sample_cyberpunk,
            coverTitleText = "Project Oct 03 20:11"
        )

        val bassShakeTpl = findTemplate("tpl_beat_phoonk") ?: templates[3]
        val cyberBeatTpl = findTemplate("tpl_cyber_beat") ?: templates[0]
        val portraitReelTpl = findTemplate("tpl_portrait_reel") ?: templates[2]
        val alpineCinemaTpl = findTemplate("tpl_alpine_cinema") ?: templates[1]
        val photo3dTpl = findTemplate("tpl_photo_3d") ?: templates[7]

        val projBassShake = buildProjectFromTemplate(bassShakeTpl).copy(
            id = "seed_proj_2",
            name = "Bass Shake Montage",
            coverDrawableRes = R.drawable.img_hero_studio,
            createdAtMs = now - 1800_000L,
            updatedAtMs = now - 600_000L
        )
        val projCyber = buildProjectFromTemplate(cyberBeatTpl).copy(
            id = "seed_proj_3",
            name = "Neon Velocity Beat Sync",
            coverDrawableRes = R.drawable.img_sample_cyberpunk,
            createdAtMs = now - 3600_000L,
            updatedAtMs = now - 1200_000L
        )
        val projPortrait = buildProjectFromTemplate(portraitReelTpl).copy(
            id = "seed_proj_4",
            name = "Flash Cut Fashion Reel",
            coverDrawableRes = R.drawable.img_sample_portrait,
            createdAtMs = now - 7200_000L,
            updatedAtMs = now - 2400_000L
        )
        val projAlpine = buildProjectFromTemplate(alpineCinemaTpl).copy(
            id = "seed_proj_5",
            name = "Dolomites 21:9 Cinema Log",
            coverDrawableRes = R.drawable.img_sample_alpine,
            createdAtMs = now - 10800_000L,
            updatedAtMs = now - 3600_000L
        )
        val projPhoto3d = buildProjectFromTemplate(photo3dTpl).copy(
            id = "seed_proj_6",
            name = "3D Zoom Pro Photo Dump",
            coverDrawableRes = R.drawable.img_sample_portrait,
            createdAtMs = now - 14400_000L,
            updatedAtMs = now - 4800_000L
        )

        return listOf(projOct03, projBassShake, projCyber, projPortrait, projAlpine, projPhoto3d)
    }
}
