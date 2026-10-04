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
    fun appName_matchesCapCut() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CapCut", appName)
    }

    @Test
    fun mainActivity_launchesHomeScreenAndNavigatesToMediaPickerAndEditor() {
        // 1. Verify MainActivity launches and displays the CapCut dark professional Home screen
        composeTestRule.onNodeWithText("CapCut").assertIsDisplayed()
        composeTestRule.onNodeWithText("PRO").assertIsDisplayed()
        composeTestRule.onNodeWithText("60fps Multi-Track NLE Engine").assertIsDisplayed()
        composeTestRule.onNodeWithText("KEYFRAMES • CURVES • 4K HDR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Autosave Active").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cta_new_video").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cta_edit_photo").assertIsDisplayed()
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
        composeTestRule.onNodeWithTag("recent_projects_header").assertIsDisplayed()
        composeTestRule.onNodeWithText("Project Oct 03 20:11").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bass Shake Montage").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manage All →").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_templates").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_ai_lab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_projects").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_inbox").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_me").assertIsDisplayed()

        // 2. Navigate: HOME -> New Video -> Media Picker
        composeTestRule.onNodeWithTag("cta_new_video").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Select Media").assertIsDisplayed()
        composeTestRule.onNodeWithTag("picker_add_button").assertIsDisplayed()

        // 3. Navigate: Media Picker -> Editor
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

        // Open Media Picker
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

        // Apply CapCut filter & video effect
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
