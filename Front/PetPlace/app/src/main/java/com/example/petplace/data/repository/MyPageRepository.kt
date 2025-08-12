package com.example.petplace.data.repository

import android.util.Log
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.data.remote.MyPageApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyPageRepository @Inject constructor(
    private val api: MyPageApiService
) {
    companion object {
        private const val TAG = "MyPageRepository"
    }

    // 마이페이지 정보 조회
    suspend fun getMyPageInfo(): Result<MyPageInfoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "마이페이지 정보 조회 요청")

                val response = api.getMyPageInfo()

                if (response.isSuccessful) {
                    val myPageInfo = response.body()
                    if (myPageInfo != null) {
                        Log.d(TAG, "마이페이지 정보 조회 성공")
                        Result.success(myPageInfo)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "마이페이지 정보 조회 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("마이페이지 정보 조회 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "마이페이지 정보 조회 중 오류", e)
                Result.failure(e)
            }
        }
    }
}