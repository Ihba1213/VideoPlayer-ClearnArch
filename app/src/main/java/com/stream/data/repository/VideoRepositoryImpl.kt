package com.stream.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.stream.data.local.dao.VideoDao
import com.stream.data.remote.datasource.PexelsRemoteDataSource
import com.stream.data.local.datasource.VideoPagingSource
import com.stream.domain.model.Video
import com.stream.domain.repository.VideoRepository
import com.stream.presentation.utils.toVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
    private val pexelsRemoteDataSource: PexelsRemoteDataSource
) : VideoRepository {

    override fun getVideos(): Flow<PagingData<Video>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { VideoPagingSource(pexelsRemoteDataSource, videoDao) }
        ).flow
    }

    override suspend fun getVideoById(id: String): Video? {
        return withContext(Dispatchers.IO) {
            videoDao.getVideoById(id)?.toVideo()
        }
    }

    
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            videoDao.clearAllVideos()
            println("DEBUG: Video cache cleared")
        }
    }
    
    suspend fun getCacheStatus(): CacheStatus {
        return withContext(Dispatchers.IO) {
            val count = videoDao.getVideoCount()
            CacheStatus(
                totalCachedVideos = count,
                isCacheEmpty = count == 0
            )
        }
    }
}

data class CacheStatus(
    val totalCachedVideos: Int,
    val isCacheEmpty: Boolean
)
