package com.example.gamevault

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.example.gamevault.data.remote.FirebaseAuthHelper
import com.example.gamevault.data.remote.FirebaseLibraryService
import com.example.gamevault.data.local.GameVaultDatabase
import com.example.gamevault.data.repository.LibraryRepository
import com.example.gamevault.ui.screens.login.LoginScreen
import com.example.gamevault.ui.screens.register.RegisterScreen
import com.example.gamevault.ui.screens.games.GameDetails.GameDetailsScreen
import com.google.firebase.auth.FirebaseAuth

enum class GameVaultDestinations(@StringRes val title: Int, val icon: ImageVector) {
    HOMEPAGE(R.string.homepage_screen_title, Icons.Default.Home),
    SEARCH(R.string.searchpage_screen_title, Icons.Default.Search),
    GAMES_LIBRARY(R.string.game_library_screen_title, Icons.Default.List),
    GAME_DETAILS(R.string.game_details_screen_title, Icons.Default.Info),
    GAMES_LIST(R.string.game_list_screen_title, Icons.Default.List),
    LOGIN(R.string.login_screen_title, Icons.Default.Info),
    REGISTER(R.string.register_screen_title, Icons.Default.Info)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GameVaultApp(
    navController: NavHostController = rememberNavController(),
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val db = remember {GameVaultDatabase.getDatabase(context)}
    val dao = db.userGameDao()
    val firebaseService = FirebaseLibraryService()
    val firebaseAuth = FirebaseAuth.getInstance()

    var isLoggedIn by remember { mutableStateOf(firebaseAuth.currentUser != null) }
    var userEmail by remember { mutableStateOf(firebaseAuth.currentUser?.email) }
    var isDataSynced by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firebaseAuth.addAuthStateListener { auth ->
            val wasLoggedOut = !isLoggedIn
            isLoggedIn = auth.currentUser != null
            userEmail = auth.currentUser?.email

            // Reset flagi synchronizacji gdy się wylogowujemy
            if (!isLoggedIn) {
                isDataSynced = false
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && !isDataSynced) {
            try {
                LibraryRepository(dao, firebaseService).syncLocalGamesToCloud()
                isDataSynced = true
            } catch (e: Exception) {
                // Obsługa błędu synchronizacji
                Log.e("GameVaultApp", "Sync failed", e)
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val screenName = route?.substringBefore("?")
    val currentScreen = when {
        route == null -> GameVaultDestinations.HOMEPAGE
        route.startsWith(GameVaultDestinations.HOMEPAGE.name) -> GameVaultDestinations.HOMEPAGE
        route.startsWith(GameVaultDestinations.SEARCH.name) -> GameVaultDestinations.SEARCH
        route.startsWith(GameVaultDestinations.GAMES_LIBRARY.name) -> GameVaultDestinations.GAMES_LIBRARY
        route.startsWith(GameVaultDestinations.GAMES_LIST.name) -> GameVaultDestinations.GAMES_LIST
        route.startsWith(GameVaultDestinations.GAME_DETAILS.name) -> GameVaultDestinations.GAME_DETAILS
        route.startsWith(GameVaultDestinations.LOGIN.name) -> GameVaultDestinations.LOGIN
        route.startsWith(GameVaultDestinations.REGISTER.name) -> GameVaultDestinations.REGISTER
        else -> GameVaultDestinations.HOMEPAGE
    }
    val hideBottomBar =
        screenName == GameVaultDestinations.LOGIN.name || screenName == GameVaultDestinations.REGISTER.name
    val hideTopBar =
        screenName == GameVaultDestinations.LOGIN.name || screenName == GameVaultDestinations.REGISTER.name

    val showBackIcon = navController.previousBackStackEntry != null
            && screenName !in listOf(
        GameVaultDestinations.LOGIN.name,
        GameVaultDestinations.REGISTER.name
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!hideTopBar) {
                GameVaultTopBar(
                    title = stringResource(currentScreen.title),
                    showNavigationIcon = showBackIcon,
                    userEmail = userEmail,
                    showWelcomeMessage = currentScreen == GameVaultDestinations.HOMEPAGE,
                    onLogout = {
                        if (isLoggedIn) {
                            FirebaseAuthHelper.signOut()
                            isLoggedIn = false
                            userEmail = null
                            navController.navigate(GameVaultDestinations.LOGIN.name) {
                                popUpTo(0)
                            }
                        } else {
                            navController.navigate(GameVaultDestinations.LOGIN.name)
                        }
                    },
                    onNavigateUp = {
                        navController.navigateUp()
                    },
                    onToggleTheme = onToggleTheme,
                    isDarkTheme = isDarkTheme
                )
            }
        },
        bottomBar = {
            if (!hideBottomBar) {
                BottomNavigationBar(navController = navController, currentScreen = currentScreen)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) GameVaultDestinations.HOMEPAGE.name else GameVaultDestinations.LOGIN.name,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            composable(GameVaultDestinations.HOMEPAGE.name) {
                HomePageScreen(navController = navController, isDataSynced = isDataSynced)
            }

            composable(GameVaultDestinations.SEARCH.name) {
                SearchScreen(navController, genreFilter = "")
            }

            composable(GameVaultDestinations.GAMES_LIBRARY.name) {
                LibraryScreen(dao, firebaseService)
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

            composable(
                route = "${GameVaultDestinations.SEARCH.name}?genreFilter={genreFilter}",
                arguments = listOf(
                    navArgument("genreFilter") {
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val genreFilter = backStackEntry.arguments?.getString("genreFilter")
                SearchScreen(navController, genreFilter = genreFilter)
            }

            /*composable(GameVaultDestinations.LOGIN.name) {
                LoginScreen(
                    onLoginSuccess = {
                        userEmail = FirebaseAuth.getInstance().currentUser?.email
                        isLoggedIn = true
                        navController.popBackStack()
                    },
                    onRegisterClick = {
                        navController.navigate("REGISTER")
                    },
                    onSkip = {
                        navController.navigate(GameVaultDestinations.HOMEPAGE.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }*/

            composable(
                route = "${GameVaultDestinations.LOGIN.name}?showVerificationDialog={showVerificationDialog}",
                arguments = listOf(
                    navArgument("showVerificationDialog") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val showVerificationDialog = backStackEntry.arguments?.getBoolean("showVerificationDialog") ?: false
                LoginScreen(
                    showVerificationDialog = showVerificationDialog,
                    onLoginSuccess = {
                        userEmail = FirebaseAuth.getInstance().currentUser?.email
                        isLoggedIn = true

                        navController.navigate(GameVaultDestinations.HOMEPAGE.name) {
                            popUpTo(GameVaultDestinations.LOGIN.name) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate("REGISTER")
                    },
                    onSkip = {
                        navController.navigate(GameVaultDestinations.HOMEPAGE.name) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(GameVaultDestinations.REGISTER.name) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("${GameVaultDestinations.LOGIN.name}?showVerificationDialog=true") {
                            popUpTo(GameVaultDestinations.REGISTER.name) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameVaultTopBar(
    title: String,
    showNavigationIcon: Boolean,
    userEmail: String?,
    showWelcomeMessage: Boolean,
    onNavigateUp: () -> Unit,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(text = title, style = MaterialTheme.typography.headlineMedium)
                userEmail?.let {
                    if (showWelcomeMessage) {
                        Text(
                            text = "Welcome ${it.substringBefore("@")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (showNavigationIcon) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme") },
                    onClick = {
                        expanded = false
                        onToggleTheme()
                    }
                )
                if (userEmail != null) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Sign Out",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            expanded = false
                            onLogout()
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Sign In / Register") },
                        onClick = {
                            expanded = false
                            onLogout()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
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

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomDestinations.forEach { destination ->
            val interactionSource = remember { MutableInteractionSource() }
            var isClicked by remember { mutableStateOf(false) }

            val rotation by animateFloatAsState(
                targetValue = if (isClicked) 180f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "iconRotation"
            )

            if (isClicked) {
                LaunchedEffect(destination) {
                    kotlinx.coroutines.delay(500)
                    isClicked = false
                }
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .graphicsLayer { rotationZ = rotation },
                        tint = if (currentScreen == destination)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                },
                label = { Text(stringResource(destination.title)) },
                selected = currentScreen == destination,
                onClick = {
                    if (currentScreen != destination) {
                        isClicked = true
                        navController.navigate(destination.name) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                interactionSource = interactionSource
            )
        }
    }
}
