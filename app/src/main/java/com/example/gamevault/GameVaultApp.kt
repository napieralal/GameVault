package com.example.gamevault

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gamevault.ui.screens.games.GameListScreen.GameListScreen
import com.example.gamevault.ui.screens.games.GamesLibrary.LibraryScreen
import com.example.gamevault.ui.screens.homepage.HomePageScreen
import com.example.gamevault.ui.screens.search.SearchScreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import com.example.gamevault.ui.screens.games.GameDetails.GameDetailsScreen

enum class GameVaultDestinations(@StringRes val title: Int, val icon: ImageVector) {
    HOMEPAGE(R.string.homepage_screen_title, Icons.Default.Home),
    SEARCH(R.string.searchpage_screen_title, Icons.Default.Search),
    GAMES_LIBRARY(R.string.game_library_screen_title, Icons.Default.List),
    GAME_DETAILS(R.string.game_details_screen_title, Icons.Default.Info),
    GAMES_LIST(R.string.game_list_screen_title, Icons.Default.List),
}

@Composable
fun GameVaultApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val screenName = backStackEntry?.destination?.route?.substringBefore("/")
    /*val currentScreenTitle = stringResource(
        GameVaultDestinations.valueOf(screenName ?: GameVaultDestinations.HOMEPAGE.name).title
    )*/
    val currentScreen = GameVaultDestinations.values().find { it.name == screenName } ?: GameVaultDestinations.HOMEPAGE

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GameVaultTopBar(
                title = stringResource(currentScreen.title),
                showNavigationIcon = navController.previousBackStackEntry != null
            ) {
                navController.navigateUp()
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, currentScreen = currentScreen)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = GameVaultDestinations.HOMEPAGE.name,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable(GameVaultDestinations.HOMEPAGE.name) {
                HomePageScreen(onNavigateToGames = {
                    navController.navigate(GameVaultDestinations.GAMES_LIST.name)
                })
            }

            composable(GameVaultDestinations.SEARCH.name) {
                SearchScreen(navController)
            }

            composable(GameVaultDestinations.GAMES_LIBRARY.name) {
                LibraryScreen()
            }

            composable(GameVaultDestinations.GAMES_LIST.name) {
                GameListScreen(navController)
            }

            composable(
                "${GameVaultDestinations.GAME_DETAILS.name}/{gameId}",
                arguments = listOf(navArgument("gameId") {
                    type = NavType.LongType
                })
            ) { backStackEntry ->
                GameDetailsScreen(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameVaultTopBar(
    title: String,
    showNavigationIcon: Boolean,
    onNavigateUp: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
        },
        navigationIcon = {
            if (showNavigationIcon)
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
        }
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentScreen: GameVaultDestinations
) {
    val bottomDestinations = listOf(
        GameVaultDestinations.HOMEPAGE,
        GameVaultDestinations.SEARCH,
        GameVaultDestinations.GAMES_LIBRARY
    )

    NavigationBar {
        bottomDestinations.forEach { destination ->
            NavigationBarItem(
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.title)) },
                selected = currentScreen == destination,
                onClick = {
                    if (currentScreen != destination) {
                        navController.navigate(destination.name) {
                            popUpTo(GameVaultDestinations.HOMEPAGE.name) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
