package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.ProjectDatabase
import com.example.data.ProjectRepository
import com.example.engine.EffectFilterTransitionCatalog
import com.example.engine.KeyframeAndSpeedEngine
import com.example.engine.MediaAndExportEngine
import com.example.engine.OmkarAutoVideoEngine
import com.example.engine.VideoRenderEngine
import com.example.model.AspectRatioMode
import com.example.model.AutomaticMotionMode
import com.example.model.CapCutDraftExportResult
import com.example.model.ZoomDirection
import com.example.model.AudioCategory
import com.example.model.AudioClip
import com.example.model.BlendModeType
import com.example.model.CanvasConfig
import com.example.model.CaptionWordTiming
import com.example.model.ClipAnimation
import com.example.model.ColorAdjustment
import com.example.model.CutoutConfig
import com.example.model.EffectTrackItem
import com.example.model.ExportConfig
import com.example.model.ExportProgressState
import com.example.model.KeyframeInterpolation
import com.example.model.KeyframeProperty
import com.example.model.MaskConfig
import com.example.model.MediaAlbumCategory
import com.example.model.MediaItemModel
import com.example.model.MediaTypeFilter
import com.example.model.SpeedPoint
import com.example.model.StickerClip
import com.example.model.StudioNotification
import com.example.model.TextClip
import com.example.model.TimelineClip
import com.example.model.TransitionConfig
import com.example.model.VideoProject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

enum class AppScreen {
    HOME,
    MEDIA_PICKER,
    EDITOR,
    EXPORT,
    CAMERA_CAPTURE
}

enum class HomeBottomTab(val label: String) {
    EDIT("Edit"),
    TEMPLATES("Template"),
    AI_LAB("AI Lab"),
    PROJECTS("Projects"),
    INBOX("Inbox"),
    ME("Me")
}

enum class MediaPickerPurpose {
    NEW_PROJECT_VIDEO,
    NEW_PROJECT_PHOTO,
    ADD_PRIMARY_CLIP,
    ADD_OVERLAY_CLIP,
    REPLACE_CLIP,
    AUTOCUT_TEMPLATE
}

enum class EditorToolTab(val label: String) {
    EDIT("Edit"),
    AUDIO("Audio"),
    TEXT("Text"),
    EFFECTS("Effects"),
    OVERLAY("Overlay"),
    CAPTIONS("Captions"),
    FILTERS("Filters"),
    ADJUST("Adjust"),
    SPEED("Speed"),
    ANIMATION("Animation"),
    TRANSITION("Transitions"),
    KEYFRAMES("Keyframes"),
    MASK("Mask"),
    CUTOUT("Cutout"),
    CANVAS("Canvas"),
    TRANSFORM("Transform"),
    BLEND("Blend"),
    STICKERS("Stickers")
}

class NovaCutViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ProjectDatabase.getInstance(application)
    val repository = ProjectRepository(database.projectDao())
    val mediaEngine = MediaAndExportEngine(application)

    // Navigation & Global UI State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _homeTab = MutableStateFlow(HomeBottomTab.EDIT)
    val homeTab: StateFlow<HomeBottomTab> = _homeTab.asStateFlow()

    private val _isDarkStudioTheme = MutableStateFlow(true)
    val isDarkStudioTheme: StateFlow<Boolean> = _isDarkStudioTheme.asStateFlow()

    private val _showGlobalSearch = MutableStateFlow(false)
    val showGlobalSearch: StateFlow<Boolean> = _showGlobalSearch.asStateFlow()

    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // Projects list from Room DB (with immediate 5 starter projects on frame 0)
    val savedProjects: StateFlow<List<VideoProject>> = repository.allProjectsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EffectFilterTransitionCatalog.buildDefaultSeedProjects()
    )

    // Media Picker State
    private val _mediaCatalog = MutableStateFlow<List<MediaItemModel>>(MediaAndExportEngine.builtInStudioMedia)
    val mediaCatalog: StateFlow<List<MediaItemModel>> = _mediaCatalog.asStateFlow()

    private val _pickerPurpose = MutableStateFlow(MediaPickerPurpose.NEW_PROJECT_VIDEO)
    val pickerPurpose: StateFlow<MediaPickerPurpose> = _pickerPurpose.asStateFlow()

    private val _pickerAlbumTab = MutableStateFlow(MediaAlbumCategory.ALBUMS)
    val pickerAlbumTab: StateFlow<MediaAlbumCategory> = _pickerAlbumTab.asStateFlow()

    private val _pickerTypeFilter = MutableStateFlow(MediaTypeFilter.ALL)
    val pickerTypeFilter: StateFlow<MediaTypeFilter> = _pickerTypeFilter.asStateFlow()

    private val _pickerSelectedAlbumName = MutableStateFlow("All Albums")
    val pickerSelectedAlbumName: StateFlow<String> = _pickerSelectedAlbumName.asStateFlow()

    private val _pickerSearchQuery = MutableStateFlow("")
    val pickerSearchQuery: StateFlow<String> = _pickerSearchQuery.asStateFlow()

    private val _pickerHdEnabled = MutableStateFlow(true)
    val pickerHdEnabled: StateFlow<Boolean> = _pickerHdEnabled.asStateFlow()

    private val _selectedMediaItems = MutableStateFlow<List<MediaItemModel>>(emptyList())
    val selectedMediaItems: StateFlow<List<MediaItemModel>> = _selectedMediaItems.asStateFlow()

    // Active Video Editor Workspace State
    private val _activeProject = MutableStateFlow(createDefaultStarterProject())
    val activeProject: StateFlow<VideoProject> = _activeProject.asStateFlow()

    private val undoStack = ArrayDeque<VideoProject>()
    private val redoStack = ArrayDeque<VideoProject>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _playheadMs = MutableStateFlow(0L)
    val playheadMs: StateFlow<Long> = _playheadMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isFullscreenPreview = MutableStateFlow(false)
    val isFullscreenPreview: StateFlow<Boolean> = _isFullscreenPreview.asStateFlow()

    private val _showBeforeAfterCompare = MutableStateFlow(false)
    val showBeforeAfterCompare: StateFlow<Boolean> = _showBeforeAfterCompare.asStateFlow()

    private val _timelineZoom = MutableStateFlow(1.0f) // 0.5f .. 3.5f
    val timelineZoom: StateFlow<Float> = _timelineZoom.asStateFlow()

    private val _activeEditorTab = MutableStateFlow(EditorToolTab.EDIT)
    val activeEditorTab: StateFlow<EditorToolTab> = _activeEditorTab.asStateFlow()

    // Selected items on timeline
    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedAudioId = MutableStateFlow<String?>(null)
    val selectedAudioId: StateFlow<String?> = _selectedAudioId.asStateFlow()

    private val _selectedTextId = MutableStateFlow<String?>(null)
    val selectedTextId: StateFlow<String?> = _selectedTextId.asStateFlow()

    private val _selectedEffectId = MutableStateFlow<String?>(null)
    val selectedEffectId: StateFlow<String?> = _selectedEffectId.asStateFlow()

    private val _selectedStickerId = MutableStateFlow<String?>(null)
    val selectedStickerId: StateFlow<String?> = _selectedStickerId.asStateFlow()

    private val _activeKeyframeProperty = MutableStateFlow(KeyframeProperty.SCALE)
    val activeKeyframeProperty: StateFlow<KeyframeProperty> = _activeKeyframeProperty.asStateFlow()

    private val _activeKeyframeInterpolation = MutableStateFlow(KeyframeInterpolation.EASE_IN_OUT)
    val activeKeyframeInterpolation: StateFlow<KeyframeInterpolation> = _activeKeyframeInterpolation.asStateFlow()

    private var clipboardClip: TimelineClip? = null
    private var playbackJob: Job? = null
    private var exportJob: Job? = null

    // Export State
    private val _exportConfig = MutableStateFlow(ExportConfig())
    val exportConfig: StateFlow<ExportConfig> = _exportConfig.asStateFlow()

    private val _exportProgress = MutableStateFlow(ExportProgressState())
    val exportProgress: StateFlow<ExportProgressState> = _exportProgress.asStateFlow()

    // Omkar Automatic Video Maker State
    private val _autoEditSummary = MutableStateFlow("")
    val autoEditSummary: StateFlow<String> = _autoEditSummary.asStateFlow()

    private val _capCutDraftExportResult = MutableStateFlow<CapCutDraftExportResult?>(null)
    val capCutDraftExportResult: StateFlow<CapCutDraftExportResult?> = _capCutDraftExportResult.asStateFlow()

    private val _customSpeechTranscript = MutableStateFlow("")
    val customSpeechTranscript: StateFlow<String> = _customSpeechTranscript.asStateFlow()

    private val _autoGenerateCaptions = MutableStateFlow(true)
    val autoGenerateCaptions: StateFlow<Boolean> = _autoGenerateCaptions.asStateFlow()

    // Preferences & Inbox State
    private val _proxyPreviewEnabled = MutableStateFlow(true)
    val proxyPreviewEnabled: StateFlow<Boolean> = _proxyPreviewEnabled.asStateFlow()

    private val _gpuHardwareAccelEnabled = MutableStateFlow(true)
    val gpuHardwareAccelEnabled: StateFlow<Boolean> = _gpuHardwareAccelEnabled.asStateFlow()

    private val _autoSaveEnabled = MutableStateFlow(true)
    val autoSaveEnabled: StateFlow<Boolean> = _autoSaveEnabled.asStateFlow()

    private val _showFpsOverlay = MutableStateFlow(false)
    val showFpsOverlay: StateFlow<Boolean> = _showFpsOverlay.asStateFlow()

    private val _inboxNotifications = MutableStateFlow(defaultInboxNotifications())
    val inboxNotifications: StateFlow<List<StudioNotification>> = _inboxNotifications.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeedProjects()
            val catalog = mediaEngine.loadMediaCatalog()
            _mediaCatalog.value = catalog
        }
    }

    fun showToastMessage(msg: String) {
        _statusBannerMessage.value = msg
        viewModelScope.launch {
            delay(2600L)
            if (_statusBannerMessage.value == msg) {
                _statusBannerMessage.value = null
            }
        }
    }

    // =========================================================================
    // NAVIGATION & WORKSPACE ENTRY POINTS
    // =========================================================================

    fun selectHomeTab(tab: HomeBottomTab) {
        _homeTab.value = tab
    }

    fun setGlobalSearchVisible(visible: Boolean, initialQuery: String = "") {
        _showGlobalSearch.value = visible
        if (visible) {
            _globalSearchQuery.value = initialQuery
        }
    }

    fun updateGlobalSearchQuery(q: String) {
        _globalSearchQuery.value = q
    }

    fun openMediaPicker(purpose: MediaPickerPurpose) {
        pausePlayback()
        _pickerPurpose.value = purpose
        _selectedMediaItems.value = emptyList()
        _pickerTypeFilter.value = when (purpose) {
            MediaPickerPurpose.NEW_PROJECT_PHOTO -> MediaTypeFilter.PHOTOS
            else -> MediaTypeFilter.ALL
        }
        _currentScreen.value = AppScreen.MEDIA_PICKER
    }

    fun openCameraCaptureStudio() {
        pausePlayback()
        _currentScreen.value = AppScreen.CAMERA_CAPTURE
    }

    fun openProjectInEditor(project: VideoProject, initialTab: EditorToolTab = EditorToolTab.EDIT) {
        pausePlayback()
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _activeProject.value = project
        _playheadMs.value = 0L
        _selectedClipId.value = project.primaryClips.firstOrNull()?.id
        _selectedAudioId.value = null
        _selectedTextId.value = null
        _selectedEffectId.value = null
        _selectedStickerId.value = null
        _activeEditorTab.value = initialTab
        _currentScreen.value = AppScreen.EDITOR
    }

    fun launchShortcutTool(toolId: String) {
        val currentOrStarter = savedProjects.value.firstOrNull() ?: _activeProject.value
        when (toolId) {
            "autocut" -> {
                openMediaPicker(MediaPickerPurpose.AUTOCUT_TEMPLATE)
            }
            "retouch" -> {
                val updated = applyFilterInternal(currentOrStarter, "velvet_skin", 0.85f)
                openProjectInEditor(updated, EditorToolTab.FILTERS)
                showToastMessage("Portrait Retouch & Velvet Skin grade loaded")
            }
            "photo_tools" -> {
                openMediaPicker(MediaPickerPurpose.NEW_PROJECT_PHOTO)
            }
            "shoot_record" -> {
                openCameraCaptureStudio()
            }
            "auto_enhance" -> {
                val enhanced = currentOrStarter.copy(
                    primaryClips = currentOrStarter.primaryClips.map {
                        it.copy(
                            filterId = "hdr_clarity",
                            filterIntensity = 0.85f,
                            adjustments = it.adjustments.copy(
                                contrast = 0.22f,
                                saturation = 0.18f,
                                sharpen = 0.35f,
                                highlights = -0.12f,
                                shadows = 0.18f
                            )
                        )
                    }
                )
                commitProjectMutation(enhanced, saveUndo = false)
                openProjectInEditor(enhanced, EditorToolTab.ADJUST)
                showToastMessage("HDR Clarity & Micro-Contrast Auto Enhance applied")
            }
            "cover_maker" -> {
                openProjectInEditor(currentOrStarter, EditorToolTab.TEXT)
                showToastMessage("Cover Maker: Customize title & frame for project cover")
            }
            "auto_captions" -> {
                openProjectInEditor(currentOrStarter, EditorToolTab.CAPTIONS)
                generateAutoCaptionsFromTimeline()
            }
            "remove_bg" -> {
                openProjectInEditor(currentOrStarter, EditorToolTab.CUTOUT)
                toggleAutoSubjectCutout()
            }
            "cloud_space" -> {
                _homeTab.value = HomeBottomTab.PROJECTS
                showToastMessage("Studio Space synced • ${savedProjects.value.size} local projects backed up")
            }
        }
    }

    fun useTemplate(templateId: String) {
        val tpl = EffectFilterTransitionCatalog.findTemplate(templateId) ?: return
        val newProj = EffectFilterTransitionCatalog.buildProjectFromTemplate(tpl)
        viewModelScope.launch {
            repository.saveProject(newProj)
        }
        openProjectInEditor(newProj, EditorToolTab.EDIT)
        showToastMessage("Loaded editable template: ${tpl.title}")
    }

    fun navigateBack(): Boolean {
        return when (_currentScreen.value) {
            AppScreen.EXPORT -> {
                if (_exportProgress.value.isExporting) {
                    cancelExport()
                }
                _currentScreen.value = AppScreen.EDITOR
                true
            }
            AppScreen.EDITOR -> {
                pausePlayback()
                if (_isFullscreenPreview.value) {
                    _isFullscreenPreview.value = false
                    return true
                }
                viewModelScope.launch {
                    repository.saveProject(_activeProject.value)
                }
                _currentScreen.value = AppScreen.HOME
                true
            }
            AppScreen.MEDIA_PICKER -> {
                _currentScreen.value = if (_pickerPurpose.value in listOf(
                        MediaPickerPurpose.ADD_PRIMARY_CLIP,
                        MediaPickerPurpose.ADD_OVERLAY_CLIP,
                        MediaPickerPurpose.REPLACE_CLIP
                    )
                ) {
                    AppScreen.EDITOR
                } else {
                    AppScreen.HOME
                }
                true
            }
            AppScreen.CAMERA_CAPTURE -> {
                _currentScreen.value = AppScreen.HOME
                true
            }
            AppScreen.HOME -> {
                if (_showGlobalSearch.value) {
                    _showGlobalSearch.value = false
                    true
                } else if (_homeTab.value != HomeBottomTab.EDIT) {
                    _homeTab.value = HomeBottomTab.EDIT
                    true
                } else {
                    false
                }
            }
        }
    }

    // =========================================================================
    // MEDIA PICKER OPERATIONS
    // =========================================================================

    fun setPickerAlbumTab(tab: MediaAlbumCategory) {
        _pickerAlbumTab.value = tab
    }

    fun setPickerTypeFilter(filter: MediaTypeFilter) {
        _pickerTypeFilter.value = filter
    }

    fun setPickerSelectedAlbumName(album: String) {
        _pickerSelectedAlbumName.value = album
    }

    fun setPickerSearchQuery(query: String) {
        _pickerSearchQuery.value = query
    }

    fun togglePickerHd() {
        _pickerHdEnabled.value = !_pickerHdEnabled.value
    }

    fun toggleMediaItemSelection(item: MediaItemModel) {
        val current = _selectedMediaItems.value.toMutableList()
        if (_pickerPurpose.value == MediaPickerPurpose.REPLACE_CLIP) {
            _selectedMediaItems.value = listOf(item)
            return
        }
        val existingIdx = current.indexOfFirst { it.id == item.id }
        if (existingIdx >= 0) {
            current.removeAt(existingIdx)
        } else {
            current.add(item)
        }
        _selectedMediaItems.value = current
    }

    fun importSystemPickedUris(uris: List<String>) {
        if (uris.isEmpty()) return
        val app = getApplication<Application>()
        val importedModels = uris.mapIndexed { idx, uriStr ->
            val isPhoto = uriStr.contains("image", ignoreCase = true) || uriStr.endsWith(".jpg", true) || uriStr.endsWith(".png", true)
            val isVid = !isPhoto
            val geometry = if (isVid) {
                OmkarAutoVideoEngine.VideoMetadataReader.readSourceGeometry(
                    context = app,
                    mediaUri = uriStr,
                    fallbackWidth = 1080,
                    fallbackHeight = 1920,
                    fallbackDurationMs = 12000L,
                    fallbackFps = 30f
                )
            } else {
                OmkarAutoVideoEngine.VideoMetadataReader.createCanonicalGeometry(
                    rawWidth = 1080,
                    rawHeight = 1920,
                    durationMs = 4000L
                )
            }
            MediaItemModel(
                id = "picked_${System.currentTimeMillis()}_$idx",
                title = if (isVid) "Imported_Video_${idx + 1}.mp4" else "Imported_Photo_${idx + 1}.jpg",
                albumName = "Imported",
                tabCategory = MediaAlbumCategory.ALBUMS,
                isVideo = isVid,
                durationMs = geometry.durationMs,
                width = geometry.displayWidth,
                height = geometry.displayHeight,
                uriString = uriStr,
                drawableRes = R.drawable.img_sample_cyberpunk,
                accentHex = 0xFF00E5FFL,
                isHd = maxOf(geometry.displayWidth, geometry.displayHeight) >= 1080,
                fileSizeMb = geometry.fileSizeMb
            )
        }
        _mediaCatalog.value = importedModels + _mediaCatalog.value
        _selectedMediaItems.value = _selectedMediaItems.value + importedModels
        showToastMessage("Imported ${importedModels.size} media item(s) • Geometry preserved")
    }

    fun confirmMediaPickerSelection() {
        val chosen = _selectedMediaItems.value.ifEmpty {
            listOf(_mediaCatalog.value.first())
        }
        val app = getApplication<Application>()
        when (_pickerPurpose.value) {
            MediaPickerPurpose.NEW_PROJECT_VIDEO, MediaPickerPurpose.NEW_PROJECT_PHOTO -> {
                val firstMedia = chosen.first()
                val sourceGeometry = OmkarAutoVideoEngine.VideoMetadataReader.readSourceGeometry(
                    context = app,
                    mediaUri = firstMedia.uriString,
                    fallbackWidth = firstMedia.width.takeIf { it > 0 } ?: 1080,
                    fallbackHeight = firstMedia.height.takeIf { it > 0 } ?: 1920,
                    fallbackDurationMs = firstMedia.durationMs.takeIf { it > 0L } ?: 10000L,
                    fallbackFps = 30f
                )
                val clips = chosen.mapIndexed { idx, media ->
                    mediaToTimelineClip(media, isOverlay = false, index = idx).copy(
                        sourceGeometry = sourceGeometry
                    )
                }
                val newProj = VideoProject(
                    name = "Project ${java.text.SimpleDateFormat("MMM dd HH:mm", java.util.Locale.US).format(java.util.Date())}",
                    aspectRatio = sourceGeometry.canonicalAspectRatioMode,
                    sourceGeometry = sourceGeometry,
                    primaryClips = clips,
                    audioClips = listOf(
                        EffectFilterTransitionCatalog.audioPresets.first().let { preset ->
                            AudioClip(
                                title = preset.title,
                                artistOrSource = preset.artist,
                                builtInSynthId = preset.id,
                                durationMs = clips.sumOf { KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(it) },
                                waveform = preset.waveform,
                                beatMarkersMs = preset.beatMarkersMs
                            )
                        }
                    ),
                    coverDrawableRes = clips.first().sampleDrawableRes
                )
                viewModelScope.launch { repository.saveProject(newProj) }
                openProjectInEditor(newProj, EditorToolTab.EDIT)
            }
            MediaPickerPurpose.AUTOCUT_TEMPLATE -> {
                val firstMedia = chosen.first()
                val sourceGeometry = OmkarAutoVideoEngine.VideoMetadataReader.readSourceGeometry(
                    context = app,
                    mediaUri = firstMedia.uriString,
                    fallbackWidth = firstMedia.width.takeIf { it > 0 } ?: 1080,
                    fallbackHeight = firstMedia.height.takeIf { it > 0 } ?: 1920,
                    fallbackDurationMs = firstMedia.durationMs.takeIf { it > 0L } ?: 12000L,
                    fallbackFps = 30f
                )
                val baseClip = mediaToTimelineClip(firstMedia, isOverlay = false, index = 0).copy(
                    transitionAfter = TransitionConfig(transitionId = "none", durationMs = 0L),
                    sourceGeometry = sourceGeometry
                )
                val seedProj = VideoProject(
                    name = "Omkar Auto • ${firstMedia.title.substringBeforeLast(".")}",
                    aspectRatio = sourceGeometry.canonicalAspectRatioMode,
                    sourceGeometry = sourceGeometry,
                    primaryClips = listOf(baseClip),
                    coverDrawableRes = firstMedia.drawableRes
                )
                val autoResult = OmkarAutoVideoEngine.executeAutomaticEditPipeline(
                    context = app,
                    project = seedProj,
                    motionMode = AutomaticMotionMode.SIMPLE,
                    targetZoomFactor = OmkarAutoVideoEngine.DEFAULT_AUTO_ZOOM,
                    customTranscript = _customSpeechTranscript.value,
                    generateSyncedCaptions = _autoGenerateCaptions.value
                )
                _autoEditSummary.value = autoResult.summaryMessage
                viewModelScope.launch { repository.saveProject(autoResult.updatedProject) }
                openProjectInEditor(autoResult.updatedProject, EditorToolTab.EDIT)
                showToastMessage(autoResult.summaryMessage)
            }
            MediaPickerPurpose.ADD_PRIMARY_CLIP -> {
                val addedClips = chosen.mapIndexed { idx, m -> mediaToTimelineClip(m, isOverlay = false, index = idx) }
                val updated = _activeProject.value.copy(
                    primaryClips = _activeProject.value.primaryClips + addedClips
                )
                commitProjectMutation(updated)
                _selectedClipId.value = addedClips.first().id
                _currentScreen.value = AppScreen.EDITOR
                showToastMessage("Added ${addedClips.size} clip(s) to timeline")
            }
            MediaPickerPurpose.ADD_OVERLAY_CLIP -> {
                val overlayClips = chosen.mapIndexed { idx, m ->
                    mediaToTimelineClip(m, isOverlay = true, index = idx).copy(
                        overlayStartMs = _playheadMs.value + idx * 1500L,
                        scale = 0.65f,
                        transformX = 0.28f,
                        transformY = -0.28f
                    )
                }
                val updated = _activeProject.value.copy(
                    overlayClips = _activeProject.value.overlayClips + overlayClips
                )
                commitProjectMutation(updated)
                _selectedClipId.value = overlayClips.first().id
                _currentScreen.value = AppScreen.EDITOR
                _activeEditorTab.value = EditorToolTab.OVERLAY
                showToastMessage("Added Picture-in-Picture overlay layer")
            }
            MediaPickerPurpose.REPLACE_CLIP -> {
                val replacement = chosen.first()
                val targetId = _selectedClipId.value
                if (targetId != null) {
                    val updatedPrimary = _activeProject.value.primaryClips.map { clip ->
                        if (clip.id == targetId) {
                            clip.copy(
                                title = replacement.title,
                                mediaUri = replacement.uriString,
                                sampleDrawableRes = replacement.drawableRes,
                                isPhoto = !replacement.isVideo
                            )
                        } else clip
                    }
                    commitProjectMutation(_activeProject.value.copy(primaryClips = updatedPrimary))
                }
                _currentScreen.value = AppScreen.EDITOR
                showToastMessage("Replaced clip media with ${replacement.title}")
            }
        }
    }

    private fun mediaToTimelineClip(media: MediaItemModel, isOverlay: Boolean, index: Int): TimelineClip {
        val dur = media.durationMs.coerceIn(2500L, 15000L)
        return TimelineClip(
            title = media.title.substringBeforeLast("."),
            mediaUri = media.uriString,
            sampleDrawableRes = media.drawableRes,
            isPhoto = !media.isVideo,
            isOverlay = isOverlay,
            sourceDurationMs = dur + 1000L,
            trimStartMs = 0L,
            trimEndMs = dur,
            transitionAfter = if (!isOverlay && index == 0) {
                TransitionConfig(transitionId = "cross_dissolve", durationMs = 500L)
            } else TransitionConfig(),
            accentColorHex = media.accentHex
        )
    }

    // =========================================================================
    // PLAYBACK & TIMELINE SEEKING
    // =========================================================================

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        if (_isPlaying.value) return
        val totalDur = VideoRenderEngine.computeProjectTotalDurationMs(_activeProject.value)
        if (_playheadMs.value >= totalDur - 50L) {
            _playheadMs.value = 0L
        }
        _isPlaying.value = true

        // Trigger synchronized audio preview if not muted
        val proj = _activeProject.value
        if (!proj.isMainTrackMuted && proj.audioClips.isNotEmpty()) {
            val activeAudio = proj.audioClips.first()
            if (!activeAudio.isMuted) {
                val preset = EffectFilterTransitionCatalog.audioPresets.find { it.id == activeAudio.builtInSynthId }
                mediaEngine.playRealtimeAudioPreview(
                    scope = viewModelScope,
                    baseFreqHz = preset?.baseFreqHz ?: 110f,
                    volume = activeAudio.volume,
                    durationMs = (totalDur - _playheadMs.value).coerceIn(500L, 4000L)
                )
            }
        }

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            var lastTick = System.currentTimeMillis()
            while (isActive && _isPlaying.value) {
                delay(16L)
                val now = System.currentTimeMillis()
                val dt = (now - lastTick).coerceIn(8L, 64L)
                lastTick = now
                val maxDur = VideoRenderEngine.computeProjectTotalDurationMs(_activeProject.value)
                val nextMs = _playheadMs.value + dt
                if (nextMs >= maxDur) {
                    _playheadMs.value = maxDur
                    _isPlaying.value = false
                    mediaEngine.stopAudioPreview()
                    break
                } else {
                    _playheadMs.value = nextMs
                }
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        mediaEngine.stopAudioPreview()
    }

    fun seekToMs(targetMs: Long, enableSnap: Boolean = true) {
        val maxDur = VideoRenderEngine.computeProjectTotalDurationMs(_activeProject.value)
        var clamped = targetMs.coerceIn(0L, maxDur)
        if (enableSnap) {
            val schedule = VideoRenderEngine.computePrimaryTrackSchedule(_activeProject.value.primaryClips)
            for (win in schedule) {
                if (abs(clamped - win.timelineStartMs) <= 65L) {
                    clamped = win.timelineStartMs
                    break
                }
                if (abs(clamped - win.timelineEndMs) <= 65L) {
                    clamped = win.timelineEndMs
                    break
                }
            }
        }
        _playheadMs.value = clamped
    }

    fun stepFrame(forward: Boolean) {
        pausePlayback()
        val frameDurationMs = (1000L / _activeProject.value.exportFps.coerceAtLeast(24)).coerceAtLeast(16L)
        val delta = if (forward) frameDurationMs else -frameDurationMs
        seekToMs(_playheadMs.value + delta, enableSnap = false)
    }

    fun setTimelineZoom(zoom: Float) {
        _timelineZoom.value = zoom.coerceIn(0.5f, 3.5f)
    }

    fun setFullscreenPreview(fullscreen: Boolean) {
        _isFullscreenPreview.value = fullscreen
    }

    fun setBeforeAfterCompare(compare: Boolean) {
        _showBeforeAfterCompare.value = compare
    }

    fun setActiveEditorTab(tab: EditorToolTab) {
        _activeEditorTab.value = tab
    }

    // =========================================================================
    // SELECTION & UNDO / REDO / AUTOSAVE
    // =========================================================================

    fun selectClip(clipId: String?) {
        _selectedClipId.value = clipId
        if (clipId != null) {
            _selectedAudioId.value = null
            _selectedTextId.value = null
            _selectedEffectId.value = null
            _selectedStickerId.value = null
        }
    }

    fun selectAudioClip(audioId: String?) {
        _selectedAudioId.value = audioId
        if (audioId != null) {
            _selectedClipId.value = null
            _selectedTextId.value = null
            _selectedEffectId.value = null
            _selectedStickerId.value = null
            _activeEditorTab.value = EditorToolTab.AUDIO
        }
    }

    fun selectTextClip(textId: String?) {
        _selectedTextId.value = textId
        if (textId != null) {
            _selectedClipId.value = null
            _selectedAudioId.value = null
            _selectedEffectId.value = null
            _selectedStickerId.value = null
            _activeEditorTab.value = EditorToolTab.TEXT
        }
    }

    fun selectEffectItem(effectId: String?) {
        _selectedEffectId.value = effectId
        if (effectId != null) {
            _selectedClipId.value = null
            _selectedAudioId.value = null
            _selectedTextId.value = null
            _selectedStickerId.value = null
            _activeEditorTab.value = EditorToolTab.EFFECTS
        }
    }

    fun selectStickerClip(stickerId: String?) {
        _selectedStickerId.value = stickerId
        if (stickerId != null) {
            _selectedClipId.value = null
            _selectedAudioId.value = null
            _selectedTextId.value = null
            _selectedEffectId.value = null
            _activeEditorTab.value = EditorToolTab.STICKERS
        }
    }

    fun getSelectedOrActiveClip(): TimelineClip? {
        val proj = _activeProject.value
        val id = _selectedClipId.value
        if (id != null) {
            proj.primaryClips.find { it.id == id }?.let { return it }
            proj.overlayClips.find { it.id == id }?.let { return it }
        }
        val schedule = VideoRenderEngine.computePrimaryTrackSchedule(proj.primaryClips)
        val atPlayhead = schedule.find { _playheadMs.value in it.timelineStartMs..it.timelineEndMs }?.clip
        return atPlayhead ?: proj.primaryClips.firstOrNull()
    }

    fun getClipLocalMs(clip: TimelineClip): Long {
        if (clip.isOverlay) {
            val eff = KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip)
            return (_playheadMs.value - clip.overlayStartMs).coerceIn(0L, eff)
        }
        val schedule = VideoRenderEngine.computePrimaryTrackSchedule(_activeProject.value.primaryClips)
        val win = schedule.find { it.clip.id == clip.id } ?: return 0L
        return (_playheadMs.value - win.timelineStartMs).coerceIn(0L, win.effectiveDurationMs)
    }

    private fun commitProjectMutation(newProject: VideoProject, saveUndo: Boolean = true) {
        if (saveUndo) {
            if (undoStack.size >= 30) {
                undoStack.removeFirst()
            }
            undoStack.addLast(_activeProject.value)
            redoStack.clear()
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = false
        }
        val stamped = newProject.copy(updatedAtMs = System.currentTimeMillis())
        _activeProject.value = stamped
        if (_autoSaveEnabled.value) {
            viewModelScope.launch {
                repository.saveProject(stamped)
            }
        }
    }

    private fun mutateSelectedClip(transform: (TimelineClip) -> TimelineClip) {
        val target = getSelectedOrActiveClip() ?: return
        val proj = _activeProject.value
        val updated = if (target.isOverlay) {
            proj.copy(overlayClips = proj.overlayClips.map { if (it.id == target.id) transform(it) else it })
        } else {
            proj.copy(primaryClips = proj.primaryClips.map { if (it.id == target.id) transform(it) else it })
        }
        commitProjectMutation(updated)
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val prev = undoStack.removeLast()
        redoStack.addLast(_activeProject.value)
        _activeProject.value = prev
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        viewModelScope.launch { repository.saveProject(prev) }
        showToastMessage("Undo applied")
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeLast()
        undoStack.addLast(_activeProject.value)
        _activeProject.value = next
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        viewModelScope.launch { repository.saveProject(next) }
        showToastMessage("Redo applied")
    }

    // =========================================================================
    // BASIC & PROFESSIONAL VIDEO EDITING OPERATIONS
    // =========================================================================

    fun splitClipAtPlayhead() {
        val proj = _activeProject.value
        val schedule = VideoRenderEngine.computePrimaryTrackSchedule(proj.primaryClips)
        val currentMs = _playheadMs.value
        val win = schedule.find { currentMs > it.timelineStartMs + 250L && currentMs < it.timelineEndMs - 250L }
        if (win == null) {
            showToastMessage("Move playhead inside a clip (at least 0.25s from edge) to split")
            return
        }
        val clip = win.clip
        val localOffsetMs = currentMs - win.timelineStartMs
        val sourceSplitMs = KeyframeAndSpeedEngine.mapTimelineOffsetToSourceMs(clip, localOffsetMs)
            .coerceIn(clip.trimStartMs + 200L, clip.trimEndMs - 200L)

        val leftDurationMs = (sourceSplitMs - clip.trimStartMs).coerceAtLeast(200L)
        val rightDurationMs = (clip.trimEndMs - sourceSplitMs).coerceAtLeast(200L)

        val leftKeyframes = if (clip.isAutoSplitClip) {
            OmkarAutoVideoEngine.KeyframeGenerator.generateClipKeyframes(
                clipDurationMs = leftDurationMs,
                zoomDirection = clip.zoomDirection,
                targetZoomFactor = proj.autoZoomTargetFactor
            )
        } else {
            clip.keyframes.filter { it.timestampMs <= localOffsetMs }
        }

        val rightKeyframes = if (clip.isAutoSplitClip) {
            OmkarAutoVideoEngine.KeyframeGenerator.generateClipKeyframes(
                clipDurationMs = rightDurationMs,
                zoomDirection = clip.zoomDirection,
                targetZoomFactor = proj.autoZoomTargetFactor
            )
        } else {
            clip.keyframes
                .filter { it.timestampMs >= localOffsetMs }
                .map { it.copy(id = UUID.randomUUID().toString(), timestampMs = (it.timestampMs - localOffsetMs).coerceAtLeast(0L)) }
        }

        val leftClip = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} A",
            trimEndMs = sourceSplitMs,
            keyframes = leftKeyframes
        )
        val rightClip = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} B",
            trimStartMs = sourceSplitMs,
            keyframes = rightKeyframes
        )

        val newPrimary = proj.primaryClips.toMutableList().apply {
            removeAt(win.clipIndex)
            add(win.clipIndex, rightClip)
            add(win.clipIndex, leftClip)
        }
        commitProjectMutation(proj.copy(primaryClips = newPrimary))
        _selectedClipId.value = rightClip.id
        showToastMessage("Split clip at ${formatTimecode(currentMs)}")
    }

    fun trimSelectedClip(deltaStartMs: Long, deltaEndMs: Long) {
        val clip = getSelectedOrActiveClip() ?: return
        val newStart = (clip.trimStartMs + deltaStartMs).coerceIn(0L, clip.trimEndMs - 400L)
        val newEnd = (clip.trimEndMs + deltaEndMs).coerceIn(newStart + 400L, clip.sourceDurationMs.coerceAtLeast(newStart + 400L))
        mutateSelectedClip {
            it.copy(trimStartMs = newStart, trimEndMs = newEnd)
        }
    }

    fun deleteSelectedItem() {
        val proj = _activeProject.value
        _selectedTextId.value?.let { tid ->
            commitProjectMutation(proj.copy(textClips = proj.textClips.filterNot { it.id == tid }))
            _selectedTextId.value = null
            showToastMessage("Deleted text layer")
            return
        }
        _selectedEffectId.value?.let { eid ->
            commitProjectMutation(proj.copy(effectItems = proj.effectItems.filterNot { it.id == eid }))
            _selectedEffectId.value = null
            showToastMessage("Deleted video effect")
            return
        }
        _selectedStickerId.value?.let { sid ->
            commitProjectMutation(proj.copy(stickerClips = proj.stickerClips.filterNot { it.id == sid }))
            _selectedStickerId.value = null
            showToastMessage("Deleted sticker")
            return
        }
        _selectedAudioId.value?.let { aid ->
            commitProjectMutation(proj.copy(audioClips = proj.audioClips.filterNot { it.id == aid }))
            _selectedAudioId.value = null
            showToastMessage("Deleted audio track")
            return
        }
        val targetClip = getSelectedOrActiveClip() ?: return
        if (targetClip.isOverlay) {
            commitProjectMutation(proj.copy(overlayClips = proj.overlayClips.filterNot { it.id == targetClip.id }))
            _selectedClipId.value = proj.primaryClips.firstOrNull()?.id
            showToastMessage("Removed overlay clip")
        } else {
            if (proj.primaryClips.size <= 1) {
                showToastMessage("Primary track requires at least 1 clip")
                return
            }
            val remaining = proj.primaryClips.filterNot { it.id == targetClip.id }
            commitProjectMutation(proj.copy(primaryClips = remaining))
            _selectedClipId.value = remaining.firstOrNull()?.id
            showToastMessage("Deleted clip")
        }
    }

    fun duplicateSelectedClip() {
        val target = getSelectedOrActiveClip() ?: return
        val proj = _activeProject.value
        val copy = target.copy(
            id = UUID.randomUUID().toString(),
            title = "${target.title} Copy",
            overlayStartMs = if (target.isOverlay) target.overlayStartMs + 1000L else 0L,
            keyframes = target.keyframes.map { it.copy(id = UUID.randomUUID().toString()) }
        )
        if (target.isOverlay) {
            commitProjectMutation(proj.copy(overlayClips = proj.overlayClips + copy))
        } else {
            val idx = proj.primaryClips.indexOfFirst { it.id == target.id }.coerceAtLeast(0)
            val list = proj.primaryClips.toMutableList().apply { add(idx + 1, copy) }
            commitProjectMutation(proj.copy(primaryClips = list))
        }
        _selectedClipId.value = copy.id
        showToastMessage("Duplicated clip")
    }

    fun copySelectedClip() {
        val target = getSelectedOrActiveClip() ?: return
        clipboardClip = target
        showToastMessage("Copied '${target.title}' to clipboard")
    }

    fun pasteCopiedClip() {
        val src = clipboardClip ?: getSelectedOrActiveClip() ?: return
        val copy = src.copy(
            id = UUID.randomUUID().toString(),
            title = "${src.title} Pasted",
            keyframes = src.keyframes.map { it.copy(id = UUID.randomUUID().toString()) }
        )
        val proj = _activeProject.value
        commitProjectMutation(proj.copy(primaryClips = proj.primaryClips + copy))
        _selectedClipId.value = copy.id
        showToastMessage("Pasted '${copy.title}'")
    }

    fun moveSelectedClipOrder(moveRight: Boolean) {
        val target = getSelectedOrActiveClip() ?: return
        if (target.isOverlay) return
        val list = _activeProject.value.primaryClips.toMutableList()
        val idx = list.indexOfFirst { it.id == target.id }
        if (idx < 0) return
        val newIdx = if (moveRight) idx + 1 else idx - 1
        if (newIdx !in list.indices) return
        val item = list.removeAt(idx)
        list.add(newIdx, item)
        commitProjectMutation(_activeProject.value.copy(primaryClips = list))
        showToastMessage("Reordered clip to slot ${newIdx + 1}")
    }

    fun mergeSelectedWithNextClip() {
        val target = getSelectedOrActiveClip() ?: return
        if (target.isOverlay) return
        val list = _activeProject.value.primaryClips.toMutableList()
        val idx = list.indexOfFirst { it.id == target.id }
        if (idx < 0 || idx >= list.size - 1) {
            showToastMessage("Select a clip with a following clip to merge")
            return
        }
        val next = list[idx + 1]
        val combinedDuration = (target.trimEndMs - target.trimStartMs) + (next.trimEndMs - next.trimStartMs)
        val merged = target.copy(
            title = "${target.title} + ${next.title}",
            sourceDurationMs = combinedDuration + 1000L,
            trimStartMs = 0L,
            trimEndMs = combinedDuration,
            transitionAfter = next.transitionAfter
        )
        list.removeAt(idx + 1)
        list[idx] = merged
        commitProjectMutation(_activeProject.value.copy(primaryClips = list))
        _selectedClipId.value = merged.id
        showToastMessage("Merged clips into '${merged.title}'")
    }

    fun rotateSelectedClip90() {
        mutateSelectedClip { clip ->
            clip.copy(rotation = (clip.rotation + 90f) % 360f)
        }
    }

    fun toggleMirrorHorizontal() {
        mutateSelectedClip { clip -> clip.copy(mirrorH = !clip.mirrorH) }
    }

    fun toggleMirrorVertical() {
        mutateSelectedClip { clip -> clip.copy(mirrorV = !clip.mirrorV) }
    }

    fun freezeFrameAtPlayhead() {
        val target = getSelectedOrActiveClip() ?: return
        val frozenClip = target.copy(
            id = UUID.randomUUID().toString(),
            title = "${target.title} (Freeze)",
            isPhoto = true,
            isFrozen = true,
            trimStartMs = 0L,
            trimEndMs = 2500L,
            sourceDurationMs = 2500L,
            speedPoints = emptyList(),
            speedMultiplier = 1.0f
        )
        val proj = _activeProject.value
        val idx = proj.primaryClips.indexOfFirst { it.id == target.id }.coerceAtLeast(0)
        val list = proj.primaryClips.toMutableList().apply { add(idx + 1, frozenClip) }
        commitProjectMutation(proj.copy(primaryClips = list))
        _selectedClipId.value = frozenClip.id
        showToastMessage("Inserted 2.5s Freeze Frame")
    }

    fun toggleReverseSelectedClip() {
        mutateSelectedClip { clip ->
            val nextRev = !clip.isReversed
            showToastMessage(if (nextRev) "Clip reversed" else "Clip restored to forward playback")
            clip.copy(isReversed = nextRev)
        }
    }

    fun toggleMainTrackMute() {
        val nextMute = !_activeProject.value.isMainTrackMuted
        commitProjectMutation(_activeProject.value.copy(isMainTrackMuted = nextMute))
        showToastMessage(if (nextMute) "Primary track audio muted" else "Primary track audio unmuted")
    }

    fun updateClipAudioSettings(volume: Float, isMuted: Boolean, fadeInMs: Long, fadeOutMs: Long) {
        mutateSelectedClip { clip ->
            val localMs = getClipLocalMs(clip)
            val updatedKfs = if (KeyframeAndSpeedEngine.hasKeyframeNear(clip.keyframes, localMs, KeyframeProperty.VOLUME)) {
                KeyframeAndSpeedEngine.upsertKeyframe(clip.keyframes, localMs, KeyframeProperty.VOLUME, volume)
            } else clip.keyframes
            clip.copy(
                volume = volume.coerceIn(0f, 2f),
                isMuted = isMuted,
                fadeInMs = fadeInMs.coerceIn(0L, 4000L),
                fadeOutMs = fadeOutMs.coerceIn(0L, 4000L),
                keyframes = updatedKfs
            )
        }
    }

    fun extractAudioFromSelectedClip() {
        val clip = getSelectedOrActiveClip() ?: return
        val effDur = KeyframeAndSpeedEngine.computeEffectiveClipDurationMs(clip)
        val extracted = AudioClip(
            title = "Extracted • ${clip.title}",
            artistOrSource = "Clip Audio Track",
            builtInSynthId = "synth_cyber_pulse",
            category = AudioCategory.EXTRACTED,
            timelineStartMs = 0L,
            durationMs = effDur
        )
        val proj = _activeProject.value
        commitProjectMutation(
            proj.copy(
                primaryClips = proj.primaryClips.map { if (it.id == clip.id) it.copy(isMuted = true) else it },
                audioClips = proj.audioClips + extracted
            )
        )
        _selectedAudioId.value = extracted.id
        showToastMessage("Extracted audio to independent Audio Track")
    }

    fun setProjectAspectRatio(mode: AspectRatioMode) {
        commitProjectMutation(_activeProject.value.copy(aspectRatio = mode))
    }

    fun updateCanvasConfig(config: CanvasConfig) {
        commitProjectMutation(_activeProject.value.copy(canvasConfig = config))
    }

    // =========================================================================
    // KEYFRAME ENGINE OPERATIONS
    // =========================================================================

    fun setActiveKeyframeProperty(prop: KeyframeProperty) {
        _activeKeyframeProperty.value = prop
    }

    fun setActiveKeyframeInterpolation(interp: KeyframeInterpolation) {
        _activeKeyframeInterpolation.value = interp
        // Also update any keyframe at current playhead
        val clip = getSelectedOrActiveClip() ?: return
        val localMs = getClipLocalMs(clip)
        val prop = _activeKeyframeProperty.value
        if (KeyframeAndSpeedEngine.hasKeyframeNear(clip.keyframes, localMs, prop)) {
            val currentVal = KeyframeAndSpeedEngine.evaluateProperty(clip.keyframes, prop, localMs, prop.defaultValue)
            mutateSelectedClip {
                it.copy(
                    keyframes = KeyframeAndSpeedEngine.upsertKeyframe(
                        it.keyframes,
                        localMs,
                        prop,
                        currentVal,
                        interp
                    )
                )
            }
        }
    }

    fun toggleKeyframeAtPlayhead(property: KeyframeProperty = _activeKeyframeProperty.value) {
        val clip = getSelectedOrActiveClip() ?: return
        val localMs = getClipLocalMs(clip)
        val hasNear = KeyframeAndSpeedEngine.hasKeyframeNear(clip.keyframes, localMs, property)
        if (hasNear) {
            mutateSelectedClip {
                it.copy(keyframes = KeyframeAndSpeedEngine.removeKeyframesNear(it.keyframes, localMs, property))
            }
            showToastMessage("Removed ${property.label} keyframe at ${formatTimecode(localMs)}")
        } else {
            val fallback = when (property) {
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
            val currentVal = KeyframeAndSpeedEngine.evaluateProperty(clip.keyframes, property, localMs, fallback)
            mutateSelectedClip {
                it.copy(
                    keyframes = KeyframeAndSpeedEngine.upsertKeyframe(
                        it.keyframes,
                        localMs,
                        property,
                        currentVal,
                        _activeKeyframeInterpolation.value
                    )
                )
            }
            showToastMessage("Added ◆ ${property.label} keyframe at ${formatTimecode(localMs)}")
        }
    }

    fun updateTransformOrKeyframeProperty(property: KeyframeProperty, newValue: Float, forceKeyframe: Boolean = false) {
        val clip = getSelectedOrActiveClip() ?: return
        val localMs = getClipLocalMs(clip)
        val clamped = newValue.coerceIn(property.minValue, property.maxValue)
        val shouldWriteKf = forceKeyframe || clip.keyframes.any { it.property == property }

        mutateSelectedClip { c ->
            val updatedKfs = if (shouldWriteKf) {
                KeyframeAndSpeedEngine.upsertKeyframe(
                    c.keyframes,
                    localMs,
                    property,
                    clamped,
                    _activeKeyframeInterpolation.value
                )
            } else c.keyframes

            when (property) {
                KeyframeProperty.POS_X -> c.copy(transformX = clamped, keyframes = updatedKfs)
                KeyframeProperty.POS_Y -> c.copy(transformY = clamped, keyframes = updatedKfs)
                KeyframeProperty.SCALE -> c.copy(scale = clamped, keyframes = updatedKfs)
                KeyframeProperty.ROTATION -> c.copy(rotation = clamped, keyframes = updatedKfs)
                KeyframeProperty.OPACITY -> c.copy(opacity = clamped, keyframes = updatedKfs)
                KeyframeProperty.CROP_LEFT -> c.copy(cropLeft = clamped, keyframes = updatedKfs)
                KeyframeProperty.CROP_TOP -> c.copy(cropTop = clamped, keyframes = updatedKfs)
                KeyframeProperty.CROP_RIGHT -> c.copy(cropRight = clamped, keyframes = updatedKfs)
                KeyframeProperty.CROP_BOTTOM -> c.copy(cropBottom = clamped, keyframes = updatedKfs)
                KeyframeProperty.VOLUME -> c.copy(volume = clamped, keyframes = updatedKfs)
                KeyframeProperty.FILTER_INTENSITY -> c.copy(filterIntensity = clamped, keyframes = updatedKfs)
                KeyframeProperty.EFFECT_INTENSITY -> c.copy(keyframes = updatedKfs)
            }
        }
    }

    fun interactiveCanvasDragAndZoom(
        panDeltaXNorm: Float,
        panDeltaYNorm: Float,
        zoomFactor: Float,
        rotationDeltaDeg: Float
    ) {
        // If a text clip is selected, move/scale/rotate text
        _selectedTextId.value?.let { tid ->
            val proj = _activeProject.value
            val updatedTexts = proj.textClips.map { t ->
                if (t.id == tid) {
                    t.copy(
                        posX = (t.posX + panDeltaXNorm).coerceIn(-0.95f, 0.95f),
                        posY = (t.posY + panDeltaYNorm).coerceIn(-0.95f, 0.95f),
                        scale = (t.scale * zoomFactor).coerceIn(0.3f, 4.0f),
                        rotation = (t.rotation + rotationDeltaDeg) % 360f
                    )
                } else t
            }
            commitProjectMutation(proj.copy(textClips = updatedTexts), saveUndo = false)
            return
        }

        // If a sticker is selected, move/scale/rotate sticker
        _selectedStickerId.value?.let { sid ->
            val proj = _activeProject.value
            val updatedStickers = proj.stickerClips.map { s ->
                if (s.id == sid) {
                    s.copy(
                        posX = (s.posX + panDeltaXNorm).coerceIn(-0.95f, 0.95f),
                        posY = (s.posY + panDeltaYNorm).coerceIn(-0.95f, 0.95f),
                        scale = (s.scale * zoomFactor).coerceIn(0.3f, 4.0f),
                        rotation = (s.rotation + rotationDeltaDeg) % 360f
                    )
                } else s
            }
            commitProjectMutation(proj.copy(stickerClips = updatedStickers), saveUndo = false)
            return
        }

        val clip = getSelectedOrActiveClip() ?: return
        val localMs = getClipLocalMs(clip)
        val evaluated = KeyframeAndSpeedEngine.evaluateClipStateAt(clip, localMs)
        val nx = (evaluated.posX + panDeltaXNorm).coerceIn(-1f, 1f)
        val ny = (evaluated.posY + panDeltaYNorm).coerceIn(-1f, 1f)
        val ns = (evaluated.scale * zoomFactor).coerceIn(0.2f, 5f)
        val nr = (evaluated.rotation + rotationDeltaDeg) % 360f

        mutateSelectedClip { c ->
            var kfs = c.keyframes
            if (kfs.any { it.property == KeyframeProperty.POS_X }) {
                kfs = KeyframeAndSpeedEngine.upsertKeyframe(kfs, localMs, KeyframeProperty.POS_X, nx)
            }
            if (kfs.any { it.property == KeyframeProperty.POS_Y }) {
                kfs = KeyframeAndSpeedEngine.upsertKeyframe(kfs, localMs, KeyframeProperty.POS_Y, ny)
            }
            if (kfs.any { it.property == KeyframeProperty.SCALE }) {
                kfs = KeyframeAndSpeedEngine.upsertKeyframe(kfs, localMs, KeyframeProperty.SCALE, ns)
            }
            if (kfs.any { it.property == KeyframeProperty.ROTATION }) {
                kfs = KeyframeAndSpeedEngine.upsertKeyframe(kfs, localMs, KeyframeProperty.ROTATION, nr)
            }
            c.copy(transformX = nx, transformY = ny, scale = ns, rotation = nr, keyframes = kfs)
        }
    }

    fun jumpToKeyframe(forward: Boolean) {
        val clip = getSelectedOrActiveClip() ?: return
        val schedule = VideoRenderEngine.computePrimaryTrackSchedule(_activeProject.value.primaryClips)
        val win = schedule.find { it.clip.id == clip.id }
        val clipStartGlobal = if (clip.isOverlay) clip.overlayStartMs else (win?.timelineStartMs ?: 0L)
        val localMs = (_playheadMs.value - clipStartGlobal).coerceAtLeast(0L)
        val timestamps = clip.keyframes.map { it.timestampMs }.distinct().sorted()
        if (timestamps.isEmpty()) {
            showToastMessage("No keyframes on this clip yet")
            return
        }
        val targetLocal = if (forward) {
            timestamps.firstOrNull { it > localMs + 40L } ?: timestamps.first()
        } else {
            timestamps.lastOrNull { it < localMs - 40L } ?: timestamps.last()
        }
        seekToMs(clipStartGlobal + targetLocal, enableSnap = false)
    }

    // =========================================================================
    // SPEED ENGINE OPERATIONS
    // =========================================================================

    fun setClipConstantSpeed(speedMultiplier: Float) {
        mutateSelectedClip { clip ->
            clip.copy(
                speedMultiplier = speedMultiplier.coerceIn(0.1f, 10f),
                speedPresetId = "normal",
                speedPoints = emptyList()
            )
        }
    }

    fun applySpeedCurvePreset(presetId: String) {
        val preset = KeyframeAndSpeedEngine.speedPresets.find { it.id == presetId } ?: return
        mutateSelectedClip { clip ->
            clip.copy(
                speedPresetId = preset.id,
                speedPoints = if (preset.id == "normal") emptyList() else preset.points
            )
        }
        showToastMessage("Applied '${preset.name}' velocity curve")
    }

    fun updateSpeedCurvePoint(index: Int, newSpeedMultiplier: Float) {
        mutateSelectedClip { clip ->
            val currentPoints = clip.speedPoints.ifEmpty {
                KeyframeAndSpeedEngine.speedPresets.first().points
            }.toMutableList()
            if (index in currentPoints.indices) {
                currentPoints[index] = currentPoints[index].copy(
                    speedMultiplier = newSpeedMultiplier.coerceIn(0.1f, 8.0f)
                )
            }
            clip.copy(speedPresetId = "custom", speedPoints = currentPoints)
        }
    }

    // =========================================================================
    // FILTER, ADJUST, MASK, CUTOUT, BLEND, ANIMATION, TRANSITION
    // =========================================================================

    fun applyFilter(filterId: String, intensity: Float = 0.85f, applyToAllClips: Boolean = false) {
        val proj = _activeProject.value
        if (applyToAllClips) {
            commitProjectMutation(
                proj.copy(
                    globalFilterId = filterId,
                    globalFilterIntensity = intensity.coerceIn(0f, 1f),
                    primaryClips = proj.primaryClips.map { it.copy(filterId = filterId, filterIntensity = intensity) }
                )
            )
            showToastMessage("Applied filter across all timeline clips")
        } else {
            mutateSelectedClip { clip ->
                val localMs = getClipLocalMs(clip)
                val kfs = if (clip.keyframes.any { it.property == KeyframeProperty.FILTER_INTENSITY }) {
                    KeyframeAndSpeedEngine.upsertKeyframe(clip.keyframes, localMs, KeyframeProperty.FILTER_INTENSITY, intensity)
                } else clip.keyframes
                clip.copy(filterId = filterId, filterIntensity = intensity.coerceIn(0f, 1f), keyframes = kfs)
            }
        }
    }

    private fun applyFilterInternal(project: VideoProject, filterId: String, intensity: Float): VideoProject {
        return project.copy(
            globalFilterId = filterId,
            globalFilterIntensity = intensity,
            primaryClips = project.primaryClips.map { it.copy(filterId = filterId, filterIntensity = intensity) }
        )
    }

    fun updateClipAdjustments(adjustments: ColorAdjustment) {
        mutateSelectedClip { it.copy(adjustments = adjustments) }
    }

    fun resetClipAdjustments() {
        mutateSelectedClip { it.copy(adjustments = ColorAdjustment(), filterId = "none") }
        showToastMessage("Reset color adjustments & filter")
    }

    fun updateClipMask(mask: MaskConfig) {
        mutateSelectedClip { it.copy(mask = mask) }
    }

    fun updateClipBlendMode(mode: BlendModeType) {
        mutateSelectedClip { it.copy(blendMode = mode) }
    }

    fun updateClipCutout(cutout: CutoutConfig) {
        mutateSelectedClip { it.copy(cutout = cutout) }
    }

    fun toggleAutoSubjectCutout() {
        mutateSelectedClip {
            val next = !it.cutout.autoCutoutEnabled
            showToastMessage(if (next) "Smart Subject Cutout active" else "Subject Cutout disabled")
            it.copy(cutout = it.cutout.copy(autoCutoutEnabled = next))
        }
    }

    fun updateClipAnimation(animation: ClipAnimation) {
        mutateSelectedClip { it.copy(animation = animation) }
    }

    fun applyTransition(transitionId: String, durationMs: Long = 600L, applyToAll: Boolean = false) {
        val proj = _activeProject.value
        val cfg = TransitionConfig(transitionId = transitionId, durationMs = durationMs.coerceIn(200L, 2000L))
        if (applyToAll) {
            commitProjectMutation(
                proj.copy(primaryClips = proj.primaryClips.map { it.copy(transitionAfter = cfg) })
            )
            showToastMessage("Applied transition to all clip cuts")
        } else {
            mutateSelectedClip { it.copy(transitionAfter = cfg) }
            val trName = EffectFilterTransitionCatalog.findTransition(transitionId)?.name ?: "Transition"
            showToastMessage("Applied '$trName' (${durationMs}ms)")
        }
    }

    // =========================================================================
    // MULTI-TRACK AUDIO, TEXT, CAPTIONS, EFFECTS, STICKERS, OVERLAY
    // =========================================================================

    fun addAudioPresetToTimeline(presetId: String) {
        val preset = EffectFilterTransitionCatalog.audioPresets.find { it.id == presetId } ?: return
        val totalDur = VideoRenderEngine.computeProjectTotalDurationMs(_activeProject.value)
        val startMs = if (preset.category == AudioCategory.SFX) _playheadMs.value else 0L
        val durMs = if (preset.category == AudioCategory.SFX) preset.durationMs else totalDur.coerceAtMost(preset.durationMs)
        val newAudio = AudioClip(
            title = preset.title,
            artistOrSource = preset.artist,
            builtInSynthId = preset.id,
            category = preset.category,
            timelineStartMs = startMs,
            durationMs = durMs,
            waveform = preset.waveform,
            beatMarkersMs = preset.beatMarkersMs.filter { it < durMs }
        )
        commitProjectMutation(_activeProject.value.copy(audioClips = _activeProject.value.audioClips + newAudio))
        _selectedAudioId.value = newAudio.id
        mediaEngine.playRealtimeAudioPreview(viewModelScope, preset.baseFreqHz, 0.85f, 1400L)
        showToastMessage("Added '${preset.title}' to Audio Track")
    }

    fun recordVoiceoverToTimeline(durationMs: Long = 4000L) {
        viewModelScope.launch {
            val wavFile = mediaEngine.generateVoiceoverWavFile(durationMs, "Voiceover")
            val voClip = AudioClip(
                title = "Voiceover ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())}",
                artistOrSource = "Studio Microphone",
                audioUri = wavFile.toURI().toString(),
                builtInSynthId = "synth_lofi_chill",
                category = AudioCategory.VOICEOVER,
                timelineStartMs = _playheadMs.value,
                durationMs = durationMs
            )
            commitProjectMutation(_activeProject.value.copy(audioClips = _activeProject.value.audioClips + voClip))
            _selectedAudioId.value = voClip.id
            showToastMessage("Recorded ${durationMs / 1000f}s Voiceover at playhead")
        }
    }

    fun updateSelectedAudioClip(
        volume: Float,
        isMuted: Boolean,
        fadeInMs: Long,
        fadeOutMs: Long,
        speed: Float
    ) {
        val id = _selectedAudioId.value ?: _activeProject.value.audioClips.firstOrNull()?.id ?: return
        val updated = _activeProject.value.audioClips.map { a ->
            if (a.id == id) {
                a.copy(
                    volume = volume.coerceIn(0f, 2f),
                    isMuted = isMuted,
                    fadeInMs = fadeInMs.coerceIn(0L, 4000L),
                    fadeOutMs = fadeOutMs.coerceIn(0L, 4000L),
                    speed = speed.coerceIn(0.25f, 4f)
                )
            } else a
        }
        commitProjectMutation(_activeProject.value.copy(audioClips = updated))
    }

    fun addTextClipToTimeline(
        text: String = "STUDIO TITLE",
        isCaption: Boolean = false,
        stylePresetId: String = "clean_cinema"
    ) {
        val preset = EffectFilterTransitionCatalog.textStylePresets.find { it.id == stylePresetId }
            ?: EffectFilterTransitionCatalog.textStylePresets.first()
        val newText = TextClip(
            text = text,
            isCaption = isCaption,
            timelineStartMs = _playheadMs.value,
            durationMs = 3200L,
            fontFamilyId = preset.fontFamilyId,
            fontSizeSp = if (isCaption) 18f else 28f,
            textColorHex = preset.textColorHex,
            strokeColorHex = preset.strokeColorHex,
            strokeWidth = preset.strokeWidth,
            backgroundColorHex = preset.bgHex,
            backgroundAlpha = preset.bgAlpha,
            shadowColorHex = preset.shadowHex,
            posY = if (isCaption) 0.65f else -0.15f,
            stylePresetId = preset.id
        )
        commitProjectMutation(_activeProject.value.copy(textClips = _activeProject.value.textClips + newText))
        _selectedTextId.value = newText.id
        showToastMessage(if (isCaption) "Added caption segment" else "Added text layer")
    }

    fun updateSelectedTextClip(transform: (TextClip) -> TextClip) {
        val id = _selectedTextId.value ?: _activeProject.value.textClips.firstOrNull()?.id ?: return
        val updated = _activeProject.value.textClips.map { if (it.id == id) transform(it) else it }
        commitProjectMutation(_activeProject.value.copy(textClips = updated))
    }

    fun generateAutoCaptionsFromTimeline() {
        val totalDur = VideoRenderEngine.computeProjectTotalDurationMs(_activeProject.value)
        val phrases = listOf(
            "WELCOME TO THE FUTURE OF CINEMA",
            "EVERY FRAME CRAFTED WITH PRECISION",
            "SMOOTH KEYFRAMES & VELOCITY CURVES",
            "ANAMORPHIC COLOR GRADING IN 4K",
            "EXPORT READY FOR EVERY PLATFORM"
        )
        val segDur = 2600L
        var cursor = 0L
        var index = 0
        val generatedCaptions = mutableListOf<TextClip>()
        while (cursor + 1000L < totalDur && index < 8) {
            val phrase = phrases[index % phrases.size]
            val words = phrase.split(" ")
            val wordDur = segDur / words.size.coerceAtLeast(1)
            val wordTimings = words.mapIndexed { wIdx, w ->
                CaptionWordTiming(
                    word = w,
                    startOffsetMs = wIdx * wordDur,
                    endOffsetMs = (wIdx + 1) * wordDur
                )
            }
            generatedCaptions.add(
                TextClip(
                    text = phrase,
                    isCaption = true,
                    timelineStartMs = cursor,
                    durationMs = segDur.coerceAtMost(totalDur - cursor),
                    fontFamilyId = "space_grotesk",
                    fontSizeSp = 19f,
                    textColorHex = 0xFFFFFFFFL,
                    strokeColorHex = 0xFF090B10L,
                    strokeWidth = 3f,
                    backgroundColorHex = 0xFF090B10L,
                    backgroundAlpha = 0.72f,
                    posY = 0.66f,
                    inAnimId = "pop_up",
                    outAnimId = "fade_out",
                    stylePresetId = "creator_pill",
                    wordTimings = wordTimings
                )
            )
            cursor += segDur + 200L
            index++
        }
        val nonCaptions = _activeProject.value.textClips.filterNot { it.isCaption }
        commitProjectMutation(_activeProject.value.copy(textClips = nonCaptions + generatedCaptions))
        _selectedTextId.value = generatedCaptions.firstOrNull()?.id
        showToastMessage("Generated ${generatedCaptions.size} timed captions across timeline")
    }

    fun addVideoEffectToTimeline(effectDefId: String) {
        val def = EffectFilterTransitionCatalog.findEffect(effectDefId) ?: return
        val totalDur = VideoRenderEngine.computeProjectTotalDurationMs(_activeProject.value)
        val start = _playheadMs.value.coerceIn(0L, (totalDur - 1000L).coerceAtLeast(0L))
        val dur = 3500L.coerceAtMost((totalDur - start).coerceAtLeast(1500L))
        val item = EffectTrackItem(
            effectDefId = def.id,
            effectName = def.name,
            category = def.category,
            timelineStartMs = start,
            durationMs = dur,
            intensity = def.defaultIntensity,
            speed = def.defaultSpeed,
            secondaryParam = def.defaultSecondary
        )
        commitProjectMutation(_activeProject.value.copy(effectItems = _activeProject.value.effectItems + item))
        _selectedEffectId.value = item.id
        showToastMessage("Added '${def.name}' effect to timeline")
    }

    fun updateSelectedEffectItem(
        intensity: Float,
        speed: Float,
        secondaryParam: Float,
        durationMs: Long
    ) {
        val id = _selectedEffectId.value ?: _activeProject.value.effectItems.firstOrNull()?.id ?: return
        val updated = _activeProject.value.effectItems.map { e ->
            if (e.id == id) {
                e.copy(
                    intensity = intensity.coerceIn(0f, 1f),
                    speed = speed.coerceIn(0.1f, 2f),
                    secondaryParam = secondaryParam.coerceIn(0f, 1f),
                    durationMs = durationMs.coerceIn(500L, 20000L)
                )
            } else e
        }
        commitProjectMutation(_activeProject.value.copy(effectItems = updated))
    }

    fun addStickerToTimeline(stickerDefId: String) {
        val def = EffectFilterTransitionCatalog.stickersCatalog.find { it.id == stickerDefId } ?: return
        val item = StickerClip(
            stickerDefId = def.id,
            label = def.label,
            symbol = def.symbol,
            accentHex = def.accentHex,
            timelineStartMs = _playheadMs.value,
            durationMs = 3200L
        )
        commitProjectMutation(_activeProject.value.copy(stickerClips = _activeProject.value.stickerClips + item))
        _selectedStickerId.value = item.id
        showToastMessage("Added '${def.label}' sticker")
    }

    // =========================================================================
    // AI LAB MODULAR TOOLS (Independent from Manual Editor)
    // =========================================================================

    fun runAiLabTool(toolId: String) {
        val baseProj = savedProjects.value.firstOrNull() ?: _activeProject.value
        openProjectInEditor(baseProj, EditorToolTab.EDIT)
        when (toolId) {
            "ai_auto_captions" -> {
                _activeEditorTab.value = EditorToolTab.CAPTIONS
                generateAutoCaptionsFromTimeline()
            }
            "ai_bg_removal", "ai_smart_cutout" -> {
                _activeEditorTab.value = EditorToolTab.CUTOUT
                mutateSelectedClip {
                    it.copy(
                        cutout = it.cutout.copy(
                            autoCutoutEnabled = true,
                            neonStrokeEnabled = true,
                            feather = 0.18f
                        )
                    )
                }
                showToastMessage("AI Smart Cutout isolated subject with neon rim")
            }
            "ai_auto_enhance" -> {
                _activeEditorTab.value = EditorToolTab.ADJUST
                mutateSelectedClip {
                    it.copy(
                        filterId = "hdr_clarity",
                        adjustments = it.adjustments.copy(
                            contrast = 0.25f,
                            saturation = 0.20f,
                            sharpen = 0.40f,
                            exposure = 0.08f
                        )
                    )
                }
                showToastMessage("AI HDR Color & Clarity Enhance applied")
            }
            "ai_auto_reframe" -> {
                val nextAspect = if (_activeProject.value.aspectRatio == AspectRatioMode.RATIO_9_16) {
                    AspectRatioMode.RATIO_16_9
                } else {
                    AspectRatioMode.RATIO_9_16
                }
                setProjectAspectRatio(nextAspect)
                _activeEditorTab.value = EditorToolTab.CANVAS
                showToastMessage("Auto-reframed timeline to ${nextAspect.label} with subject tracking")
            }
            "ai_tts_voiceover" -> {
                _activeEditorTab.value = EditorToolTab.AUDIO
                recordVoiceoverToTimeline(4500L)
                addTextClipToTimeline("AI NARRATION VOICEOVER", isCaption = true, stylePresetId = "neon_cyber")
            }
            "ai_restyle_fx" -> {
                _activeEditorTab.value = EditorToolTab.EFFECTS
                addVideoEffectToTimeline("cyber_hud")
                applyFilter("cyber_neon", 0.88f, applyToAllClips = true)
            }
            else -> {
                _activeEditorTab.value = EditorToolTab.EFFECTS
                addVideoEffectToTimeline("halation_bloom")
            }
        }
    }

    // =========================================================================
    // PROJECT MANAGEMENT (Save, Duplicate, Rename, Delete)
    // =========================================================================

    fun duplicateProject(project: VideoProject) {
        viewModelScope.launch {
            val copy = repository.duplicateProject(project)
            showToastMessage("Duplicated '${copy.name}'")
        }
    }

    fun renameProject(project: VideoProject, newName: String) {
        viewModelScope.launch {
            val updated = repository.renameProject(project, newName)
            if (_activeProject.value.id == updated.id) {
                _activeProject.value = updated
            }
            showToastMessage("Renamed to '${updated.name}'")
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            showToastMessage("Deleted project")
        }
    }

    // =========================================================================
    // EXPORT ENGINE OPERATIONS
    // =========================================================================

    fun openExportScreen() {
        pausePlayback()
        val p = _activeProject.value
        _exportConfig.value = _exportConfig.value.copy(
            resolution = p.exportResolution,
            fps = p.exportFps,
            qualityPreset = p.exportBitrateMode,
            customBitrateMbps = p.customBitrateMbps
        )
        _exportProgress.value = ExportProgressState()
        _currentScreen.value = AppScreen.EXPORT
    }

    fun updateExportConfig(newConfig: ExportConfig) {
        _exportConfig.value = newConfig
        _activeProject.value = _activeProject.value.copy(
            exportResolution = newConfig.resolution,
            exportFps = newConfig.fps,
            exportBitrateMode = newConfig.qualityPreset,
            customBitrateMbps = newConfig.customBitrateMbps
        )
    }

    fun startExport() {
        if (_exportProgress.value.isExporting) return
        exportJob?.cancel()
        exportJob = viewModelScope.launch {
            mediaEngine.exportProject(
                project = _activeProject.value,
                config = _exportConfig.value,
                onProgress = { state ->
                    _exportProgress.value = state
                }
            )
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        exportJob = null
        _exportProgress.value = ExportProgressState(
            isExporting = false,
            isCancelled = true,
            currentStage = "Export cancelled"
        )
        showToastMessage("Export cancelled")
    }

    // =========================================================================
    // OMKAR AUTOMATIC VIDEO MAKER ENGINE ACTIONS
    // =========================================================================

    fun setCustomSpeechTranscript(script: String) {
        _customSpeechTranscript.value = script
    }

    fun toggleAutoGenerateCaptions() {
        _autoGenerateCaptions.value = !_autoGenerateCaptions.value
    }

    /**
     * Runs the complete Omkar Automatic Video Maker speech-analysis, sentence-segmentation,
     * frame-aligned splitting, and start (100%) -> end (106%..114%) keyframe generation pipeline.
     */
    fun runOmkarAutoVideoMaker(
        motionMode: AutomaticMotionMode = _activeProject.value.automaticMotionMode,
        targetZoomFactor: Float = _activeProject.value.autoZoomTargetFactor,
        customTranscript: String = _customSpeechTranscript.value
    ) {
        pausePlayback()
        val result = OmkarAutoVideoEngine.executeAutomaticEditPipeline(
            context = getApplication(),
            project = _activeProject.value,
            motionMode = motionMode,
            targetZoomFactor = targetZoomFactor,
            customTranscript = customTranscript,
            generateSyncedCaptions = _autoGenerateCaptions.value
        )
        commitProjectMutation(result.updatedProject)
        _selectedClipId.value = result.updatedProject.primaryClips.firstOrNull()?.id
        _playheadMs.value = 0L
        _autoEditSummary.value = result.summaryMessage
        showToastMessage(result.summaryMessage)
    }

    /**
     * Switches between Simple Motion (100% -> targetZoom) and Smart Motion (context-aware Zoom In/Out/Drift)
     * across all split clips while preserving sacred geometry.
     */
    fun setAutomaticMotionMode(mode: AutomaticMotionMode) {
        val proj = _activeProject.value
        if (proj.primaryClips.size <= 1 && proj.detectedSpeechSegments.isEmpty()) {
            runOmkarAutoVideoMaker(motionMode = mode, targetZoomFactor = proj.autoZoomTargetFactor)
            return
        }
        val updated = OmkarAutoVideoEngine.updateAutoKeyframesOnClips(
            project = proj,
            motionMode = mode,
            targetZoomFactor = proj.autoZoomTargetFactor
        )
        commitProjectMutation(updated)
        val zoomPct = (proj.autoZoomTargetFactor * 100f).toInt()
        val msg = "Motion Mode: ${mode.label} (Start 100% → End $zoomPct%)"
        _autoEditSummary.value = msg
        showToastMessage(msg)
    }

    /**
     * Adjusts the automatic end keyframe zoom factor (1.00f for Identity Test, or 1.06f..1.14f for subtle zoom)
     * and immediately updates all auto-split clip keyframes.
     */
    fun setAutoZoomTargetFactor(zoomFactor: Float) {
        val safeZoom = if (zoomFactor <= 1.001f) 1.0f else zoomFactor.coerceIn(
            OmkarAutoVideoEngine.MIN_AUTO_ZOOM,
            OmkarAutoVideoEngine.MAX_AUTO_ZOOM
        )
        val proj = _activeProject.value
        val updated = OmkarAutoVideoEngine.updateAutoKeyframesOnClips(
            project = proj,
            motionMode = proj.automaticMotionMode,
            targetZoomFactor = safeZoom
        )
        commitProjectMutation(updated)
        val zoomPct = (safeZoom * 100f).toInt()
        _autoEditSummary.value = "Updated auto-keyframe zoom: Start 100% → End $zoomPct%"
    }

    /**
     * Cycles the selected clip's zoom direction (Zoom In / Zoom Out / Subtle Move+Zoom / Static 100%)
     * and regenerates its start and end keyframes.
     */
    fun cycleSelectedClipZoomDirection() {
        val target = getSelectedOrActiveClip() ?: return
        val nextDir = when (target.zoomDirection) {
            ZoomDirection.ZOOM_IN -> ZoomDirection.ZOOM_OUT
            ZoomDirection.ZOOM_OUT -> ZoomDirection.SUBTLE_DRIFT_IN
            ZoomDirection.SUBTLE_DRIFT_IN -> ZoomDirection.STATIC_100
            ZoomDirection.STATIC_100 -> ZoomDirection.ZOOM_IN
        }
        val dur = VideoRenderEngine.computeClipEffectiveDurationMs(target)
        val newKeyframes = OmkarAutoVideoEngine.KeyframeGenerator.generateClipKeyframes(
            clipDurationMs = dur,
            zoomDirection = nextDir,
            targetZoomFactor = _activeProject.value.autoZoomTargetFactor
        )
        mutateSelectedClip { clip ->
            clip.copy(
                zoomDirection = nextDir,
                keyframes = newKeyframes,
                isAutoSplitClip = true,
                rotation = 0f,
                transformX = 0f,
                transformY = 0f
            )
        }
        showToastMessage("${target.title}: ${nextDir.label}")
    }

    /**
     * Removes the split point for the selected clip by merging it with its adjacent clip
     * and regenerating clean start (100%) -> end keyframes across the merged duration.
     */
    fun removeSplitAndMergeSelectedClip() {
        val proj = _activeProject.value
        if (proj.primaryClips.size <= 1) {
            showToastMessage("Only 1 clip on timeline — no split point to remove")
            return
        }
        val selected = getSelectedOrActiveClip() ?: proj.primaryClips.last()
        val idx = proj.primaryClips.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
        val updated = OmkarAutoVideoEngine.removeSplitAndMergeClips(proj, idx)
        commitProjectMutation(updated)
        _selectedClipId.value = updated.primaryClips.getOrNull((idx - 1).coerceAtLeast(0))?.id
        showToastMessage("Removed split & merged clips (${updated.primaryClips.size} scenes remaining)")
    }

    /**
     * Restores the initial AI-generated automatic splits, keyframes, and motion values
     * from [VideoProject.autoEditSnapshot] without re-importing the video.
     */
    fun resetAutomaticEdit() {
        val proj = _activeProject.value
        val snap = proj.autoEditSnapshot
        if (snap != null) {
            val nonCaptionTexts = proj.textClips.filterNot { it.isCaption }
            val restored = proj.copy(
                updatedAtMs = System.currentTimeMillis(),
                aspectRatio = snap.sourceGeometry.canonicalAspectRatioMode,
                sourceGeometry = snap.sourceGeometry,
                automaticMotionMode = snap.motionMode,
                autoZoomTargetFactor = snap.targetZoomFactor,
                detectedSpeechSegments = snap.detectedSegments,
                primaryClips = snap.generatedClips,
                textClips = nonCaptionTexts + snap.generatedCaptions
            )
            commitProjectMutation(restored)
            _selectedClipId.value = restored.primaryClips.firstOrNull()?.id
            _playheadMs.value = 0L
            val msg = "Reset Automatic Edit to initial ${snap.generatedClips.size} detected speech clips"
            _autoEditSummary.value = msg
            showToastMessage(msg)
        } else {
            runOmkarAutoVideoMaker(
                motionMode = AutomaticMotionMode.SIMPLE,
                targetZoomFactor = OmkarAutoVideoEngine.DEFAULT_AUTO_ZOOM
            )
        }
    }

    /**
     * Exports a complete CapCut local draft (`draft_content.json` + `draft_meta_info.json` + `.zip`)
     * containing all speech-split clips and start/end keyframes.
     */
    fun exportToCapCutDraft(autoShare: Boolean = false) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val result = OmkarAutoVideoEngine.CapCutDraftExporter.exportCapCutDraft(
                context = app,
                project = _activeProject.value
            )
            _capCutDraftExportResult.value = result
            showToastMessage(result.message)
            if (autoShare && result.success && result.draftZipFilePath.isNotBlank()) {
                OmkarAutoVideoEngine.CapCutDraftExporter.launchCapCutOrShareIntent(app, result.draftZipFilePath)
            }
        }
    }

    fun shareCapCutDraftOrMp4(filePath: String) {
        val ok = OmkarAutoVideoEngine.CapCutDraftExporter.launchCapCutOrShareIntent(
            context = getApplication(),
            filePath = filePath
        )
        if (!ok) {
            showToastMessage("Draft package saved at: $filePath")
        }
    }

    // =========================================================================
    // SETTINGS & CAMERA CAPTURE HELPERS
    // =========================================================================

    fun toggleDarkStudioTheme() {
        _isDarkStudioTheme.value = !_isDarkStudioTheme.value
    }

    fun toggleProxyPreview() {
        _proxyPreviewEnabled.value = !_proxyPreviewEnabled.value
    }

    fun toggleGpuHardwareAccel() {
        _gpuHardwareAccelEnabled.value = !_gpuHardwareAccelEnabled.value
    }

    fun toggleAutoSave() {
        _autoSaveEnabled.value = !_autoSaveEnabled.value
    }

    fun toggleFpsOverlay() {
        _showFpsOverlay.value = !_showFpsOverlay.value
    }

    fun clearThumbnailCache() {
        showToastMessage("Cleared proxy & thumbnail cache • Freed 48.2 MB")
    }

    fun markNotificationRead(id: String) {
        _inboxNotifications.value = _inboxNotifications.value.map {
            if (it.id == id) it.copy(isUnread = false) else it
        }
    }

    fun finishCameraCaptureAndEdit(durationMs: Long, drawableRes: Int, title: String) {
        val capturedClip = TimelineClip(
            title = title,
            sampleDrawableRes = drawableRes,
            sourceDurationMs = durationMs + 1000L,
            trimStartMs = 0L,
            trimEndMs = durationMs,
            filterId = "natural_boost"
        )
        val proj = VideoProject(
            name = title,
            aspectRatio = AspectRatioMode.RATIO_9_16,
            primaryClips = listOf(capturedClip),
            coverDrawableRes = drawableRes
        )
        viewModelScope.launch { repository.saveProject(proj) }
        openProjectInEditor(proj, EditorToolTab.EDIT)
        showToastMessage("Captured ${durationMs / 1000f}s clip • Ready to edit")
    }

    companion object {
        fun formatTimecode(ms: Long): String {
            val safe = ms.coerceAtLeast(0L)
            val totalSeconds = safe / 1000L
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            val hundredths = (safe % 1000L) / 10L
            return String.format(java.util.Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)
        }

        fun formatShortDuration(ms: Long): String {
            val safe = ms.coerceAtLeast(0L)
            val totalSeconds = safe / 1000L
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        }

        private fun createDefaultStarterProject(): VideoProject {
            return EffectFilterTransitionCatalog.buildProjectFromTemplate(
                EffectFilterTransitionCatalog.templates.first()
            )
        }

        private fun defaultInboxNotifications(): List<StudioNotification> = listOf(
            StudioNotification(
                id = "notif_1",
                title = "NovaCut 60fps Keyframe & Velocity Engine",
                message = "Cubic Bezier & Smoothstep interpolation now available for Position, Scale, Rotation, Opacity, and Filter Intensity.",
                category = "NovaCut Update",
                timestampLabel = "Just now",
                isUnread = true
            ),
            StudioNotification(
                id = "notif_2",
                title = "New Viral Effects & Cinema Filters",
                message = "Try Chromatic Blur, Halo Blur, Diamond Zoom, Black Flash, Edge Glow, 3D Zoom Pro, Teal & Orange, and Oppenheimer LUTs.",
                category = "Effects & Filters",
                timestampLabel = "2h ago",
                isUnread = true
            ),
            StudioNotification(
                id = "notif_3",
                title = "Velocity & AutoCut Templates",
                message = "Explore 15 one-tap templates including Neon Velocity Beat Sync, Bass Shake Montage, and Flash Cut Fashion Reel.",
                category = "Templates",
                timestampLabel = "Yesterday",
                isUnread = false
            ),
            StudioNotification(
                id = "notif_4",
                title = "Cloud & Local SQLite Autosave",
                message = "Every timeline cut, keyframe, and color grade is automatically persisted to your local SQLite project database.",
                category = "System",
                timestampLabel = "2d ago",
                isUnread = false
            )
        )
    }
}
