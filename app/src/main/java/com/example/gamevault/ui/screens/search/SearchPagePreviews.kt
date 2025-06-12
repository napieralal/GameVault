package com.example.gamevault.ui.screens.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.gamevault.ui.theme.GameVaultTheme

@Preview(showBackground = true)
@Composable
fun SearchBarWithFilterIconPreview() {
    GameVaultTheme {
        SearchBarWithFilterIcon(
            query = "Zelda",
            onQueryChange = {},
            onFilterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SortBarPreview() {
    GameVaultTheme {
        SortBar(
            currentSort = SortType.RELEVANCE,
            onSortSelected = {}
        )
    }
}


