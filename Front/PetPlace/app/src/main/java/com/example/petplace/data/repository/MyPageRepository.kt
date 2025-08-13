package com.example.petplace.data.repository

import android.util.Log
import com.example.petplace.data.model.mypage.MyPageInfoResponse
import com.example.petplace.data.model.mypage.PetProductRequest
import com.example.petplace.data.model.mypage.PetProductResponse
import com.example.petplace.data.model.mypage.ProfileImageRequest
import com.example.petplace.data.model.mypage.ProfileIntroductionRequest
import com.example.petplace.data.model.mypage.ProfileIntroductionResponse
import com.example.petplace.data.model.mypage.ProfileUpdateRequest
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

    suspend fun updateProfile(
        nickname: String? = null,
        curPassword: String? = null,
        newPassword: String? = null,
        imgSrc: String? = null,
        regionId: Int? = null
    ): Result<MyPageInfoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "프로필 업데이트 요청")

                val request = ProfileUpdateRequest(
                    nickname = nickname,
                    curPassword = curPassword,
                    newPassword = newPassword,
                    imgSrc = imgSrc,
                    regionId = regionId
                )
                val response = api.updateProfile(request)

                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        Log.d(TAG, "프로필 업데이트 성공")
                        Result.success(result)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("서버 응답이 비어있습니다"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "프로필 업데이트 실패: ${response.code()} ${response.message()}")
                    Log.e(TAG, "에러 응답 본문: $errorBody")

                    val errorMessage = when (response.code()) {
                        400 -> "잘못된 요청입니다. 입력 내용을 확인해주세요."
                        401 -> "현재 비밀번호가 올바르지 않습니다."
                        403 -> "권한이 없습니다."
                        404 -> "사용자를 찾을 수 없습니다."
                        409 -> "이미 사용중인 닉네임입니다."
                        500 -> "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                        else -> "프로필 업데이트 중 오류가 발생했습니다 (${response.code()})"
                    }

                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "프로필 업데이트 중 네트워크 오류", e)
                Result.failure(Exception("네트워크 오류: ${e.message}"))
            }
        }
    }

    // 프로필 이미지 업데이트
    suspend fun updateProfileImage(imgSrc: String): Result<MyPageInfoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "프로필 이미지 업데이트 요청")

                val request = ProfileImageRequest(imgSrc = imgSrc)
                val response = api.updateProfileImage(request)

                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        Log.d(TAG, "프로필 이미지 업데이트 성공")
                        Result.success(result)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "프로필 이미지 업데이트 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("프로필 이미지 업데이트 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "프로필 이미지 업데이트 중 오류", e)
                Result.failure(e)
            }
        }
    }

    // 프로필 소개글 저장 (신규 등록 또는 업데이트)
    suspend fun saveProfileIntroduction(content: String, isUpdate: Boolean = true): Result<ProfileIntroductionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val action = if (isUpdate) "업데이트" else "등록"
                Log.d(TAG, "프로필 소개글 $action 요청 - 내용: '$content'")

                val request = ProfileIntroductionRequest(content = content)
                val response = if (isUpdate) {
                    Log.d(TAG, "PUT 요청으로 소개글 업데이트")
                    api.updateProfileIntroduction(request)
                } else {
                    Log.d(TAG, "POST 요청으로 소개글 등록")
                    api.createProfileIntroduction(request)
                }

                Log.d(TAG, "API 응답 - 코드: ${response.code()}, 성공여부: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        Log.d(TAG, "프로필 소개글 $action 성공 - 응답: $result")
                        Result.success(result)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("서버 응답이 비어있습니다"))
                    }
                } else {
                    // 에러 응답 본문 로그
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "프로필 소개글 $action 실패: ${response.code()} ${response.message()}")
                    Log.e(TAG, "에러 응답 본문: $errorBody")

                    // 더 구체적인 에러 메시지 제공
                    val errorMessage = when (response.code()) {
                        400 -> "잘못된 요청입니다. 소개글 내용을 확인해주세요."
                        401 -> "인증이 필요합니다. 다시 로그인해주세요."
                        403 -> "권한이 없습니다."
                        404 -> "프로필을 찾을 수 없습니다."
                        500 -> "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                        else -> "소개글 $action 중 오류가 발생했습니다 (${response.code()})"
                    }

                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "프로필 소개글 저장 중 네트워크 오류", e)
                Result.failure(Exception("네트워크 오류: ${e.message}"))
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