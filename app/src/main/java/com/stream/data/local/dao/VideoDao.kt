package com.stream.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stream.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<VideoEntity>)

    // Query operations
    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): VideoEntity?

    @Query("SELECT * FROM videos ORDER BY lastAccessed DESC LIMIT :limit OFFSET :offset")
    suspend fun getVideos(limit: Int, offset: Int): List<VideoEntity>


    @Query("SELECT * FROM videos ORDER BY lastAccessed DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    // Update operations
    @Query("UPDATE videos SET lastAccessed = :timestamp WHERE id = :videoId")
    suspend fun updateLastAccessed(videoId: String, timestamp: Long = System.currentTimeMillis())

    // Delete operations
    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: String)

    @Query("DELETE FROM videos")
    suspend fun clearAllVideos()

    // Paging source
    @Query("SELECT * FROM videos ORDER BY lastAccessed DESC")
    fun getVideosPagingSource(): PagingSource<Int, VideoEntity>

    // Count operations
    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getVideoCount(): Int
}