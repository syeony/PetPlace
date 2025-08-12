package com.example.petplace.data.repository

import android.util.Log
import com.example.petplace.data.model.chat.*
import com.example.petplace.data.remote.ChatApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
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

    suspend fun getUnreads(chatRoomId: Long, userId: Long): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "안 읽은 메시지 수 요청: chatRoomId=$chatRoomId, userId=$userId")

                val response = chatApiService.getUnreads(chatRoomId, userId)

                if (response.isSuccessful) {
                    val count = response.body()
                    if (count != null) {
                        Log.d(TAG, "안 읽은 메시지 수 가져오기 성공: $count")
                        Result.success(count)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "안 읽은 메시지 수 가져오기 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("안 읽은 메시지 수 가져오기 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "안 읽은 메시지 수 가져오기 중 오류", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getParticipants(chatRoomId: Long): Result<List<ChatPartnerResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "채팅방 참가자 목록 요청: chatRoomId=$chatRoomId")

                val response = chatApiService.getParticipants(chatRoomId)

                if (response.isSuccessful) {
                    val participants = response.body()
                    if (participants != null) {
                        Log.d(TAG, "참가자 목록 가져오기 성공: ${participants.size}명")
                        Result.success(participants)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "참가자 목록 가져오기 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("참가자 목록 가져오기 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "참가자 목록 가져오기 중 오류", e)
                Result.failure(e)
            }
        }
    }

}