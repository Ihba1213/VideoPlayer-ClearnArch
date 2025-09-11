package com.stream.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.stream.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity)

    // Query operations
    @Query("SELECT * FROM downloads WHERE videoId = :videoId")
    suspend fun getDownloadByVideoId(videoId: String): DownloadEntity?

    @Query("SELECT * FROM downloads ORDER BY updatedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status")
    fun getDownloadsByStatus(status: String): Flow<List<DownloadEntity>>

    // Update operations
    @Query("UPDATE downloads SET status = :status, progress = :progress, downloadedBytes = :downloadedBytes, totalBytes = :totalBytes, updatedAt = :timestamp WHERE videoId = :videoId")
    suspend fun updateDownloadProgress(videoId: String, status: String, progress: Int, downloadedBytes: Long, totalBytes: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE downloads SET status = :status, localPath = :localPath, updatedAt = :timestamp WHERE videoId = :videoId")
    suspend fun updateDownloadComplete(videoId: String, status: String, localPath: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE downloads SET status = :status, updatedAt = :timestamp WHERE videoId = :videoId")
    suspend fun updateDownloadStatus(videoId: String, status: String, timestamp: Long = System.currentTimeMillis())

    // Delete operations
    @Query("DELETE FROM downloads WHERE videoId = :videoId")
    suspend fun deleteDownload(videoId: String)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()
}
