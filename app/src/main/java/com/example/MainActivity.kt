package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.HomeAndTabsScreen
import com.example.ui.screens.MainEditorScreen
import com.example.ui.screens.MediaPickerScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.NovaCutViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val novaViewModel: NovaCutViewModel = viewModel()
            val isDarkStudio by novaViewModel.isDarkStudioTheme.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkStudio) {
                NovaCutAppRoot(viewModel = novaViewModel)
            }
        }
    }
}

@Composable
fun NovaCutAppRoot(viewModel: NovaCutViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
    ) {
        when (currentScreen) {
            AppScreen.HOME -> HomeAndTabsScreen(viewModel = viewModel)
            AppScreen.MEDIA_PICKER -> MediaPickerScreen(viewModel = viewModel)
            AppScreen.EDITOR -> MainEditorScreen(viewModel = viewModel)
            AppScreen.EXPORT -> ExportScreen(viewModel = viewModel)
            AppScreen.CAMERA_CAPTURE -> CameraCaptureScreen(viewModel = viewModel)
        }

        AnimatedVisibility(
            visible = bannerMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                color = StudioElevated.copy(alpha = 0.95f),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 10.dp,
                modifier = Modifier.border(1.dp, CyanAccent.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = bannerMessage.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
