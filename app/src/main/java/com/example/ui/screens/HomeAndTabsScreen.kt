package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.engine.EffectFilterTransitionCatalog
import com.example.engine.VideoRenderEngine
import com.example.model.VideoProject
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.KeyframeCrimson
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.EditorToolTab
import com.example.viewmodel.HomeBottomTab
import com.example.viewmodel.MediaPickerPurpose
import com.example.viewmodel.NovaCutViewModel

@Composable
fun HomeAndTabsScreen(viewModel: NovaCutViewModel) {
    val activeTab by viewModel.homeTab.collectAsStateWithLifecycle()
    val projects by viewModel.savedProjects.collectAsStateWithLifecycle()
    val showSearch by viewModel.showGlobalSearch.collectAsStateWithLifecycle()
    var projectToRename by remember { mutableStateOf<VideoProject?>(null) }
    var renameInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomeTopBar(
                onSearchClick = { viewModel.setGlobalSearchVisible(true) },
                onQuickImportClick = { viewModel.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_VIDEO) },
                onCloudClick = { viewModel.launchShortcutTool("cloud_space") },
                onProfileClick = { viewModel.selectHomeTab(HomeBottomTab.ME) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                HomeBottomTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectHomeTab(tab) },
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    HomeBottomTab.EDIT -> Icons.Default.MovieCreation
                                    HomeBottomTab.TEMPLATES -> Icons.Default.VideoLibrary
                                    HomeBottomTab.AI_LAB -> Icons.Default.AutoAwesome
                                    HomeBottomTab.PROJECTS -> Icons.Default.FolderSpecial
                                    HomeBottomTab.INBOX -> Icons.Default.MarkEmailUnread
                                    HomeBottomTab.ME -> Icons.Default.Person
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudioBg,
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                HomeBottomTab.EDIT -> HomeEditDashboard(
                    projects = projects,
                    onNewVideo = { viewModel.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_VIDEO) },
                    onEditPhoto = { viewModel.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_PHOTO) },
                    onLaunchTool = { viewModel.launchShortcutTool(it) },
                    onOpenProject = { viewModel.openProjectInEditor(it) },
                    onRenameProject = {
                        projectToRename = it
                        renameInput = it.name
                    },
                    onDuplicateProject = { viewModel.duplicateProject(it) },
                    onDeleteProject = { viewModel.deleteProject(it.id) },
                    onViewAllProjects = { viewModel.selectHomeTab(HomeBottomTab.PROJECTS) }
                )
                HomeBottomTab.TEMPLATES -> TemplatesBrowserTab(
                    onUseTemplate = { viewModel.useTemplate(it) }
                )
                HomeBottomTab.AI_LAB -> AiLabStudioTab(
                    onRunAiTool = { viewModel.runAiLabTool(it) }
                )
                HomeBottomTab.PROJECTS -> ProjectsManagerTab(
                    projects = projects,
                    onNewProject = { viewModel.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_VIDEO) },
                    onOpenProject = { viewModel.openProjectInEditor(it) },
                    onRenameProject = {
                        projectToRename = it
                        renameInput = it.name
                    },
                    onDuplicateProject = { viewModel.duplicateProject(it) },
                    onDeleteProject = { viewModel.deleteProject(it.id) }
                )
                HomeBottomTab.INBOX -> InboxTab(viewModel = viewModel)
                HomeBottomTab.ME -> MeSettingsTab(viewModel = viewModel)
            }

            if (showSearch) {
                GlobalSearchDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.setGlobalSearchVisible(false) }
                )
            }

            projectToRename?.let { target ->
                AlertDialog(
                    onDismissRequest = { projectToRename = null },
                    containerColor = StudioElevated,
                    title = { Text("Rename Project", color = TextPrimary) },
                    text = {
                        OutlinedTextField(
                            value = renameInput,
                            onValueChange = { renameInput = it },
                            singleLine = true,
                            label = { Text("Project Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.renameProject(target, renameInput)
                                projectToRename = null
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { projectToRename = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onSearchClick: () -> Unit,
    onQuickImportClick: () -> Unit,
    onCloudClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(CyanAccent, VioletAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MovieCreation,
                    contentDescription = "NovaCut Studio",
                    tint = StudioBg,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NovaCut",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = CyanAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "60fps Multi-Track NLE Engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onQuickImportClick,
                modifier = Modifier.testTag("home_import_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Import media",
                    tint = TextPrimary
                )
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.testTag("home_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Global search",
                    tint = TextPrimary
                )
            }
            IconButton(
                onClick = onCloudClick,
                modifier = Modifier.testTag("home_cloud_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Cloud Space",
                    tint = CyanAccent
                )
            }
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("home_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Workspace settings",
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun HomeEditDashboard(
    projects: List<VideoProject>,
    onNewVideo: () -> Unit,
    onEditPhoto: () -> Unit,
    onLaunchTool: (String) -> Unit,
    onOpenProject: (VideoProject) -> Unit,
    onRenameProject: (VideoProject) -> Unit,
    onDuplicateProject: (VideoProject) -> Unit,
    onDeleteProject: (VideoProject) -> Unit,
    onViewAllProjects: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Hero Create Area ("New video" & "Edit photo")
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StudioBorder, RoundedCornerShape(22.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_studio),
                            contentDescription = "Studio hero backdrop",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(206.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(206.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            StudioBg.copy(alpha = 0.45f),
                                            StudioBg.copy(alpha = 0.88f),
                                            StudioBg
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = StudioElevated.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "KEYFRAMES • CURVES • 4K HDR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = "Autosave Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Main CTA: New video
                                Box(
                                    modifier = Modifier
                                        .weight(1.65f)
                                        .height(112.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(CyanAccent, Color(0xFF0091EA), VioletAccent)
                                            )
                                        )
                                        .clickable(onClick = onNewVideo)
                                        .testTag("cta_new_video")
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            color = StudioBg.copy(alpha = 0.24f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "New video",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "New video",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = StudioBg,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Multi-track timeline & effects",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = StudioBg.copy(alpha = 0.82f)
                                            )
                                        }
                                    }
                                }

                                // Secondary CTA: Edit photo
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(112.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(StudioElevated.copy(alpha = 0.92f))
                                        .border(1.dp, StudioBorder, RoundedCornerShape(18.dp))
                                        .clickable(onClick = onEditPhoto)
                                        .testTag("cta_edit_photo")
                                        .padding(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            color = VioletAccent.copy(alpha = 0.22f),
                                            shape = CircleShape,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = "Edit photo",
                                                    tint = Color(0xFFD1C4E9),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "Edit photo",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "RAW & HSL grade",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Tool Shortcuts Grid (All 9 functional!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Studio Toolkit",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val shortcuts = listOf(
                    ShortcutToolItem("autocut", "AutoCut", Icons.Default.AutoAwesome, CyanAccent),
                    ShortcutToolItem("retouch", "Retouch", Icons.Default.AutoFixHigh, Color(0xFFFF80AB)),
                    ShortcutToolItem("photo_tools", "Photo tools", Icons.Default.Image, Color(0xFFFFB300)),
                    ShortcutToolItem("shoot_record", "Shoot & record", Icons.Default.PhotoCamera, KeyframeCrimson),
                    ShortcutToolItem("auto_enhance", "Auto enhance", Icons.Default.Tune, Color(0xFF00E676)),
                    ShortcutToolItem("cover_maker", "Cover maker", Icons.Default.Wallpaper, Color(0xFFB388FF)),
                    ShortcutToolItem("auto_captions", "Auto captions", Icons.Default.ClosedCaption, Color(0xFF40C4FF)),
                    ShortcutToolItem("remove_bg", "Remove BG", Icons.Default.ContentCut, Color(0xFF1DE9B6)),
                    ShortcutToolItem("cloud_space", "Cloud/Space", Icons.Default.CloudDone, VioletAccent)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    shortcuts.chunked(3).forEach { rowTools ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowTools.forEach { tool ->
                                Surface(
                                    color = StudioCard,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, StudioBorder, RoundedCornerShape(14.dp))
                                        .clickable { onLaunchTool(tool.id) }
                                        .testTag("shortcut_${tool.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                                .background(tool.tint.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = tool.icon,
                                                contentDescription = tool.label,
                                                tint = tool.tint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = tool.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Recent Projects Horizontal Carousel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent projects (${projects.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    TextButton(onClick = onViewAllProjects) {
                        Text("Manage All →", color = CyanAccent, style = MaterialTheme.typography.labelLarge)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects, key = { it.id }) { proj ->
                        RecentProjectCarouselCard(
                            project = proj,
                            onClick = { onOpenProject(proj) }
                        )
                    }
                }
            }
        }

        // 4. Continue Editing Detailed Draft List
        item {
            Text(
                text = "Continue editing",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        items(projects, key = { "row_${it.id}" }) { proj ->
            ProjectDetailRowCard(
                project = proj,
                onOpen = { onOpenProject(proj) },
                onRename = { onRenameProject(proj) },
                onDuplicate = { onDuplicateProject(proj) },
                onDelete = { onDeleteProject(proj) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

private data class ShortcutToolItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val tint: Color
)

@Composable
private fun RecentProjectCarouselCard(
    project: VideoProject,
    onClick: () -> Unit
) {
    val totalDurMs = VideoRenderEngine.computeProjectTotalDurationMs(project)
    val coverRes = project.primaryClips.firstOrNull()?.sampleDrawableRes ?: project.coverDrawableRes
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        modifier = Modifier
            .width(168.dp)
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("recent_project_${project.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = project.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, StudioBg.copy(alpha = 0.8f))
                            )
                        )
                )
                Surface(
                    color = StudioBg.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = NovaCutViewModel.formatShortDuration(totalDurMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    color = CyanAccent.copy(alpha = 0.88f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Continue editing",
                            tint = StudioBg,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.aspectRatio.label} • ${project.exportResolution} • ${project.primaryClips.size} clips",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ProjectDetailRowCard(
    project: VideoProject,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurMs = VideoRenderEngine.computeProjectTotalDurationMs(project)
    val coverRes = project.primaryClips.firstOrNull()?.sampleDrawableRes ?: project.coverDrawableRes

    Surface(
        color = StudioCard,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 84.dp, height = 62.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = project.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = StudioBg.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = NovaCutViewModel.formatShortDuration(totalDurMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${project.aspectRatio.label} • ${project.exportResolution} ${project.exportFps}fps • ${project.primaryClips.size} clips",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.effectItems.size} FX • ${project.textClips.size} Texts • Autosaved",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent
                )
            }

            Row {
                IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = "Rename project",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate project",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete project",
                        tint = KeyframeCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplatesBrowserTab(onUseTemplate: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = remember { listOf("All") + EffectFilterTransitionCatalog.templateCategories }
    val filtered = remember(selectedCategory) {
        if (selectedCategory == "All") {
            EffectFilterTransitionCatalog.templates
        } else {
            EffectFilterTransitionCatalog.templates.filter { it.category == selectedCategory }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = StudioBg,
                        containerColor = StudioCard,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { tpl ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                        .clickable { onUseTemplate(tpl.id) }
                        .testTag("template_card_${tpl.id}")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(148.dp)
                        ) {
                            Image(
                                painter = painterResource(id = tpl.previewDrawableRes),
                                contentDescription = tpl.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, StudioBg.copy(alpha = 0.88f))
                                        )
                                    )
                            )
                            Surface(
                                color = Color(tpl.accentHex),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = tpl.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StudioBg,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "${tpl.clipSlotsCount} Clips • ${tpl.durationSec}s • ${tpl.aspectRatio.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            )
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = tpl.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${tpl.author} • ${tpl.usesCountLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onUseTemplate(tpl.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = StudioBg
                                ),
                                contentPadding = PaddingValues(vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                            ) {
                                Text("Edit Template", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiLabStudioTab(onRunAiTool: (String) -> Unit) {
    val aiTools = listOf(
        Triple("ai_auto_captions", "Auto Captions & Karaoke Sync", "Speech-to-text timeline caption generator with word-level timing highlights"),
        Triple("ai_smart_cutout", "Smart Subject Cutout", "Isolate subjects from background with customizable neon rim & feathering"),
        Triple("ai_auto_enhance", "HDR Cinema Color Enhance", "Automatic dynamic range expansion, skin-tone protection & micro-sharpening"),
        Triple("ai_auto_reframe", "Intelligent Auto Reframe", "Convert 16:9 widescreen to 9:16 vertical Reels with subject motion keyframes"),
        Triple("ai_tts_voiceover", "Studio Voiceover & Narration", "Synthesize synchronized narration track with matched subtitles"),
        Triple("ai_restyle_fx", "Cyberpunk & Anamorphic Restyle", "Apply multi-layer shader effects and LUT color grade in one tap")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                color = VioletAccent.copy(alpha = 0.16f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VioletAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MODULAR AI LAB • OPTIONAL WORKFLOWS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Accelerate repetitive tasks with smart tools. The core NovaCut NLE editor operates 100% independently for full manual control.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        items(aiTools, key = { it.first }) { (id, title, desc) ->
            Surface(
                color = StudioCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                    .clickable { onRunAiTool(id) }
                    .testTag("ai_tool_$id")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = title,
                            tint = CyanAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onRunAiTool(id) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = StudioBg)
                    ) {
                        Text("Launch")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectsManagerTab(
    projects: List<VideoProject>,
    onNewProject: () -> Unit,
    onOpenProject: (VideoProject) -> Unit,
    onRenameProject: (VideoProject) -> Unit,
    onDuplicateProject: (VideoProject) -> Unit,
    onDeleteProject: (VideoProject) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) projects
        else projects.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Local Project Database", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text("${projects.size} saved timelines • Real-time SQLite Autosave", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Button(
                    onClick = onNewProject,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = StudioBg)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New")
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter saved projects by name…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filtered, key = { it.id }) { proj ->
            ProjectDetailRowCard(
                project = proj,
                onOpen = { onOpenProject(proj) },
                onRename = { onRenameProject(proj) },
                onDuplicate = { onDuplicateProject(proj) },
                onDelete = { onDeleteProject(proj) }
            )
        }
    }
}

@Composable
private fun InboxTab(viewModel: NovaCutViewModel) {
    val notifications by viewModel.inboxNotifications.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Studio Inbox & Engine Releases", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }
        items(notifications, key = { it.id }) { notif ->
            Surface(
                color = StudioCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (notif.isUnread) CyanAccent.copy(alpha = 0.5f) else StudioBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        viewModel.markNotificationRead(notif.id)
                        viewModel.openProjectInEditor(viewModel.activeProject.value)
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(notif.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                        Text(notif.timestampLabel, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(notif.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(notif.message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun MeSettingsTab(viewModel: NovaCutViewModel) {
    val proxyEnabled by viewModel.proxyPreviewEnabled.collectAsStateWithLifecycle()
    val gpuEnabled by viewModel.gpuHardwareAccelEnabled.collectAsStateWithLifecycle()
    val autoSave by viewModel.autoSaveEnabled.collectAsStateWithLifecycle()
    val fpsOverlay by viewModel.showFpsOverlay.collectAsStateWithLifecycle()
    val darkTheme by viewModel.isDarkStudioTheme.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Studio Engine & Preferences", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Text("Configure hardware decoding, proxy preview & autosave", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        item {
            SettingToggleRow(
                title = "GPU Hardware Acceleration",
                subtitle = "Use hardware ColorMatrix & shader compositing for 60fps preview",
                checked = gpuEnabled,
                onToggle = { viewModel.toggleGpuHardwareAccel() }
            )
        }
        item {
            SettingToggleRow(
                title = "Smart Proxy Preview",
                subtitle = "Downscale 4K media frames during timeline scrubbing to prevent frame drops",
                checked = proxyEnabled,
                onToggle = { viewModel.toggleProxyPreview() }
            )
        }
        item {
            SettingToggleRow(
                title = "Real-Time SQLite Autosave",
                subtitle = "Persist every cut, keyframe, and adjustment immediately to local database",
                checked = autoSave,
                onToggle = { viewModel.toggleAutoSave() }
            )
        }
        item {
            SettingToggleRow(
                title = "Preview Telemetry HUD",
                subtitle = "Display live render FPS, resolution, and active keyframe count on canvas",
                checked = fpsOverlay,
                onToggle = { viewModel.toggleFpsOverlay() }
            )
        }
        item {
            SettingToggleRow(
                title = "Dark Studio Home Workspace",
                subtitle = "Use precision obsidian dark theme across Home and Media Picker",
                checked = darkTheme,
                onToggle = { viewModel.toggleDarkStudioTheme() }
            )
        }
        item {
            Surface(
                color = StudioCard,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Media & Thumbnail LRU Cache", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("24 MB memory-aware bitmap pool active", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Button(
                        onClick = { viewModel.clearThumbnailCache() },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioElevated, contentColor = CyanAccent)
                    ) {
                        Text("Purge Cache")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        color = StudioCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StudioBg,
                    checkedTrackColor = CyanAccent
                )
            )
        }
    }
}

@Composable
fun GlobalSearchDialog(
    viewModel: NovaCutViewModel,
    onDismiss: () -> Unit
) {
    val query by viewModel.globalSearchQuery.collectAsStateWithLifecycle()
    val results = remember(query) { EffectFilterTransitionCatalog.searchAll(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Text("Global Studio Search", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateGlobalSearchQuery(it) },
                    placeholder = { Text("Effects, filters, transitions, templates, fonts, sounds…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanAccent) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results, key = { "${it.type}_${it.id}" }) { item ->
                        Surface(
                            color = StudioCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    onDismiss()
                                    val proj = viewModel.activeProject.value
                                    when (item.type) {
                                        "EFFECT" -> {
                                            viewModel.openProjectInEditor(proj, EditorToolTab.EFFECTS)
                                            viewModel.addVideoEffectToTimeline(item.id)
                                        }
                                        "FILTER" -> {
                                            viewModel.openProjectInEditor(proj, EditorToolTab.FILTERS)
                                            viewModel.applyFilter(item.id, 0.85f)
                                        }
                                        "TRANSITION" -> {
                                            viewModel.openProjectInEditor(proj, EditorToolTab.TRANSITION)
                                            viewModel.applyTransition(item.id, 600L)
                                        }
                                        "TEMPLATE" -> {
                                            viewModel.useTemplate(item.id)
                                        }
                                        "FONT" -> {
                                            viewModel.openProjectInEditor(proj, EditorToolTab.TEXT)
                                            viewModel.addTextClipToTimeline("CUSTOM FONT", false)
                                            viewModel.updateSelectedTextClip { it.copy(fontFamilyId = item.id) }
                                        }
                                        "STICKER" -> {
                                            viewModel.openProjectInEditor(proj, EditorToolTab.STICKERS)
                                            viewModel.addStickerToTimeline(item.id)
                                        }
                                        "SOUND" -> {
                                            viewModel.openProjectInEditor(proj, EditorToolTab.AUDIO)
                                            viewModel.addAudioPresetToTimeline(item.id)
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(item.accentHex))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Text("Apply →", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CyanAccent)
            }
        }
    )
}
