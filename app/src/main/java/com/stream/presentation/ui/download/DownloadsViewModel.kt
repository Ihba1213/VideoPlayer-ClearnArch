package com.stream.presentation.ui.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stream.domain.model.VideoDownload
import com.stream.domain.repository.DownloadRepository
import com.stream.data.repository.DownloadRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _downloads = MutableStateFlow<List<VideoDownload>>(emptyList())
    val downloads: StateFlow<List<VideoDownload>> = _downloads.asStateFlow()

    private val _navigationEvent = MutableStateFlow<String?>(null)
    val navigationEvent: StateFlow<String?> = _navigationEvent.asStateFlow()

    init {
        viewModelScope.launch {
            downloadRepository.getDownloads().collect { downloadList ->
                _downloads.value = downloadList
            }
        }
    }

    fun cancelDownload(videoId: String) {
        viewModelScope.launch { 
            downloadRepository.cancelDownload(videoId)
        }
    }

    fun deleteDownload(videoId: String) {
        viewModelScope.launch {
            if (downloadRepository is DownloadRepositoryImpl) {
                downloadRepository.deleteDownload(videoId)
            }
        }
    }

    fun playDownload(videoId: String) {
        _navigationEvent.value = videoId
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}




