package com.pdm.ml_face_detection.domain.models

import android.hardware.camera2.params.Face

data class FaceResult(
    val faceVisible: Boolean = false,
    val faceNeutralExpression: Boolean = false,
    val leftEyeOpen: Boolean = false,
    val rightEyeOpen: Boolean = false,
    val headPosition: HeadPosition = HeadPosition(),
    val faces: List<Face> = emptyList(),
    val imageWidth: Int = 0,
    val imageHeight: Int = 0
) {
    val requirementsMeet: Boolean
        get() = faceVisible && faceNeutralExpression && leftEyeOpen
                && rightEyeOpen && headPosition.isValid
}
