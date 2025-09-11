package com.stream.presentation.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.stream.domain.model.Video
import com.stream.domain.model.DownloadStatus
import com.stream.presentation.ui.list.VideoListViewModel
import com.stream.presentation.theme.LocalAppColors
import okhttp3.internal.concurrent.formatDuration

@Composable
fun VideoListScreen(
    viewModel: VideoListViewModel = hiltViewModel(),
    onNavigateToVideoPlayer: (String) -> Unit = {}
) {
    val videos = viewModel.videos.collectAsLazyPagingItems()
    val downloadStates = viewModel.downloadStates
    val navigationEvent = viewModel.navigationEvent.value
    val uiState = viewModel.uiState.value

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { videoId ->
            onNavigateToVideoPlayer(videoId)
            viewModel.onNavigationHandled()
        }
    }

    VideoListContent(
        videos = videos,
        downloadStates = downloadStates,
        onVideoClick = viewModel::onVideoClick,
        onDownloadClick = viewModel::onDownloadClick,
        onRefresh = { 
            videos.refresh()
            viewModel.refreshVideos()
        },
        onCheckCache = viewModel::checkCacheStatus,
        uiState = uiState,
        onClearMessage = viewModel::clearMessage,
        onClearError = viewModel::clearError
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListContent(
    videos: LazyPagingItems<Video>,
    downloadStates: Map<String, DownloadStatus>,
    onVideoClick: (Video) -> Unit,
    onDownloadClick: (Video) -> Unit,
    onRefresh: () -> Unit,
    onCheckCache: () -> Unit,
    uiState: VideoListUiState,
    onClearMessage: () -> Unit,
    onClearError: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyGridState(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cache status header
            item(span = { GridItemSpan(maxLineSpan) }) {
                CacheStatusHeader(
                    onCheckCache = onCheckCache,
                    onRefresh = onRefresh
                )
            }
            items(
                count = videos.itemCount,
                key = { index -> videos[index]?.id ?: index }
            ) { index ->
                videos[index]?.let { video ->
                    VideoListItem(
                        video = video,
                        downloadStatus = downloadStates[video.id],
                        onVideoClick = { onVideoClick(video) },
                        onDownloadClick = { onDownloadClick(video) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                when (videos.loadState.append) {
                    is LoadState.Loading -> {
                        CenteredLoadingIndicator(
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                    is LoadState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorIndicator(
                                message = "Failed to load more videos",
                                onRetry = { videos.retry() }
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        // Initial loading state
        if (videos.loadState.refresh is LoadState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
            }
        }
        
        // Initial error state
        if (videos.loadState.refresh is LoadState.Error) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ErrorIndicator(
                    message = "Failed to load videos",
                    onRetry = { videos.refresh() },
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
        
        // Show messages and errors from ViewModel
        uiState.message?.let { message ->
            Snackbar(
                action = {
                    TextButton(onClick = onClearMessage) {
                        Text("Dismiss")
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(message)
            }
        }
        
        uiState.error?.let { error ->
            Snackbar(
                action = {
                    TextButton(onClick = onClearError) {
                        Text("Dismiss")
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun VideoListItem(
    video: Video,
    downloadStatus: DownloadStatus?,
    onVideoClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Card(
        modifier = modifier,
        onClick = onVideoClick
    ) {
        Column {
            Box(modifier = Modifier.aspectRatio(16f / 9f)) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                DurationBadge(
                    duration = video.duration,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )


                downloadStatus?.let { status ->
                    DownloadStatusIndicator(
                        status = status,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = video.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Download button
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    when (downloadStatus) {
                        DownloadStatus.DOWNLOADING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        DownloadStatus.DOWNLOADED -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Downloaded"
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DurationBadge(duration: Long, modifier: Modifier = Modifier) {
    val durationText = remember(duration) {
        formatDuration(duration)
    }

    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = durationText,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
fun DownloadStatusIndicator(status: DownloadStatus, modifier: Modifier = Modifier) {
    val icon = remember(status) {
        when (status) {
            DownloadStatus.DOWNLOADING -> Icons.Default.Circle
            DownloadStatus.DOWNLOADED -> Icons.Default.CheckCircle
            DownloadStatus.FAILED -> Icons.Default.Error
            else -> null
        }
    }

    val color = remember(status) {
        when (status) {
            DownloadStatus.DOWNLOADING -> Color.Blue
            DownloadStatus.DOWNLOADED -> Color.Green
            DownloadStatus.FAILED -> Color.Red
            else -> Color.Transparent
        }
    }

    icon?.let {
        Icon(
            imageVector = it,
            contentDescription = status.name,
            tint = color,
            modifier = modifier
        )
    }
}


@Composable
fun CenteredLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Debug: Add a background to see the centering
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color.Black.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
fun ErrorIndicator(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(48.dp),
            tint = Color.Red
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        IconButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CacheStatusHeader(
    onCheckCache: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Video Cache Status",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        
        Row {
            IconButton(onClick = onCheckCache) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Check Cache"
                )
            }
            
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Refresh"
                )
            }
        }
    }
}