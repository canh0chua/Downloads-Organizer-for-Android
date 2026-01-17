package com.canh0chua.downloadsorganizer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.canh0chua.downloadsorganizer.model.FileItem
import com.canh0chua.downloadsorganizer.model.FileType
import com.canh0chua.downloadsorganizer.viewmodel.FileViewModel
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    viewModel: FileViewModel,
    fileTypeName: String,
    onBackClick: () -> Unit
) {
    val fileState by viewModel.fileState.collectAsState()
    val fileType = FileType.valueOf(fileTypeName)
    val files = fileState[fileType] ?: emptyList()
    val context = LocalContext.current

    var selectedItems by remember { mutableStateOf(setOf<FileItem>()) }
    var showRenameDialog by remember { mutableStateOf<FileItem?>(null) }
    var showCleanupDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    var cleanupDays by remember { mutableStateOf("30") }

    Scaffold(
        topBar = {
            if (selectedItems.isEmpty()) {
                TopAppBar(
                    title = { Text(fileType.displayName) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCleanupDialog = true }) {
                            Icon(Icons.Default.CleaningServices, contentDescription = "Cleanup old files")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("${selectedItems.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedItems = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        if (selectedItems.size == 1) {
                            IconButton(onClick = {
                                val item = selectedItems.first()
                                showRenameDialog = item
                                renameValue = item.name
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename")
                            }
                        }
                        IconButton(onClick = {
                            viewModel.deleteFiles(selectedItems.toList())
                            selectedItems = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (files.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No files found in this category")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files) { fileItem ->
                        val isSelected = selectedItems.contains(fileItem)
                        FileListItem(
                            fileItem = fileItem,
                            isSelected = isSelected,
                            onClick = {
                                if (selectedItems.isNotEmpty()) {
                                    selectedItems = if (isSelected) {
                                        selectedItems - fileItem
                                    } else {
                                        selectedItems + fileItem
                                    }
                                } else {
                                    openFile(context, fileItem)
                                }
                            },
                            onLongClick = {
                                selectedItems = selectedItems + fileItem
                            }
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename File") },
            text = {
                TextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("New name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRenameDialog?.let { viewModel.renameFile(it, renameValue) }
                    showRenameDialog = null
                    selectedItems = emptySet()
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCleanupDialog) {
        AlertDialog(
            onDismissRequest = { showCleanupDialog = false },
            title = { Text("Cleanup Old Files") },
            text = {
                Column {
                    Text("Delete files older than (days):")
                    TextField(
                        value = cleanupDays,
                        onValueChange = { if (it.all { char -> char.isDigit() }) cleanupDays = it },
                        singleLine = true,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    cleanupDays.toIntOrNull()?.let { viewModel.deleteFilesOlderThan(it) }
                    showCleanupDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCleanupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    fileItem: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            modifier = Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
            headlineContent = {
                Text(
                    text = fileItem.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(text = formatFileSize(fileItem.size))
            },
            leadingContent = {
                Box {
                    if (fileItem.type == FileType.IMAGE || fileItem.type == FileType.VIDEO || fileItem.type == FileType.APK) {
                        AsyncImage(
                            model = File(fileItem.path),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    if (isSelected) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.align(Alignment.BottomEnd).size(18.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}

fun openFile(context: Context, fileItem: FileItem) {
    val file = File(fileItem.path)
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "com.canh0chua.downloadsorganizer.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Open with"))
}

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}
