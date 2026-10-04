package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.KeyframeProperty
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MediaPickerPurpose
import com.example.viewmodel.NovaCutViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun appName_matchesNovaCut() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NovaCut", appName)
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
        val clip = vm.getSelectedOrActiveClip()!!
        assertTrue(clip.keyframes.any { it.property == KeyframeProperty.SCALE })

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
