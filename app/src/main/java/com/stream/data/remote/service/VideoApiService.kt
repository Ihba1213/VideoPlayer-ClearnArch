package com.stream.data.remote.service

import com.google.gson.annotations.SerializedName
import com.stream.domain.model.Video

data class VideoDto(
    @SerializedName("_id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnail") val thumbnailUrl: String,
    @SerializedName("sources") val videoUrl: String,
    @SerializedName("duration") val duration: Long,
    @SerializedName("publishedAt") val publishedAt: String,
    @SerializedName("author") val author: String
) {
    fun toVideo(): Video {
        return Video(
            id = id,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            duration = duration,
            publishedAt = publishedAt,
            author = author
        )
    }
}