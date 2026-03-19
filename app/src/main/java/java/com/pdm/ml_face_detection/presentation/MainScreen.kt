package com.pdm.ml_face_detection.presentation

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.pdm.ml_face_detection.R
import com.pdm.ml_face_detection.domain.models.FaceResult
import com.pdm.ml_face_detection.presentation.components.CameraPreview
import com.pdm.ml_face_detection.presentation.components.FaceResultOverlay
import java.com.pdm.ml_face_detection.presentation.components.FaceResultOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val cameraPermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)

    if (cameraPermissionState.status.isGranted) {
        MainScreenContent(
            state = viewModel.uiState.value,
            onRotateCamera = { viewModel.rotateCamera() },
            onResult = { viewModel.updateFaceDetection(it) }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                "Камера необходима для работы приложения. Пожалуйста, разрешите доступ."
            } else {
                "Для работы функций детекции лиц требуется разрешение на использование камеры."
            }
            Text(
                text = textToShow,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Разрешить")
            }
        }
    }
}

@Composable
fun MainScreenContent(
    state: UIState,
    onRotateCamera: () -> Unit,
    onResult: (FaceResult) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Предпросмотр камеры
        CameraPreview(Modifier.fillMaxSize(), state.camera) {
            onResult(it)
        }

        // 2. Рамка вокруг лица (Overlay)
        FaceResultOverlay(
            modifier = Modifier.fillMaxSize(),
            faceResult = state.faceResult,
            isBackCamera = state.camera == CameraSelector.LENS_FACING_BACK
        )

        // 3. Маска на экране
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.ellipse_face),
                contentDescription = null,
                alpha = 0.4f
            )
        }

        // 4. Интерфейс управления и индикаторы
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Индикаторы состояния лица
            FaceStatusIndicators(state.faceResult)

            Spacer(modifier = Modifier.weight(1f))

            if (!state.faceResult.faceVisible) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "ЛИЦО НЕ НАЙДЕНО",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Кнопка поворота
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onRotateCamera() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(id = R.drawable.baseline_cameraswitch_24),
                        contentDescription = "Rotate",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun FaceStatusIndicators(result: FaceResult) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 40.dp)
    ) {
        StatusBadge(label = "😊", isActive = result.faceNeutralExpression)
        StatusBadge(label = "👁️ L", isActive = result.leftEyeOpen)
        StatusBadge(label = "👁️ R", isActive = result.rightEyeOpen)
    }
}

@Composable
fun StatusBadge(label: String, isActive: Boolean) {
    Surface(
        shape = CircleShape,
        color = (if (isActive) Color.Green else Color.Red).copy(alpha = 0.7f),
        contentColor = Color.White
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ContentPreview() {
    MainScreenContent(state = UIState(), onRotateCamera = {}, onResult = {})
}
