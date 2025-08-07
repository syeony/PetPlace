package com.example.petplace.data.remote

import com.example.petplace.data.model.chat.ChatMessageResponse
import com.example.petplace.data.model.chat.ChatPartnerResponse
import com.example.petplace.data.model.chat.ChatRoomResponse
import com.example.petplace.data.model.chat.CreateChatRoomRequest
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {

    // 채팅방 참가 (Query Parameter 사용)
    @POST("/api/chat/rooms/{chatRoomId}/join")
    suspend fun joinChatRoom(
        @Path("chatRoomId") chatRoomId: Long,
        @Query("userId") userId: Long
    ): Response<Unit>

    // 채팅방 생성
    @POST("/api/chat/room")
    suspend fun createChatRoom(
        @Body request: CreateChatRoomRequest
    ): Response<ChatRoomResponse>

    // 채팅방 메시지 조회
    @GET("/api/chat/{chatRoomId}/messages")
    suspend fun getChatMessages(
        @Path("chatRoomId") chatRoomId: Long
    ): Response<List<ChatMessageResponse>>

    // 채팅방 목록 조회 (Query Parameter 사용)
    @GET("/api/chat/rooms")
    suspend fun getChatRooms(
        @Query("userId") userId: Long
    ): Response<List<ChatRoomResponse>>


    // 채팅방 안 읽은 메시지 수 조회
    @GET("/api/chat/rooms/{chatRoomId}/unread")
    suspend fun getUnreads(
        @Path("chatRoomId") chatRoomId: Long,
        @Query("userId") userId: Long
    ): Response<Int>

    // 채팅방 참여자 정보 조회
    @GET("/api/chat/rooms/{chatRoomId}/participants")
    suspend fun getParticipants(
        @Path("chatRoomId") chatRoomId: Long
    ): Response<List<ChatPartnerResponse>>
}