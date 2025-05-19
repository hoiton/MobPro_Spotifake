package ch.hslu.spotifake

import MiniPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.hslu.spotifake.ui.library.LibraryViewModel
import ch.hslu.spotifake.ui.library.PlaylistScreen
import ch.hslu.spotifake.ui.library.TracksScreen
import ch.hslu.spotifake.ui.navigation.BottomNavigation
import ch.hslu.spotifake.ui.navigation.BottomNavigationItem
import ch.hslu.spotifake.ui.navigation.SpotifakeScreens
import ch.hslu.spotifake.ui.player.PlayerView
import ch.hslu.spotifake.ui.theme.SpotifakeTheme
import ch.hslu.spotifake.ui.upload.UploadView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpotifakeTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val navigationItems = remember {
                    mutableStateListOf(
                        BottomNavigationItem(
                            route = SpotifakeScreens.Library.route,
                            title = SpotifakeScreens.Library.name,
                            selectedIcon = Icons.AutoMirrored.Filled.List,
                            unselectedIcon = Icons.AutoMirrored.Outlined.List,
                        ),
                        BottomNavigationItem(
                            route = SpotifakeScreens.Playlists.route,
                            title = SpotifakeScreens.Playlists.name,
                            selectedIcon = Icons.Default.FolderOpen,
                            unselectedIcon = Icons.Default.FolderOpen,
                        ),
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column {
                            if (currentRoute != SpotifakeScreens.Player.route) {
                                MiniPlayer(
                                    onClick = { navController.navigate(SpotifakeScreens.Player.route) }
                                )
                            }
                            BottomNavigation(
                                navController = navController,
                                navigationItems = navigationItems
                            )
                        }
                    }
                ) { innerPadding ->
                    SpotifakeNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SpotifakeNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = SpotifakeScreens.Library.route,
        modifier = modifier,
    ) {
        composable(route = SpotifakeScreens.Player.route) {
            PlayerView()
        }
        composable(route = SpotifakeScreens.Upload.route) {
            UploadView(
                navHostController = navController,
            )
        }
        composable(route = SpotifakeScreens.Library.route) {
            TracksScreen(
                playlistId = 0,
                viewModel = hiltViewModel(),
                navHostController = navController,
                isPrimaryLibrary = true,
            )
        }
        composable(route = SpotifakeScreens.Playlists.route) {
            val viewModel: LibraryViewModel = hiltViewModel()
            PlaylistScreen(
                viewModel = viewModel,
                onPlaylistSelected = { playlistId ->
                    navController.navigate("track/$playlistId")
                }
            )
        }
        composable(
            route = "track/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.IntType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getInt("playlistId") ?: return@composable
            val viewModel: LibraryViewModel = hiltViewModel()
            TracksScreen(
                viewModel = viewModel,
                playlistId = playlistId,
                navHostController = navController,
                isPrimaryLibrary = false,
            )
        }
    }
}
