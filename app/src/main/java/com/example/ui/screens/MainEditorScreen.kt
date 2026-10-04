package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.KeyframeAndSpeedEngine
import com.example.engine.VideoRenderEngine
import com.example.model.VideoProject
import com.example.ui.theme.AudioEmerald
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EffectPurple
import com.example.ui.theme.KeyframeCrimson
import com.example.ui.theme.OverlayTeal
import com.example.ui.theme.StickerCoral
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.EditorToolTab
import com.example.viewmodel.MediaPickerPurpose
import com.example.viewmodel.NovaCutViewModel

@Composable
fun MainEditorScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }

    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val playheadMs by viewModel.playheadMs.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isFullscreen by viewModel.isFullscreenPreview.collectAsStateWithLifecycle()
    val showBeforeAfter by viewModel.showBeforeAfterCompare.collectAsStateWithLifecycle()
    val timelineZoom by viewModel.timelineZoom.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeEditorTab.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val selectedClipId by viewModel.selectedClipId.collectAsStateWithLifecycle()
    val selectedAudioId by viewModel.selectedAudioId.collectAsStateWithLifecycle()
    val selectedTextId by viewModel.selectedTextId.collectAsStateWithLifecycle()
    val selectedEffectId by viewModel.selectedEffectId.collectAsStateWithLifecycle()
    val selectedStickerId by viewModel.selectedStickerId.collectAsStateWithLifecycle()
    val showSearch by viewModel.showGlobalSearch.collectAsStateWithLifecycle()
    val showFpsOverlay by viewModel.showFpsOverlay.collectAsStateWithLifecycle()
    val proxyEnabled by viewModel.proxyPreviewEnabled.collectAsStateWithLifecycle()

    val totalDurationMs = remember(project) {
        VideoRenderEngine.computeProjectTotalDurationMs(project)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // 1. TOP BAR (Close, Undo/Redo, Search, Resolution selector, Export button)
        if (!isFullscreen) {
            EditorTopHeaderBar(
                project = project,
                canUndo = canUndo,
                canRedo = canRedo,
                onBack = { viewModel.navigateBack() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onSearch = { viewModel.setGlobalSearchVisible(true) },
                onSelectResolutionAndFps = { res, fps ->
                    viewModel.updateExportConfig(
                        viewModel.exportConfig.value.copy(resolution = res, fps = fps)
                    )
                },
                onExport = { viewModel.openExportScreen() }
            )
        }

        // 2. CENTER VIDEO PREVIEW VIEWPORT + TRANSPORT BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isFullscreen) 1f else 0.36f)
                .background(Color(0xFF05070B)),
            contentAlignment = Alignment.Center
        ) {
            val aspect = project.aspectRatio.aspectValue
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.92f)
                    .aspectRatio(aspect, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            val normX = pan.x / size.width.coerceAtLeast(1)
                            val normY = pan.y / size.height.coerceAtLeast(1)
                            viewModel.interactiveCanvasDragAndZoom(normX, normY, zoom, rotation)
                        }
                    }
                    .testTag("editor_preview_canvas")
            ) {
                val decodeDim = if (proxyEnabled) 540 else 960
                Canvas(modifier = Modifier.fillMaxSize()) {
                    VideoRenderEngine.renderCompositeFrame(
                        canvas = drawContext.canvas.nativeCanvas,
                        width = size.width,
                        height = size.height,
                        project = project,
                        playheadMs = playheadMs,
                        bitmapLookup = { clip -> viewModel.mediaEngine.getClipBitmap(clip, decodeDim) },
                        showOriginalBeforeGrade = showBeforeAfter
                    )
                }

                if (showBeforeAfter) {
                    Surface(
                        color = StudioBg.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "ORIGINAL (BEFORE GRADE)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (showFpsOverlay) {
                    val activeClip = viewModel.getSelectedOrActiveClip()
                    Surface(
                        color = StudioBg.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "60 FPS • ${project.exportResolution} • ${activeClip?.keyframes?.size ?: 0} KF",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (isFullscreen) {
                IconButton(
                    onClick = { viewModel.setFullscreenPreview(false) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(StudioBg.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = "Exit fullscreen", tint = Color.White)
                }
            }
        }

        // 3. PLAYBACK TRANSPORT & KEYFRAME DIAMOND BAR
        EditorTransportBar(
            playheadMs = playheadMs,
            totalDurationMs = totalDurationMs,
            isPlaying = isPlaying,
            isFullscreen = isFullscreen,
            showBeforeAfter = showBeforeAfter,
            hasKeyframeAtPlayhead = run {
                val clip = viewModel.getSelectedOrActiveClip()
                if (clip != null) {
                    val localMs = viewModel.getClipLocalMs(clip)
                    KeyframeAndSpeedEngine.hasKeyframeNear(clip.keyframes, localMs)
                } else false
            },
            onPlayPause = { viewModel.togglePlayPause() },
            onStepBackward = { viewModel.stepFrame(false) },
            onStepForward = { viewModel.stepFrame(true) },
            onAddOrRemoveKeyframe = { viewModel.toggleKeyframeAtPlayhead() },
            onToggleBeforeAfter = { viewModel.setBeforeAfterCompare(!showBeforeAfter) },
            onToggleFullscreen = { viewModel.setFullscreenPreview(!isFullscreen) }
        )

        if (!isFullscreen) {
            // 4. PRECISION MULTI-TRACK TIMELINE
            MultiTrackTimelineSection(
                project = project,
                playheadMs = playheadMs,
                totalDurationMs = totalDurationMs,
                zoom = timelineZoom,
                selectedClipId = selectedClipId,
                selectedAudioId = selectedAudioId,
                selectedTextId = selectedTextId,
                selectedEffectId = selectedEffectId,
                selectedStickerId = selectedStickerId,
                onSeek = { viewModel.seekToMs(it) },
                onZoomChange = { viewModel.setTimelineZoom(it) },
                onSelectClip = { viewModel.selectClip(it) },
                onSelectAudio = { viewModel.selectAudioClip(it) },
                onSelectText = { viewModel.selectTextClip(it) },
                onSelectEffect = { viewModel.selectEffectItem(it) },
                onSelectSticker = { viewModel.selectStickerClip(it) },
                onToggleMainMute = { viewModel.toggleMainTrackMute() },
                onAddClipClick = { viewModel.openMediaPicker(MediaPickerPurpose.ADD_PRIMARY_CLIP) },
                onTransitionBadgeClick = { clipId ->
                    viewModel.selectClip(clipId)
                    viewModel.setActiveEditorTab(EditorToolTab.TRANSITION)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.32f)
            )

            // 5. SELECTED CLIP QUICK ACTION STRIP
            SelectedClipQuickActionStrip(viewModel = viewModel)

            // 6. ACTIVE TOOL PARAMETER PANEL (Edit, Keyframes, Speed, Filters, Adjust, Effects, Text, Audio, etc.)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.24f)
                    .background(StudioSurface)
            ) {
                EditorToolInspectorPanel(
                    activeTab = activeTab,
                    viewModel = viewModel
                )
            }

            // 7. BOTTOM SCROLLABLE MASTER TOOLBAR
            EditorBottomMasterToolbar(
                activeTab = activeTab,
                onSelectTab = { viewModel.setActiveEditorTab(it) }
            )
        }
    }

    if (showSearch) {
        GlobalSearchDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setGlobalSearchVisible(false) }
        )
    }
}

@Composable
private fun EditorTopHeaderBar(
    project: VideoProject,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSearch: () -> Unit,
    onSelectResolutionAndFps: (String, Int) -> Unit,
    onExport: () -> Unit
) {
    var showResMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("editor_close_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close editor", tint = TextPrimary)
            }

            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("editor_undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) TextPrimary else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("editor_redo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) TextPrimary else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onSearch,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("editor_search_button")
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search effects & tools", tint = TextPrimary, modifier = Modifier.size(20.dp))
            }

            // Resolution & FPS Selector Pill
            Box {
                Surface(
                    color = StudioElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable { showResMenu = true }
                        .testTag("editor_resolution_selector")
                ) {
                    Text(
                        text = "${project.exportResolution} • ${project.exportFps}fps ▾",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                DropdownMenu(
                    expanded = showResMenu,
                    onDismissRequest = { showResMenu = false },
                    modifier = Modifier.background(StudioElevated)
                ) {
                    listOf(
                        "720p" to 30,
                        "1080p" to 30,
                        "1080p" to 60,
                        "1440p" to 60,
                        "2160p" to 60
                    ).forEach { (res, fps) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$res • ${fps}fps",
                                    color = if (project.exportResolution == res && project.exportFps == fps) CyanAccent else TextPrimary
                                )
                            },
                            onClick = {
                                onSelectResolutionAndFps(res, fps)
                                showResMenu = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = onExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = StudioBg
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("editor_export_button")
            ) {
                Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditorTransportBar(
    playheadMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    isFullscreen: Boolean,
    showBeforeAfter: Boolean,
    hasKeyframeAtPlayhead: Boolean,
    onPlayPause: () -> Unit,
    onStepBackward: () -> Unit,
    onStepForward: () -> Unit,
    onAddOrRemoveKeyframe: () -> Unit,
    onToggleBeforeAfter: () -> Unit,
    onToggleFullscreen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Timecode readout
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = NovaCutViewModel.formatTimecode(playheadMs),
                style = MaterialTheme.typography.labelSmall,
                color = CyanAccent
            )
            Text(
                text = " / ${NovaCutViewModel.formatTimecode(totalDurationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // Play / Pause + Frame step
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onStepBackward, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous frame", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CyanAccent.copy(alpha = 0.16f))
                    .testTag("editor_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = CyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = onStepForward, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next frame", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Keyframe Diamond Button + Before/After + Fullscreen
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(
                color = if (hasKeyframeAtPlayhead) KeyframeCrimson.copy(alpha = 0.24f) else StudioElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .border(
                        1.dp,
                        if (hasKeyframeAtPlayhead) KeyframeCrimson else StudioBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(onClick = onAddOrRemoveKeyframe)
                    .testTag("editor_keyframe_button")
            ) {
                Text(
                    text = if (hasKeyframeAtPlayhead) "◆ KF-" else "◇ KF+",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasKeyframeAtPlayhead) KeyframeCrimson else TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }

            IconButton(
                onClick = onToggleBeforeAfter,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("editor_compare_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Compare,
                    contentDescription = "Compare before and after",
                    tint = if (showBeforeAfter) TextAmber else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("editor_fullscreen_button")
            ) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen preview",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MultiTrackTimelineSection(
    project: VideoProject,
    playheadMs: Long,
    totalDurationMs: Long,
    zoom: Float,
    selectedClipId: String?,
    selectedAudioId: String?,
    selectedTextId: String?,
    selectedEffectId: String?,
    selectedStickerId: String?,
    onSeek: (Long) -> Unit,
    onZoomChange: (Float) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSelectText: (String) -> Unit,
    onSelectEffect: (String) -> Unit,
    onSelectSticker: (String) -> Unit,
    onToggleMainMute: () -> Unit,
    onAddClipClick: () -> Unit,
    onTransitionBadgeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = remember(project.primaryClips) {
        VideoRenderEngine.computePrimaryTrackSchedule(project.primaryClips)
    }
    val dpPerSecond = (44f * zoom).coerceIn(22f, 150f)
    val totalWidthDp: Dp = ((totalDurationMs / 1000f) * dpPerSecond + 120f).dp
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .background(Color(0xFF0B0E15))
            .border(width = 0.5.dp, color = StudioBorder)
    ) {
        // Timeline Scrubber & Zoom Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIMELINE",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = playheadMs.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1000f),
                colors = SliderDefaults.colors(
                    thumbColor = CyanAccent,
                    activeTrackColor = CyanAccent,
                    inactiveTrackColor = StudioBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .testTag("timeline_seek_slider")
            )
            IconButton(
                onClick = { onZoomChange(zoom - 0.25f) },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out timeline", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = String.format(java.util.Locale.US, "%.1fx", zoom),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            IconButton(
                onClick = { onZoomChange(zoom + 0.25f) },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in timeline", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }

        // Scrollable Multi-Track Workspace with Playhead Overlay
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScroll)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Track Headers Column
                Column(
                    modifier = Modifier
                        .width(58.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp)) // Ruler spacer
                    // Main video mute header
                    Row(
                        modifier = Modifier
                            .height(44.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioCard)
                            .clickable(onClick = onToggleMainMute)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (project.isMainTrackMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute main track",
                            tint = if (project.isMainTrackMuted) KeyframeCrimson else CyanAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("VID", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    }
                    TrackLabelPill("PIP/FX", OverlayTeal)
                    TrackLabelPill("TXT/CC", TextAmber)
                    TrackLabelPill("AUDIO", AudioEmerald)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tracks Canvas Area
                Box(modifier = Modifier.width(totalWidthDp).fillMaxHeight()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 1. Time Ruler with Second Ticks
                        TimeRulerRow(totalDurationMs = totalDurationMs, dpPerSecond = dpPerSecond)

                        // 2. Primary Video Track (with Filmstrip, Keyframe Diamonds, Transitions & + Button)
                        Row(
                            modifier = Modifier.height(44.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            schedule.forEach { win ->
                                val clip = win.clip
                                val clipWidthDp = ((win.effectiveDurationMs / 1000f) * dpPerSecond).coerceAtLeast(38f).dp
                                val isSelected = selectedClipId == clip.id

                                Box(
                                    modifier = Modifier
                                        .width(clipWidthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(StudioCard)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) CyanAccent else StudioBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            onSelectClip(clip.id)
                                            onSeek(win.timelineStartMs + 50L)
                                        }
                                        .testTag("timeline_clip_${clip.id}")
                                ) {
                                    // Repeating thumbnail filmstrip
                                    Row(modifier = Modifier.fillMaxSize()) {
                                        val thumbCount = (clipWidthDp.value / 36f).toInt().coerceIn(1, 8)
                                        repeat(thumbCount) {
                                            Image(
                                                painter = painterResource(id = clip.sampleDrawableRes),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                alpha = 0.65f,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                            )
                                        }
                                    }

                                    // Clip Info Overlay
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = clip.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (clip.speedMultiplier != 1.0f || clip.speedPoints.isNotEmpty()) {
                                            Text(
                                                text = if (clip.speedPoints.isNotEmpty()) "Curve" else "${clip.speedMultiplier}x",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyanAccent
                                            )
                                        }
                                    }

                                    // Keyframe Diamonds Plotted Along the Clip
                                    clip.keyframes.map { it.timestampMs }.distinct().forEach { kfMs ->
                                        val frac = (kfMs.toFloat() / win.effectiveDurationMs.toFloat()).coerceIn(0.05f, 0.95f)
                                        val xOffsetDp = (clipWidthDp.value * frac - 5f).dp
                                        Text(
                                            text = "◆",
                                            color = KeyframeCrimson,
                                            fontSize = 10.sp,
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .offset(x = xOffsetDp, y = (-2).dp)
                                        )
                                    }
                                }

                                // Transition Chip between clips
                                if (win.clipIndex < schedule.size - 1) {
                                    val hasTrans = clip.transitionAfter.transitionId != "none"
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 2.dp)
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (hasTrans) CyanAccent else StudioElevated)
                                            .clickable { onTransitionBadgeClick(clip.id) }
                                            .testTag("transition_badge_${clip.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Edit transition",
                                            tint = if (hasTrans) StudioBg else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))
                            // Add Clip (+) Button at end of primary track
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioElevated)
                                    .border(1.dp, CyanAccent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .clickable(onClick = onAddClipClick)
                                    .testTag("timeline_add_clip_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add clip", tint = CyanAccent, modifier = Modifier.size(18.dp))
                            }
                        }

                        // 3. Overlay / PIP + Video Effects Track
                        Box(modifier = Modifier.height(22.dp).fillMaxWidth()) {
                            project.overlayClips.forEach { ov ->
                                val effDur = KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(ov)
                                val startDp = ((ov.overlayStartMs / 1000f) * dpPerSecond).dp
                                val widthDp = ((effDur / 1000f) * dpPerSecond).coerceAtLeast(32f).dp
                                val isSel = selectedClipId == ov.id
                                Box(
                                    modifier = Modifier
                                        .offset(x = startDp)
                                        .width(widthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(OverlayTeal.copy(alpha = 0.28f))
                                        .border(1.dp, if (isSel) Color.White else OverlayTeal, RoundedCornerShape(5.dp))
                                        .clickable { onSelectClip(ov.id) }
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text("PIP: ${ov.title}", style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
                                }
                            }

                            project.effectItems.forEach { fx ->
                                val startDp = ((fx.timelineStartMs / 1000f) * dpPerSecond).dp
                                val widthDp = ((fx.durationMs / 1000f) * dpPerSecond).coerceAtLeast(36f).dp
                                val isSel = selectedEffectId == fx.id
                                Box(
                                    modifier = Modifier
                                        .offset(x = startDp)
                                        .width(widthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(EffectPurple.copy(alpha = 0.28f))
                                        .border(1.dp, if (isSel) Color.White else EffectPurple, RoundedCornerShape(5.dp))
                                        .clickable { onSelectEffect(fx.id) }
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text("FX: ${fx.effectName}", style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
                                }
                            }
                        }

                        // 4. Text / Captions / Stickers Track
                        Box(modifier = Modifier.height(22.dp).fillMaxWidth()) {
                            project.textClips.forEach { txt ->
                                val startDp = ((txt.timelineStartMs / 1000f) * dpPerSecond).dp
                                val widthDp = ((txt.durationMs / 1000f) * dpPerSecond).coerceAtLeast(36f).dp
                                val isSel = selectedTextId == txt.id
                                Box(
                                    modifier = Modifier
                                        .offset(x = startDp)
                                        .width(widthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(TextAmber.copy(alpha = 0.26f))
                                        .border(1.dp, if (isSel) Color.White else TextAmber, RoundedCornerShape(5.dp))
                                        .clickable { onSelectText(txt.id) }
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = if (txt.isCaption) "CC: ${txt.text}" else "T: ${txt.text}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }

                            project.stickerClips.forEach { stk ->
                                val startDp = ((stk.timelineStartMs / 1000f) * dpPerSecond).dp
                                val widthDp = ((stk.durationMs / 1000f) * dpPerSecond).coerceAtLeast(30f).dp
                                val isSel = selectedStickerId == stk.id
                                Box(
                                    modifier = Modifier
                                        .offset(x = startDp)
                                        .width(widthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(StickerCoral.copy(alpha = 0.28f))
                                        .border(1.dp, if (isSel) Color.White else StickerCoral, RoundedCornerShape(5.dp))
                                        .clickable { onSelectSticker(stk.id) }
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(stk.symbol, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
                                }
                            }
                        }

                        // 5. Audio Track with Rendered Waveform & Beat Markers
                        Box(modifier = Modifier.height(26.dp).fillMaxWidth()) {
                            project.audioClips.forEach { aud ->
                                val startDp = ((aud.timelineStartMs / 1000f) * dpPerSecond).dp
                                val widthDp = ((aud.durationMs / 1000f) * dpPerSecond).coerceAtLeast(48f).dp
                                val isSel = selectedAudioId == aud.id
                                Box(
                                    modifier = Modifier
                                        .offset(x = startDp)
                                        .width(widthDp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AudioEmerald.copy(alpha = 0.22f))
                                        .border(1.dp, if (isSel) Color.White else AudioEmerald, RoundedCornerShape(6.dp))
                                        .clickable { onSelectAudio(aud.id) }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val samples = aud.waveform.ifEmpty { listOf(0.5f, 0.8f, 0.4f, 0.9f) }
                                        val barCount = (size.width / 7f).toInt().coerceAtLeast(4)
                                        for (i in 0 until barCount) {
                                            val amp = samples[i % samples.size].coerceIn(0.15f, 1f)
                                            val x = i * 7f + 4f
                                            val barH = size.height * amp * 0.75f
                                            drawLine(
                                                color = AudioEmerald.copy(alpha = 0.65f),
                                                start = Offset(x, (size.height - barH) / 2f),
                                                end = Offset(x, (size.height + barH) / 2f),
                                                strokeWidth = 3f
                                            )
                                        }
                                    }
                                    Text(
                                        text = "♪ ${aud.title}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(horizontal = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Playhead Vertical Line Indicator
                    val playheadOffsetDp = ((playheadMs / 1000f) * dpPerSecond).dp
                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffsetDp)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackLabelPill(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .height(22.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .background(StudioCard),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent, fontSize = 9.sp)
    }
}

@Composable
private fun TimeRulerRow(totalDurationMs: Long, dpPerSecond: Float) {
    val totalSeconds = (totalDurationMs / 1000L).toInt().coerceAtLeast(5) + 2
    Row(
        modifier = Modifier.height(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (sec in 0..totalSeconds) {
            Box(modifier = Modifier.width(dpPerSecond.dp)) {
                Text(
                    text = String.format(java.util.Locale.US, "%02d:00", sec),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun SelectedClipQuickActionStrip(viewModel: NovaCutViewModel) {
    val actions = listOf(
        Triple("Split", Icons.Default.ContentCut) { viewModel.splitClipAtPlayhead() },
        Triple("Trim -0.5s", Icons.Default.Timeline) { viewModel.trimSelectedClip(0L, -500L) },
        Triple("Trim +0.5s", Icons.Default.Timeline) { viewModel.trimSelectedClip(0L, 500L) },
        Triple("Delete", Icons.Default.DeleteOutline) { viewModel.deleteSelectedItem() },
        Triple("Duplicate", Icons.Default.ContentCopy) { viewModel.duplicateSelectedClip() },
        Triple("Copy", Icons.Default.ContentCopy) { viewModel.copySelectedClip() },
        Triple("Paste", Icons.Default.ContentPaste) { viewModel.pasteCopiedClip() },
        Triple("Move ◀", Icons.Default.SwapHoriz) { viewModel.moveSelectedClipOrder(false) },
        Triple("Move ▶", Icons.Default.SwapHoriz) { viewModel.moveSelectedClipOrder(true) },
        Triple("Merge", Icons.Default.MergeType) { viewModel.mergeSelectedWithNextClip() },
        Triple("Freeze", Icons.Default.History) { viewModel.freezeFrameAtPlayhead() },
        Triple("Reverse", Icons.Default.SlowMotionVideo) { viewModel.toggleReverseSelectedClip() },
        Triple("Rotate 90°", Icons.Default.Rotate90DegreesCcw) { viewModel.rotateSelectedClip90() },
        Triple("Mirror H", Icons.Default.Flip) { viewModel.toggleMirrorHorizontal() },
        Triple("Flip V", Icons.Default.Flip) { viewModel.toggleMirrorVertical() },
        Triple("Extract Audio", Icons.Default.GraphicEq) { viewModel.extractAudioFromSelectedClip() },
        Triple("Replace", Icons.Default.SwapHoriz) { viewModel.openMediaPicker(MediaPickerPurpose.REPLACE_CLIP) }
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioCard)
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(actions) { (label, icon, action) ->
            Surface(
                color = StudioElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .clickable { action() }
                    .testTag("quick_action_${label.lowercase().replace(" ", "_")}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = label, tint = CyanAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun EditorBottomMasterToolbar(
    activeTab: EditorToolTab,
    onSelectTab: (EditorToolTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBg)
            .border(0.5.dp, StudioBorder)
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(EditorToolTab.entries) { tab ->
            val selected = activeTab == tab
            val icon: ImageVector = when (tab) {
                EditorToolTab.EDIT -> Icons.Default.ContentCut
                EditorToolTab.AUDIO -> Icons.Default.Audiotrack
                EditorToolTab.TEXT -> Icons.Default.TextFields
                EditorToolTab.EFFECTS -> Icons.Default.AutoAwesome
                EditorToolTab.OVERLAY -> Icons.Default.Layers
                EditorToolTab.CAPTIONS -> Icons.Default.ClosedCaption
                EditorToolTab.FILTERS -> Icons.Default.FilterVintage
                EditorToolTab.ADJUST -> Icons.Default.Tune
                EditorToolTab.SPEED -> Icons.Default.Speed
                EditorToolTab.ANIMATION -> Icons.Default.Animation
                EditorToolTab.TRANSITION -> Icons.Default.SwapHoriz
                EditorToolTab.KEYFRAMES -> Icons.Default.Timeline
                EditorToolTab.MASK -> Icons.Default.Crop
                EditorToolTab.CUTOUT -> Icons.Default.AutoFixHigh
                EditorToolTab.CANVAS -> Icons.Default.AspectRatio
                EditorToolTab.TRANSFORM -> Icons.Default.Rotate90DegreesCcw
                EditorToolTab.BLEND -> Icons.Default.BlurOn
                EditorToolTab.STICKERS -> Icons.Default.EmojiEmotions
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) CyanAccent.copy(alpha = 0.16f) else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("editor_tab_${tab.name.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.label,
                    tint = if (selected) CyanAccent else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) CyanAccent else TextSecondary
                )
            }
        }
    }
}
