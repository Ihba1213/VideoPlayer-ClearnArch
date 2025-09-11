package com.stream.data.remote.service

import com.stream.data.remote.model.PexelsResponse
import com.stream.data.remote.model.PexelsVideo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PexelsApiService {

    @GET("videos/popular")
    suspend fun getPopularVideos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 20
    ): PexelsResponse

    @GET("videos/videos/{id}")
    suspend fun getVideoById(@Path("id") id: Int): PexelsVideo
}