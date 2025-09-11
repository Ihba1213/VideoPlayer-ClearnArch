package com.stream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val videoId: String,
    val status: String, // Enum as string: NOT_DOWNLOADED, QUEUED, DOWNLOADING, DOWNLOADED, FAILED
    val progress: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val localPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
