package com.stream.data.repository

import com.stream.data.local.dao.DownloadDao
import com.stream.data.local.entity.DownloadEntity
import com.stream.domain.model.Video
import com.stream.domain.model.VideoDownload
import com.stream.domain.model.DownloadStatus
import com.stream.domain.repository.DownloadRepository
import com.stream.presentation.utils.toVideoDownload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// data/repository/DownloadRepositoryImpl.kt
class DownloadRepositoryImpl @Inject constructor(
    private val downloadDao: DownloadDao
) : DownloadRepository {

    override suspend fun getDownloads(): Flow<List<VideoDownload>> =
        downloadDao.getAllDownloads().map { list -> list.map { it.toVideoDownload() } }

    override suspend fun getDownloadStatus(videoId: String): DownloadStatus? {
        return withContext(Dispatchers.IO) {
            downloadDao.getDownloadByVideoId(videoId)?.toVideoDownload()?.status
        }
    }

    override suspend fun getDownloadedVideoPath(videoId: String): String? {
        return withContext(Dispatchers.IO) {
            downloadDao.getDownloadByVideoId(videoId)?.localPath
        }
    }

    override suspend fun queueDownload(video: Video) {
        withContext(Dispatchers.IO) {
            val downloadEntity = DownloadEntity(
                videoId = video.id,
                status = DownloadStatus.QUEUED.name,
                progress = 0,
                downloadedBytes = 0,
                totalBytes = 0
            )
            downloadDao.insert(downloadEntity)
        }
    }

    suspend fun updateDownloadProgress(
        videoId: String,
        status: DownloadStatus,
        progress: Int,
        downloadedBytes: Long,
        totalBytes: Long
    ) {
        withContext(Dispatchers.IO) {
            downloadDao.updateDownloadProgress(
                videoId = videoId,
                status = status.name,
                progress = progress,
                downloadedBytes = downloadedBytes,
                totalBytes = totalBytes
            )
        }
    }

    suspend fun updateDownloadComplete(
        videoId: String,
        status: DownloadStatus,
        localPath: String
    ) {
        withContext(Dispatchers.IO) {
            downloadDao.updateDownloadComplete(
                videoId = videoId,
                status = status.name,
                localPath = localPath
            )
        }
    }

    override suspend fun cancelDownload(videoId: String) {
        withContext(Dispatchers.IO) {
            downloadDao.deleteDownload(videoId)
        }
    }

    override suspend fun startDownload(video: Video) {
        queueDownload(video)
    }

    override fun getDownloadStates(): Flow<Map<String, DownloadStatus>> {
        return downloadDao.getAllDownloads().map { downloads ->
            downloads.associate { it.videoId to DownloadStatus.valueOf(it.status) }
        }
    }

    suspend fun deleteDownload(videoId: String) {
        withContext(Dispatchers.IO) {
            // First delete the downloaded file if it exists
            val download = downloadDao.getDownloadByVideoId(videoId)
            download?.localPath?.let { path ->
                try {
                    File(path).delete()
                } catch (e: Exception) {
                    // Log error but continue with database deletion
                }
            }

            // Then delete the database record
            downloadDao.deleteDownload(videoId)
        }
    }
}