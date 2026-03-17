package com.unixi.authapp.home

data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.Tab1
)

enum class HomeTab {
    Tab1,
    Tab2
}