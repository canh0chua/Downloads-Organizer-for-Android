package com.canh0chua.downloadsorganizer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canh0chua.downloadsorganizer.model.FileType
import com.canh0chua.downloadsorganizer.viewmodel.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FileViewModel,
    onCategoryClick: (FileType) -> Unit
) {
    val fileState by viewModel.fileState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads Organizer") },
                actions = {
                    IconButton(onClick = { viewModel.scanFiles() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.organizeFiles() },
                icon = { Icon(Icons.Default.Build, contentDescription = null) },
                text = { Text("Organize Files") }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                val sortedTypes = FileType.entries.toMutableList()
                val topTypes = listOf(FileType.INCOMING_DOWNLOADS, FileType.INCOMING_QUICK_SHARE)
                sortedTypes.removeAll(topTypes)
                val finalOrder = topTypes + sortedTypes

                items(finalOrder) { type ->
                    val count = fileState[type]?.size ?: 0
                    CategoryCard(
                        type = type,
                        count = count,
                        onClick = { onCategoryClick(type) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    type: FileType,
    count: Int,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Count in top right
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = getIconForType(type),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (type == FileType.INCOMING_DOWNLOADS || type == FileType.INCOMING_QUICK_SHARE)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun getIconForType(type: FileType): ImageVector {
    return when (type) {
        FileType.IMAGE -> Icons.Default.Image
        FileType.VIDEO -> Icons.Default.Movie
        FileType.AUDIO -> Icons.Default.MusicNote
        FileType.DOC -> Icons.Default.Description
        FileType.PDF -> Icons.Default.PictureAsPdf
        FileType.APK -> Icons.Default.Android
        FileType.OTHER -> Icons.Default.QuestionMark
        FileType.INCOMING_DOWNLOADS -> Icons.Default.DownloadForOffline
        FileType.INCOMING_QUICK_SHARE -> Icons.Default.Share
    }
}
