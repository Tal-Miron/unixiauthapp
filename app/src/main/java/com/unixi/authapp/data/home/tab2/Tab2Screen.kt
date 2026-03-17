package com.unixi.authapp.home.tab2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun Tab2Screen(
    modifier: Modifier = Modifier,
    viewModel: Tab2ViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Device Information",
                style = MaterialTheme.typography.headlineSmall
            )

            HorizontalDivider()

            DeviceInfoRow(
                label = "Device Model",
                value = uiState.model
            )

            DeviceInfoRow(
                label = "Manufacturer",
                value = uiState.manufacturer
            )

            DeviceInfoRow(
                label = "Operating System",
                value = uiState.os
            )

            DeviceInfoRow(
                label = "OS Version",
                value = uiState.osVersion
            )

            DeviceInfoRow(
                label = "SDK Version",
                value = uiState.sdkVersion
            )

            DeviceInfoRow(
                label = "Language",
                value = uiState.language
            )

            DeviceInfoRow(
                label = "App Version",
                value = uiState.appVersion
            )
        }
    }
}

@Composable
private fun DeviceInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}