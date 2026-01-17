package com.canh0chua.downloadsorganizer.viewmodel

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canh0chua.downloadsorganizer.model.FileItem
import com.canh0chua.downloadsorganizer.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.io.File
import java.util.Locale

class FileViewModel : ViewModel() {

    private val TAG = "FileViewModel"

    private val _fileState = MutableStateFlow<Map<FileType, List<FileItem>>>(emptyMap())
    val fileState: StateFlow<Map<FileType, List<FileItem>>> = _fileState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    enum class SortOrder {
        NAME_ASC, NAME_DESC, DATE_ASC, DATE_DESC, SIZE_ASC, SIZE_DESC
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        // Trigger a re-sort of existing state without re-scanning
        _fileState.value = _fileState.value.mapValues { (_, items) -> sortItems(items, order) }
    }

    private fun sortItems(items: List<FileItem>, order: SortOrder): List<FileItem> {
        return when (order) {
            SortOrder.NAME_ASC -> items.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> items.sortedByDescending { it.name.lowercase() }
            SortOrder.DATE_ASC -> items.sortedBy { it.lastModified }
            SortOrder.DATE_DESC -> items.sortedByDescending { it.lastModified }
            SortOrder.SIZE_ASC -> items.sortedBy { it.size }
            SortOrder.SIZE_DESC -> items.sortedByDescending { it.size }
        }
    }

    private val _hasPermission = MutableStateFlow(true) // Default to true to avoid flicker if already granted
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    fun updatePermissionStatus(granted: Boolean) {
        _hasPermission.value = granted
    }

    fun scanFiles(): kotlinx.coroutines.Job {
        return viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val quickShareDir = File(downloadsDir, "Quick Share")
            
            val allFiles = mutableListOf<File>()
            
            // Recursive scan of Downloads
            scanDirectory(downloadsDir, allFiles)
            
            // Recursive scan of Quick Share if it exists and isn't already inside Downloads (usually is, but let's be safe)
            if (quickShareDir.exists() && quickShareDir.parentFile != downloadsDir) {
                scanDirectory(quickShareDir, allFiles)
            }

            val categorized = allFiles.map { file ->
                val type = when {
                    file.parentFile == downloadsDir -> FileType.INCOMING_DOWNLOADS
                    file.parentFile == quickShareDir -> FileType.INCOMING_QUICK_SHARE
                    else -> getFileType(file)
                }
                
                FileItem(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    type = type,
                    lastModified = file.lastModified()
                )
            }.groupBy { it.type }.mapValues { (_, items) -> sortItems(items, _sortOrder.value) }

            _fileState.value = categorized
            _isLoading.value = false
            Log.d(TAG, "Scanned ${allFiles.size} files in total.")
        }
    }

    fun deleteFiles(items: List<FileItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            items.forEach { item ->
                val file = File(item.path)
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "Deleted file: ${item.path}")
                }
            }
            scanFiles()
        }
    }

    fun renameFile(item: FileItem, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(item.path)
            if (file.exists()) {
                val newFile = File(file.parentFile, newName)
                if (file.renameTo(newFile)) {
                    Log.d(TAG, "Renamed file to: ${newFile.absolutePath}")
                }
            }
            scanFiles()
        }
    }

    fun deleteFilesOlderThan(days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val threshold = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
            var deletedCount = 0
            
            _fileState.value.values.flatten().forEach { item ->
                val file = File(item.path)
                if (file.exists() && file.lastModified() < threshold) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            Log.d(TAG, "Deleted $deletedCount files older than $days days")
            scanFiles()
        }
    }

    private fun getFileType(file: File): FileType {
        val extension = file.extension.lowercase(Locale.getDefault())
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic" -> FileType.IMAGE
            "mp4", "mkv", "avi", "mov", "wmv" -> FileType.VIDEO
            "mp3", "wav", "ogg", "flac", "m4a" -> FileType.AUDIO
            "doc", "docx", "txt", "rtf", "odt" -> FileType.DOC
            "pdf" -> FileType.PDF
            "apk", "xapk", "xapkm", "apkm", "eapk" -> FileType.APK
            else -> FileType.OTHER
        }
    }

    private fun scanDirectory(directory: File, fileList: MutableList<File>) {
        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                scanDirectory(file, fileList)
            } else if (file.isFile) {
                fileList.add(file)
            }
        }
    }

    fun organizeFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val quickShareDir = File(downloadsDir, "Quick Share")
            
            val managedDirs = FileType.values().map { File(downloadsDir, it.displayName) }.toMutableSet()
            managedDirs.add(downloadsDir)
            if (quickShareDir.exists()) managedDirs.add(quickShareDir)
            
            _fileState.value.forEach { (type, items) ->
                val targetDir = File(downloadsDir, type.displayName)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                
                items.forEach { item ->
                    val sourceFile = File(item.path)
                    val targetFile = File(targetDir, item.name)
                    
                    // Move if it's in a managed folder but not its target folder
                    val parent = sourceFile.parentFile
                    if (sourceFile.exists() && parent != null && parent in managedDirs && parent != targetDir) {
                        if (sourceFile.renameTo(targetFile)) {
                            Log.d(TAG, "Repaired/Organized: ${item.name} -> ${type.displayName}")
                        } else {
                            Log.e(TAG, "Failed to move: ${item.name} from ${sourceFile.parent} to ${targetDir.absolutePath}")
                        }
                    }
                }
            }
            scanFiles() // Rescan after organizing
        }
    }
}
