package com.pdm.ml_face_detection.data

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.pdm.ml_face_detection.domain.FaceDetectorProcessor
import com.pdm.ml_face_detection.domain.models.FaceResult

class MLKitFaceDetectorProcessor : FaceDetectorProcessor {

    private val detector: FaceDetector

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        detector = FaceDetection.getClient(options)
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class)
    override fun processFace(
        image: ImageProxy,
        onSuccess: (FaceResult) -> Unit,
        onFailure: (Exception) -> Unit,
        onComplete: () -> Unit
    ) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            detectInImage(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        onSuccess(FaceResult(faceVisible = false))
                    } else {
                        onSuccess(getFaceResults(faces[0]))
                    }
                }
                .addOnFailureListener {
                    onFailure(it)
                }
                .addOnCompleteListener {
                    onComplete()
                }
        } else {
            onComplete()
        }
    }

    private fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    private fun getFaceResults(face: Face): FaceResult {
        var faceResult = FaceResult(faceVisible = true)
        
        face.smilingProbability?.let {
            faceResult = faceResult.copy(faceNeutralExpression = it <= 0.2f)
        }

        face.leftEyeOpenProbability?.let {
            faceResult = faceResult.copy(leftEyeOpen = it >= 0.6f)
        }
        face.rightEyeOpenProbability?.let {
            faceResult = faceResult.copy(rightEyeOpen = it >= 0.6f)
        }
        
        face.headEulerAngleY.let {
            when {
                it < -5.5f -> { /* повернута влево */ }
                it > 5.5f -> { /* повернута вправо */ }
            }
        }
        
        return faceResult
    }

    override fun stop() {
        detector.close()
    }
}
