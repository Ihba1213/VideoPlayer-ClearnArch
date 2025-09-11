package com.stream.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.stream.data.local.dao.VideoDao
import com.stream.data.local.dao.DownloadDao
import com.stream.data.local.entity.VideoEntity
import com.stream.data.local.entity.DownloadEntity

@Database(
    entities = [VideoEntity::class, DownloadEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "media_stream_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}