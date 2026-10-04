package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.model.MediaAlbumCategory
import com.example.model.MediaItemModel
import com.example.model.MediaTypeFilter
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
fun MediaPickerScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }

    val catalog by viewModel.mediaCatalog.collectAsStateWithLifecycle()
    val activeAlbumTab by viewModel.pickerAlbumTab.collectAsStateWithLifecycle()
    val typeFilter by viewModel.pickerTypeFilter.collectAsStateWithLifecycle()
    val selectedAlbumName by viewModel.pickerSelectedAlbumName.collectAsStateWithLifecycle()
    val searchQuery by viewModel.pickerSearchQuery.collectAsStateWithLifecycle()
    val hdEnabled by viewModel.pickerHdEnabled.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedMediaItems.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importSystemPickedUris(uris.map { it.toString() })
        }
    }

    val availableAlbums = remember(catalog, activeAlbumTab) {
        val albumsInTab = catalog.filter { it.tabCategory == activeAlbumTab }.map { it.albumName }.distinct()
        listOf("All Albums") + albumsInTab
    }

    val filteredItems = remember(catalog, activeAlbumTab, typeFilter, selectedAlbumName, searchQuery) {
        catalog.filter { item ->
            val tabMatch = item.tabCategory == activeAlbumTab
            val typeMatch = when (typeFilter) {
                MediaTypeFilter.ALL -> true
                MediaTypeFilter.VIDEOS -> item.isVideo
                MediaTypeFilter.PHOTOS -> !item.isVideo
            }
            val albumMatch = selectedAlbumName == "All Albums" || item.albumName == selectedAlbumName
            val queryMatch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.albumName.contains(searchQuery, ignoreCase = true)
            tabMatch && typeMatch && albumMatch && queryMatch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Top Header with Close, Album Selector, System Picker button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier.testTag("picker_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close media picker",
                    tint = TextPrimary
                )
            }

            Text(
                text = "Select Media",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioElevated,
                    contentColor = CyanAccent
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Device Gallery", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Primary Tabs: Albums | Generated | Spaces | Library
        TabRow(
            selectedTabIndex = MediaAlbumCategory.entries.indexOf(activeAlbumTab),
            containerColor = StudioSurface,
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[MediaAlbumCategory.entries.indexOf(activeAlbumTab)]),
                    color = CyanAccent
                )
            }
        ) {
            MediaAlbumCategory.entries.forEach { tab ->
                val selected = activeAlbumTab == tab
                Tab(
                    selected = selected,
                    onClick = {
                        viewModel.setPickerAlbumTab(tab)
                        viewModel.setPickerSelectedAlbumName("All Albums")
                    },
                    modifier = Modifier.testTag("picker_tab_${tab.name.lowercase()}"),
                    text = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selected) CyanAccent else TextSecondary
                        )
                    }
                )
            }
        }

        // Secondary Tabs (All | Videos | Photos) + Album filter chips + Search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MediaTypeFilter.entries.forEach { filter ->
                        val isSelected = typeFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setPickerTypeFilter(filter) },
                            modifier = Modifier.testTag("picker_subtab_${filter.name.lowercase()}"),
                            label = { Text(filter.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent,
                                selectedLabelColor = StudioBg,
                                containerColor = StudioCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(availableAlbums) { album ->
                        val isAlbumSel = selectedAlbumName == album
                        FilterChip(
                            selected = isAlbumSel,
                            onClick = { viewModel.setPickerSelectedAlbumName(album) },
                            label = { Text(album, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioElevated,
                                selectedLabelColor = CyanAccent,
                                containerColor = StudioBg,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setPickerSearchQuery(it) },
                placeholder = { Text("Search clips by name or album…", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }

        // 3-Column Rounded Media Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val selIndex = selectedItems.indexOfFirst { it.id == item.id }
                val isSelected = selIndex >= 0
                MediaGridTile(
                    item = item,
                    selectionOrder = if (isSelected) selIndex + 1 else null,
                    onClick = { viewModel.toggleMediaItemSelection(item) }
                )
            }
        }

        // Bottom Multi-Select Tray + HD Toggle + Add Button
        Surface(
            color = StudioSurface,
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, StudioBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (selectedItems.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        items(selectedItems, key = { "tray_${it.id}" }) { sel ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.5.dp, CyanAccent, RoundedCornerShape(10.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = sel.drawableRes),
                                    contentDescription = sel.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .background(StudioBg.copy(alpha = 0.85f), CircleShape)
                                        .clickable { viewModel.toggleMediaItemSelection(sel) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove from selection",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // HD toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (hdEnabled) CyanAccent.copy(alpha = 0.16f) else StudioCard)
                            .border(
                                1.dp,
                                if (hdEnabled) CyanAccent else StudioBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.togglePickerHd() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("picker_hd_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hd,
                            contentDescription = "HD quality toggle",
                            tint = if (hdEnabled) CyanAccent else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hdEnabled) "HD / 4K Source" else "Proxy Standard",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hdEnabled) CyanAccent else TextSecondary
                        )
                    }

                    val count = selectedItems.size
                    Button(
                        onClick = { viewModel.confirmMediaPickerSelection() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = StudioBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("picker_add_button")
                    ) {
                        Text(
                            text = if (count > 0) "Add ($count)" else "Add Selected (1)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaGridTile(
    item: MediaItemModel,
    selectionOrder: Int?,
    onClick: () -> Unit
) {
    val isSelected = selectionOrder != null
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) CyanAccent else StudioBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag("media_tile_${item.id}")
    ) {
        if (item.uriString.isNotBlank()) {
            AsyncImage(
                model = item.uriString,
                contentDescription = item.title,
                placeholder = painterResource(id = item.drawableRes),
                error = painterResource(id = item.drawableRes),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = item.drawableRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            StudioBg.copy(alpha = 0.25f),
                            Color.Transparent,
                            StudioBg.copy(alpha = 0.82f)
                        )
                    )
                )
        )

        // Selection badge in top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) CyanAccent else StudioBg.copy(alpha = 0.65f))
                .border(1.5.dp, if (isSelected) CyanAccent else Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text = selectionOrder.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioBg,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // HD badge top-left
        if (item.isHd) {
            Surface(
                color = StudioBg.copy(alpha = 0.72f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = if (item.width >= 3840) "4K" else "HD",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        // Title + Duration bottom row
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title.substringBeforeLast("."),
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (item.isVideo) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = NovaCutViewModel.formatShortDuration(item.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            } else {
                Text(
                    text = "IMG",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFD54F)
                )
            }
        }
    }
}

@Composable
fun CameraCaptureScreen(viewModel: NovaCutViewModel) {
    BackHandler { viewModel.navigateBack() }

    var selectedDurationMs by remember { mutableStateOf(6000L) }
    var selectedBackdropIdx by remember { mutableStateOf(0) }
    var teleprompterVisible by remember { mutableStateOf(true) }
    val backdrops = listOf(
        R.drawable.img_sample_cyberpunk to "Neon Street 4K",
        R.drawable.img_sample_portrait to "Studio Rimlight",
        R.drawable.img_sample_alpine to "Alpine Drone"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Image(
            painter = painterResource(id = backdrops[selectedBackdropIdx].first),
            contentDescription = "Camera viewfinder",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier.background(StudioBg.copy(alpha = 0.65f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Surface(
                color = StudioBg.copy(alpha = 0.75f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.clickable { teleprompterVisible = !teleprompterVisible }
            ) {
                Text(
                    text = if (teleprompterVisible) "Teleprompter: ON" else "Teleprompter: OFF",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanAccent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            IconButton(
                onClick = { selectedBackdropIdx = (selectedBackdropIdx + 1) % backdrops.size },
                modifier = Modifier.background(StudioBg.copy(alpha = 0.65f), CircleShape)
            ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch scene", tint = Color.White)
            }
        }

        if (teleprompterVisible) {
            Surface(
                color = StudioBg.copy(alpha = 0.78f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 82.dp, start = 24.dp, end = 24.dp)
                    .border(1.dp, CyanAccent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("STUDIO TELEPROMPTER", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"Welcome back to the channel! Today we're grading 4K anamorphic footage with real-time velocity curves and keyframes.\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Bottom Record Dock
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, StudioBg.copy(alpha = 0.92f)))
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(3000L to "3s", 6000L to "6s", 10000L to "10s").forEach { (dur, label) ->
                    FilterChip(
                        selected = selectedDurationMs == dur,
                        onClick = { selectedDurationMs = dur },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = StudioBg
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(KeyframeCrimson)
                    .clickable {
                        viewModel.finishCameraCaptureAndEdit(
                            durationMs = selectedDurationMs,
                            drawableRes = backdrops[selectedBackdropIdx].first,
                            title = "Studio_Capture_${backdrops[selectedBackdropIdx].second.replace(" ", "_")}"
                        )
                    }
                    .testTag("camera_record_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = "Capture clip",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap to capture & open in Timeline Editor", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
