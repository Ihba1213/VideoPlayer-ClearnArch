package com.stream.domain.usecase

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.stream.domain.model.Video
import com.stream.domain.model.DownloadStatus
import com.stream.domain.repository.DownloadRepository
import com.stream.worker.VideoDownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DownloadVideoUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val downloadRepository: DownloadRepository
) {
    suspend operator fun invoke(video: Video) {
        // Check if already downloading or downloaded
        val existingStatus = downloadRepository.getDownloadStatus(video.id)
        if (existingStatus in listOf(DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING)) {
            return
        }

        // Create download request
        val downloadData = workDataOf(
            VideoDownloadWorker.KEY_VIDEO_ID to video.id,
            VideoDownloadWorker.KEY_VIDEO_URL to video.videoUrl
        )

        val downloadRequest = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
            .setInputData(downloadData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag("video_download_${video.id}")
            .build()

        // Enqueue the download
        workManager.enqueue(downloadRequest)

        // Insert into repository queue
        downloadRepository.queueDownload(video)
    }
}


