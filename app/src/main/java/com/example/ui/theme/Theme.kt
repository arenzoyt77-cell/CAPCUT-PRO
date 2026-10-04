package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NovaCutDarkScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF001F26),
    primaryContainer = Color(0xFF003844),
    onPrimaryContainer = Color(0xFFB8F4FF),
    secondary = VioletAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF281B52),
    onSecondaryContainer = Color(0xFFE5DEFF),
    tertiary = KeyframeCrimson,
    onTertiary = Color.White,
    background = StudioBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioCard,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = Color(0xFF1F2637)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = NovaCutDarkScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
