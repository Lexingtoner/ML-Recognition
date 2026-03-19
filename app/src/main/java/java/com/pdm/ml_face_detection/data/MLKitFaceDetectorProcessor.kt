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

@Suppress("OPT_IN_ARGUMENT_IS_NOT_MARKER")
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
            val rotationDegrees = image.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
            
            // Определяем размеры с учетом поворота для корректного маппинга координат
            val width = if (rotationDegrees % 180 == 0) image.width else image.height
            val height = if (rotationDegrees % 180 == 0) image.height else image.width

            detectInImage(inputImage)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        onSuccess(FaceResult(faceVisible = false, imageWidth = width, imageHeight = height))
                    } else {
                        onSuccess(getFaceResults(faces, width, height))
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

    private fun getFaceResults(faces: List<Face>, width: Int, height: Int): FaceResult {
        val face = faces[0]
        var faceResult = FaceResult(
            faceVisible = true,
            faces = face as List<android.hardware.camera2.params.Face>,
            imageWidth = width,
            imageHeight = height
        )
        
        face.smilingProbability?.let {
            faceResult = faceResult.copy(faceNeutralExpression = it <= 0.2f)
        }

        face.leftEyeOpenProbability?.let {
            faceResult = faceResult.copy(leftEyeOpen = it >= 0.6f)
        }
        face.rightEyeOpenProbability?.let {
            faceResult = faceResult.copy(rightEyeOpen = it >= 0.6f)
        }
        
        return faceResult
    }

    override fun stop() {
        detector.close()
    }
}
