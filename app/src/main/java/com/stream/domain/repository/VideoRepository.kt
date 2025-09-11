package com.stream.domain.repository

import androidx.paging.PagingData
import com.stream.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getVideos(): Flow<PagingData<Video>>
    suspend fun getVideoById(id: String): Video?
}