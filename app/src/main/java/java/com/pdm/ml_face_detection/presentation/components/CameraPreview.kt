package com.pdm.ml_face_detection.presentation.components

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pdm.ml_face_detection.data.MLKitFaceDetectorProcessor
import com.pdm.ml_face_detection.domain.models.FaceResult
import com.pdm.ml_face_detection.presentation.ImageAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraLensFacing: Int = CameraSelector.LENS_FACING_BACK,
    onResult: (FaceResult) -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    val cameraExecutor: ExecutorService = remember {
        Executors.newSingleThreadExecutor()
    }

    val analyzer = remember {
        ImageAnalyzer(
            MLKitFaceDetectorProcessor(),
            onResults = onResult
        )
    }

    DisposableEffect(cameraLensFacing) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx)
        },
        update = { previewView ->
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(cameraLensFacing)
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build()
                        .apply {
                            setAnalyzer(cameraExecutor, analyzer)
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )

                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", e)
                }

            }, ContextCompat.getMainExecutor(context))
        }
    )
}
