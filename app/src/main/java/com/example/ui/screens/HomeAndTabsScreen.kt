package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.ui.theme.AudioEmerald
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
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg),
        containerColor = StudioBg,
        topBar = {
            HomeTopBar(
                onSearchClick = { viewModel.setGlobalSearchVisible(true) },
                onQuickImportClick = { viewModel.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_VIDEO) },
                onCloudClick = { viewModel.launchShortcutTool("cloud_space") },
                onProfileClick = { viewModel.selectHomeTab(HomeBottomTab.ME) }
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF0B0E16),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Color(0xFF1D2536))
            ) {
                NavigationBar(
                    containerColor = Color(0xFF0B0E16),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .height(64.dp)
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
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanAccent,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanAccent.copy(alpha = 0.20f),
                                unselectedIconColor = Color(0xFF7D8799),
                                unselectedTextColor = Color(0xFF7D8799)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0C1322),
                            StudioBg,
                            Color(0xFF06080C)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            when (activeTab) {
                HomeBottomTab.EDIT -> HomeEditDashboard(
                    projects = projects,
                    onNewVideo = { viewModel.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_VIDEO) },
                    onEditPhoto = { viewModel.openMediaPicker(MediaPickerPurpose.NEW_PROJECT_PHOTO) },
                    onAutoEditing = { viewModel.openOmkarAutoVideoMakerScreen() },
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
            .background(Color(0xFF080B11))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Premium Dark Video Editor Clapperboard Logo Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(8.dp, RoundedCornerShape(10.dp), ambientColor = CyanAccent, spotColor = CyanAccent)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CyanAccent, Color(0xFF3D7BFF), VioletAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MovieCreation,
                    contentDescription = "App Logo",
                    tint = Color(0xFF05070B),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NovaCut",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("home_brand_title")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = CyanAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(5.dp),
                        modifier = Modifier.border(0.5.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HeaderIconButton(
                icon = Icons.Default.FileUpload,
                contentDescription = "Import media",
                tint = TextPrimary,
                testTag = "home_import_button",
                onClick = onQuickImportClick
            )
            HeaderIconButton(
                icon = Icons.Default.Search,
                contentDescription = "Global search",
                tint = TextPrimary,
                testTag = "home_search_button",
                onClick = onSearchClick
            )
            HeaderIconButton(
                icon = Icons.Default.CloudDone,
                contentDescription = "Cloud Space",
                tint = CyanAccent,
                testTag = "home_cloud_button",
                onClick = onCloudClick
            )
            HeaderIconButton(
                icon = Icons.Default.Person,
                contentDescription = "Workspace settings",
                tint = TextPrimary,
                testTag = "home_profile_button",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF121723),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .size(36.dp)
            .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeEditDashboard(
    projects: List<VideoProject>,
    onNewVideo: () -> Unit,
    onEditPhoto: () -> Unit,
    onAutoEditing: () -> Unit,
    onLaunchTool: (String) -> Unit,
    onOpenProject: (VideoProject) -> Unit,
    onRenameProject: (VideoProject) -> Unit,
    onDuplicateProject: (VideoProject) -> Unit,
    onDeleteProject: (VideoProject) -> Unit,
    onViewAllProjects: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_dashboard_list"),
        contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // =====================================================================
        // 1. HERO / CREATE SECTION (Dark Premium Hero Card + Auto Editing)
        // =====================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0E131F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    CyanAccent.copy(alpha = 0.42f),
                                    Color(0xFF1F293D),
                                    VioletAccent.copy(alpha = 0.38f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Subtle atmospheric studio lighting backdrop
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_studio),
                            contentDescription = "Studio hero backdrop",
                            contentScale = ContentScale.Crop,
                            alpha = 0.28f,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0B1322).copy(alpha = 0.65f),
                                            Color(0xFF0B101B).copy(alpha = 0.92f),
                                            Color(0xFF0A0E17)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            // Technical feature strip + Autosave Active status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = Color(0xFF121B2C),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.border(
                                        0.5.dp,
                                        CyanAccent.copy(alpha = 0.45f),
                                        RoundedCornerShape(6.dp)
                                    )
                                ) {
                                    Text(
                                        text = "KEYFRAMES • CURVES • 4K HDR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(AudioEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Autosave Active",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Main Dark Action Cards: New video (Primary Highlighted) & Edit photo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HeroActionCard(
                                    title = "New video",
                                    subtitle = "Multi-track timeline & effects",
                                    icon = Icons.Default.Add,
                                    isPrimary = true,
                                    testTag = "cta_new_video",
                                    onClick = onNewVideo,
                                    modifier = Modifier.weight(1.55f)
                                )

                                HeroActionCard(
                                    title = "Edit photo",
                                    subtitle = "RAW & HSL grade",
                                    icon = Icons.Default.Image,
                                    isPrimary = false,
                                    testTag = "cta_edit_photo",
                                    onClick = onEditPhoto,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // First-Class Independent Feature Card: AUTO EDITING -> OMKAR AUTOMATIC VIDEO MAKER
                            AutoEditingHeroCard(
                                onClick = onAutoEditing,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 2. STUDIO TOOLKIT (3x3 Compact Premium Dark Grid)
        // =====================================================================
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
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val shortcuts = listOf(
                    ShortcutToolItem("autocut", "AutoCut", Icons.Default.AutoAwesome, CyanAccent),
                    ShortcutToolItem("retouch", "Retouch", Icons.Default.AutoFixHigh, Color(0xFFFF80AB)),
                    ShortcutToolItem("photo_tools", "Photo Tools", Icons.Default.Image, Color(0xFFFFB300)),
                    ShortcutToolItem("shoot_record", "Shoot and Record", Icons.Default.PhotoCamera, KeyframeCrimson),
                    ShortcutToolItem("auto_enhance", "Auto Enhance", Icons.Default.Tune, Color(0xFF00E676)),
                    ShortcutToolItem("cover_maker", "Cover Maker", Icons.Default.Wallpaper, Color(0xFFB388FF)),
                    ShortcutToolItem("auto_captions", "Auto Captions", Icons.Default.ClosedCaption, Color(0xFF40C4FF)),
                    ShortcutToolItem("remove_bg", "Remove Background", Icons.Default.ContentCut, Color(0xFF1DE9B6)),
                    ShortcutToolItem("cloud_space", "Cloud / Space", Icons.Default.CloudDone, VioletAccent)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    shortcuts.chunked(3).forEach { rowTools ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTools.forEach { tool ->
                                StudioToolkitCompactButton(
                                    tool = tool,
                                    onClick = { onLaunchTool(tool.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. RECENT PROJECTS CAROUSEL
        // =====================================================================
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
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("recent_projects_header")
                    )
                    TextButton(
                        onClick = onViewAllProjects,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Manage All →",
                            color = CyanAccent,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

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

        // =====================================================================
        // 4. CONTINUE EDITING (Featured Most Recently Edited Project Card)
        // =====================================================================
        item {
            Text(
                text = "Continue editing",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        projects.firstOrNull()?.let { latestProject ->
            item(key = "featured_continue_${latestProject.id}") {
                FeaturedContinueEditingCard(
                    project = latestProject,
                    onContinue = { onOpenProject(latestProject) },
                    onRename = { onRenameProject(latestProject) },
                    onDuplicate = { onDuplicateProject(latestProject) },
                    onDelete = { onDeleteProject(latestProject) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isPrimary: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "heroCardScale"
    )

    val backgroundBrush = if (isPrimary) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF2CE8FF),
                Color(0xFF3B8DFF),
                Color(0xFF764DFF)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF151929),
                Color(0xFF101422)
            )
        )
    }

    val borderBrush = if (isPrimary) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF8CF6FF),
                Color(0xFF5CA0FF),
                Color(0xFF9E7BFF)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                VioletAccent.copy(alpha = 0.45f),
                Color(0xFF232B42)
            )
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(84.dp)
            .shadow(
                elevation = if (isPrimary) 12.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isPrimary) CyanAccent else VioletAccent,
                spotColor = if (isPrimary) CyanAccent else VioletAccent
            )
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPrimary) {
                                Color(0xFF071624).copy(alpha = 0.28f)
                            } else {
                                VioletAccent.copy(alpha = 0.24f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isPrimary) Color.White else Color(0xFFD7C6FF),
                        modifier = Modifier.size(19.dp)
                    )
                }

                if (isPrimary) {
                    Surface(
                        color = Color(0xFF071624).copy(alpha = 0.28f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "CREATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = title,
                    style = if (isPrimary) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    color = if (isPrimary) Color(0xFF050A14) else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPrimary) Color(0xFF0B192C) else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AutoEditingHeroCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "autoEditingCardScale"
    )

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF101D33),
            Color(0xFF14182D),
            Color(0xFF1E1436)
        )
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            CyanAccent.copy(alpha = 0.85f),
            Color(0xFF4D8BFF).copy(alpha = 0.70f),
            VioletAccent.copy(alpha = 0.85f)
        )
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(58.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(15.dp),
                ambientColor = CyanAccent,
                spotColor = VioletAccent
            )
            .clip(RoundedCornerShape(15.dp))
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(15.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("cta_auto_editing")
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Sparkles + Video Timeline Dual Badge Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    CyanAccent.copy(alpha = 0.25f),
                                    VioletAccent.copy(alpha = 0.28f)
                                )
                            )
                        )
                        .border(
                            0.75.dp,
                            CyanAccent.copy(alpha = 0.6f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Auto Editing",
                        tint = CyanAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚡ Auto Editing",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "AI-powered automatic video editing",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right-side OMKAR Timeline Badge
            Surface(
                color = CyanAccent.copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.border(
                    0.5.dp,
                    CyanAccent.copy(alpha = 0.55f),
                    RoundedCornerShape(8.dp)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AUTO EDITING",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
private fun StudioToolkitCompactButton(
    tool: ShortcutToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "toolkitScale"
    )

    Surface(
        color = Color(0xFF111622),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(42.dp)
            .border(1.dp, Color(0xFF1D2536), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("shortcut_${tool.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(tool.tint.copy(alpha = 0.15f))
                    .border(0.5.dp, tool.tint.copy(alpha = 0.35f), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = tool.tint,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tool.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentProjectCarouselCard(
    project: VideoProject,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "recentCardScale"
    )

    val totalDurMs = VideoRenderEngine.computeProjectTotalDurationMs(project)
    val coverRes = EffectFilterTransitionCatalog.safeDrawableRes(
        project.primaryClips.firstOrNull()?.sampleDrawableRes ?: project.coverDrawableRes
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111622)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .width(164.dp)
            .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("recent_project_${project.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
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
                                listOf(Color.Transparent, StudioBg.copy(alpha = 0.84f))
                            )
                        )
                )
                // Duration badge bottom-right
                Surface(
                    color = StudioBg.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = NovaCutViewModel.formatShortDuration(totalDurMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
                // Play indicator top-left
                Surface(
                    color = CyanAccent.copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(7.dp)
                        .size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play project",
                            tint = StudioBg,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.aspectRatio.label} • ${project.exportResolution.uppercase()} • ${project.primaryClips.size} clips",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FeaturedContinueEditingCard(
    project: VideoProject,
    onContinue: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurMs = VideoRenderEngine.computeProjectTotalDurationMs(project)
    val coverRes = EffectFilterTransitionCatalog.safeDrawableRes(
        project.primaryClips.firstOrNull()?.sampleDrawableRes ?: project.coverDrawableRes
    )

    Surface(
        color = Color(0xFF111724),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(CyanAccent.copy(alpha = 0.45f), Color(0xFF1F283B))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onContinue)
            .testTag("continue_editing_featured_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 104.dp, height = 74.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .border(1.dp, Color(0xFF243048), RoundedCornerShape(11.dp))
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
                            .background(StudioBg.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = StudioBg.copy(alpha = 0.75f),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Continue editing",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Surface(
                        color = StudioBg.copy(alpha = 0.85f),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = CyanAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LATEST DRAFT",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${project.aspectRatio.label} • ${project.exportResolution.uppercase()} • ${project.primaryClips.size} clips",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${project.exportFps}fps • ${project.effectItems.size} FX • ${project.textClips.size} Texts",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7F93B5)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Continue / Edit button + project options (Rename, Duplicate, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRename, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.DriveFileRenameOutline,
                            contentDescription = "Rename project",
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate project",
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete project",
                            tint = KeyframeCrimson,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = StudioBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("continue_editing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
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
    val coverRes = EffectFilterTransitionCatalog.safeDrawableRes(
        project.primaryClips.firstOrNull()?.sampleDrawableRes ?: project.coverDrawableRes
    )

    Surface(
        color = Color(0xFF111622),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1D2536), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 58.dp)
                    .clip(RoundedCornerShape(9.dp))
            ) {
                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = project.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = StudioBg.copy(alpha = 0.82f),
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.aspectRatio.label} • ${project.exportResolution.uppercase()} • ${project.primaryClips.size} clips",
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
                IconButton(onClick = onRename, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = "Rename project",
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(onClick = onDuplicate, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate project",
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete project",
                        tint = KeyframeCrimson,
                        modifier = Modifier.size(17.dp)
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
                                painter = painterResource(
                                    id = EffectFilterTransitionCatalog.safeDrawableRes(tpl.previewDrawableRes)
                                ),
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
                        text = "NOVACUT AI LAB • SMART WORKFLOWS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Accelerate editing with NovaCut Auto Captions, Smart Cutout, Auto Reframe, and Velocity Restyle. The NovaCut multi-track NLE editor gives you 100% manual control.",
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
                    Text("NovaCut Cloud & Local Projects", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
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
            Text("NovaCut Inbox & Creator Updates", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
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
            Text("NovaCut Pro Engine & Preferences", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
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
            Text("Search NovaCut", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
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
