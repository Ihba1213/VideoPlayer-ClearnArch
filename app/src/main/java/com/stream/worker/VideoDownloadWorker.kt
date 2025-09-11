package com.stream.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.stream.domain.repository.DownloadRepository
import com.stream.domain.model.DownloadStatus
import com.stream.data.repository.DownloadRepositoryImpl
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class VideoDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DownloadWorkerEntryPoint {
        fun downloadRepository(): DownloadRepository
    }

    override suspend fun doWork(): Result {
        val videoId = inputData.getString(KEY_VIDEO_ID) ?: return Result.failure()
        val videoUrl = inputData.getString(KEY_VIDEO_URL) ?: return Result.failure()

        return try {
            val downloadRepository = EntryPointAccessors.fromApplication(
                applicationContext,
                DownloadWorkerEntryPoint::class.java
            ).downloadRepository()

            // Update status to downloading
            updateDownloadStatus(downloadRepository, videoId, DownloadStatus.DOWNLOADING, 0)

            // Create downloads directory
            val downloadsDir = File(applicationContext.getExternalFilesDir(null), "downloads")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val localFile = File(downloadsDir, "video_${videoId}.mp4")
            
            // Download the video file
            downloadVideoFile(videoUrl, localFile, videoId, downloadRepository)
            
            // Mark as downloaded with actual local path
            val localPath = localFile.absolutePath
            if (downloadRepository is DownloadRepositoryImpl) {
                downloadRepository.updateDownloadComplete(videoId, DownloadStatus.DOWNLOADED, localPath)
            }

            Result.success()
        } catch (e: Exception) {
            val downloadRepository = EntryPointAccessors.fromApplication(
                applicationContext,
                DownloadWorkerEntryPoint::class.java
            ).downloadRepository()
            updateDownloadStatus(downloadRepository, videoId, DownloadStatus.FAILED, 0)
            Result.failure()
        }
    }

    private suspend fun downloadVideoFile(
        videoUrl: String,
        localFile: File,
        videoId: String,
        downloadRepository: DownloadRepository
    ) = withContext(Dispatchers.IO) {
        try {
            val url = URL(videoUrl)
            val connection = url.openConnection()
            val contentLength = connection.contentLength
            val inputStream = connection.getInputStream()
            val outputStream = FileOutputStream(localFile)
            
            val buffer = ByteArray(8192)
            var totalBytesRead = 0L
            var bytesRead: Int
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                // Update progress
                val progress = if (contentLength > 0) {
                    ((totalBytesRead * 100) / contentLength).toInt()
                } else {
                    0
                }
                
                updateDownloadStatus(
                    downloadRepository, 
                    videoId, 
                    DownloadStatus.DOWNLOADING, 
                    progress, 
                    totalBytesRead, 
                    contentLength.toLong()
                )
                
                // Small delay to prevent overwhelming the UI
                delay(10)
            }
            
            inputStream.close()
            outputStream.close()
            
            println("DEBUG: Video downloaded successfully to: ${localFile.absolutePath}")
        } catch (e: Exception) {
            println("DEBUG: Download failed: ${e.message}")
            throw e
        }
    }

    private suspend fun updateDownloadStatus(
        downloadRepository: DownloadRepository,
        videoId: String,
        status: DownloadStatus,
        progress: Int,
        downloadedBytes: Long = 0,
        totalBytes: Long = 1000000L // 1MB dummy total
    ) {
        withContext(Dispatchers.IO) {
            if (downloadRepository is DownloadRepositoryImpl) {
                downloadRepository.updateDownloadProgress(
                    videoId = videoId,
                    status = status,
                    progress = progress,
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes
                )
            }
        }

        setProgressAsync(
            workDataOf(
                KEY_VIDEO_ID to videoId,
                KEY_PROGRESS to progress,
                KEY_STATUS to status.name
            )
        )
    }

    companion object {
        const val KEY_VIDEO_ID = "video_id"
        const val KEY_VIDEO_URL = "video_url"
        const val KEY_PROGRESS = "progress"
        const val KEY_STATUS = "status"
    }
}