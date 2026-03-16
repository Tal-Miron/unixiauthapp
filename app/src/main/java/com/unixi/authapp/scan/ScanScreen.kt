package com.unixi.authapp.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unixi.authapp.util.CameraPreview
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScanScreen(
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState.navigateToAuth) {
        if (uiState.navigateToAuth) {
            onNavigateToAuth()
            viewModel.onNavigationHandled()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (hasCameraPermission) {
            CameraPreview(
                onQrScanned = viewModel::onQrScanned
            )
        } else {
            CameraPermissionContent(
                onRequestPermission = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }

        if (uiState.isLoading) {
            LoadingContent()
        }

        uiState.dialogState?.let { dialogState ->
            ScanDialog(
                dialogState = dialogState,
                onDismiss = viewModel::dismissDialog
            )
        }
    }
}

@Composable
private fun CameraPermissionContent(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Camera permission is required to scan a QR code.",
                style = MaterialTheme.typography.bodyLarge
            )

            TextButton(
                onClick = onRequestPermission
            ) {
                Text(text = "Grant Permission")
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun ScanDialog(
    dialogState: ScanDialogState,
    onDismiss: () -> Unit
) {
    val title = when (dialogState) {
        ScanDialogState.NotFound -> "QR Code Not Found"
        is ScanDialogState.UnknownError -> "Something Went Wrong"
        is ScanDialogState.RateLimited -> "Too Many Attempts"
    }

    val message = when (dialogState) {
        ScanDialogState.NotFound -> {
            "The QR code could not be resolved. Please try again."
        }

        is ScanDialogState.UnknownError -> {
            dialogState.message ?: "An unexpected error occurred. Please try again."
        }

        is ScanDialogState.RateLimited -> {
            "Too many failed attempts. Please wait ${dialogState.remainingSeconds} seconds before scanning again."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "OK")
            }
        }
    )
}