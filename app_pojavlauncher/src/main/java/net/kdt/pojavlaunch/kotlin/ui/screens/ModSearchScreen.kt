package net.kdt.pojavlaunch.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.snapshotFlow
import net.ashmeet.hyperlauncher.R
import net.kdt.pojavlaunch.modloaders.modpacks.imagecache.ModIconCache
import net.kdt.pojavlaunch.modloaders.modpacks.models.Constants
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem

@Composable
fun ModSearchScreen(
    searchQuery: String,
    isLoading: Boolean,
    statusVisible: Boolean,
    statusText: String,
    statusColor: Color,
    items: List<ModItem>,
    expandedItemId: String?,
    expandedDetail: ModDetail?,
    detailLoading: Boolean,
    selectedVersionIndex: Int,
    lastPage: Boolean,
    tasksRunning: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onFilterClick: () -> Unit,
    onImportClick: () -> Unit,
    onItemClick: (ModItem) -> Unit,
    onLoadMore: () -> Unit,
    onVersionSelected: (Int) -> Unit,
    onInstallClick: (ModItem) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        if (items.isEmpty() && searchQuery.isEmpty() && !isLoading) {
            onSearchSubmit()
        }
    }

    LaunchedEffect(listState, items.size, lastPage, isLoading) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }.collect { lastVisibleIndex ->
            if (!lastPage && !isLoading && items.isNotEmpty() && lastVisibleIndex >= items.lastIndex - 1) {
                onLoadMore()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            onSearchQueryChange(it)
                            if (it.isEmpty()) {
                                onSearchSubmit()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search modpacks") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }

                if (statusVisible) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.iconCacheTag }
            ) { _, item ->
                val isExpanded = item.id != null && item.id == expandedItemId
                ModSearchItemCard(
                    item = item,
                    expanded = isExpanded,
                    detail = if (isExpanded) expandedDetail else null,
                    detailLoading = isExpanded && detailLoading,
                    selectedVersionIndex = selectedVersionIndex,
                    tasksRunning = tasksRunning,
                    onClick = { onItemClick(item) },
                    onVersionSelected = onVersionSelected,
                    onInstallClick = { onInstallClick(item) }
                )
            }

            if (!lastPage && items.isNotEmpty()) {
                item(key = "load_more_footer") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onImportClick, modifier = Modifier.fillMaxWidth()) {
            Text("Import Local Modpack")
        }
    }
}

@Composable
private fun ModSearchItemCard(
    item: ModItem,
    expanded: Boolean,
    detail: ModDetail?,
    detailLoading: Boolean,
    selectedVersionIndex: Int,
    tasksRunning: Boolean,
    onClick: () -> Unit,
    onVersionSelected: (Int) -> Unit,
    onInstallClick: () -> Unit
) {
    val versionNames = detail?.versionNames?.filterNotNull().orEmpty()
    val versionIndex = when {
        versionNames.isEmpty() -> 0
        selectedVersionIndex < 0 -> 0
        selectedVersionIndex > versionNames.lastIndex -> versionNames.lastIndex
        else -> selectedVersionIndex
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.Top
            ) {
                ModIcon(item = item)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) 12 else 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (item.isModpack) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
                            ) {
                                Text(
                                    text = "Modpack",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = when (item.apiSource) {
                                    Constants.SOURCE_CURSEFORGE -> "CurseForge"
                                    Constants.SOURCE_MODRINTH -> "Modrinth"
                                    else -> "Source"
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    when {
                        detailLoading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            }
                        }

                        detail == null -> {
                            Text(
                                text = "Unable to load version details.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }

                        else -> {
                            Text(
                                text = "Version",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            VersionDropdown(
                                options = versionNames,
                                selectedIndex = versionIndex,
                                enabled = versionNames.isNotEmpty(),
                                onSelectedIndex = onVersionSelected
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onInstallClick,
                                enabled = versionNames.isNotEmpty() && !tasksRunning,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Install")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModIcon(item: ModItem) {
    var bitmap by remember(item.id) { mutableStateOf<Bitmap?>(null) }
    val iconCache = remember { ModIconCache() }

    LaunchedEffect(item.id) {
        iconCache.getImage({ bm ->
            bitmap = bm
        }, item.iconCacheTag, item.imageUrl)
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = sourceDrawable(item.apiSource)),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun VersionDropdown(
    options: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onSelectedIndex: (Int) -> Unit
) {
    var expanded by remember(options, selectedIndex) { mutableStateOf(false) }
    val currentText = options.getOrNull(selectedIndex).orEmpty()

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = currentText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            },
            placeholder = { Text("Select version") },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
        )

        DropdownMenu(
            expanded = expanded && enabled && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onSelectedIndex(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun sourceDrawable(apiSource: Int): Int {
    return when (apiSource) {
        Constants.SOURCE_CURSEFORGE -> R.drawable.ic_curseforge
        Constants.SOURCE_MODRINTH -> R.drawable.ic_modrinth
        else -> R.drawable.ic_filter
    }
}
