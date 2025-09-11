package com.stream.data.remote.datasource

import com.stream.data.remote.service.PexelsApiService
import com.stream.data.remote.model.PexelsResponse
import javax.inject.Inject

class PexelsRemoteDataSource @Inject constructor(
    private val apiService: PexelsApiService
) {
    suspend fun getPopularVideos(page: Int = 1, perPage: Int = 20): PexelsResponse {
        return apiService.getPopularVideos(perPage = perPage, page = page)
    }
}
