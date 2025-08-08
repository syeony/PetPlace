package com.example.petplace.data.model.feed

// POST /api/feeds  ───────── Request
// tagIds · images → 선택이므로 Nullable 로 선언
data class FeedCreateReq(
    val content:  String,
    val regionId: Long,
    val category: String,
    val tagIds:   List<Long>?     = null,
    val images:   List<CreateImage>? = null
)

//이미지 1장
data class CreateImage(
    val refId:   Long?  = null,               // 서버가 무시한다면 null 전송
    val refType: String = "FEED",             // 고정
    val src:     String,
    val sort:    Int                          // 1,2,3…(사진 위치)
)

// POST /api/feeds ───────── Response
// 피드 1개를 그대로 돌려줍니다.
data class FeedCreateRes(
    val id:          Long,
    val content:     String,
    val userId:      Long,
    val userNick:    String,
    val userImg:     String?,
    val regionId:    Long,
    val category:    String,
    val createdAt:   String,
    val updatedAt:   String?,
    val deletedAt:   String?,
    val liked: Boolean?,
    val likes:       Int,
    val views:       Int,
    val tags:        List<TagRes>,
    val images:      List<ImageRes>,
    val commentCount:Int,
    val comments:    List<CommentRes>
)
