package com.pdm.ml_face_detection.presentation

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.pdm.ml_face_detection.domain.FaceDetectorProcessor
import com.pdm.ml_face_detection.domain.models.FaceResult

class ImageAnalyzer(
    private val processor: FaceDetectorProcessor,
    private val onResults: (FaceResult) -> Unit
) : ImageAnalysis.Analyzer {

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        processor.processFace(
            image,
            onSuccess = {
                onResults(it)
            },
            onFailure = {
                onResults(FaceResult())
            },
            onComplete = {
                image.close()
            }
        )
    }
}
