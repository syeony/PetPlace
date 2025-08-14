package com.example.petplace.data.local.onDevice

data class Detection(
    val label: String,           // "cat" | "dog"
    val score: Float,            // 0.0 ~ 1.0
    val left: Float,             // 원본 비트맵 좌표계
    val top: Float,
    val right: Float,
    val bottom: Float
)
data class PetBoxDto(
    val xmin: Int,
    val ymin: Int,
    val xmax: Int,
    val ymax: Int,
    val wFace: Double // = confidence
)
