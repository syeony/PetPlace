package com.example.petplace.data.repository

import android.util.Log
import com.example.petplace.data.model.pet.PetRes
import com.example.petplace.data.model.pet.PetInfoResponse
import com.example.petplace.data.model.pet.PetRequest
import com.example.petplace.data.model.pet.PetResponse
import com.example.petplace.data.model.pet.PetUpdateRequest
import com.example.petplace.data.remote.PetApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepository @Inject constructor(
    private val api: PetApiService
) {
    companion object {
        private const val TAG = "PetRepository"
    }

    suspend fun getMyPets(): List<PetRes> = api.getMyPets()

    suspend fun getPetInfo(petId: Int): Result<PetInfoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "펫 정보 조회 요청 - ID: $petId")
                val response = api.getPetInfo(petId)
                if (response.isSuccessful) {
                    val petInfo = response.body()
                    if (petInfo != null) {
                        Log.d(TAG, "펫 정보 조회 성공 - 이름: ${petInfo.name}")
                        Result.success(petInfo)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "펫 정보 조회 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("펫 정보 조회 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "펫 정보 조회 중 오류", e)
                Result.failure(e)
            }
        }
    }


    // 펫 추가
    suspend fun addPet(request: PetRequest): Result<PetResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "펫 추가 요청 - 이름: ${request.name}, 동물: ${request.animal}")

                val response = api.addPet(request)

                if (response.isSuccessful) {
                    val petResponse = response.body()
                    if (petResponse != null) {
                        Log.d(TAG, "펫 추가 성공 - ID: ${petResponse.id}")
                        Result.success(petResponse)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "펫 추가 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("펫 추가 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "펫 추가 중 오류", e)
                Result.failure(e)
            }
        }
    }

    // 펫 정보 수정
    suspend fun updatePet(id: Int, request: PetUpdateRequest): Result<PetResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "펫 정보 수정 요청 - ID: $id, 이름: ${request.name}")

                val response = api.updatePet(id, request)

                if (response.isSuccessful) {
                    val petResponse = response.body()
                    if (petResponse != null) {
                        Log.d(TAG, "펫 정보 수정 성공 - ID: ${petResponse.id}")
                        Result.success(petResponse)
                    } else {
                        Log.e(TAG, "응답은 성공했지만 body가 null")
                        Result.failure(Exception("응답은 성공했지만 body가 null"))
                    }
                } else {
                    Log.e(TAG, "펫 정보 수정 실패: ${response.code()} ${response.message()}")
                    Result.failure(Exception("펫 정보 수정 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "펫 정보 수정 중 오류", e)
                Result.failure(e)
            }
        }
    }

    // 편의 메소드: 펫 정보 저장 (추가/수정 자동 판단)
    suspend fun savePetInfo(
        petId: Int? = null, // null이면 추가, 값이 있으면 수정
        name: String,
        animal: String,
        breed: String,
        sex: String,
        birthday: String,
        imgSrc: String?,
        tnr: Boolean
    ): Result<PetResponse> {
        return if (petId == null) {
            // 새 펫 추가
            val request = PetRequest(
                name = name,
                animal = animal,
                breed = breed,
                sex = sex,
                birthday = birthday,
                imgSrc = imgSrc,
                tnr = tnr
            )
            addPet(request)
        } else {
            // 기존 펫 수정
            val request = PetUpdateRequest(
                name = name,
                animal = animal,
                breed = breed,
                sex = sex,
                birthday = birthday,
                imgSrc = imgSrc,
                tnr = tnr
            )
            updatePet(petId, request)
        }
    }
}