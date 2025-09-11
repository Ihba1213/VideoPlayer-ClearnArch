package com.stream.domain.usecase

import com.stream.domain.model.Video
import com.stream.domain.repository.VideoRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class GetVideoUseCaseTest {

    private lateinit var getVideoUseCase: GetVideoUseCase
    private lateinit var mockVideoRepository: VideoRepository

    @Before
    fun setup() {
        mockVideoRepository = mockk()
        getVideoUseCase = GetVideoUseCase(mockVideoRepository)
    }

    @Test
    fun `invoke should return video when repository returns video`() = runTest {
        // Given
        val videoId = "test_video_123"
        val expectedVideo = createTestVideo()
        coEvery { mockVideoRepository.getVideoById(videoId) } returns expectedVideo

        // When
        val result = getVideoUseCase(videoId)

        // Then
        assertNotNull(result)
        assertEquals(expectedVideo, result)
        assertEquals(videoId, result?.id)
    }

    @Test
    fun `invoke should return null when repository returns null`() = runTest {
        // Given
        val videoId = "non_existent_video"
        coEvery { mockVideoRepository.getVideoById(videoId) } returns null

        // When
        val result = getVideoUseCase(videoId)

        // Then
        assertNull(result)
    }

    @Test
    fun `invoke should handle empty string video id`() = runTest {
        // Given
        val videoId = ""
        coEvery { mockVideoRepository.getVideoById(videoId) } returns null

        // When
        val result = getVideoUseCase(videoId)

        // Then
        assertNull(result)
    }

    @Test
    fun `invoke should handle special characters in video id`() = runTest {
        // Given
        val videoId = "video@#$%^&*()_+"
        val expectedVideo = createTestVideo().copy(id = videoId)
        coEvery { mockVideoRepository.getVideoById(videoId) } returns expectedVideo

        // When
        val result = getVideoUseCase(videoId)

        // Then
        assertNotNull(result)
        assertEquals(videoId, result?.id)
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
