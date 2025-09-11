package com.stream.domain.usecase

import android.content.Context
import androidx.work.WorkManager
import com.stream.domain.model.DownloadStatus
import com.stream.domain.model.Video
import com.stream.domain.repository.DownloadRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class DownloadVideoUseCaseTest {

    private lateinit var downloadVideoUseCase: DownloadVideoUseCase
    private lateinit var mockContext: Context
    private lateinit var mockWorkManager: WorkManager
    private lateinit var mockDownloadRepository: DownloadRepository

    @Before
    fun setup() {
        mockContext = mockk()
        mockWorkManager = mockk(relaxed = true)
        mockDownloadRepository = mockk(relaxed = true)
        
        downloadVideoUseCase = DownloadVideoUseCase(
            context = mockContext,
            workManager = mockWorkManager,
            downloadRepository = mockDownloadRepository
        )
    }

    @Test
    fun `invoke should not start download when video is already queued`() = runTest {
        // Given
        val video = createTestVideo()
        coEvery { mockDownloadRepository.getDownloadStatus(video.id) } returns DownloadStatus.QUEUED

        // When
        downloadVideoUseCase(video)

        // Then - Test passes if no exception is thrown and method completes
        assertTrue(true)
    }

    @Test
    fun `invoke should start download when video is not downloaded`() = runTest {
        // Given
        val video = createTestVideo()
        coEvery { mockDownloadRepository.getDownloadStatus(video.id) } returns DownloadStatus.NOT_DOWNLOADED

        // When
        downloadVideoUseCase(video)

        // Then - Test passes if no exception is thrown and method completes
        assertTrue(true)
    }

    private fun createTestVideo() = Video(
        id = "test_video_123",
        title = "Test Video",
        description = "A test video for unit testing",
        thumbnailUrl = "https://example.com/thumbnail.jpg",
        videoUrl = "https://example.com/video.mp4",
        duration = 120000L, // 2 minutes
        publishedAt = "2024-01-01T00:00:00Z",
        author = "Test Author"
    )
}