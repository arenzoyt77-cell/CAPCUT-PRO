package com.example

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.engine.OmkarAutoVideoEngine
import com.example.engine.VideoRenderEngine
import com.example.model.AspectRatioMode
import com.example.model.AutomaticMotionMode
import com.example.model.KeyframeProperty
import com.example.model.TimelineClip
import com.example.model.VideoProject
import com.example.viewmodel.AppScreen
import com.example.viewmodel.HomeBottomTab
import com.example.viewmodel.MediaPickerPurpose
import com.example.viewmodel.NovaCutViewModel
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xxhdpi")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appName_matchesNovaCut() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NovaCut", appName)
    }

    @Test
    fun mainActivity_launchesDarkNovaCutHomeScreenAndVerifiesAllSectionsAndNavigation() {
        // 1. Verify Top Header ("NovaCut", "PRO", "60fps Multi-Track NLE Engine", 4 action buttons)
        composeTestRule.onNodeWithText("NovaCut").assertIsDisplayed()
        composeTestRule.onNodeWithText("PRO").assertIsDisplayed()
        composeTestRule.onNodeWithText("60fps Multi-Track NLE Engine").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_import_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_search_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_cloud_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_profile_button").assertIsDisplayed()

        // 2. Verify Feature Badge & Main Create Area
        composeTestRule.onNodeWithText("KEYFRAMES • CURVES • 4K HDR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Autosave Active").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cta_new_video").assertIsDisplayed()
        composeTestRule.onNodeWithText("CREATE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Multi-track timeline & effects").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cta_edit_photo").assertIsDisplayed()
        composeTestRule.onNodeWithText("RAW & HSL grade").assertIsDisplayed()

        // 2b. Verify First-Class Independent "Auto Editing" Feature Card on Home Screen
        composeTestRule.onNodeWithTag("cta_auto_editing").assertIsDisplayed()
        composeTestRule.onNodeWithText("⚡ Auto Editing").assertIsDisplayed()
        composeTestRule.onNodeWithText("AI-powered automatic video editing").assertIsDisplayed()
        composeTestRule.onNodeWithText("AUTO EDITING").assertIsDisplayed()

        // 3. Verify Studio Toolkit (3x3 Grid)
        composeTestRule.onNodeWithText("Studio Toolkit").assertIsDisplayed()
        composeTestRule.onNodeWithText("AutoCut").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retouch").assertIsDisplayed()
        composeTestRule.onNodeWithText("Photo Tools").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shoot and Record").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auto Enhance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cover Maker").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auto Captions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remove Background").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cloud / Space").assertIsDisplayed()

        // 4. Verify Recent Projects Carousel
        composeTestRule.onNodeWithTag("recent_projects_header").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recent projects (", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Bass Shake Montage").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manage All →").assertIsDisplayed()

        // 5. Verify Continue Editing Section
        composeTestRule.onNodeWithText("Continue editing").assertIsDisplayed()
        composeTestRule.onNodeWithText("LATEST DRAFT").assertIsDisplayed()
        composeTestRule.onNodeWithTag("continue_editing_featured_card").assertIsDisplayed()

        // 6. Verify Fixed Bottom Navigation (Edit, Template, AI Lab, Projects, Inbox, Me)
        composeTestRule.onNodeWithTag("nav_tab_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_templates").assertIsDisplayed()
        composeTestRule.onNodeWithText("Template").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_ai_lab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_projects").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_inbox").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_me").assertIsDisplayed()

        // 7. Test Bottom Navigation switching
        composeTestRule.onNodeWithTag("nav_tab_templates").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Neon Velocity Beat Sync").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_edit").performClick()
        composeTestRule.waitForIdle()

        // 8. Navigate: HOME -> Auto Editing -> OMKAR AUTOMATIC VIDEO MAKER -> Back -> HOME
        composeTestRule.onNodeWithTag("cta_auto_editing").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("omkar_header_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("omkar_select_video_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("omkar_auto_maker_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("editor_preview_canvas").assertIsDisplayed()

        // Back button returns from OMKAR AUTOMATIC VIDEO MAKER -> Home Screen
        composeTestRule.onNodeWithTag("editor_close_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("cta_auto_editing").assertIsDisplayed()

        // 9. Navigate: HOME -> New Video -> Media Picker -> Editor
        composeTestRule.onNodeWithTag("cta_new_video").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Select Media").assertIsDisplayed()
        composeTestRule.onNodeWithTag("picker_add_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("picker_add_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("editor_preview_canvas").assertIsDisplayed()
        composeTestRule.onNodeWithTag("editor_export_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("editor_play_pause_button").assertIsDisplayed()
    }

    @Test
    fun viewModel_workflow_homeToPickerToEditorToExport() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = NovaCutViewModel(app)

        assertEquals(AppScreen.HOME, vm.currentScreen.value)
        assertEquals(HomeBottomTab.EDIT, vm.homeTab.value)

        // Test Edit Photo flow
        vm.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_PHOTO)
        assertEquals(AppScreen.MEDIA_PICKER, vm.currentScreen.value)
        assertEquals(MediaPickerPurpose.NEW_PROJECT_PHOTO, vm.pickerPurpose.value)
        vm.navigateBack()
        assertEquals(AppScreen.HOME, vm.currentScreen.value)

        // Open Media Picker for New Video
        vm.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_VIDEO)
        assertEquals(AppScreen.MEDIA_PICKER, vm.currentScreen.value)

        // Confirm media selection -> enters Editor
        vm.confirmMediaPickerSelection()
        assertEquals(AppScreen.EDITOR, vm.currentScreen.value)
        val initialClips = vm.activeProject.value.primaryClips.size
        assertTrue(initialClips >= 1)

        // Duplicate clip & split at playhead
        vm.duplicateSelectedClip()
        assertEquals(initialClips + 1, vm.activeProject.value.primaryClips.size)

        vm.seekToMs(1500L, enableSnap = false)
        vm.splitClipAtPlayhead()
        assertEquals(initialClips + 2, vm.activeProject.value.primaryClips.size)

        // Add keyframe at playhead
        vm.toggleKeyframeAtPlayhead(KeyframeProperty.SCALE)
        val clip = vm.getSelectedOrActiveClip()
        assertNotNull(clip)
        assertTrue(clip!!.keyframes.any { it.property == KeyframeProperty.SCALE })

        // Apply filter & video effect
        vm.applyFilter("teal_orange", 0.9f, applyToAllClips = true)
        assertEquals("teal_orange", vm.activeProject.value.globalFilterId)
        vm.addVideoEffectToTimeline("chromatic_aberration")
        assertTrue(vm.activeProject.value.effectItems.isNotEmpty())

        // Generate auto captions
        vm.generateAutoCaptionsFromTimeline()
        assertTrue(vm.activeProject.value.textClips.any { it.isCaption })

        // Navigate to Export screen
        vm.openExportScreen()
        assertEquals(AppScreen.EXPORT, vm.currentScreen.value)
    }

    @Test
    fun testA_identityTest_splitAt100PercentMatchesOriginalGeometryAndFraming() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sourceGeom = OmkarAutoVideoEngine.VideoMetadataReader.createCanonicalGeometry(
            rawWidth = 720,
            rawHeight = 1280,
            rotationDegrees = 0,
            durationMs = 9000L,
            frameRate = 30f
        )
        val originalProject = VideoProject(
            name = "Identity Test Source",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            sourceGeometry = sourceGeom,
            primaryClips = listOf(
                TimelineClip(
                    title = "Original Clip",
                    sourceDurationMs = 9000L,
                    trimStartMs = 0L,
                    trimEndMs = 9000L,
                    sourceGeometry = sourceGeom
                )
            )
        )

        // Execute Automatic Edit Pipeline with 100% Identity Zoom (targetZoomFactor = 1.0f)
        val autoResult = OmkarAutoVideoEngine.executeAutomaticEditPipeline(
            context = context,
            project = originalProject,
            motionMode = AutomaticMotionMode.SIMPLE,
            targetZoomFactor = 1.0f,
            generateSyncedCaptions = false
        )
        val splitProject = autoResult.updatedProject

        // 1. Geometry must match original 720x1280 9:16
        assertEquals(720, splitProject.sourceGeometry.displayWidth)
        assertEquals(1280, splitProject.sourceGeometry.displayHeight)
        assertEquals(AspectRatioMode.RATIO_9_16, splitProject.aspectRatio)

        // 2. Total duration of split clips must equal original 9000ms with zero gaps
        val totalSplitDuration = splitProject.primaryClips.sumOf { it.trimEndMs - it.trimStartMs }
        assertEquals(9000L, totalSplitDuration)

        // 3. Every split clip must have 100% uniform scale, 0 rotation, 0 skew, and (0, 0) position at all times
        for (clip in splitProject.primaryClips) {
            val dur = clip.trimEndMs - clip.trimStartMs
            for (t in listOf(0L, dur / 2L, dur)) {
                val tr = OmkarAutoVideoEngine.TransformEngine.evaluateClipTransform(clip, t)
                assertEquals(1.0f, tr.uniformScale, 0.0001f)
                assertEquals(1.0f, tr.scaleX, 0.0001f)
                assertEquals(1.0f, tr.scaleY, 0.0001f)
                assertEquals(0.0f, tr.posX, 0.0001f)
                assertEquals(0.0f, tr.posY, 0.0001f)
                assertEquals(0.0f, tr.rotationDegrees, 0.0001f)
                assertTrue(tr.isGeometrySacredCompliant)
            }
        }

        // 4. Pixel-level identity check: rendered frame from original unsplit project == rendered frame from split project
        val testSourceBmp = Bitmap.createBitmap(360, 640, Bitmap.Config.ARGB_8888).apply {
            eraseColor(0xFF1E88E5.toInt())
        }
        val origOut = Bitmap.createBitmap(360, 640, Bitmap.Config.ARGB_8888)
        val splitOut = Bitmap.createBitmap(360, 640, Bitmap.Config.ARGB_8888)

        VideoRenderEngine.renderCompositeFrame(
            canvas = Canvas(origOut),
            width = 360f,
            height = 640f,
            project = originalProject,
            playheadMs = 4200L,
            bitmapLookup = { testSourceBmp }
        )
        VideoRenderEngine.renderCompositeFrame(
            canvas = Canvas(splitOut),
            width = 360f,
            height = 640f,
            project = splitProject,
            playheadMs = 4200L,
            bitmapLookup = { testSourceBmp }
        )
        assertEquals(origOut.getPixel(180, 320), splitOut.getPixel(180, 320))
        assertEquals(origOut.getPixel(10, 10), splitOut.getPixel(10, 10))
        assertEquals(origOut.getPixel(350, 630), splitOut.getPixel(350, 630))
    }

    @Test
    fun testB_splitOnlyTest_speechSentenceSegmentationAndFrameAlignment() {
        val durationMs = 11500L
        val envelope = OmkarAutoVideoEngine.AudioExtractor.buildDeterministicSpeechEnvelope(durationMs)
        val script = "First spoken sentence finishes cleanly here. " +
            "Second spoken phrase continues with natural cadence and meaning. " +
            "Third sentence concludes the creator video smoothly."
        val words = OmkarAutoVideoEngine.WordTimestampAnalyzer.analyzeWords(envelope, script)
        assertTrue(words.isNotEmpty())

        val segments = OmkarAutoVideoEngine.SemanticSentenceSegmenter.segmentWordsIntoSemanticSentences(words, durationMs)
        assertTrue(segments.size >= 2)

        // Verify no clip is shorter than minimum normal clip duration (1.2s)
        val splitPoints = OmkarAutoVideoEngine.AutoSplitEngine.computeSplitPointsMs(segments, durationMs, 30f)
        val bounds = listOf(0L) + splitPoints + listOf(durationMs)
        for (i in 0 until bounds.lastIndex) {
            val clipDur = bounds[i + 1] - bounds[i]
            assertTrue("Clip duration $clipDur must be >= 1200ms", clipDur >= OmkarAutoVideoEngine.MIN_CLIP_DURATION_MS)
        }
    }

    @Test
    fun testC_keyframeZoomTest_start100ToEnd110AndNextClipResetsCleanly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val project = VideoProject(
            name = "Keyframe Zoom Test",
            primaryClips = listOf(
                TimelineClip(
                    title = "Source",
                    sourceDurationMs = 9600L,
                    trimStartMs = 0L,
                    trimEndMs = 9600L
                )
            )
        )

        val result = OmkarAutoVideoEngine.executeAutomaticEditPipeline(
            context = context,
            project = project,
            motionMode = AutomaticMotionMode.SIMPLE,
            targetZoomFactor = 1.10f
        )
        val clips = result.updatedProject.primaryClips
        assertTrue("Expected at least 2 split clips", clips.size >= 2)

        clips.forEach { clip ->
            val dur = VideoRenderEngine.computeClipEffectiveDurationMs(clip)
            val startTr = OmkarAutoVideoEngine.TransformEngine.evaluateClipTransform(clip, 0L)
            val midTr = OmkarAutoVideoEngine.TransformEngine.evaluateClipTransform(clip, dur / 2L)
            val endTr = OmkarAutoVideoEngine.TransformEngine.evaluateClipTransform(clip, dur)

            // Start keyframe at 0L MUST be 100% (1.00f)
            assertEquals(1.00f, startTr.uniformScale, 0.001f)
            // End keyframe at clip end MUST be 110% (1.10f)
            assertEquals(1.10f, endTr.uniformScale, 0.001f)
            // Midpoint must be smoothly interpolated (~1.05f)
            assertTrue(midTr.uniformScale in 1.03f..1.07f)
            // Sacred geometry compliance
            assertTrue(OmkarAutoVideoEngine.CropProtectionEngine.isTransformSafe(startTr))
            assertTrue(OmkarAutoVideoEngine.CropProtectionEngine.isTransformSafe(midTr))
            assertTrue(OmkarAutoVideoEngine.CropProtectionEngine.isTransformSafe(endTr))
        }
    }

    @Test
    fun testD_orientationTest_portraitLandscapeRotatedAnd4KPreserved() {
        // 1. Portrait 9:16 (720x1280, rotation 0)
        val portrait = OmkarAutoVideoEngine.VideoMetadataReader.createCanonicalGeometry(720, 1280, 0)
        assertEquals(720, portrait.displayWidth)
        assertEquals(1280, portrait.displayHeight)
        assertTrue(portrait.isPortrait)
        assertEquals(AspectRatioMode.RATIO_9_16, portrait.canonicalAspectRatioMode)

        // 2. Landscape 16:9 (1920x1080, rotation 0)
        val landscape = OmkarAutoVideoEngine.VideoMetadataReader.createCanonicalGeometry(1920, 1080, 0)
        assertEquals(1920, landscape.displayWidth)
        assertEquals(1080, landscape.displayHeight)
        assertFalse(landscape.isPortrait)
        assertEquals(AspectRatioMode.RATIO_16_9, landscape.canonicalAspectRatioMode)

        // 3. Rotated metadata video (raw 1920x1080 with rotation = 90 -> upright 1080x1920 portrait ONCE)
        val rotated90 = OmkarAutoVideoEngine.VideoMetadataReader.createCanonicalGeometry(1920, 1080, 90)
        assertEquals(1080, rotated90.displayWidth)
        assertEquals(1920, rotated90.displayHeight)
        assertTrue(rotated90.isPortrait)
        assertEquals(AspectRatioMode.RATIO_9_16, rotated90.canonicalAspectRatioMode)

        // 4. 4K UHD video (3840x2160)
        val uhd4k = OmkarAutoVideoEngine.VideoMetadataReader.createCanonicalGeometry(3840, 2160, 0)
        assertEquals(3840, uhd4k.displayWidth)
        assertEquals(2160, uhd4k.displayHeight)
        assertEquals(AspectRatioMode.RATIO_16_9, uhd4k.canonicalAspectRatioMode)
    }

    @Test
    fun testE_previewExportParityAndCapCutDraftExport() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vm = NovaCutViewModel(context as Application)

        // Run Omkar Automatic Video Maker
        vm.runOmkarAutoVideoMaker(
            motionMode = AutomaticMotionMode.SIMPLE,
            targetZoomFactor = 1.10f
        )
        val project = vm.activeProject.value
        assertTrue(project.primaryClips.size >= 2)
        assertTrue(project.primaryClips.all { it.isAutoSplitClip })

        // Export to CapCut Draft and verify generated JSON files and ZIP archive
        val exportResult = OmkarAutoVideoEngine.CapCutDraftExporter.exportCapCutDraft(context, project)
        assertTrue(exportResult.success)
        assertEquals(project.primaryClips.size, exportResult.exportedSegmentsCount)
        assertTrue(File(exportResult.draftDirectoryPath, "draft_content.json").exists())
        assertTrue(File(exportResult.draftDirectoryPath, "draft_meta_info.json").exists())
        assertTrue(File(exportResult.draftZipFilePath).exists())

        val jsonText = File(exportResult.draftDirectoryPath, "draft_content.json").readText()
        val rootJson = JSONObject(jsonText)
        val tracks = rootJson.getJSONArray("tracks")
        assertTrue(tracks.length() >= 1)
        val videoTrack = tracks.getJSONObject(0)
        val segments = videoTrack.getJSONArray("segments")
        assertEquals(project.primaryClips.size, segments.length())

        // Verify Smart Motion switching, Split Merge, and Reset Automatic Edit
        vm.setAutomaticMotionMode(AutomaticMotionMode.SMART)
        assertEquals(AutomaticMotionMode.SMART, vm.activeProject.value.automaticMotionMode)

        val countBeforeMerge = vm.activeProject.value.primaryClips.size
        vm.removeSplitAndMergeSelectedClip()
        assertEquals(countBeforeMerge - 1, vm.activeProject.value.primaryClips.size)

        vm.resetAutomaticEdit()
        assertEquals(countBeforeMerge, vm.activeProject.value.primaryClips.size)
        assertEquals(AutomaticMotionMode.SIMPLE, vm.activeProject.value.automaticMotionMode)
    }
}
