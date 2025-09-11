package com.stream.data.remote.datasource

import com.stream.domain.model.Video
import com.stream.presentation.utils.toVideo
import com.stream.data.remote.service.PexelsApiService
import java.io.IOException
import javax.inject.Inject

class ApiDataSource @Inject constructor(
    private val pexelsApiService: PexelsApiService
) {
    suspend fun getPopularVideos(page: Int, perPage: Int = 20): List<Video> {
        return try {
            val response = pexelsApiService.getPopularVideos(page, perPage)
            response.videos.map { it.toVideo() }
        } catch (e: Exception) {
            throw IOException("Failed to fetch popular videos: ${e.message}", e)
        }
    }

    suspend fun getVideoById(id: String): Video? {
        return try {
            val pexelsVideo = pexelsApiService.getVideoById(id.toInt())
            pexelsVideo.toVideo()
        } catch (e: Exception) {
            null // Return null if video not found or error occurs
        }
    }
}