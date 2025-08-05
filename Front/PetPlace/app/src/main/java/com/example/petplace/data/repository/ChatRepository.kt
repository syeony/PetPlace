package com.example.petplace.data.repository

import android.util.Log
import com.example.petplace.data.model.chat.*
import com.example.petplace.data.remote.ChatApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatApiService: ChatApiService
) {
    companion object {
        private const val TAG = "ChatRepository"
    }

    // 채팅방 참가
    suspend fun joinChatRoom(chatRoomId: Long, userId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "채팅방 참가 요청: chatRoomId=$chatRoomId, userId=$userId")

                val response = chatApiService.joinChatRoom(
                    chatRoomId = chatRoomId,
                    userId = userId
                )

                if (response.isSuccessful) {
                    Log.d(TAG, "채팅방 참가 성공")
                    Result.success(Unit)
                } else {
                    Log.e(TAG, "채팅방 참가 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("채팅방 참가 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "채팅방 참가 중 오류", e)
                Result.failure(e)
            }
        }
    }

    // 채팅방 메시지 조회
    suspend fun getChatMessages(chatRoomId: Long): Result<List<ChatMessageResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "채팅 메시지 조회: chatRoomId=$chatRoomId")

                val response = chatApiService.getChatMessages(chatRoomId = chatRoomId)

                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    Log.d(TAG, "채팅 메시지 조회 성공: ${messages.size}개")
                    Result.success(messages)
                } else {
                    Log.e(TAG, "채팅 메시지 조회 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("메시지 조회 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "채팅 메시지 조회 중 오류", e)
                Result.failure(e)
            }
        }
    }

    // 채팅방 목록 조회
    suspend fun getChatRooms(userId: Long): Result<List<ChatRoomResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "채팅방 목록 조회: userId=$userId")

                val response = chatApiService.getChatRooms(userId = userId)

                if (response.isSuccessful) {
                    val chatRooms = response.body() ?: emptyList()
                    Log.d(TAG, "채팅방 목록 조회 성공: ${chatRooms.size}개")
                    Result.success(chatRooms)
                } else {
                    Log.e(TAG, "채팅방 목록 조회 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("채팅방 목록 조회 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "채팅방 목록 조회 중 오류", e)
                Result.failure(e)
            }
        }
    }

    // 채팅방 생성
    suspend fun createChatRoom(userId1: Long, userId2: Long): Result<ChatRoomResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "채팅방 생성: userId1=$userId1, userId2=$userId2")

                val response = chatApiService.createChatRoom(
                    CreateChatRoomRequest(userId1 = userId1, userId2 = userId2)
                )

                if (response.isSuccessful) {
                    val chatRoom = response.body()
                    if (chatRoom != null) {
                        Log.d(TAG, "채팅방 생성 성공: chatRoomId=${chatRoom.chatRoomId}")
                        Result.success(chatRoom)
                    } else {
                        Log.e(TAG, "채팅방 생성 응답이 null")
                        Result.failure(Exception("채팅방 생성 응답이 null"))
                    }
                } else {
                    Log.e(TAG, "채팅방 생성 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("채팅방 생성 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "채팅방 생성 중 오류", e)
                Result.failure(e)
            }
        }
    }

}