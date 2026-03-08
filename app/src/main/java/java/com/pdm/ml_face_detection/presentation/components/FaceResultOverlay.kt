package com.pdm.ml_face_detection.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pdm.ml_face_detection.domain.models.FaceResult

@Supressed("OPT_IN_IS_NOT_ENABLED")
@Composable
fun FaceResultOverlay(
    modifier: Modifier = Modifier,
    faceResult: FaceResult,
    isBackCamera: Boolean
) {
    Canvas(modifier = modifier) {
        if (faceResult.imageWidth <= 0 || faceResult.imageHeight <= 0) return@Canvas

        val scaleX = size.width / faceResult.imageWidth.toFloat()
        val scaleY = size.height / faceResult.imageHeight.toFloat()

        faceResult.faces.forEach { face ->
            val boundingBox = face.bounds

            val correctedLeft = if (!isBackCamera) {
                size.width - (boundingBox.right * scaleX)
            } else {
                boundingBox.left * scaleX
            }
            val correctedRight = if (!isBackCamera) {
                size.width - (boundingBox.left * scaleX)
            } else {
                boundingBox.right * scaleX
            }

            drawRect(
                color = if (faceResult.requirementsMeet) Color.Green else Color.Red,
                topLeft = Offset(
                    x = correctedLeft,
                    y = boundingBox.top * scaleY
                ),
                size = Size(
                    width = correctedRight - correctedLeft,
                    height = (boundingBox.bottom - boundingBox.top) * scaleY
                ),
                style = Stroke(width = 4f)
            )
        }
    }
}
