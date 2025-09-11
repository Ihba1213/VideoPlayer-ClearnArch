package com.stream.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.work.WorkManager
import com.stream.domain.manager.PlayerManager
import com.stream.domain.manager.PlayerManagerImpl
import com.stream.domain.repository.VideoRepository
import com.stream.domain.usecase.GetVideoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

// di/ManagerModule.kt
@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun providePlayerManager(
        @ApplicationContext context: Context,
        cache: SimpleCache
    ): PlayerManager {
        return PlayerManagerImpl(context, cache)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideSimpleCache(@ApplicationContext context: Context): SimpleCache {
        val cacheDir = File(context.cacheDir, "exo-cache")
        val cacheSize = 100 * 1024 * 1024L // 100 MB
        return SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(cacheSize))
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}

// Update UseCaseModule to include GetVideoUseCase
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // ... other use cases

    @Provides
    fun provideGetVideoUseCase(videoRepository: VideoRepository): GetVideoUseCase {
        return GetVideoUseCase(videoRepository)
    }
}