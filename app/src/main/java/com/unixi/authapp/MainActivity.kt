package com.unixi.authapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.unixi.authapp.ui.theme.UnixiAuthAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deviceInfoProvider = DeviceInfoProvider(this)
        val deviceInfo = deviceInfoProvider.getDeviceInfo()

        setContent {
            UnixiAuthAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        deviceInfo = deviceInfo,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}



@Composable
fun Greeting(deviceInfo: DeviceInfo, modifier: Modifier = Modifier) {
    Text(
        text = "Model: ${deviceInfo.model}\n" +
                "Manufacturer: ${deviceInfo.manufacturer}\n" +
                "OS: ${deviceInfo.os}\n" +
                "OS Version: ${deviceInfo.osVersion}\n" +
                "SDK: ${deviceInfo.sdkVersion}\n" +
                "Language: ${deviceInfo.language}\n" +
                "App Version: ${deviceInfo.appVersion}",
        modifier = modifier
    )
}