package com.stream.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object VideoList : Screen("video_list", "Home", Icons.Default.Home) {
        const val routeWithArgs = "video_list"
        const val videoIdArg = "videoId"
    }
    object VideoPlayer : Screen("video_player/{videoId}", "Player", Icons.Default.PlayArrow) {
        const val routeWithArgs = "video_player/{videoId}"
        const val videoIdArg = "videoId"
        fun createRoute(videoId: String) = "video_player/$videoId"
    }
    object Downloads : Screen("downloads", "Downloads", Icons.Default.Download) {
        const val routeWithArgs = "downloads"
        const val videoIdArg = "videoId"
    }
}
