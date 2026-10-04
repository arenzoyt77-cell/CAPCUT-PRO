package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.EffectFilterTransitionCatalog
import com.example.engine.KeyframeAndSpeedEngine
import com.example.engine.OmkarAutoVideoEngine
import com.example.model.AspectRatioMode
import com.example.model.AutomaticMotionMode
import com.example.model.BlendModeType
import com.example.model.CanvasBackgroundType
import com.example.model.CurveControlPoint
import com.example.model.KeyframeInterpolation
import com.example.model.KeyframeProperty
import com.example.model.MaskType
import com.example.ui.theme.AudioEmerald
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EffectPurple
import com.example.ui.theme.KeyframeCrimson
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.TextAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.EditorToolTab
import com.example.viewmodel.MediaPickerPurpose
import com.example.viewmodel.NovaCutViewModel

@Composable
fun EditorToolInspectorPanel(
    activeTab: EditorToolTab,
    viewModel: NovaCutViewModel
) {
    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val playheadMs by viewModel.playheadMs.collectAsStateWithLifecycle()
    val activeClip = remember(project, playheadMs, viewModel.selectedClipId.collectAsStateWithLifecycle().value) {
        viewModel.getSelectedOrActiveClip()
    }

    when (activeTab) {
        EditorToolTab.EDIT -> EditClipInspectorPanel(viewModel, activeClip)
        EditorToolTab.KEYFRAMES -> KeyframeEnginePanel(viewModel, activeClip)
        EditorToolTab.SPEED -> SpeedEnginePanel(viewModel, activeClip)
        EditorToolTab.FILTERS -> FilterEnginePanel(viewModel, activeClip)
        EditorToolTab.ADJUST -> ColorAdjustmentsPanel(viewModel, activeClip)
        EditorToolTab.EFFECTS -> VideoEffectsPanel(viewModel)
        EditorToolTab.TRANSITION -> TransitionsEnginePanel(viewModel, activeClip)
        EditorToolTab.TEXT -> TextEditorPanel(viewModel, isCaptionMode = false)
        EditorToolTab.CAPTIONS -> TextEditorPanel(viewModel, isCaptionMode = true)
        EditorToolTab.AUDIO -> AudioEditorPanel(viewModel)
        EditorToolTab.OVERLAY -> OverlayEditorPanel(viewModel, activeClip)
        EditorToolTab.ANIMATION -> AnimationEditorPanel(viewModel, activeClip)
        EditorToolTab.MASK -> MaskEditorPanel(viewModel, activeClip)
        EditorToolTab.CUTOUT -> CutoutEditorPanel(viewModel, activeClip)
        EditorToolTab.CANVAS -> CanvasAndAspectRatioPanel(viewModel)
        EditorToolTab.TRANSFORM -> TransformAndCropPanel(viewModel, activeClip)
        EditorToolTab.BLEND -> BlendModePanel(viewModel, activeClip)
        EditorToolTab.STICKERS -> StickersEditorPanel(viewModel)
    }
}

@Composable
private fun EditClipInspectorPanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val autoSummary by viewModel.autoEditSummary.collectAsStateWithLifecycle()
    val capCutResult by viewModel.capCutDraftExportResult.collectAsStateWithLifecycle()
    val effDur = KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip)
    val startTransform = OmkarAutoVideoEngine.TransformEngine.evaluateClipTransform(clip, 0L)
    val endTransform = OmkarAutoVideoEngine.TransformEngine.evaluateClipTransform(clip, effDur)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // OMKAR AUTOMATIC VIDEO MAKER — SPEECH SPLIT & KEYFRAME CONTROL DECK
        item {
            Surface(
                color = StudioCard,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyanAccent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .testTag("omkar_auto_maker_card")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "OMKAR AUTOMATIC VIDEO MAKER",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sacred Geometry: ${project.sourceGeometry.displayWidth}×${project.sourceGeometry.displayHeight} (${project.aspectRatio.label}) • Rot 0° • Uniform Scale",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        SmallActionButton(
                            label = "Analyze & Auto Split",
                            tint = CyanAccent
                        ) {
                            viewModel.runOmkarAutoVideoMaker()
                        }
                    }

                    // Motion Mode + Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutomaticMotionMode.entries.forEach { mode ->
                            val isSelected = project.automaticMotionMode == mode
                            Surface(
                                color = if (isSelected) CyanAccent.copy(alpha = 0.22f) else StudioElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSelected) CyanAccent else StudioBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setAutomaticMotionMode(mode) }
                                    .testTag("motion_mode_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) CyanAccent else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        SmallActionButton("Reset Auto Edit", tint = TextAmber) {
                            viewModel.resetAutomaticEdit()
                        }
                        SmallActionButton("Export to CapCut", tint = AudioEmerald) {
                            viewModel.exportToCapCutDraft(autoShare = false)
                        }
                    }

                    // Zoom Intensity Presets (100% Identity, 108%, 110%, 112%, 114%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "End Zoom:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        listOf(1.00f to "100% (Identity)", 1.08f to "108%", 1.10f to "110%", 1.12f to "112%", 1.14f to "114%").forEach { (factor, label) ->
                            val active = kotlin.math.abs(project.autoZoomTargetFactor - factor) < 0.008f
                            Surface(
                                color = if (active) VioletAccent.copy(alpha = 0.26f) else StudioElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .border(1.dp, if (active) VioletAccent else StudioBorder, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.setAutoZoomTargetFactor(factor) }
                                    .testTag("zoom_preset_${(factor * 100).toInt()}")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Active Clip Speech & Keyframe Telemetry
                    Text(
                        text = "Clip ${clip.title}: Start KF ${(startTransform.uniformScale * 100).toInt()}% → End KF ${(endTransform.uniformScale * 100).toInt()}% (${clip.zoomDirection.label})" +
                            if (clip.speechText.isNotBlank()) " • \"${clip.speechText}\"" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SmallActionButton("Cycle Motion (${clip.zoomDirection.label})") {
                            viewModel.cycleSelectedClipZoomDirection()
                        }
                        SmallActionButton("Merge / Remove Split") {
                            viewModel.removeSplitAndMergeSelectedClip()
                        }
                    }

                    if (autoSummary.isNotBlank()) {
                        Text(
                            text = autoSummary,
                            style = MaterialTheme.typography.labelSmall,
                            color = AudioEmerald
                        )
                    }

                    capCutResult?.let { res ->
                        if (res.success) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = res.message,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    modifier = Modifier.weight(1f)
                                )
                                SmallActionButton("Share Draft", tint = CyanAccent) {
                                    viewModel.shareCapCutDraftOrMp4(res.draftZipFilePath)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clip: ${clip.title} (${NovaCutViewModel.formatShortDuration(effDur)})",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallActionButton("Split") { viewModel.splitClipAtPlayhead() }
                    SmallActionButton("Duplicate") { viewModel.duplicateSelectedClip() }
                    SmallActionButton("Delete", tint = KeyframeCrimson) { viewModel.deleteSelectedItem() }
                }
            }
        }

        item {
            CompactSliderRow(
                label = "Volume (${(clip.volume * 100).toInt()}%)",
                value = clip.volume,
                range = 0f..2f,
                onValueChange = { viewModel.updateClipAudioSettings(it, clip.isMuted, clip.fadeInMs, clip.fadeOutMs) }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    CompactSliderRow(
                        label = "Fade In (${clip.fadeInMs}ms)",
                        value = clip.fadeInMs.toFloat(),
                        range = 0f..3000f,
                        onValueChange = { viewModel.updateClipAudioSettings(clip.volume, clip.isMuted, it.toLong(), clip.fadeOutMs) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    CompactSliderRow(
                        label = "Fade Out (${clip.fadeOutMs}ms)",
                        value = clip.fadeOutMs.toFloat(),
                        range = 0f..3000f,
                        onValueChange = { viewModel.updateClipAudioSettings(clip.volume, clip.isMuted, clip.fadeInMs, it.toLong()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyframeEnginePanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    val activeProp by viewModel.activeKeyframeProperty.collectAsStateWithLifecycle()
    val activeInterp by viewModel.activeKeyframeInterpolation.collectAsStateWithLifecycle()
    val localMs = viewModel.getClipLocalMs(clip)
    val evaluatedVal = KeyframeAndSpeedEngine.evaluateProperty(
        keyframes = clip.keyframes,
        property = activeProp,
        clipLocalMs = localMs,
        fallbackValue = when (activeProp) {
            KeyframeProperty.POS_X -> clip.transformX
            KeyframeProperty.POS_Y -> clip.transformY
            KeyframeProperty.SCALE -> clip.scale
            KeyframeProperty.ROTATION -> clip.rotation
            KeyframeProperty.OPACITY -> clip.opacity
            KeyframeProperty.CROP_LEFT -> clip.cropLeft
            KeyframeProperty.CROP_TOP -> clip.cropTop
            KeyframeProperty.CROP_RIGHT -> clip.cropRight
            KeyframeProperty.CROP_BOTTOM -> clip.cropBottom
            KeyframeProperty.VOLUME -> clip.volume
            KeyframeProperty.FILTER_INTENSITY -> clip.filterIntensity
            KeyframeProperty.EFFECT_INTENSITY -> 0.8f
        }
    )
    val hasKfHere = KeyframeAndSpeedEngine.hasKeyframeNear(clip.keyframes, localMs, activeProp)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keyframe Engine • ${clip.keyframes.size} Active ◆",
                    style = MaterialTheme.typography.titleSmall,
                    color = CyanAccent
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.jumpToKeyframe(false) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous keyframe", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }
                    SmallActionButton(
                        label = if (hasKfHere) "◆ Remove KF" else "◇ Add Keyframe",
                        tint = if (hasKfHere) KeyframeCrimson else CyanAccent
                    ) {
                        viewModel.toggleKeyframeAtPlayhead(activeProp)
                    }
                    IconButton(onClick = { viewModel.jumpToKeyframe(true) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next keyframe", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Property Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(KeyframeProperty.entries) { prop ->
                    val selected = activeProp == prop
                    val count = clip.keyframes.count { it.property == prop }
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setActiveKeyframeProperty(prop) },
                        label = {
                            Text(
                                text = if (count > 0) "${prop.label} ($count◆)" else prop.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = StudioBg,
                            containerColor = StudioCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // Value Slider (automatically writes/interpolates keyframe at current playhead)
        item {
            CompactSliderRow(
                label = "${activeProp.label}: ${String.format(java.util.Locale.US, "%.2f", evaluatedVal)}",
                value = evaluatedVal,
                range = activeProp.minValue..activeProp.maxValue,
                onValueChange = {
                    viewModel.updateTransformOrKeyframeProperty(activeProp, it, forceKeyframe = true)
                }
            )
        }

        // Interpolation Curve Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(KeyframeInterpolation.entries) { interp ->
                    val isSel = activeInterp == interp
                    FilterChip(
                        selected = isSel,
                        onClick = { viewModel.setActiveKeyframeInterpolation(interp) },
                        label = { Text(interp.label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletAccent,
                            selectedLabelColor = Color.White,
                            containerColor = StudioCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedEnginePanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    var isCurveMode by remember { mutableStateOf(clip.speedPoints.isNotEmpty()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isCurveMode,
                        onClick = { isCurveMode = false },
                        label = { Text("Normal Speed") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = StudioBg
                        )
                    )
                    FilterChip(
                        selected = isCurveMode,
                        onClick = { isCurveMode = true },
                        label = { Text("Curve Velocity") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = StudioBg
                        )
                    )
                }
                Text(
                    text = "Out: ${NovaCutViewModel.formatShortDuration(KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent
                )
            }
        }

        if (!isCurveMode) {
            item {
                CompactSliderRow(
                    label = "Constant Speed: ${String.format(java.util.Locale.US, "%.2fx", clip.speedMultiplier)}",
                    value = clip.speedMultiplier,
                    range = 0.1f..8.0f,
                    onValueChange = { viewModel.setClipConstantSpeed(it) }
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 4.0f, 8.0f)) { spd ->
                        SmallActionButton("${spd}x") { viewModel.setClipConstantSpeed(spd) }
                    }
                }
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(KeyframeAndSpeedEngine.speedPresets) { preset ->
                        val isSel = clip.speedPresetId == preset.id
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.applySpeedCurvePreset(preset.id) },
                            label = { Text(preset.name, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletAccent,
                                selectedLabelColor = Color.White,
                                containerColor = StudioCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Visual Velocity Curve Graph + Point Sliders
            item {
                val points = clip.speedPoints.ifEmpty { KeyframeAndSpeedEngine.speedPresets.first().points }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioCard)
                            .padding(8.dp)
                    ) {
                        val path = Path()
                        points.forEachIndexed { idx, pt ->
                            val x = pt.positionFraction * size.width
                            val normY = 1f - ((pt.speedMultiplier - 0.1f) / 5.5f).coerceIn(0f, 1f)
                            val y = normY * size.height
                            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            drawCircle(color = CyanAccent, radius = 5f, center = Offset(x, y))
                        }
                        drawPath(path = path, color = CyanAccent, style = Stroke(width = 3f))
                    }

                    Column(modifier = Modifier.weight(1.8f)) {
                        points.forEachIndexed { index, pt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("P${index + 1}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(22.dp))
                                Slider(
                                    value = pt.speedMultiplier,
                                    onValueChange = { viewModel.updateSpeedCurvePoint(index, it) },
                                    valueRange = 0.2f..5.0f,
                                    modifier = Modifier.weight(1f).height(18.dp)
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1fx", pt.speedMultiplier),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterEnginePanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    var selectedCat by remember { mutableStateOf("Featured") }
    val filteredList = remember(selectedCat) {
        val list = EffectFilterTransitionCatalog.filters.filter { it.category == selectedCat }
        list.ifEmpty { EffectFilterTransitionCatalog.filters }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CompactSliderRow(
                    label = "Intensity (${(clip.filterIntensity * 100).toInt()}%)",
                    value = clip.filterIntensity,
                    range = 0f..1f,
                    onValueChange = { viewModel.applyFilter(clip.filterId, it, false) }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            SmallActionButton("Reset") { viewModel.applyFilter("none", 0f, false) }
            Spacer(modifier = Modifier.width(6.dp))
            SmallActionButton("Apply All") { viewModel.applyFilter(clip.filterId, clip.filterIntensity, true) }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(EffectFilterTransitionCatalog.filterCategories) { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat },
                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = StudioBg,
                        containerColor = StudioCard,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredList, key = { it.id }) { filter ->
                val isSel = clip.filterId == filter.id
                Surface(
                    color = StudioCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .width(96.dp)
                        .height(54.dp)
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) CyanAccent else StudioBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.applyFilter(filter.id, clip.filterIntensity.coerceAtLeast(0.75f)) }
                        .testTag("filter_item_${filter.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(filter.accentHex))
                        )
                        Text(
                            text = filter.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) CyanAccent else TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorAdjustmentsPanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    val adj = clip.adjustments
    var subMode by remember { mutableStateOf("Basic") } // Basic, HSL, Curves
    var selectedHslIdx by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Basic", "HSL", "RGB & Curves").forEach { mode ->
                        FilterChip(
                            selected = subMode == mode,
                            onClick = { subMode = mode },
                            label = { Text(mode, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = StudioBg
                            )
                        )
                    }
                }
                SmallActionButton("Reset Grade") { viewModel.resetClipAdjustments() }
            }
        }

        when (subMode) {
            "Basic" -> {
                item {
                    CompactSliderRow("Brightness", adj.brightness, -0.6f..0.6f) {
                        viewModel.updateClipAdjustments(adj.copy(brightness = it))
                    }
                }
                item {
                    CompactSliderRow("Contrast", adj.contrast, -0.6f..0.8f) {
                        viewModel.updateClipAdjustments(adj.copy(contrast = it))
                    }
                }
                item {
                    CompactSliderRow("Saturation", adj.saturation, -1.0f..1.0f) {
                        viewModel.updateClipAdjustments(adj.copy(saturation = it))
                    }
                }
                item {
                    CompactSliderRow("Exposure", adj.exposure, -0.8f..0.8f) {
                        viewModel.updateClipAdjustments(adj.copy(exposure = it))
                    }
                }
                item {
                    CompactSliderRow("Highlights", adj.highlights, -0.8f..0.8f) {
                        viewModel.updateClipAdjustments(adj.copy(highlights = it))
                    }
                }
                item {
                    CompactSliderRow("Shadows", adj.shadows, -0.8f..0.8f) {
                        viewModel.updateClipAdjustments(adj.copy(shadows = it))
                    }
                }
                item {
                    CompactSliderRow("Temperature (Warm/Cool)", adj.temperature, -1.0f..1.0f) {
                        viewModel.updateClipAdjustments(adj.copy(temperature = it))
                    }
                }
                item {
                    CompactSliderRow("Tint (Green/Magenta)", adj.tint, -1.0f..1.0f) {
                        viewModel.updateClipAdjustments(adj.copy(tint = it))
                    }
                }
                item {
                    CompactSliderRow("Fade", adj.fade, 0f..0.8f) {
                        viewModel.updateClipAdjustments(adj.copy(fade = it))
                    }
                }
                item {
                    CompactSliderRow("Sharpen", adj.sharpen, 0f..1f) {
                        viewModel.updateClipAdjustments(adj.copy(sharpen = it))
                    }
                }
                item {
                    CompactSliderRow("Vignette", adj.vignette, -1f..1f) {
                        viewModel.updateClipAdjustments(adj.copy(vignette = it))
                    }
                }
                item {
                    CompactSliderRow("35mm Film Grain", adj.grain, 0f..1f) {
                        viewModel.updateClipAdjustments(adj.copy(grain = it))
                    }
                }
            }
            "HSL" -> {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(adj.hslBands.indices.toList()) { idx ->
                            val band = adj.hslBands[idx]
                            FilterChip(
                                selected = selectedHslIdx == idx,
                                onClick = { selectedHslIdx = idx },
                                label = { Text(band.bandName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
                val activeBand = adj.hslBands.getOrElse(selectedHslIdx) { adj.hslBands.first() }
                item {
                    CompactSliderRow("${activeBand.bandName} Hue", activeBand.hueShift, -180f..180f) { v ->
                        val updated = adj.hslBands.toMutableList().apply {
                            this[selectedHslIdx] = activeBand.copy(hueShift = v)
                        }
                        viewModel.updateClipAdjustments(adj.copy(hslBands = updated))
                    }
                }
                item {
                    CompactSliderRow("${activeBand.bandName} Saturation", activeBand.saturationShift, -1f..1f) { v ->
                        val updated = adj.hslBands.toMutableList().apply {
                            this[selectedHslIdx] = activeBand.copy(saturationShift = v)
                        }
                        viewModel.updateClipAdjustments(adj.copy(hslBands = updated))
                    }
                }
                item {
                    CompactSliderRow("${activeBand.bandName} Luminance", activeBand.luminanceShift, -1f..1f) { v ->
                        val updated = adj.hslBands.toMutableList().apply {
                            this[selectedHslIdx] = activeBand.copy(luminanceShift = v)
                        }
                        viewModel.updateClipAdjustments(adj.copy(hslBands = updated))
                    }
                }
            }
            else -> {
                item {
                    CompactSliderRow("Red Channel Gain", adj.redGain, 0.5f..1.5f) {
                        viewModel.updateClipAdjustments(adj.copy(redGain = it))
                    }
                }
                item {
                    CompactSliderRow("Green Channel Gain", adj.greenGain, 0.5f..1.5f) {
                        viewModel.updateClipAdjustments(adj.copy(greenGain = it))
                    }
                }
                item {
                    CompactSliderRow("Blue Channel Gain", adj.blueGain, 0.5f..1.5f) {
                        viewModel.updateClipAdjustments(adj.copy(blueGain = it))
                    }
                }
                item {
                    val midOutput = adj.lumaCurvePoints.getOrNull(2)?.output ?: 0.5f
                    CompactSliderRow("Master Luma Curve Midpoint", midOutput, 0.15f..0.85f) { newMid ->
                        val newPts = listOf(
                            CurveControlPoint(0f, 0f),
                            CurveControlPoint(0.25f, (newMid * 0.5f).coerceIn(0f, 1f)),
                            CurveControlPoint(0.5f, newMid),
                            CurveControlPoint(0.75f, (0.5f + newMid * 0.5f).coerceIn(0f, 1f)),
                            CurveControlPoint(1f, 1f)
                        )
                        viewModel.updateClipAdjustments(adj.copy(lumaCurvePoints = newPts))
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoEffectsPanel(viewModel: NovaCutViewModel) {
    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val selectedEffectId by viewModel.selectedEffectId.collectAsStateWithLifecycle()
    val activeEffect = project.effectItems.find { it.id == selectedEffectId } ?: project.effectItems.firstOrNull()
    var selectedCat by remember { mutableStateOf("Trending") }
    val effectsInCat = remember(selectedCat) {
        EffectFilterTransitionCatalog.effects.filter { it.category == selectedCat }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (activeEffect != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active FX: ${activeEffect.effectName}", style = MaterialTheme.typography.labelLarge, color = EffectPurple)
                    SmallActionButton("Remove FX", tint = KeyframeCrimson) {
                        viewModel.selectEffectItem(activeEffect.id)
                        viewModel.deleteSelectedItem()
                    }
                }
                CompactSliderRow(
                    label = "FX Intensity (${(activeEffect.intensity * 100).toInt()}%) • Speed (${(activeEffect.speed * 100).toInt()}%)",
                    value = activeEffect.intensity,
                    range = 0f..1f,
                    onValueChange = {
                        viewModel.updateSelectedEffectItem(it, activeEffect.speed, activeEffect.secondaryParam, activeEffect.durationMs)
                    }
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(EffectFilterTransitionCatalog.effectCategories) { cat ->
                    FilterChip(
                        selected = selectedCat == cat,
                        onClick = { selectedCat = cat },
                        label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EffectPurple,
                            selectedLabelColor = StudioBg
                        )
                    )
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(effectsInCat, key = { it.id }) { def ->
                    Surface(
                        color = StudioCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(114.dp)
                            .height(58.dp)
                            .border(1.dp, Color(def.accentHex).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { viewModel.addVideoEffectToTimeline(def.id) }
                            .testTag("effect_card_${def.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(def.name, style = MaterialTheme.typography.labelSmall, color = TextPrimary, maxLines = 1)
                            Text("+ Apply FX", style = MaterialTheme.typography.labelSmall, color = Color(def.accentHex))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitionsEnginePanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    var selectedCat by remember { mutableStateOf("Overlay") }
    var durationMs by remember(clip.id) { mutableStateOf(clip.transitionAfter.durationMs.toFloat()) }
    val filtered = remember(selectedCat) {
        val list = EffectFilterTransitionCatalog.transitions.filter {
            it.category == selectedCat
        }
        list.ifEmpty { EffectFilterTransitionCatalog.transitions }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CompactSliderRow(
                    label = "Duration: ${durationMs.toInt()}ms",
                    value = durationMs,
                    range = 200f..1800f,
                    onValueChange = {
                        durationMs = it
                        viewModel.applyTransition(clip.transitionAfter.transitionId, it.toLong(), false)
                    }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            SmallActionButton("Apply to All") {
                viewModel.applyTransition(clip.transitionAfter.transitionId, durationMs.toLong(), true)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(EffectFilterTransitionCatalog.transitionCategories) { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat },
                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = StudioBg
                    )
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.id }) { tr ->
                val isSel = clip.transitionAfter.transitionId == tr.id
                Surface(
                    color = StudioCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .width(104.dp)
                        .height(52.dp)
                        .border(if (isSel) 2.dp else 1.dp, if (isSel) CyanAccent else StudioBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.applyTransition(tr.id, durationMs.toLong(), false) }
                        .testTag("transition_item_${tr.id}")
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(tr.name, style = MaterialTheme.typography.labelSmall, color = if (isSel) CyanAccent else TextPrimary, maxLines = 1)
                        Text(tr.category, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TextEditorPanel(
    viewModel: NovaCutViewModel,
    isCaptionMode: Boolean
) {
    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val selectedTextId by viewModel.selectedTextId.collectAsStateWithLifecycle()
    val activeText = project.textClips.find { it.id == selectedTextId } ?: project.textClips.firstOrNull()
    var inputContent by remember(activeText?.id) { mutableStateOf(activeText?.text ?: "STUDIO TITLE") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallActionButton(if (isCaptionMode) "+ Add Caption" else "+ Add Text", tint = TextAmber) {
                        viewModel.addTextClipToTimeline(
                            text = if (isCaptionMode) "NEW SUBTITLE LINE" else "STUDIO HEADLINE",
                            isCaption = isCaptionMode
                        )
                    }
                    if (isCaptionMode) {
                        SmallActionButton("Auto Captions (STT)", tint = CyanAccent) {
                            viewModel.generateAutoCaptionsFromTimeline()
                        }
                    }
                }
                if (activeText != null) {
                    SmallActionButton("Delete Text", tint = KeyframeCrimson) {
                        viewModel.selectTextClip(activeText.id)
                        viewModel.deleteSelectedItem()
                    }
                }
            }
        }

        if (activeText != null) {
            item {
                OutlinedTextField(
                    value = inputContent,
                    onValueChange = { newText ->
                        inputContent = newText
                        viewModel.updateSelectedTextClip { it.copy(text = newText) }
                    },
                    singleLine = true,
                    label = { Text("Text Content") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Font Families Selector
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(EffectFilterTransitionCatalog.fontFamiliesCatalog) { (fontId, fontLabel) ->
                        FilterChip(
                            selected = activeText.fontFamilyId == fontId,
                            onClick = { viewModel.updateSelectedTextClip { it.copy(fontFamilyId = fontId) } },
                            label = { Text(fontLabel, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Style Presets
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(EffectFilterTransitionCatalog.textStylePresets, key = { it.id }) { preset ->
                        SmallActionButton(preset.name) {
                            viewModel.updateSelectedTextClip {
                                it.copy(
                                    fontFamilyId = preset.fontFamilyId,
                                    textColorHex = preset.textColorHex,
                                    strokeColorHex = preset.strokeColorHex,
                                    strokeWidth = preset.strokeWidth,
                                    backgroundColorHex = preset.bgHex,
                                    backgroundAlpha = preset.bgAlpha,
                                    shadowColorHex = preset.shadowHex,
                                    stylePresetId = preset.id
                                )
                            }
                        }
                    }
                }
            }

            item {
                CompactSliderRow("Font Size (${activeText.fontSizeSp.toInt()}sp)", activeText.fontSizeSp, 12f..64f) { sz ->
                    viewModel.updateSelectedTextClip { it.copy(fontSizeSp = sz) }
                }
            }
        }
    }
}

@Composable
private fun AudioEditorPanel(viewModel: NovaCutViewModel) {
    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val selectedAudioId by viewModel.selectedAudioId.collectAsStateWithLifecycle()
    val activeAudio = project.audioClips.find { it.id == selectedAudioId } ?: project.audioClips.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallActionButton("● Record Voiceover", tint = KeyframeCrimson) {
                        viewModel.recordVoiceoverToTimeline(4000L)
                    }
                    SmallActionButton("Extract Clip Audio", tint = AudioEmerald) {
                        viewModel.extractAudioFromSelectedClip()
                    }
                }
                if (activeAudio != null) {
                    SmallActionButton("Delete Audio", tint = KeyframeCrimson) {
                        viewModel.selectAudioClip(activeAudio.id)
                        viewModel.deleteSelectedItem()
                    }
                }
            }
        }

        if (activeAudio != null) {
            item {
                CompactSliderRow(
                    label = "${activeAudio.title} Vol (${(activeAudio.volume * 100).toInt()}%)",
                    value = activeAudio.volume,
                    range = 0f..2f,
                    onValueChange = {
                        viewModel.updateSelectedAudioClip(it, activeAudio.isMuted, activeAudio.fadeInMs, activeAudio.fadeOutMs, activeAudio.speed)
                    }
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(EffectFilterTransitionCatalog.audioPresets, key = { it.id }) { preset ->
                    Surface(
                        color = StudioCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(142.dp)
                            .height(54.dp)
                            .border(1.dp, AudioEmerald.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { viewModel.addAudioPresetToTimeline(preset.id) }
                            .testTag("audio_preset_${preset.id}")
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(preset.title, style = MaterialTheme.typography.labelSmall, color = TextPrimary, maxLines = 1)
                            Text("${preset.category.label} • ${preset.bpm} BPM", style = MaterialTheme.typography.bodySmall, color = AudioEmerald)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayEditorPanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.openMediaPicker(MediaPickerPurpose.ADD_OVERLAY_CLIP) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = StudioBg)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add PIP Overlay")
            }
            SmallActionButton("Duplicate Layer") { viewModel.duplicateSelectedClip() }
        }
        if (clip != null) {
            CompactSliderRow("Layer Opacity (${(clip.opacity * 100).toInt()}%)", clip.opacity, 0f..1f) {
                viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.OPACITY, it)
            }
            CompactSliderRow("Layer Scale (${String.format(java.util.Locale.US, "%.2fx", clip.scale)})", clip.scale, 0.2f..3f) {
                viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.SCALE, it)
            }
        }
    }
}

@Composable
private fun AnimationEditorPanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    var animTab by remember { mutableStateOf("IN") }
    val presets = remember(animTab) {
        EffectFilterTransitionCatalog.animationsCatalog.filter { it.type == animTab }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("IN" to "In Animation", "OUT" to "Out Animation", "LOOP" to "Combo / Loop").forEach { (id, label) ->
                FilterChip(
                    selected = animTab == id,
                    onClick = { animTab = id },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets, key = { "${it.type}_${it.id}" }) { preset ->
                val isSel = when (animTab) {
                    "IN" -> clip.animation.inAnimId == preset.id
                    "OUT" -> clip.animation.outAnimId == preset.id
                    else -> clip.animation.loopAnimId == preset.id
                }
                SmallActionButton(
                    label = preset.name,
                    tint = if (isSel) CyanAccent else TextPrimary
                ) {
                    val current = clip.animation
                    val updated = when (animTab) {
                        "IN" -> current.copy(inAnimId = preset.id, inDurationMs = preset.defaultDurationMs.coerceAtLeast(450L))
                        "OUT" -> current.copy(outAnimId = preset.id, outDurationMs = preset.defaultDurationMs.coerceAtLeast(450L))
                        else -> current.copy(loopAnimId = preset.id, loopCycleMs = preset.defaultDurationMs.coerceAtLeast(800L))
                    }
                    viewModel.updateClipAnimation(updated)
                }
            }
        }
    }
}

@Composable
private fun MaskEditorPanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    val mask = clip.mask
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(MaskType.entries) { type ->
                FilterChip(
                    selected = mask.type == type,
                    onClick = { viewModel.updateClipMask(mask.copy(type = type)) },
                    label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        if (mask.type != MaskType.NONE) {
            CompactSliderRow("Mask Size (${(mask.radiusOrWidth * 100).toInt()}%)", mask.radiusOrWidth, 0.1f..0.8f) {
                viewModel.updateClipMask(mask.copy(radiusOrWidth = it, heightRatio = it))
            }
            CompactSliderRow("Feather Softness (${(mask.feather * 100).toInt()}%)", mask.feather, 0f..0.5f) {
                viewModel.updateClipMask(mask.copy(feather = it))
            }
        }
    }
}

@Composable
private fun CutoutEditorPanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    val cut = clip.cutout
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = cut.autoCutoutEnabled,
                    onClick = { viewModel.updateClipCutout(cut.copy(autoCutoutEnabled = !cut.autoCutoutEnabled)) },
                    label = { Text("Auto Subject Cutout") }
                )
                FilterChip(
                    selected = cut.chromaKeyEnabled,
                    onClick = { viewModel.updateClipCutout(cut.copy(chromaKeyEnabled = !cut.chromaKeyEnabled)) },
                    label = { Text("Chroma Key / Green Screen") }
                )
                FilterChip(
                    selected = cut.neonStrokeEnabled,
                    onClick = { viewModel.updateClipCutout(cut.copy(neonStrokeEnabled = !cut.neonStrokeEnabled)) },
                    label = { Text("Neon Rim") }
                )
            }
        }
        item {
            CompactSliderRow("Chroma Tolerance (${(cut.tolerance * 100).toInt()}%)", cut.tolerance, 0.05f..0.95f) {
                viewModel.updateClipCutout(cut.copy(tolerance = it))
            }
        }
        item {
            CompactSliderRow("Edge Feather & Spill Suppression", cut.feather, 0f..0.6f) {
                viewModel.updateClipCutout(cut.copy(feather = it, spillSuppression = it))
            }
        }
    }
}

@Composable
private fun CanvasAndAspectRatioPanel(viewModel: NovaCutViewModel) {
    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val cfg = project.canvasConfig

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(AspectRatioMode.entries) { mode ->
                FilterChip(
                    selected = project.aspectRatio == mode,
                    onClick = { viewModel.setProjectAspectRatio(mode) },
                    label = { Text(mode.label) },
                    modifier = Modifier.testTag("aspect_ratio_${mode.name.lowercase()}")
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanvasBackgroundType.entries.forEach { bgType ->
                FilterChip(
                    selected = cfg.type == bgType,
                    onClick = { viewModel.updateCanvasConfig(cfg.copy(type = bgType)) },
                    label = { Text("Canvas: ${bgType.label}") }
                )
            }
        }
    }
}

@Composable
private fun TransformAndCropPanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SmallActionButton("Rotate 90°") { viewModel.rotateSelectedClip90() }
                SmallActionButton("Mirror H") { viewModel.toggleMirrorHorizontal() }
                SmallActionButton("Flip V") { viewModel.toggleMirrorVertical() }
                SmallActionButton("Reset Transform") {
                    viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.POS_X, 0f)
                    viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.POS_Y, 0f)
                    viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.SCALE, 1f)
                    viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.ROTATION, 0f)
                }
            }
        }
        item {
            CompactSliderRow("Scale (${String.format(java.util.Locale.US, "%.2fx", clip.scale)})", clip.scale, 0.2f..4f) {
                viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.SCALE, it)
            }
        }
        item {
            CompactSliderRow("Rotation (${clip.rotation.toInt()}°)", clip.rotation, -180f..180f) {
                viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.ROTATION, it)
            }
        }
        item {
            CompactSliderRow("Crop Trim (${(clip.cropLeft * 100).toInt()}%)", clip.cropLeft, 0f..0.4f) {
                viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.CROP_LEFT, it)
                viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.CROP_RIGHT, it)
            }
        }
    }
}

@Composable
private fun BlendModePanel(
    viewModel: NovaCutViewModel,
    clip: com.example.model.TimelineClip?
) {
    if (clip == null) return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(BlendModeType.entries) { mode ->
                FilterChip(
                    selected = clip.blendMode == mode,
                    onClick = { viewModel.updateClipBlendMode(mode) },
                    label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
        CompactSliderRow("Blend Opacity (${(clip.opacity * 100).toInt()}%)", clip.opacity, 0f..1f) {
            viewModel.updateTransformOrKeyframeProperty(KeyframeProperty.OPACITY, it)
        }
    }
}

@Composable
private fun StickersEditorPanel(viewModel: NovaCutViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Tap to add animated studio stickers to timeline:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EffectFilterTransitionCatalog.stickersCatalog, key = { it.id }) { stk ->
                Surface(
                    color = StudioCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .border(1.dp, Color(stk.accentHex), RoundedCornerShape(10.dp))
                        .clickable { viewModel.addStickerToTimeline(stk.id) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(stk.symbol, style = MaterialTheme.typography.titleSmall, color = Color(stk.accentHex))
                }
            }
        }
    }
}

@Composable
private fun CompactSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.width(142.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = CyanAccent,
                activeTrackColor = CyanAccent,
                inactiveTrackColor = StudioBorder
            ),
            modifier = Modifier
                .weight(1f)
                .height(22.dp)
        )
    }
}

@Composable
private fun SmallActionButton(
    label: String,
    tint: Color = CyanAccent,
    onClick: () -> Unit
) {
    Surface(
        color = StudioElevated,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .border(1.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
