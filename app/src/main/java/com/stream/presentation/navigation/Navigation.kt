package com.stream.presentation.ui.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object VideoList : Screen("video_list", "Home", Icons.Default.Home)
    data object Downloads : Screen("downloads", "Downloads", Icons.Default.Download)
    data object VideoPlayer : Screen("video/{videoId}", "Player", Icons.Default.Home) {
        const val videoIdArg = "videoId"
        val routeWithArgs = "video/{$videoIdArg}"
        fun createRoute(videoId: String) = "video/$videoId"
    }
}




