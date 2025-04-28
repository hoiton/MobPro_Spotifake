package ch.hslu.spotifake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.hslu.spotifake.ui.navigation.BottomNavigation
import ch.hslu.spotifake.ui.navigation.BottomNavigationItem
import ch.hslu.spotifake.ui.navigation.SpotifakeScreens
import ch.hslu.spotifake.ui.theme.SpotifakeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpotifakeTheme {
                val navController = rememberNavController()
                val navigationItems = remember {
                    mutableStateListOf(
                        BottomNavigationItem(
                            route = SpotifakeScreens.Player.name,
                            title = SpotifakeScreens.Player.name,
                            selectedIcon = Icons.Filled.PlayArrow,
                            unselectedIcon = Icons.Outlined.Home,
                            hasNews = false
                        ),
                        BottomNavigationItem(
                            route = SpotifakeScreens.Library.name,
                            title = SpotifakeScreens.Library.name,
                            selectedIcon = Icons.AutoMirrored.Filled.List,
                            unselectedIcon = Icons.AutoMirrored.Outlined.List,
                            hasNews = false
                        ),
                        // ToDo: Add Third view
//                        BottomNavigationItem(
//                            route = SpotifakeScreens.Settings.name,
//                            title = SpotifakeScreens.Settings.name,
//                            selectedIcon = Icons.Filled.Star,
//                            unselectedIcon = Icons.Outlined.Star,
//                            hasNews = true,
//                        )
                    )
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigation(
                            navController = navController,
                            navigationItems = navigationItems
                        )
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
        startDestination = SpotifakeScreens.Player.name,
        modifier = modifier,
    ) {
        composable(route = SpotifakeScreens.Player.name) {
            Text(text = "Player Screen")
        }
        composable(route = SpotifakeScreens.Library.name) {
            Text(text = "Library Screen")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SpotifakeTheme {
        Greeting("Android")
    }
}