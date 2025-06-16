package com.example.gamevault

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamevault.data.remote.RetrofitClient
import com.example.gamevault.ui.screens.games.GamesLibrary.LibraryViewModel
import com.example.gamevault.ui.screens.games.GameListScreen.GameListScreenViewModel
import com.example.gamevault.ui.screens.games.GameDetails.GameDetailsViewModel
import com.example.gamevault.ui.screens.homepage.HomePageViewModel
import com.example.gamevault.ui.screens.search.SearchViewModel


object GameVaultAppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            GameListScreenViewModel(
                this.createSavedStateHandle(),
                gamesListApiService = RetrofitClient.apiServiceInstance
            )
        }

        initializer {
            GameDetailsViewModel(
                this.createSavedStateHandle(),
                gameDetailsApiService = RetrofitClient.apiServiceInstance,
            )
        }

        initializer {
            SearchViewModel(
                this.createSavedStateHandle(),
                searchApiService = RetrofitClient.apiServiceInstance,
            )
        }

        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
            HomePageViewModel(
                application,
                this.createSavedStateHandle(),
                homeApiService = RetrofitClient.apiServiceInstance,
                )
            /*HomePageViewModel(
                this.createSavedStateHandle(),
                homeApiService = RetrofitClient.apiServiceInstance,
            )*/
        }

        initializer {
            val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
            LibraryViewModel(application)
        }

        /*initializer {
            JokeListViewModel(
                this.createSavedStateHandle()
            )
        }*/
    }

}
   /*
    val PreviewFactory = viewModelFactory {
        initializer {
            CategorySelectViewModel()
        }

        initializer {
            JokeListViewModel(
                SavedStateHandle(
                    mapOf("category" to "Programming")
                )
            )
        }
    }
}*/
