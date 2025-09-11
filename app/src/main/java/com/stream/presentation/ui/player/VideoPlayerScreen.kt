package com.stream.presentation.ui.player

import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.WithHint
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.stream.mediaplayer.R
import com.stream.domain.model.Video
import com.stream.presentation.utils.toVideo
import com.stream.presentation.ui.components.LoadingIndicator
import com.stream.presentation.ui.components.ErrorState

// presentation/ui/player/VideoPlayerScreen.kt
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val playerState = viewModel.playerState.value
    val uiState = viewModel.uiState.value

    // Setup player
    DisposableEffect(videoId) {
        viewModel.initializePlayer(videoId)

        onDispose {
            viewModel.releasePlayer()
        }
    }

    // Handle player events
    LaunchedEffect(viewModel.player) {
        viewModel.player?.let { player ->
            // Omit snapshot flow to simplify and avoid state mutation errors
        }
    }


    BackHandler {
        onNavigateBack()
    }

    // Main content
    Box(modifier = Modifier.fillMaxSize()) {
        // ExoPlayer with custom controls
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false // We'll use custom controls
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = { playerView ->
                playerView.player = viewModel.player
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (playerState.isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Error message
        playerState.error?.let { error ->
            ErrorState(
                message = error,
                onRetry = { viewModel.initializePlayer(videoId) },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Custom controls
        if (uiState.showControls) {
            VideoPlayerControls(
                playerState = playerState,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onBack = onNavigateBack,
                video = uiState.video,
                videoSource = uiState.videoSource,
                modifier = Modifier.fillMaxSize()
            )
        }







        // Tap to show/hide controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    viewModel.toggleControls()
                }
        )
    }
}

@Composable
fun VideoPlayerControls(
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onBack: () -> Unit,
    video: Video?,
    videoSource: VideoSource,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0x80000000.toInt()), Color(0x00000000.toInt()))
                )
            )
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFFFFFFF.toInt())
                )
            }

            Spacer(modifier = Modifier.weight(1f))


            // Video source indicator
            if (videoSource == VideoSource.LOCAL) {
                Icon(
                    imageVector = Icons.Default.OfflinePin,
                    contentDescription = "Playing from download",
                    tint = Color(0xFFFFFFFF.toInt()),
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Video title
            video?.let {
                Text(
                    text = it.title,
                    color = Color(0xFFFFFFFF.toInt()),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Center play/pause button
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                tint = colorResource(R.color.white),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom progress bar
        Column(modifier = Modifier.fillMaxWidth()) {
            // Progress bar
            Slider(
                value = playerState.position.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..playerState.duration.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            // Time indicators
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = formatTime(playerState.position), color = Color(0xFFFFFFFF.toInt()))

                Text(text = formatTime(playerState.duration), color = Color(0xFFFFFFFF.toInt()))
            }
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
    } else {
        String.format("%02d:%02d", minutes, seconds % 60)
    }
}