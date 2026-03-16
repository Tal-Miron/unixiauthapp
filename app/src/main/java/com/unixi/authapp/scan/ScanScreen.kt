package com.unixi.authapp.scan
import androidx.compose.ui.graphics.graphicsLayer
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unixi.authapp.util.CameraPreview
import org.koin.androidx.compose.koinViewModel

@Composable
fun ScanScreen(
    onNavigateToAuth: () -> Unit,
    onShowError: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isPermissionPermanentlyDenied by remember {
        mutableStateOf(false)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted

        if (!isGranted && activity != null) {
            isPermissionPermanentlyDenied =
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.CAMERA
                )
        }
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
/*

    LaunchedEffect(uiState.dialogState) {
        when (val dialogState = uiState.dialogState) {
            ScanDialogState.NotFound -> {
                onShowError("The QR code could not be resolved.")
            }

            is ScanDialogState.InvalidUrl -> {
                onShowError(dialogState.message)
            }

            is ScanDialogState.UnknownError -> {
                onShowError(
                    dialogState.message ?: "An unexpected error occurred."
                )
            }

            is ScanDialogState.RateLimited,
            null -> Unit
        }
    }
*/

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            hasCameraPermission -> {
                ScanCameraWindow()
            }

            isPermissionPermanentlyDenied -> {
                CameraPermissionSettingsContent(
                    onOpenSettings = {
                        openAppSettings(context)
                    }
                )
            }

            else -> {
                CameraPermissionContent(
                    onRequestPermission = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
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
private fun ScanCameraWindow(
    modifier: Modifier = Modifier,
    onQrScanned: ((String) -> Unit)? = null,
    viewModel: ScanViewModel = koinViewModel()
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val instructionColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        CameraPreview(
            onQrScanned = onQrScanned ?: viewModel::onQrScanned
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
                .drawWithContent {
                    drawContent()

                    drawRect(
                        color = backgroundColor
                    )

                    val windowSize = minOf(size.width, size.height) * 0.72f
                    val left = (size.width - windowSize) / 2f
                    val top = (size.height - windowSize) / 2f

                    drawRect(
                        color = Color.Transparent,
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(windowSize, windowSize),
                        blendMode = BlendMode.Clear
                    )

                    drawRect(
                        color = instructionColor,
                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(windowSize, windowSize),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
        )

        Text(
            text = "Align the QR code inside the square",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@Composable
private fun CameraPermissionContent(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Camera permission is required to scan a QR code.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
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
private fun CameraPermissionSettingsContent(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Camera permission was denied. Please enable it in app settings.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(
                onClick = onOpenSettings
            ) {
                Text(text = "Open Settings")
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
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f)),
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
        is ScanDialogState.InvalidFormat -> "Invalid QR Format"
        is ScanDialogState.InvalidUrl -> "Invalid Backend URL"
        is ScanDialogState.UnknownError -> "Something Went Wrong"
        is ScanDialogState.RateLimited -> "Too Many Attempts"
    }

    val message = when (dialogState) {
        ScanDialogState.NotFound -> {
            "The QR code could not be resolved. Please try again."
        }

        is ScanDialogState.InvalidFormat -> {
            dialogState.message
        }

        is ScanDialogState.InvalidUrl -> {
            dialogState.message
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

private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}