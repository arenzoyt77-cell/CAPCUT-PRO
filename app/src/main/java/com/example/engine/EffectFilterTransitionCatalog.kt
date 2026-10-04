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
        "Basic", "Portrait", "Cinema", "Retro", "Vintage", "B&W",
        "Food", "Nature", "Night", "Warm", "Cool", "Moody",
        "Film", "HDR", "Social", "Creative"
    )

    val filters: List<FilterDefinition> = listOf(
        // Basic
        FilterDefinition("natural_boost", "Natural Boost", "Basic", contrast = 0.12f, saturation = 0.18f, brightness = 0.04f, accentHex = 0xFF00E5FFL),
        FilterDefinition("vivid_pop", "Vivid Pop", "Basic", contrast = 0.22f, saturation = 0.38f, brightness = 0.05f, accentHex = 0xFF00E676L),
        FilterDefinition("crisp_clean", "Crisp Clean", "Basic", contrast = 0.18f, saturation = 0.08f, temperature = -0.08f, accentHex = 0xFF40C4FFL),
        // Portrait
        FilterDefinition("velvet_skin", "Velvet Skin", "Portrait", contrast = -0.06f, saturation = 0.10f, brightness = 0.08f, temperature = 0.14f, fade = 0.08f, accentHex = 0xFFFF80ABL),
        FilterDefinition("peach_glow", "Peach Glow", "Portrait", saturation = 0.16f, brightness = 0.07f, temperature = 0.22f, tint = 0.15f, rScale = 1.08f, accentHex = 0xFFFFAB91L),
        FilterDefinition("studio_soft", "Studio Soft", "Portrait", contrast = 0.08f, saturation = -0.08f, brightness = 0.06f, fade = 0.12f, accentHex = 0xFFF48FB1L),
        // Cinema
        FilterDefinition("teal_orange", "Teal & Orange", "Cinema", contrast = 0.28f, saturation = 0.20f, temperature = -0.15f, rScale = 1.12f, gScale = 1.02f, bScale = 1.14f, vignette = 0.28f, accentHex = 0xFF00BFA5L),
        FilterDefinition("anamorphic_gold", "Anamorphic Gold", "Cinema", contrast = 0.24f, saturation = 0.14f, temperature = 0.32f, rScale = 1.14f, gScale = 1.06f, bScale = 0.88f, vignette = 0.22f, accentHex = 0xFFFFD54FL),
        FilterDefinition("imax_noir", "Blockbuster Steel", "Cinema", contrast = 0.34f, saturation = -0.22f, temperature = -0.26f, bScale = 1.15f, vignette = 0.35f, accentHex = 0xFF546E7AL),
        // Retro
        FilterDefinition("retro_80s", "1984 Synth", "Retro", contrast = 0.15f, saturation = 0.28f, tint = 0.28f, fade = 0.22f, rScale = 1.10f, bScale = 1.16f, accentHex = 0xFFE040FBL),
        FilterDefinition("polaroid_fade", "Instant Fade", "Retro", contrast = -0.10f, saturation = -0.14f, temperature = 0.24f, fade = 0.30f, rScale = 1.08f, gScale = 1.04f, bScale = 0.90f, accentHex = 0xFFFFCC80L),
        FilterDefinition("vhs_amber", "Cassette Warmth", "Retro", contrast = 0.12f, saturation = -0.18f, temperature = 0.36f, fade = 0.24f, accentHex = 0xFFFF8A65L),
        // Vintage
        FilterDefinition("sepia_heirloom", "Heirloom Sepia", "Vintage", contrast = 0.14f, saturation = -0.55f, temperature = 0.45f, rScale = 1.18f, gScale = 1.04f, bScale = 0.82f, vignette = 0.30f, accentHex = 0xFFA1887FL),
        FilterDefinition("aged_kodachrome", "Kodak 64", "Vintage", contrast = 0.26f, saturation = 0.18f, temperature = 0.20f, rScale = 1.12f, gScale = 0.98f, bScale = 0.88f, accentHex = 0xFFD4E157L),
        FilterDefinition("dusty_vinyl", "Dusty Vinyl", "Vintage", contrast = -0.08f, saturation = -0.28f, fade = 0.34f, vignette = 0.25f, accentHex = 0xFF8D6E63L),
        // B&W
        FilterDefinition("silver_nitrate", "Silver Nitrate", "B&W", contrast = 0.36f, saturation = -1.0f, brightness = 0.02f, vignette = 0.25f, accentHex = 0xFFCFD8DCL),
        FilterDefinition("noir_shadow", "High Contrast Noir", "B&W", contrast = 0.55f, saturation = -1.0f, brightness = -0.06f, vignette = 0.42f, accentHex = 0xFF90A4AEL),
        FilterDefinition("matte_mono", "Matte Mono", "B&W", contrast = 0.10f, saturation = -1.0f, fade = 0.28f, accentHex = 0xFFB0BEC5L),
        // Food
        FilterDefinition("gourmet_warm", "Bistro Warm", "Food", contrast = 0.20f, saturation = 0.36f, temperature = 0.28f, rScale = 1.12f, gScale = 1.05f, accentHex = 0xFFFF7043L),
        FilterDefinition("fresh_citrus", "Fresh Citrus", "Food", contrast = 0.16f, saturation = 0.42f, brightness = 0.07f, gScale = 1.10f, accentHex = 0xFFFFCA28L),
        FilterDefinition("espresso_roast", "Espresso Rich", "Food", contrast = 0.28f, saturation = 0.12f, temperature = 0.22f, vignette = 0.20f, accentHex = 0xFF6D4C41L),
        // Nature
        FilterDefinition("emerald_forest", "Emerald Canopy", "Nature", contrast = 0.18f, saturation = 0.26f, gScale = 1.15f, bScale = 1.04f, accentHex = 0xFF00C853L),
        FilterDefinition("alpine_air", "Alpine Air", "Nature", contrast = 0.22f, saturation = 0.20f, temperature = -0.18f, bScale = 1.12f, accentHex = 0xFF00B0FFL),
        FilterDefinition("autumn_foliage", "Autumn Gold", "Nature", contrast = 0.20f, saturation = 0.30f, temperature = 0.34f, rScale = 1.14f, accentHex = 0xFFFF6D00L),
        // Night
        FilterDefinition("cyber_neon", "Cyberpunk Night", "Night", contrast = 0.32f, saturation = 0.34f, temperature = -0.24f, tint = 0.28f, rScale = 1.08f, bScale = 1.22f, accentHex = 0xFF00E5FFL),
        FilterDefinition("tokyo_midnight", "Tokyo Midnight", "Night", contrast = 0.28f, saturation = 0.22f, temperature = -0.32f, gScale = 1.06f, bScale = 1.20f, vignette = 0.28f, accentHex = 0xFF7C4DFFL),
        FilterDefinition("tungsten_street", "Sodium Vapor", "Night", contrast = 0.26f, saturation = 0.18f, temperature = 0.38f, rScale = 1.16f, gScale = 1.06f, bScale = 0.84f, accentHex = 0xFFFFAB00L),
        // Warm
        FilterDefinition("golden_hour", "Golden Hour", "Warm", contrast = 0.16f, saturation = 0.24f, temperature = 0.42f, rScale = 1.16f, gScale = 1.06f, bScale = 0.86f, accentHex = 0xFFFFB300L),
        FilterDefinition("sahara_sun", "Sahara Sun", "Warm", contrast = 0.22f, saturation = 0.18f, temperature = 0.48f, brightness = 0.04f, accentHex = 0xFFFF8F00L),
        FilterDefinition("amber_honey", "Amber Honey", "Warm", contrast = 0.12f, saturation = 0.20f, temperature = 0.30f, fade = 0.14f, accentHex = 0xFFFFC107L),
        // Cool
        FilterDefinition("arctic_blue", "Arctic Ice", "Cool", contrast = 0.20f, saturation = 0.10f, temperature = -0.42f, bScale = 1.20f, accentHex = 0xFF18FFFFL),
        FilterDefinition("nordic_fjord", "Nordic Fjord", "Cool", contrast = 0.18f, saturation = -0.20f, temperature = -0.30f, gScale = 1.04f, bScale = 1.14f, accentHex = 0xFF4FC3F7L),
        FilterDefinition("cobalt_glass", "Cobalt Glass", "Cool", contrast = 0.26f, saturation = 0.16f, temperature = -0.36f, bScale = 1.24f, accentHex = 0xFF2979FFL),
        // Moody
        FilterDefinition("dark_emerald", "Moody Pine", "Moody", contrast = 0.24f, saturation = -0.28f, brightness = -0.06f, gScale = 1.05f, fade = 0.18f, vignette = 0.36f, accentHex = 0xFF26A69AL),
        FilterDefinition("rainy_slate", "Rainy Slate", "Moody", contrast = 0.18f, saturation = -0.38f, temperature = -0.20f, fade = 0.22f, vignette = 0.32f, accentHex = 0xFF78909CL),
        FilterDefinition("obsidian_fade", "Obsidian Ash", "Moody", contrast = 0.30f, saturation = -0.45f, brightness = -0.08f, fade = 0.24f, vignette = 0.40f, accentHex = 0xFF455A64L),
        // Film
        FilterDefinition("portra_400", "Portra 400", "Film", contrast = 0.14f, saturation = 0.12f, temperature = 0.18f, fade = 0.12f, rScale = 1.06f, gScale = 1.03f, accentHex = 0xFFFFCC80L),
        FilterDefinition("cinestill_800t", "CineStill 800T", "Film", contrast = 0.28f, saturation = 0.18f, temperature = -0.28f, tint = 0.12f, rScale = 1.06f, bScale = 1.18f, accentHex = 0xFF00E5FFL),
        FilterDefinition("fuji_velvia", "Velvia 50", "Film", contrast = 0.32f, saturation = 0.44f, gScale = 1.08f, bScale = 1.08f, accentHex = 0xFF00E676L),
        // HDR
        FilterDefinition("hdr_clarity", "HDR Clarity", "HDR", contrast = 0.42f, saturation = 0.28f, brightness = 0.03f, vignette = 0.15f, accentHex = 0xFF00E5FFL),
        FilterDefinition("dynamic_range", "Ultra Dynamic", "HDR", contrast = 0.36f, saturation = 0.34f, temperature = 0.06f, accentHex = 0xFF7C4DFFL),
        // Social
        FilterDefinition("aesthetic_cream", "Clean Cream", "Social", contrast = -0.04f, saturation = -0.10f, brightness = 0.08f, temperature = 0.16f, fade = 0.16f, accentHex = 0xFFFFE082L),
        FilterDefinition("reel_punch", "Reel Punch", "Social", contrast = 0.26f, saturation = 0.32f, brightness = 0.04f, accentHex = 0xFFFF4081L),
        // Creative
        FilterDefinition("ultraviolet", "Ultraviolet Dream", "Creative", contrast = 0.24f, saturation = 0.30f, tint = 0.45f, rScale = 1.14f, gScale = 0.88f, bScale = 1.24f, accentHex = 0xFFD500F9L),
        FilterDefinition("infrared_false", "Aerochrome IR", "Creative", contrast = 0.30f, saturation = 0.35f, rScale = 1.32f, gScale = 0.82f, bScale = 1.06f, accentHex = 0xFFFF1744L),
        FilterDefinition("matrix_code", "Matrix Phosphor", "Creative", contrast = 0.34f, saturation = -0.20f, rScale = 0.78f, gScale = 1.28f, bScale = 0.84f, accentHex = 0xFF00E676L)
    )

    val effectCategories = listOf(
        "Trending", "Basic", "Lens", "Retro", "Glitch", "Distortion",
        "Light", "Spark", "Particles", "Nature", "Comic", "3D",
        "Motion", "Shake", "Blur", "Neon", "Dream", "Film",
        "Noise", "Chromatic", "VHS", "RGB", "Cyber", "Energy", "Body"
    )

    val effects: List<EffectDefinition> = listOf(
        EffectDefinition("chromatic_aberration", "Chromatic Split", "Trending", "Anamorphic red/cyan channel fringe at frame edges", "Fringe Spread", "Pulse Speed", "Edge Falloff", 0.80f, 0.50f, 0.65f, 0xFF00E5FFL),
        EffectDefinition("halation_bloom", "Halation Glow", "Trending", "Warm cinema film highlight bloom and soft diffusion", "Glow Radius", "Threshold", "Warmth", 0.75f, 0.40f, 0.70f, 0xFFFF8A65L),
        EffectDefinition("bass_impact_shake", "Bass Impact", "Trending", "Rhythmic camera punch zoom and directional shake", "Impact Force", "Frequency", "Zoom Amount", 0.85f, 0.75f, 0.60f, 0xFFFF3366L),
        EffectDefinition("gaussian_blur", "Gaussian Blur", "Blur", "Smooth optical defocus across the frame", "Blur Radius", "Softness", "Mix", 0.65f, 0.30f, 0.50f, 0xFF64B5F6L),
        EffectDefinition("motion_blur", "Directional Blur", "Blur", "High-velocity horizontal motion streak blur", "Streak Length", "Angle", "Mix", 0.75f, 0.60f, 0.50f, 0xFF4FC3F7L),
        EffectDefinition("zoom_blur", "Zoom Warp Blur", "Blur", "Radial center-outward zoom streak bursts", "Burst Strength", "Center X", "Center Y", 0.80f, 0.50f, 0.50f, 0xFF29B6F6L),
        EffectDefinition("radial_spin_blur", "Radial Spin", "Blur", "Rotational vortex blur around focal point", "Spin Angle", "Radius", "Smoothness", 0.70f, 0.65f, 0.50f, 0xFF00E5FFL),
        EffectDefinition("cinema_vignette", "Cinema Vignette", "Basic", "Natural optical lens falloff darkening frame corners", "Darkness", "Roundness", "Feather", 0.75f, 0.50f, 0.60f, 0xFF90A4AEL),
        EffectDefinition("edge_sharpen", "Crisp Sharpen", "Basic", "Micro-contrast unsharp mask detail enhancer", "Sharpness", "Radius", "Threshold", 0.70f, 0.40f, 0.50f, 0xFF81C784L),
        EffectDefinition("fisheye_lens", "Fisheye 8mm", "Lens", "Wide-angle barrel distortion with curved horizon", "Curvature", "Zoom Trim", "Vignette", 0.80f, 0.50f, 0.60f, 0xFF4DB6ACL),
        EffectDefinition("anamorphic_flare", "Anamorphic Flare", "Lens", "Horizontal sapphire streak lens flare across highlights", "Flare Brightness", "Sweep Speed", "Streak Width", 0.85f, 0.45f, 0.75f, 0xFF00B0FFL),
        EffectDefinition("vhs_tape", "VHS 1992 Tape", "VHS", "Authentic magnetic tape tracking lines, timecode & color bleed", "Tracking Jitter", "Noise Level", "Color Bleed", 0.80f, 0.65f, 0.70f, 0xFFFFB74DL),
        EffectDefinition("crt_scanlines", "CRT Monitor", "Retro", "Phosphor RGB aperture grill and rolling scanlines", "Line Density", "Roll Speed", "Curvature", 0.75f, 0.55f, 0.60f, 0xFFAED581L),
        EffectDefinition("old_film_16mm", "16mm Projector", "Film", "Scratches, dust gate weave, and flickering projector bulb", "Scratch Density", "Flicker Rate", "Sepia Tone", 0.75f, 0.60f, 0.55f, 0xFFDCE775L),
        EffectDefinition("film_grain_35mm", "35mm Film Grain", "Film", "Organic silver-halide celluloid grain texture", "Grain Size", "Roughness", "Luma Mix", 0.65f, 0.70f, 0.50f, 0xFFFFD54FL),
        EffectDefinition("glitch_displacement", "Digital Glitch", "Glitch", "Horizontal RGB block slicing and datamosh tear", "Slice Amount", "Glitch Rate", "RGB Offset", 0.80f, 0.75f, 0.65f, 0xFFFF4081L),
        EffectDefinition("rgb_split", "RGB Prism Split", "RGB", "Independent Red, Green, and Blue channel separation", "Separation", "Rotation", "Pulse", 0.80f, 0.60f, 0.50f, 0xFFE040FBL),
        EffectDefinition("pixel_mosaic", "Mosaic Pixelate", "Distortion", "Retro 8-bit square pixelation grid", "Block Size", "Grid Contrast", "Mix", 0.70f, 0.40f, 0.50f, 0xFF7C4DFFL),
        EffectDefinition("water_ripple", "Liquid Ripple", "Distortion", "Concentric refractive wave ripples across frame", "Wave Amplitude", "Ripple Speed", "Frequency", 0.75f, 0.65f, 0.60f, 0xFF00E5FFL),
        EffectDefinition("sine_wave", "Heat Haze Wave", "Distortion", "Sinusoidal mirage distortion warp", "Warp Amount", "Wave Speed", "Wavelength", 0.70f, 0.60f, 0.55f, 0xFFFF8A65L),
        EffectDefinition("warm_light_leak", "Solar Light Leak", "Light", "Organic amber and crimson film edge light burns", "Burn Intensity", "Drift Speed", "Warmth", 0.80f, 0.45f, 0.75f, 0xFFFFAB40L),
        EffectDefinition("strobe_flash", "Strobe Flash", "Light", "High-energy club white/cyan rhythmic exposure flash", "Flash Peak", "Strobe BPM", "Decay", 0.75f, 0.80f, 0.50f, 0xFFFFFFFFL),
        EffectDefinition("sparkle_bokeh", "Diamond Sparkles", "Spark", "Multi-point starlight glints on bright specular areas", "Star Size", "Twinkle Speed", "Density", 0.80f, 0.60f, 0.70f, 0xFFFFF176L),
        EffectDefinition("starfield_particles", "Floating Embers", "Particles", "Glowing bokeh dust and rising firefly particles", "Particle Count", "Rise Speed", "Glow", 0.85f, 0.55f, 0.65f, 0xFFFFB300L),
        EffectDefinition("rain_drops", "Cinema Rain", "Nature", "Diagonal atmospheric rain streaks with mist", "Rain Density", "Wind Angle", "Streak Length", 0.80f, 0.75f, 0.60f, 0xFF4FC3F7L),
        EffectDefinition("snowfall", "Winter Snow", "Nature", "Soft layered foreground and background snowflakes", "Flake Count", "Fall Speed", "Swirl", 0.75f, 0.50f, 0.55f, 0xFFE1F5FEL),
        EffectDefinition("comic_halftone", "Pop Art Halftone", "Comic", "CMYK Ben-Day dot matrix and bold ink contours", "Dot Scale", "Ink Weight", "Color Pop", 0.80f, 0.50f, 0.70f, 0xFFFF5252L),
        EffectDefinition("perspective_3d", "3D Perspective Tilt", "3D", "Dynamic 3D card pitch/yaw floating camera rig", "Tilt Angle", "Orbit Speed", "Depth", 0.75f, 0.55f, 0.60f, 0xFF7C4DFFL),
        EffectDefinition("quad_mirror", "Quad Mirror", "Motion", "4-way symmetrical kaleidoscope tile reflection", "Tile Split", "Slide Speed", "Zoom", 0.85f, 0.50f, 0.50f, 0xFF1DE9B6L),
        EffectDefinition("kaleidoscope", "Kaleidoscope Prism", "Creative", "Multi-faceted hexagonal mirror prism rotation", "Facets", "Spin Speed", "Radius", 0.85f, 0.60f, 0.70f, 0xFFE040FBL),
        EffectDefinition("handheld_shake", "Handheld Docu", "Shake", "Organic cinema verite camera operator motion", "Sway Amount", "Frequency", "Rotation", 0.65f, 0.50f, 0.45f, 0xFFFFCA28L),
        EffectDefinition("neon_edge_aura", "Neon Edge Glow", "Neon", "Electric cyan and magenta laser contour glow", "Neon Brightness", "Pulse Speed", "Hue Shift", 0.85f, 0.65f, 0.75f, 0xFF00E5FFL),
        EffectDefinition("dreamy_mist", "Dreamy Diffusion", "Dream", "Pro-Mist filter pastel highlight halo and soft contrast", "Mist Strength", "Bloom Size", "Pastel Tint", 0.75f, 0.40f, 0.65f, 0xFFF48FB1L),
        EffectDefinition("digital_noise", "ISO 12800 Noise", "Noise", "High-frequency chroma and luminance sensor noise", "Noise Amount", "Chroma Mix", "Speed", 0.70f, 0.80f, 0.50f, 0xFFB0BEC5L),
        EffectDefinition("chromatic_wave", "Chromatic Wave", "Chromatic", "Liquid rainbow prism dispersion wave", "Dispersion", "Wave Speed", "Angle", 0.80f, 0.60f, 0.65f, 0xFF00E5FFL),
        EffectDefinition("cyber_hud", "Cyberpunk HUD", "Cyber", "Futuristic telemetry reticle, scan grid, and data readout", "HUD Opacity", "Scan Speed", "Grid Scale", 0.85f, 0.65f, 0.70f, 0xFF00E676L),
        EffectDefinition("energy_aura", "Super Aura", "Energy", "High-voltage plasma arcs and radial shockwave rings", "Plasma Power", "Arc Speed", "Ring Scale", 0.85f, 0.75f, 0.70f, 0xFF00E5FFL),
        EffectDefinition("body_neon_clone", "Subject Neon Rim", "Body", "Glowing dual-tone silhouette rim around subject", "Rim Width", "Pulse Speed", "Offset", 0.80f, 0.60f, 0.65f, 0xFFFF3366L),
        EffectDefinition("hue_cycle", "Psychedelic Hue", "Retro", "Continuous 360-degree color wheel phase rotation", "Hue Shift", "Cycle Speed", "Saturation", 0.80f, 0.70f, 0.75f, 0xFFD500F9L),
        EffectDefinition("thermal_vision", "Thermal Infrared", "Cyber", "False-color heat signature palette mapping", "Heat Contrast", "Palette Shift", "Glow", 0.85f, 0.50f, 0.65f, 0xFFFF6D00L)
    )

    val transitionCategories = listOf(
        "Basic", "Fade", "Blur", "Slide", "Split", "Zoom", "Spin",
        "Flash", "Glitch", "Light", "Mask", "Motion", "3D", "Creative"
    )

    val transitions: List<TransitionDefinition> = listOf(
        TransitionDefinition("none", "None (Cut)", "Basic", 0L, 0xFF636E82L),
        TransitionDefinition("cross_dissolve", "Cross Dissolve", "Basic", 600L, 0xFF00E5FFL),
        TransitionDefinition("dip_to_black", "Fade to Black", "Fade", 700L, 0xFF90A4AEL),
        TransitionDefinition("dip_to_white", "Fade to White", "Fade", 650L, 0xFFFFFFFFL),
        TransitionDefinition("blur_dissolve", "Optical Blur Dissolve", "Blur", 600L, 0xFF64B5F6L),
        TransitionDefinition("whip_pan_left", "Whip Pan Left", "Slide", 450L, 0xFF00E676L),
        TransitionDefinition("whip_pan_right", "Whip Pan Right", "Slide", 450L, 0xFF00E676L),
        TransitionDefinition("push_up", "Vertical Push Up", "Slide", 500L, 0xFF1DE9B6L),
        TransitionDefinition("split_horizontal", "Split Barn Door", "Split", 550L, 0xFFFFB300L),
        TransitionDefinition("split_vertical", "Vertical Split", "Split", 550L, 0xFFFFB300L),
        TransitionDefinition("zoom_in_warp", "Hyper Zoom In", "Zoom", 500L, 0xFFFF3366L),
        TransitionDefinition("zoom_out_warp", "Hyper Zoom Out", "Zoom", 500L, 0xFFFF3366L),
        TransitionDefinition("spin_360", "Vortex Spin 360", "Spin", 550L, 0xFF7C4DFFL),
        TransitionDefinition("camera_flash", "Paprazzi Flash", "Flash", 400L, 0xFFFFF176L),
        TransitionDefinition("glitch_tear", "Datamosh Glitch", "Glitch", 450L, 0xFFFF4081L),
        TransitionDefinition("light_leak_burn", "Film Burn Leak", "Light", 700L, 0xFFFF8A65L),
        TransitionDefinition("circle_iris", "Circle Iris Wipe", "Mask", 600L, 0xFF00E5FFL),
        TransitionDefinition("diamond_wipe", "Diamond Mask", "Mask", 600L, 0xFFB388FFL),
        TransitionDefinition("motion_shutter", "Shutter Roll", "Motion", 500L, 0xFF4DB6ACL),
        TransitionDefinition("cube_3d", "3D Cube Rotate", "3D", 650L, 0xFF7C4DFFL),
        TransitionDefinition("kaleido_morph", "Kaleido Morph", "Creative", 700L, 0xFFE040FBL)
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
            author = "@novastudio_pro",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 14.5f,
            usesCountLabel = "142.8K uses",
            previewDrawableRes = R.drawable.img_sample_cyberpunk,
            accentHex = 0xFF00E5FFL,
            defaultFilterId = "cyber_neon",
            defaultEffectId = "chromatic_aberration",
            defaultTransitionId = "zoom_in_warp",
            titleOverlayText = "NIGHT VELOCITY",
            subtitleOverlayText = "2026 ANAMORPHIC EDITION",
            audioPresetId = "synth_cyber_pulse"
        ),
        TemplateDefinition(
            id = "tpl_alpine_cinema",
            title = "Dolomites 21:9 Cinema Log",
            category = "Cinematic",
            author = "@cinema_colorist",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            clipSlotsCount = 3,
            durationSec = 18.0f,
            usesCountLabel = "98.4K uses",
            previewDrawableRes = R.drawable.img_sample_alpine,
            accentHex = 0xFFFFB300L,
            defaultFilterId = "teal_orange",
            defaultEffectId = "halation_bloom",
            defaultTransitionId = "cross_dissolve",
            titleOverlayText = "BEYOND THE PEAKS",
            subtitleOverlayText = "SHOT ON 35MM ANAMORPHIC",
            audioPresetId = "synth_orchestral_horizon"
        ),
        TemplateDefinition(
            id = "tpl_portrait_reel",
            title = "Flash Cut Fashion Reel",
            category = "Reels",
            author = "@editorial_cuts",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 4,
            durationSec = 12.0f,
            usesCountLabel = "215.1K uses",
            previewDrawableRes = R.drawable.img_sample_portrait,
            accentHex = 0xFFFF3366L,
            defaultFilterId = "velvet_skin",
            defaultEffectId = "sparkle_bokeh",
            defaultTransitionId = "camera_flash",
            titleOverlayText = "STUDIO SERIES",
            subtitleOverlayText = "COLLECTION 04",
            audioPresetId = "synth_trap_beat"
        ),
        TemplateDefinition(
            id = "tpl_beat_phoonk",
            title = "Bass Shake Montage",
            category = "Beat",
            author = "@velocity_fx",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 4,
            durationSec = 11.2f,
            usesCountLabel = "310.5K uses",
            previewDrawableRes = R.drawable.img_hero_studio,
            accentHex = 0xFF7C4DFFL,
            defaultFilterId = "hdr_clarity",
            defaultEffectId = "bass_impact_shake",
            defaultTransitionId = "glitch_tear",
            titleOverlayText = "MAXIMUM OVERDRIVE",
            subtitleOverlayText = "SPEED RAMP + SHAKE",
            audioPresetId = "synth_phonk_drive"
        ),
        TemplateDefinition(
            id = "tpl_shorts_hook",
            title = "High-Retention Creator Hook",
            category = "Shorts",
            author = "@creator_lab",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 15.0f,
            usesCountLabel = "89.2K uses",
            previewDrawableRes = R.drawable.img_sample_cyberpunk,
            accentHex = 0xFF00E676L,
            defaultFilterId = "vivid_pop",
            defaultEffectId = "edge_sharpen",
            defaultTransitionId = "whip_pan_left",
            titleOverlayText = "3 SECRETS OF CINEMA",
            subtitleOverlayText = "WATCH UNTIL THE END",
            audioPresetId = "synth_lofi_chill"
        ),
        TemplateDefinition(
            id = "tpl_travel_diary",
            title = "Kodachrome Travel Postcard",
            category = "Travel",
            author = "@nomad_frames",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 16.0f,
            usesCountLabel = "74.9K uses",
            previewDrawableRes = R.drawable.img_sample_alpine,
            accentHex = 0xFFFF8A65L,
            defaultFilterId = "aged_kodachrome",
            defaultEffectId = "film_grain_35mm",
            defaultTransitionId = "light_leak_burn",
            titleOverlayText = "SUMMER IN THE ALPS",
            subtitleOverlayText = "FIELD NOTES • CH. 02",
            audioPresetId = "synth_acoustic_sun"
        ),
        TemplateDefinition(
            id = "tpl_gaming_frag",
            title = "240FPS Cyber Frag Highlight",
            category = "Gaming",
            author = "@esports_vfx",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            clipSlotsCount = 3,
            durationSec = 13.5f,
            usesCountLabel = "128.0K uses",
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
            title = "3D Parallax Photo Dump",
            category = "Photo",
            author = "@lens_poetry",
            aspectRatio = AspectRatioMode.RATIO_4_5,
            clipSlotsCount = 4,
            durationSec = 10.0f,
            usesCountLabel = "66.3K uses",
            previewDrawableRes = R.drawable.img_sample_portrait,
            accentHex = 0xFFE040FBL,
            defaultFilterId = "polaroid_fade",
            defaultEffectId = "perspective_3d",
            defaultTransitionId = "camera_flash",
            titleOverlayText = "WEEKEND ARCHIVE",
            subtitleOverlayText = "35MM STILL SERIES",
            audioPresetId = "synth_lofi_chill"
        ),
        TemplateDefinition(
            id = "tpl_lyrics_glow",
            title = "Neon Kinetic Typography Lyrics",
            category = "Lyrics",
            author = "@type_motion",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 2,
            durationSec = 14.0f,
            usesCountLabel = "112.4K uses",
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
            title = "Golden Confetti Celebration",
            category = "Birthday",
            author = "@moment_studio",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 12.5f,
            usesCountLabel = "53.7K uses",
            previewDrawableRes = R.drawable.img_sample_portrait,
            accentHex = 0xFFFFD54FL,
            defaultFilterId = "golden_hour",
            defaultEffectId = "sparkle_bokeh",
            defaultTransitionId = "circle_iris",
            titleOverlayText = "HAPPY BIRTHDAY",
            subtitleOverlayText = "ANOTHER CHAPTER BEGINS",
            audioPresetId = "synth_acoustic_sun"
        ),
        TemplateDefinition(
            id = "tpl_love_dream",
            title = "Soft Pro-Mist Romance",
            category = "Love",
            author = "@velvet_reels",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 3,
            durationSec = 15.0f,
            usesCountLabel = "91.8K uses",
            previewDrawableRes = R.drawable.img_sample_alpine,
            accentHex = 0xFFF48FB1L,
            defaultFilterId = "peach_glow",
            defaultEffectId = "dreamy_mist",
            defaultTransitionId = "light_leak_burn",
            titleOverlayText = "FOREVER GOLDEN",
            subtitleOverlayText = "OUR STORY IN MOTION",
            audioPresetId = "synth_orchestral_horizon"
        ),
        TemplateDefinition(
            id = "tpl_daily_vlog",
            title = "Minimalist Aesthetic Day Vlog",
            category = "Vlog",
            author = "@studio_minimal",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            clipSlotsCount = 4,
            durationSec = 16.5f,
            usesCountLabel = "104.2K uses",
            previewDrawableRes = R.drawable.img_hero_studio,
            accentHex = 0xFF4DB6ACL,
            defaultFilterId = "portra_400",
            defaultEffectId = "cinema_vignette",
            defaultTransitionId = "cross_dissolve",
            titleOverlayText = "07:30 AM // STUDIO LOG",
            subtitleOverlayText = "A DAY IN THE LIFE",
            audioPresetId = "synth_lofi_chill"
        ),
        TemplateDefinition(
            id = "tpl_youtube_intro",
            title = "Clean Tech Channel Opener",
            category = "Intro",
            author = "@broadcast_design",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            clipSlotsCount = 2,
            durationSec = 8.0f,
            usesCountLabel = "79.5K uses",
            previewDrawableRes = R.drawable.img_hero_studio,
            accentHex = 0xFF00E5FFL,
            defaultFilterId = "crisp_clean",
            defaultEffectId = "anamorphic_flare",
            defaultTransitionId = "whip_pan_right",
            titleOverlayText = "NOVACUT ORIGINALS",
            subtitleOverlayText = "SUBSCRIBE • NEW EPISODE",
            audioPresetId = "synth_cyber_pulse"
        ),
        TemplateDefinition(
            id = "tpl_cinema_outro",
            title = "End Credits & Social Callout",
            category = "Outro",
            author = "@broadcast_design",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            clipSlotsCount = 2,
            durationSec = 9.0f,
            usesCountLabel = "44.1K uses",
            previewDrawableRes = R.drawable.img_sample_cyberpunk,
            accentHex = 0xFF7C4DFFL,
            defaultFilterId = "imax_noir",
            defaultEffectId = "starfield_particles",
            defaultTransitionId = "dip_to_black",
            titleOverlayText = "THANKS FOR WATCHING",
            subtitleOverlayText = "WATCH NEXT EPISODE →",
            audioPresetId = "synth_orchestral_horizon"
        ),
        TemplateDefinition(
            id = "tpl_social_promo",
            title = "Bold Brand Product Drop",
            category = "Social",
            author = "@agency_motion",
            aspectRatio = AspectRatioMode.RATIO_1_1,
            clipSlotsCount = 3,
            durationSec = 10.5f,
            usesCountLabel = "61.9K uses",
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
            artist = "NovaCut Synth Lab",
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
            artist = "Aether Strings",
            category = AudioCategory.MUSIC,
            durationMs = 18000L,
            bpm = 96,
            baseFreqHz = 146.8f,
            waveform = listOf(0.20f, 0.35f, 0.50f, 0.68f, 0.82f, 0.95f, 0.88f, 0.75f, 0.60f, 0.78f, 0.92f, 0.85f, 0.64f, 0.48f, 0.32f, 0.22f),
            beatMarkersMs = (625L..17500L step 625L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_phonk_drive",
            title = "Tokyo Drift Overdrive",
            artist = "Kuro Bassline",
            category = AudioCategory.MUSIC,
            durationMs = 14000L,
            bpm = 140,
            baseFreqHz = 82.4f,
            waveform = listOf(0.92f, 0.45f, 0.98f, 0.52f, 0.95f, 0.40f, 0.99f, 0.60f, 0.94f, 0.48f, 0.97f, 0.50f, 0.96f, 0.44f, 0.93f, 0.55f),
            beatMarkersMs = (428L..13500L step 428L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_lofi_chill",
            title = "Midnight Espresso Vinyl",
            artist = "Velvet Keys",
            category = AudioCategory.MUSIC,
            durationMs = 16000L,
            bpm = 85,
            baseFreqHz = 196.0f,
            waveform = listOf(0.42f, 0.65f, 0.50f, 0.72f, 0.48f, 0.68f, 0.54f, 0.75f, 0.46f, 0.62f, 0.52f, 0.70f, 0.44f, 0.66f, 0.50f, 0.60f),
            beatMarkersMs = (705L..15500L step 705L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_trap_beat",
            title = "Studio Runway 808",
            artist = "Metro Pulse",
            category = AudioCategory.MUSIC,
            durationMs = 15000L,
            bpm = 150,
            baseFreqHz = 65.4f,
            waveform = listOf(0.90f, 0.30f, 0.85f, 0.40f, 0.96f, 0.35f, 0.88f, 0.45f, 0.92f, 0.32f, 0.95f, 0.38f, 0.89f, 0.42f, 0.94f, 0.36f),
            beatMarkersMs = (400L..14600L step 400L).toList()
        ),
        AudioPresetDefinition(
            id = "synth_acoustic_sun",
            title = "Golden Coast Acoustic",
            artist = "Solstice Duo",
            category = AudioCategory.MUSIC,
            durationMs = 16000L,
            bpm = 110,
            baseFreqHz = 220.0f,
            waveform = listOf(0.38f, 0.62f, 0.74f, 0.56f, 0.80f, 0.64f, 0.72f, 0.52f, 0.78f, 0.60f, 0.70f, 0.54f, 0.76f, 0.58f, 0.68f, 0.46f),
            beatMarkersMs = (545L..15500L step 545L).toList()
        ),
        AudioPresetDefinition(
            id = "sfx_whoosh_cinematic",
            title = "Anamorphic Whoosh Impact",
            artist = "NovaCut Foley",
            category = AudioCategory.SFX,
            durationMs = 2000L,
            bpm = 120,
            baseFreqHz = 310f,
            waveform = listOf(0.15f, 0.45f, 0.98f, 0.75f, 0.35f, 0.12f),
            beatMarkersMs = listOf(450L)
        ),
        AudioPresetDefinition(
            id = "sfx_sub_drop",
            title = "Cinema Sub-Bass Boom",
            artist = "NovaCut Foley",
            category = AudioCategory.SFX,
            durationMs = 2500L,
            bpm = 120,
            baseFreqHz = 55f,
            waveform = listOf(0.99f, 0.85f, 0.65f, 0.45f, 0.25f, 0.10f),
            beatMarkersMs = listOf(100L)
        ),
        AudioPresetDefinition(
            id = "sfx_camera_shutter",
            title = "35mm SLR Shutter Click",
            artist = "NovaCut Foley",
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
            artist = "NovaCut Foley",
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
        AnimationPreset("pop_up", "Spring Pop", "IN", 450L),
        AnimationPreset("slide_up", "Slide Up", "IN", 500L),
        AnimationPreset("whip_left", "Whip Left", "IN", 400L),
        AnimationPreset("zoom_drop", "Zoom Drop", "IN", 500L),
        AnimationPreset("spin_in", "Spin Reveal", "IN", 550L),
        AnimationPreset("glitch_in", "Glitch Snap", "IN", 450L),
        AnimationPreset("typewriter", "Typewriter", "IN", 800L),

        AnimationPreset("none", "None", "OUT", 0L),
        AnimationPreset("fade_out", "Fade Out", "OUT", 500L),
        AnimationPreset("slide_down", "Slide Down", "OUT", 450L),
        AnimationPreset("zoom_vanish", "Zoom Vanish", "OUT", 450L),
        AnimationPreset("glitch_out", "Glitch Dissolve", "OUT", 450L),

        AnimationPreset("none", "None", "LOOP", 0L),
        AnimationPreset("pulse", "Heartbeat Pulse", "LOOP", 1000L),
        AnimationPreset("float_wave", "Floating Wave", "LOOP", 1600L),
        AnimationPreset("neon_flicker", "Neon Flicker", "LOOP", 800L),
        AnimationPreset("pendulum", "Pendulum Tilt", "LOOP", 1400L),
        AnimationPreset("jitter", "Handheld Jitter", "LOOP", 600L)
    )

    val stickersCatalog: List<StickerDefinition> = listOf(
        StickerDefinition("stk_rec", "REC Indicator", "● REC", "Studio", 0xFFFF3366L),
        StickerDefinition("stk_4k", "4K UHD Badge", "4K UHD", "Studio", 0xFF00E5FFL),
        StickerDefinition("stk_fire", "Trending Fire", "🔥 HOT", "Social", 0xFFFF6E40L),
        StickerDefinition("stk_star", "Cinema Award", "★ SELECTION", "Cinema", 0xFFFFD54FL),
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
}
