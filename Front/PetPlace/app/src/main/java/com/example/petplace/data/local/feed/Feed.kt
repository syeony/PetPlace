//package com.example.petplace.data.local.feed
//
//// Feed/Tag ────────────────────────────────────────────────────────────────
//data class TagDto(
//    val id:    Long,
//    val name:  String
//)
//
//// Feed/cImg
//data class ImgDto(
//    val src: String,
//    val sort: Long
//)
//
//// 댓글 & 대댓글 ────────────────────────────────────────────────────────────
//data class CommentDto(
//    val id:               Long,
//    val parentCommentId:  Long?,            // null ⇒ 최상위 댓글
//    val feedId:           Long,
//    val content:          String,
//    val userId:           Long,
//    val userNick:         String,
//    val userImg:          String?,          // null 가능
//    val createdAt:        String,           // "2025-08-01T14:51:19" (ISO-8601)
//    val updatedAt:        String? = null,
//    val deletedAt:        String? = null,
//    val replies:          List<CommentDto> = emptyList()   // 재귀 구조
//)
//
//// 피드(단건) ────────────────────────────────────────────────────────────────
//data class FeedDto(
//    val id:          Long,
//    val content:     String,
//    val userId:      Long,
//    val userNick:    String,
//    val userImg:     String?,        // nullable ⇒ 서버가 null 내려줄 수 있음
//    val images:  List<ImgDto>? = emptyList(),       // 민이가 준 샘플데이터에 없어서 내가 임시로 추가
//    val regionId:    Int,
//    val category:    String,
//    val createdAt:   String,
//    val updatedAt:   String? = null,
//    val deletedAt:   String? = null,
//    val likes:       Int = 0,
//    val views:       Int = 0,
//    val tags:        List<TagDto> = emptyList(),
//    val commentCount:Int = 0,
//    val comments:    List<CommentDto> = emptyList()
//)
