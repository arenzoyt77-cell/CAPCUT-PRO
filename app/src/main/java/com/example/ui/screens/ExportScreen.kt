package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.VideoRenderEngine
import com.example.ui.theme.AudioEmerald
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.KeyframeCrimson
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NovaCutViewModel

@Composable
fun ExportScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }

    val project by viewModel.activeProject.collectAsStateWithLifecycle()
    val config by viewModel.exportConfig.collectAsStateWithLifecycle()
    val progress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val totalDurationMs = VideoRenderEngine.computeProjectTotalDurationMs(project)
    val coverRes = project.primaryClips.firstOrNull()?.sampleDrawableRes ?: project.coverDrawableRes

    val bitrateMbps = when (config.qualityPreset) {
        "Lower" -> 8.5f
        "Standard" -> 16.0f
        "High" -> 28.0f
        else -> config.customBitrateMbps
    }
    val resScale = when (config.resolution) {
        "480p" -> 0.45f
        "720p" -> 0.7f
        "1440p" -> 1.6f
        "2160p" -> 2.8f
        else -> 1.0f
    }
    val estimatedSizeMb = ((totalDurationMs / 1000f) * (bitrateMbps / 8f) * resScale).coerceAtLeast(1.8f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier.testTag("export_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to editor", tint = TextPrimary)
            }
            Text("Export Studio Master", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.width(40.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Project Summary Card
            item {
                Surface(
                    color = StudioCard,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = coverRes),
                            contentDescription = project.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 96.dp, height = 72.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(project.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Duration: ${NovaCutViewModel.formatTimecode(totalDurationMs)} • Aspect: ${project.aspectRatio.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "Estimated Size: %.1f MB (%s)", estimatedSizeMb, config.codec),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent
                            )
                        }
                    }
                }
            }

            if (!progress.isExporting && !progress.isCompleted) {
                // 1. Resolution Selection (480p, 720p, 1080p, 1440p, 2160p)
                item {
                    ExportParameterCard(title = "Resolution") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("480p", "720p", "1080p", "1440p", "2160p")) { res ->
                                FilterChip(
                                    selected = config.resolution == res,
                                    onClick = { viewModel.updateExportConfig(config.copy(resolution = res)) },
                                    modifier = Modifier.testTag("export_res_$res"),
                                    label = {
                                        Text(if (res == "2160p") "2160p (4K)" else res)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent,
                                        selectedLabelColor = StudioBg,
                                        containerColor = StudioElevated,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Frame Rate Selection (24, 25, 30, 50, 60)
                item {
                    ExportParameterCard(title = "Frame Rate (FPS)") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(24, 25, 30, 50, 60)) { fps ->
                                FilterChip(
                                    selected = config.fps == fps,
                                    onClick = { viewModel.updateExportConfig(config.copy(fps = fps)) },
                                    modifier = Modifier.testTag("export_fps_$fps"),
                                    label = { Text("${fps}fps") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent,
                                        selectedLabelColor = StudioBg,
                                        containerColor = StudioElevated,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Quality / Bitrate Selection (Lower, Standard, High, Custom)
                item {
                    ExportParameterCard(title = "Quality & Bitrate (${String.format(java.util.Locale.US, "%.1f Mbps", bitrateMbps)})") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("Lower", "Standard", "High", "Custom")) { preset ->
                                    FilterChip(
                                        selected = config.qualityPreset == preset,
                                        onClick = { viewModel.updateExportConfig(config.copy(qualityPreset = preset)) },
                                        modifier = Modifier.testTag("export_quality_${preset.lowercase()}"),
                                        label = { Text(preset) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyanAccent,
                                            selectedLabelColor = StudioBg,
                                            containerColor = StudioElevated,
                                            labelColor = TextPrimary
                                        )
                                    )
                                }
                            }
                            if (config.qualityPreset == "Custom") {
                                Slider(
                                    value = config.customBitrateMbps,
                                    onValueChange = { viewModel.updateExportConfig(config.copy(customBitrateMbps = it)) },
                                    valueRange = 4f..65f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = CyanAccent,
                                        activeTrackColor = CyanAccent
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. Codec Selector
                item {
                    ExportParameterCard(title = "Hardware Encoder / Format") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("H.264 / AVC", "H.265 / HEVC").forEach { codec ->
                                FilterChip(
                                    selected = config.codec == codec,
                                    onClick = { viewModel.updateExportConfig(config.copy(codec = codec)) },
                                    label = { Text(codec) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent,
                                        selectedLabelColor = StudioBg,
                                        containerColor = StudioElevated,
                                        labelColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Start Export CTA
                item {
                    Button(
                        onClick = { viewModel.startExport() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = StudioBg
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_export_button")
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Export (${config.resolution} • ${config.fps}fps)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Active Export Progress Card
            if (progress.isExporting) {
                item {
                    Surface(
                        color = StudioCard,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, CyanAccent, RoundedCornerShape(18.dp))
                            .testTag("export_progress_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${progress.progressPercent}%",
                                style = MaterialTheme.typography.displayLarge,
                                color = CyanAccent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = progress.currentStage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rendered ${progress.renderedFrames} / ${progress.totalFrames} frames • Keep screen open",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                progress = { (progress.progressPercent / 100f).coerceIn(0f, 1f) },
                                color = CyanAccent,
                                trackColor = StudioElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            OutlinedButton(
                                onClick = { viewModel.cancelExport() },
                                modifier = Modifier.testTag("cancel_export_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = KeyframeCrimson)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel Export", color = KeyframeCrimson)
                            }
                        }
                    }
                }
            }

            // Export Completed Success State
            if (progress.isCompleted) {
                item {
                    Surface(
                        color = StudioCard,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, AudioEmerald, RoundedCornerShape(18.dp))
                            .testTag("export_success_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(AudioEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Export succeeded",
                                    tint = AudioEmerald,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Master Exported (100%)",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = progress.currentStage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AudioEmerald
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Output: ${progress.outputFilePath.substringAfterLast("/")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_SUBJECT, project.name)
                                            putExtra(Intent.EXTRA_TEXT, "Exported with NovaCut (${config.resolution} ${config.fps}fps)")
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanAccent,
                                        contentColor = StudioBg
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share")
                                }

                                OutlinedButton(
                                    onClick = { viewModel.navigateBack() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Back to Editor", color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Error Recovery Banner
            progress.errorMessage?.let { err ->
                item {
                    Surface(
                        color = KeyframeCrimson.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, KeyframeCrimson, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = KeyframeCrimson)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(err, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            Button(
                                onClick = { viewModel.startExport() },
                                colors = ButtonDefaults.buttonColors(containerColor = KeyframeCrimson)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportParameterCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = StudioCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
