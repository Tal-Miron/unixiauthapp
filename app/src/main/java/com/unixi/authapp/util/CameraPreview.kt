package com.unixi.authapp.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

    val cameraExecutor = rememberCameraExecutor()

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            bindCameraUseCases(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                cameraProvider = cameraProvider,
                cameraExecutor = cameraExecutor,
                onQrScanned = onQrScanned
            )
        }

        cameraProviderFuture.addListener(
            listener,
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            cameraProviderFuture.get().unbindAll()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView }
    )
}

@Composable
private fun rememberCameraExecutor(): ExecutorService {
    return remember {
        Executors.newSingleThreadExecutor()
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    cameraExecutor: ExecutorService,
    onQrScanned: (String) -> Unit
) {
    val preview = Preview.Builder()
        .build()
        .also { cameraPreview ->
            cameraPreview.setSurfaceProvider(previewView.surfaceProvider)
        }

    val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    val barcodeScanner = BarcodeScanning.getClient(scannerOptions)

    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { analysis ->
            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image

                if (mediaImage == null) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val inputImage = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        handleDetectedBarcodes(
                            barcodes = barcodes,
                            onQrScanned = onQrScanned
                        )
                    }
                    .addOnFailureListener { exception ->
                        Log.e("CameraPreview", "QR scan failed.", exception)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }

    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageAnalyzer
    )
}

private fun handleDetectedBarcodes(
    barcodes: List<Barcode>,
    onQrScanned: (String) -> Unit
) {
    val qrValue = barcodes
        .firstOrNull()
        ?.rawValue
        ?.takeIf { it.isNotBlank() }
        ?: return

    onQrScanned(qrValue)
}