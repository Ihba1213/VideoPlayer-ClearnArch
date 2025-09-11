package com.stream.domain.repository

import com.stream.domain.model.Video
import com.stream.domain.model.VideoDownload
import com.stream.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    suspend fun getDownloads(): Flow<List<VideoDownload>>
    suspend fun getDownloadStatus(videoId: String): DownloadStatus?
    suspend fun getDownloadedVideoPath(videoId: String): String?
    suspend fun queueDownload(video: Video)
    suspend fun cancelDownload(videoId: String)
    suspend fun startDownload(video: Video)
    fun getDownloadStates(): Flow<Map<String, DownloadStatus>>
}
