package com.stream.domain.usecase

import androidx.paging.PagingData
import com.stream.domain.model.Video
import com.stream.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVideosUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    operator fun invoke(): Flow<PagingData<Video>> {
        return videoRepository.getVideos()
    }
}


