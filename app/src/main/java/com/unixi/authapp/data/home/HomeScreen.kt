package com.unixi.authapp.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unixi.authapp.home.tab1.Tab1Screen
import com.unixi.authapp.home.tab2.Tab2Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            when (uiState.selectedTab) {
                HomeTab.Tab1 -> Tab1Screen()
                HomeTab.Tab2 -> Tab2Screen()
            }
        }

        NavigationBar(
            modifier = Modifier.navigationBarsPadding()
        ) {
            NavigationBarItem(
                selected = uiState.selectedTab == HomeTab.Tab1,
                onClick = {
                    viewModel.onTabSelected(HomeTab.Tab1)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Home"
                    )
                },
                label = {
                    Text(text = "Home")
                }
            )

            NavigationBarItem(
                selected = uiState.selectedTab == HomeTab.Tab2,
                onClick = {
                    viewModel.onTabSelected(HomeTab.Tab2)
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Device Information"
                    )
                },
                label = {
                    Text(text = "Device")
                }
            )
        }
    }
}

@Composable
private fun Tab1Placeholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tab 1 content will appear here.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun Tab2Placeholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Tab 2 content will appear here.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}