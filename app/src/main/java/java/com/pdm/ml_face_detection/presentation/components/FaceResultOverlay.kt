package java.com.pdm.ml_face_detection.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pdm.ml_face_detection.domain.models.FaceResult

@Composable
fun FaceResultOverlay(
    modifier: Modifier = Modifier,
    faceResult: FaceResult,
    isBackCamera: Boolean
) {
    Canvas(modifier = modifier) {
        // Масштабируем координаты рамки из размеров изображения в размеры Canvas
        val scaleX = size.width / faceResult.imageWidth.toFloat()
        val scaleY = size.height / faceResult.imageHeight.toFloat()

        faceResult.faces.forEach { face ->
            val boundingBox = face.bounds

            // Зеркалим рамку по горизонтали, если используется задняя камера
            val correctedLeft = if (isBackCamera) {
                size.width - (boundingBox.right * scaleX)
            } else {
                boundingBox.left * scaleX
            }
            val correctedRight = if (isBackCamera) {
                size.width - (boundingBox.left * scaleX)
            } else {
                boundingBox.right * scaleX
            }

            drawRect(
                color = Color.Red,
                topLeft = Offset(
                    x = correctedLeft,
                    y = boundingBox.top * scaleY
                ),
                size = Size(
                    width = correctedRight - correctedLeft,
                    height = (boundingBox.bottom - boundingBox.top) * scaleY
                ),
                style = Stroke(width = 2f)
            )
        }
    }
}