package com.stream.domain.usecase

import com.stream.domain.model.Video
import com.stream.domain.repository.VideoRepository
import javax.inject.Inject

class GetVideoUseCase @Inject constructor(
    private val videoRepository: VideoRepository
) {
    suspend operator fun invoke(id: String): Video? {
        return videoRepository.getVideoById(id)
    }
}


