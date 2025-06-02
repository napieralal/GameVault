package com.example.gamevault

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamevault.network.RetrofitClient
import com.example.gamevault.ui.screens.games.GameListScreen.GameListScreenViewModel
import com.example.gamevault.ui.screens.games.GameDetails.GameDetailsViewModel
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
