package com.stream.presentation.utils

import com.stream.data.local.entity.VideoEntity
import com.stream.data.local.entity.DownloadEntity
import com.stream.domain.model.Video
import com.stream.domain.model.VideoDownload
import com.stream.domain.model.DownloadStatus
import com.stream.data.remote.model.PexelsVideo

// Extension functions for converting between data models and domain models
fun Video.toVideoEntity(): VideoEntity {
    return VideoEntity(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        videoUrl = videoUrl,
        duration = duration,
        publishedAt = publishedAt,
        author = author
    )
}

fun VideoEntity.toVideo(): Video {
    return Video(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
        videoUrl = videoUrl,
        duration = duration,
        publishedAt = publishedAt,
        author = author
    )
}

fun DownloadEntity.toVideoDownload(): VideoDownload {
    return VideoDownload(
        videoId = videoId,
        status = try {
            DownloadStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            DownloadStatus.NOT_DOWNLOADED
        },
        progress = progress,
        downloadedBytes = downloadedBytes,
        totalBytes = totalBytes,
        localPath = localPath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun VideoDownload.toDownloadEntity(): DownloadEntity {
    return DownloadEntity(
        videoId = videoId,
        status = status.name,
        progress = progress,
        downloadedBytes = downloadedBytes,
        totalBytes = totalBytes,
        localPath = localPath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun PexelsVideo.toVideo(): Video {
    return Video(
        id = id.toString(),
        title = "Video ${id}",
        description = "Video from Pexels",
        thumbnailUrl = image,
        videoUrl = videoFiles.firstOrNull()?.link ?: "",
        duration = duration.toLong() * 1000, // Convert to milliseconds
        publishedAt = "",
        author = user.name
    )
}