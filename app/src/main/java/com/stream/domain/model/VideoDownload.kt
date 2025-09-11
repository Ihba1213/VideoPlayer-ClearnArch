package com.stream.domain.model

data class VideoDownload(
    val videoId: String,
    val status: DownloadStatus,
    val progress: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val localPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
