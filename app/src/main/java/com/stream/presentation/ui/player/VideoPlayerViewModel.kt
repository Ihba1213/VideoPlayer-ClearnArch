package com.stream.presentation.ui.player

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Player
import com.stream.domain.model.Video
import com.stream.domain.repository.DownloadRepository
import com.stream.domain.usecase.GetVideoUseCase
import com.stream.domain.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// presentation/viewmodel/VideoPlayerViewModel.kt
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val getVideoUseCase: GetVideoUseCase,
    private val playerManager: PlayerManager,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _playerState = mutableStateOf(PlayerState())
    val playerState: State<PlayerState> = _playerState

    private val _uiState = mutableStateOf(VideoPlayerUiState())
    val uiState: State<VideoPlayerUiState> = _uiState

    val player: Player? get() = playerManager.player

    fun initializePlayer(videoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _playerState.value = _playerState.value.copy(isLoading = true)

            try {
                // Check if video is downloaded locally
                val localPath = downloadRepository.getDownloadedVideoPath(videoId)

                if (localPath != null) {
                    // Play from local storage
                    val localFile = java.io.File(localPath)
                    if (localFile.exists()) {
                        playerManager.playVideo(Uri.fromFile(localFile), isLocal = true)
                        _uiState.value = _uiState.value.copy(
                            videoSource = VideoSource.LOCAL,
                            video = null // We don't need video info for local playback
                        )
                        println("DEBUG: Playing local video from: $localPath")
                    } else {
                        println("DEBUG: Local file not found: $localPath")
                        _uiState.value = _uiState.value.copy(
                            error = "Downloaded video file not found"
                        )
                    }
                } else {
                    // Fetch video info and play from network
                    val video = getVideoUseCase(videoId)
                    if (video != null) {
                        println("DEBUG: Video found - Title: ${video.title}, URL: ${video.videoUrl}")
                        
                        // Use the actual video URL from the API
                        playerManager.playVideo(Uri.parse(video.videoUrl), isLocal = false)
                        _uiState.value = _uiState.value.copy(
                            videoSource = VideoSource.REMOTE,
                            video = video
                        )
                    } else {
                        println("DEBUG: Video not found for ID: $videoId, trying test video")
                        // Fallback to a test video URL
                        val testVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        playerManager.playVideo(Uri.parse(testVideoUrl), isLocal = false)
                        _uiState.value = _uiState.value.copy(
                            videoSource = VideoSource.REMOTE,
                            video = null
                        )
                        println("DEBUG: Using test video URL: $testVideoUrl")
                    }
                }

                // Start observing player state
                observePlayerState()

                _playerState.value = _playerState.value.copy(
                    isLoading = false,
                    error = null
                )
                
                // Show controls when video loads
                _uiState.value = _uiState.value.copy(showControls = true)
                println("DEBUG: Controls shown - video loaded")
                
                // Add a small delay to ensure player is ready
                kotlinx.coroutines.delay(1000)
                println("DEBUG: Player ready, isPlaying: ${playerManager.player?.isPlaying}")
            } catch (e: Exception) {
                _playerState.value = _playerState.value.copy(
                    isLoading = false,
                    error = "Failed to load video: ${e.message}"
                )
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load video: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun observePlayerState() {
        playerManager.player?.let { player ->
            println("DEBUG: Setting up player state observer")
            
            // Start a coroutine to update position periodically
            viewModelScope.launch {
                while (player == playerManager.player) {
                    kotlinx.coroutines.delay(500) // Update more frequently
                    val wasPlaying = _playerState.value.isPlaying
                    val isPlaying = player.isPlaying
                    
                    _playerState.value = _playerState.value.copy(
                        isPlaying = isPlaying,
                        position = player.currentPosition,
                        bufferedPosition = player.bufferedPosition,
                        duration = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L,
                        isLoading = player.playbackState == Player.STATE_BUFFERING
                    )
                    
                    // Show controls when video starts playing
                    if (!wasPlaying && isPlaying) {
                        _uiState.value = _uiState.value.copy(showControls = true)
                        println("DEBUG: Auto-showing controls - video started playing")
                        
                        // Auto-hide controls after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        if (isPlaying) {
                            _uiState.value = _uiState.value.copy(showControls = false)
                            println("DEBUG: Auto-hiding controls after 3 seconds")
                        }
                    }
                }
            }
        }
    }

    fun play() {
        println("DEBUG: Play button clicked")
        playerManager.play()
    }

    fun pause() {
        println("DEBUG: Pause button clicked")
        playerManager.pause()
    }

    fun seekTo(position: Long) {
        playerManager.seekTo(position)
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun togglePlayPause() {
        if (playerState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }


    fun toggleControls() {
        _uiState.value = _uiState.value.copy(showControls = !uiState.value.showControls)
    }

    fun hideControls() {
        _uiState.value = _uiState.value.copy(showControls = false)
    }

    fun showControls() {
        _uiState.value = _uiState.value.copy(showControls = true)
    }

    fun setError(error: String?) {
        _playerState.value = _playerState.value.copy(error = error)
        _uiState.value = _uiState.value.copy(error = error)
    }

    fun clearError() {
        _playerState.value = _playerState.value.copy(error = null)
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun releasePlayer() {
        playerManager.release()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

data class PlayerState(
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val error: String? = null,
    val duration: Long = 0,
    val position: Long = 0,
    val bufferedPosition: Long = 0
)

data class VideoPlayerUiState(
    val isLoading: Boolean = false,
    val video: Video? = null,
    val videoSource: VideoSource = VideoSource.REMOTE,
    val showControls: Boolean = true,
    val error: String? = null
)

enum class VideoSource {
    LOCAL, REMOTE
}