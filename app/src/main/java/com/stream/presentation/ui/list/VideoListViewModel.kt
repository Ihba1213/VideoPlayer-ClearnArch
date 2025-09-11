package com.stream.presentation.ui.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.stream.domain.model.Video
import com.stream.domain.model.DownloadStatus
import com.stream.domain.repository.VideoRepository
import com.stream.domain.usecase.GetVideosUseCase
import com.stream.domain.usecase.DownloadVideoUseCase
import com.stream.domain.usecase.GetDownloadStatusUseCase
import com.stream.data.repository.VideoRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

// presentation/viewmodel/VideoListViewModel.kt
@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val downloadVideoUseCase: DownloadVideoUseCase,
    private val getDownloadStatusUseCase: GetDownloadStatusUseCase,
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(VideoListUiState())
    val uiState: State<VideoListUiState> = _uiState

    val videos = getVideosUseCase().cachedIn(viewModelScope)

    private val _downloadStates = mutableStateMapOf<String, DownloadStatus>()
    val downloadStates: Map<String, DownloadStatus> get() = _downloadStates

    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent: State<String?> = _navigationEvent

    init {
        observeDownloadStates()
    }

    fun onVideoClick(video: Video) {
        _navigationEvent.value = video.id
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    fun onDownloadClick(video: Video) {
        viewModelScope.launch {
            try {
                downloadVideoUseCase(video)
                _uiState.value = _uiState.value.copy(
                    message = "Download started for ${video.title}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start download: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    fun refreshVideos() {
        viewModelScope.launch {
            try {
                if (videoRepository is VideoRepositoryImpl) {
                    videoRepository.clearCache()
                    _uiState.value = _uiState.value.copy(
                        message = "Cache cleared, refreshing videos..."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to refresh videos: ${e.message}"
                )
            }
        }
    }
    
    fun checkCacheStatus() {
        viewModelScope.launch {
            try {
                if (videoRepository is VideoRepositoryImpl) {
                    val status = videoRepository.getCacheStatus()
                    _uiState.value = _uiState.value.copy(
                        message = "Cache status: ${status.totalCachedVideos} videos cached"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to check cache status: ${e.message}"
                )
            }
        }
    }

    private fun observeDownloadStates() {
        viewModelScope.launch {
            // This would observe a flow from the download repository
            // For now, we'll simulate it with a simple loop
            while (true) {
                delay(1000)
                // In a real implementation, this would update from a Flow
                // For example: downloadRepository.getDownloadStates().collect { states ->
                //     _downloadStates.putAll(states)
                // }
            }
        }
    }
}

data class VideoListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)