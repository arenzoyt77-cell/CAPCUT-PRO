package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.model.KeyframeProperty
import com.example.viewmodel.AppScreen
import com.example.viewmodel.HomeBottomTab
import com.example.viewmodel.MediaPickerPurpose
import com.example.viewmodel.NovaCutViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
        composeTestRule.onNodeWithText("Recent projects (6)").assertIsDisplayed()
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

        // 8. Navigate: HOME -> New Video -> Media Picker -> Editor
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
}
