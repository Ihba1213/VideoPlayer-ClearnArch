package com.stream.data.remote.model

import com.google.gson.annotations.SerializedName

data class PexelsResponse(
    @SerializedName("page")
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("url")
    val url: String,
    @SerializedName("videos")
    val videos: List<PexelsVideo>
)

data class PexelsVideo(
    @SerializedName("id")
    val id: Int,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("url")
    val url: String,
    @SerializedName("image")
    val image: String,
    @SerializedName("user")
    val user: PexelsUser,
    @SerializedName("video_files")
    val videoFiles: List<PexelsVideoFile>
)

data class PexelsUser(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String
)

data class PexelsVideoFile(
    @SerializedName("id")
    val id: Int,
    @SerializedName("quality")
    val quality: String,
    @SerializedName("file_type")
    val fileType: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
    @SerializedName("link")
    val link: String
)

data class PexelsSearchResponse(
    @SerializedName("page")
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("url")
    val url: String,
    @SerializedName("videos")
    val videos: List<PexelsVideo>
)
