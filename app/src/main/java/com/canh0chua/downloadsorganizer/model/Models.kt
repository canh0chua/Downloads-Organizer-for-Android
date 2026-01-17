package com.canh0chua.downloadsorganizer.model

enum class FileType(val displayName: String) {
    IMAGE("Images"),
    VIDEO("Videos"),
    AUDIO("Audio"),
    DOC("Documents"),
    PDF("PDFs"),
    APK("APKs"),
    OTHER("Other")
}

data class FileItem(
    val name: String,
    val path: String,
    val size: Long,
    val type: FileType,
    val lastModified: Long
)
