package com.stream.presentation.ui.download

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.stream.domain.model.VideoDownload
import com.stream.domain.model.DownloadStatus
import com.stream.presentation.ui.download.DownloadsViewModel
import com.stream.presentation.ui.components.EmptyState

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel(),
    onNavigateToVideoPlayer: (String) -> Unit = {}
) {
    val downloads by viewModel.downloads.collectAsState(emptyList())
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { videoId ->
            onNavigateToVideoPlayer(videoId)
            viewModel.onNavigationHandled()
        }
    }

    DownloadsContent(
        downloads = downloads,
        onCancelDownload = viewModel::cancelDownload,
        onDeleteDownload = viewModel::deleteDownload,
        onPlayDownload = viewModel::playDownload
    )
}

@Composable
fun DownloadsContent(
    downloads: List<VideoDownload>,
    onCancelDownload: (String) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onPlayDownload: (String) -> Unit
) {
    if (downloads.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Download,
            message = "No downloads yet"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(downloads, key = { it.videoId }) { download ->
                DownloadItem(
                    download = download,
                    onCancel = { onCancelDownload(download.videoId) },
                    onDelete = { onDeleteDownload(download.videoId) },
                    onPlay = { onPlayDownload(download.videoId) }
                )
            }
        }
    }
}

@Composable
fun DownloadItem(
    download: VideoDownload,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title and status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Video ${download.videoId}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = download.status.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (download.status) {
                        DownloadStatus.DOWNLOADED -> Color(0xFF4CAF50)
                        DownloadStatus.DOWNLOADING -> Color(0xFF2196F3)
                        DownloadStatus.FAILED -> Color(0xFFF44336)
                        else -> Color(0xFF9E9E9E)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar for downloading items
            if (download.status == DownloadStatus.DOWNLOADING) {
                LinearProgressIndicator(
                    progress = { download.progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${download.progress}%",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Cancel, "Cancel download")
                        }
                    }
                    DownloadStatus.DOWNLOADED -> {
                        IconButton(onClick = onPlay) {
                            Icon(Icons.Default.PlayArrow, "Play video")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete download")
                        }
                    }
                    DownloadStatus.FAILED -> {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete failed download")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}