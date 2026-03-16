package com.unixi.authapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.unixi.authapp.navigation.AppNavGraph
import com.unixi.authapp.ui.theme.UnixiAuthAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnixiAuthAppTheme {
                AppNavGraph()
            }
        }
    }
}



/*
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
}*/
