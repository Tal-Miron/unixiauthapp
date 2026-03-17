package com.unixi.authapp.home.tab2

import androidx.lifecycle.ViewModel
import com.unixi.authapp.util.DeviceInfoProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Tab2ViewModel(
    deviceInfoProvider: DeviceInfoProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        deviceInfoProvider.getDeviceInfo().let { deviceInfo ->
            Tab2UiState(
                model = deviceInfo.model,
                manufacturer = deviceInfo.manufacturer,
                osVersion = deviceInfo.osVersion,
                sdkVersion = deviceInfo.sdkVersion.toString(),
                language = deviceInfo.language,
                os = deviceInfo.os,
                appVersion = deviceInfo.appVersion
            )
        }
    )

    val uiState: StateFlow<Tab2UiState> = _uiState.asStateFlow()
}