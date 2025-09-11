package com.stream.di

import androidx.media3.cast.BuildConfig
import com.google.gson.GsonBuilder
import com.stream.data.remote.service.PexelsApiService
import com.stream.data.remote.datasource.PexelsRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.pexels.com/v1/"
    private const val API_KEY = "YOUR_API_KEY" // Replace with your actual API key

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithAuth = originalRequest.newBuilder()
                    .header("Authorization", API_KEY)
                    .build()
                chain.proceed(requestWithAuth)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(
                GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create()
            ))
            .build()
    }

    @Provides
    @Singleton
    fun providePexelsApiService(retrofit: Retrofit): PexelsApiService {
        return retrofit.create(PexelsApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePexelsRemoteDataSource(apiService: PexelsApiService): PexelsRemoteDataSource {
        return PexelsRemoteDataSource(apiService)
    }
}