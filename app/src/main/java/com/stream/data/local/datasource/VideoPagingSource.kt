package com.stream.data.local.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.stream.data.remote.datasource.PexelsRemoteDataSource
import com.stream.data.local.dao.VideoDao
import com.stream.domain.model.Video
import com.stream.data.remote.model.PexelsVideo
import com.stream.presentation.utils.toVideo
import com.stream.presentation.utils.toVideoEntity
import java.io.IOException

class VideoPagingSource(
    private val pexelsRemoteDataSource: PexelsRemoteDataSource,
    private val videoDao: VideoDao
) : PagingSource<Int, Video>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Video> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize
            val offset = (page - 1) * pageSize
            
            println("DEBUG: Loading page $page with pageSize $pageSize, offset $offset")
            
            // First, try to get videos from cache
            val cachedVideos = videoDao.getVideos(pageSize, offset)
            println("DEBUG: Found ${cachedVideos.size} cached videos for page $page")
            
            if (cachedVideos.isNotEmpty()) {
                // Return cached videos
                val videos = cachedVideos.map { it.toVideo() }
                println("DEBUG: Returning ${videos.size} cached videos")
                
                LoadResult.Page(
                    data = videos,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (videos.size < pageSize) null else page + 1
                )
            } else {
                println("DEBUG: No cached data, fetching from API")
                val pexelsResponse = pexelsRemoteDataSource.getPopularVideos(page, pageSize)
                val videos = pexelsResponse.videos.map { it.toVideo() }

                if (videos.isNotEmpty()) {
                    val videoEntities = videos.map { it.toVideoEntity() }
                    videoDao.insertAll(videoEntities)
                    println("DEBUG: Cached ${videoEntities.size} videos to database")
                }
                
                LoadResult.Page(
                    data = videos,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (videos.isEmpty()) null else page + 1
                )
            }
        } catch (e: IOException) {
            println("DEBUG: IOException in VideoPagingSource: ${e.message}")
            LoadResult.Error(e)
        } catch (e: Exception) {
            println("DEBUG: Exception in VideoPagingSource: ${e.message}")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Video>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
