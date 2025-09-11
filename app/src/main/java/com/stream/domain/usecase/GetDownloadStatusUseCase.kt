package com.stream.domain.usecase

import com.stream.domain.model.DownloadStatus
import com.stream.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadStatusUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    operator fun invoke(): Flow<Map<String, DownloadStatus>> {
        return downloadRepository.getDownloadStates()
    }
}


