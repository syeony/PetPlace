package com.example.petplace.data.local.Walk

data class Post(
    val id: Long,                 // ✅ 추가
    val category: String,
    val title: String,
    val body: String,
    val date: String,
    val time: String,

    // 대표 썸네일 1장 (sort==1 또는 최솟값)
    val imageUrl: String,

    // ✅ 전체 이미지 리스트(상세에서 슬라이드용)
    val images: List<String> = emptyList(),

    val reporterName: String,
    val reporterAvatarUrl: String?
)


data class WalkWriteForm(
    val category: String,
    val title: String,
    val details: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val image: String?
)