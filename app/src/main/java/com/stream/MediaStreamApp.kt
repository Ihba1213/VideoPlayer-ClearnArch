package com.stream

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.stream.presentation.navigation.Screen
import com.stream.presentation.ui.list.VideoListScreen
import com.stream.presentation.ui.player.VideoPlayerScreen
import com.stream.presentation.ui.download.DownloadsScreen
import com.stream.presentation.theme.Bullet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MediaStreamingApp() {
    val appState = rememberMediaStreamingAppState()
    Bullet {
        Scaffold(
            bottomBar = {
                if (appState.shouldShowBottomBar) {
                    BottomNavigationBar(
                        destinations = appState.topLevelDestinations,
                        currentDestination = appState.currentDestination,
                        onNavigateToDestination = appState::navigate
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavigationHost(appState)
            }
        }
    }
}


@Composable
fun NavigationHost(appState: MediaStreamingAppState) {
    NavHost(
        navController = appState.navController,
        startDestination = Screen.VideoList.route
    ) {
        composable(Screen.VideoList.route) {
            VideoListScreen(
                onNavigateToVideoPlayer = { videoId ->
                    appState.navigateToVideoPlayer(videoId)
                }
            )
        }
        composable(
            route = Screen.VideoPlayer.routeWithArgs,
            arguments = listOf(
                navArgument(Screen.VideoPlayer.videoIdArg) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString(Screen.VideoPlayer.videoIdArg)
            VideoPlayerScreen(
                videoId = videoId ?: "",
                onNavigateBack = { appState.navController.popBackStack() }
            )
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateToVideoPlayer = { videoId ->
                    appState.navigateToVideoPlayer(videoId)
                }
            )
        }
    }
}

@Stable
class MediaStreamingAppState(
    val navController: NavHostController,
    private val coroutineScope: CoroutineScope
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val shouldShowBottomBar: Boolean
        @Composable get() = currentDestination?.route in topLevelDestinations.map { it.route }

    val topLevelDestinations = listOf(
        Screen.VideoList,
        Screen.Downloads
    )

    fun navigate(destination: Screen) {
        coroutineScope.launch {
            navController.navigate(destination.route) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun navigateToVideoPlayer(videoId: String) {
        coroutineScope.launch {
            navController.navigate(Screen.VideoPlayer.createRoute(videoId))
        }
    }

    fun popBackStack() {
        navController.popBackStack()
    }
}

@Composable
fun rememberMediaStreamingAppState(
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(navController, coroutineScope) {
    MediaStreamingAppState(navController, coroutineScope)
}

@Composable
fun BottomNavigationBar(
    destinations: List<Screen>,
    currentDestination: NavDestination?,
    onNavigateToDestination: (Screen) -> Unit
) {
    NavigationBar {
        destinations.forEach { screen ->
            val selected = currentDestination?.route == screen.route

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) }
            )
        }
    }
}