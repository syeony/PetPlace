package com.example.petplace.data.repository

import android.util.Log
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.data.model.mypage.PetProductRequest
import com.example.petplace.data.model.mypage.PetProductResponse
import com.example.petplace.data.remote.MyPageApiService
import com.example.petplace.presentation.feature.mypage.SupplyType
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

    // 펫 프로덕트 이미지 업데이트
    suspend fun updatePetProductImage(request: PetProductRequest): Result<PetProductResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "펫 프로덕트 이미지 업데이트 요청")

                val response = api.updatePetProductImage(request)

                if (response.isSuccessful) {
                    val petProductResponse = response.body()
                    if (petProductResponse != null) {
                        Log.d(TAG, "펫 프로덕트 이미지 업데이트 성공")
                        Result.success(petProductResponse)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "펫 프로덕트 이미지 업데이트 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("펫 프로덕트 이미지 업데이트 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "펫 프로덕트 이미지 업데이트 중 오류", e)
                Result.failure(e)
            }
        }
    }

    // 펫 용품 저장 (등록/수정 자동 판단)
    suspend fun savePetSupplyInfo(
        supplyType: SupplyType,
        imageUri: String,
        hasExistingImage: Boolean = false
    ): Result<PetProductResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "펫 용품 저장 요청 - 타입: $supplyType, 기존 이미지 여부: $hasExistingImage")

                // 용품 타입에 따른 요청 데이터 생성
                val request = PetProductRequest(
                    src = imageUri,
                    sort = when (supplyType) {
                        SupplyType.BATH -> 1
                        SupplyType.FOOD -> 2
                        SupplyType.WASTE -> 3
                    },
                )

                // 기존 이미지가 있으면 업데이트, 없으면 추가
                val response = if (hasExistingImage) {
                    Log.d(TAG, "기존 펫 용품 이미지 업데이트")
                    api.updatePetProductImage(request)
                } else {
                    Log.d(TAG, "새 펫 용품 이미지 추가")
                    api.addPetProductImage(request)
                }

                if (response.isSuccessful) {
                    val petProductResponse = response.body()
                    if (petProductResponse != null) {
                        Log.d(TAG, "펫 용품 저장 성공")
                        Result.success(petProductResponse)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "펫 용품 저장 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("펫 용품 저장 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "펫 용품 저장 중 오류", e)
                Result.failure(e)
            }
        }
    }
}