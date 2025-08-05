package com.example.petplace.presentation.feature.join


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.repository.JoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
//
//@HiltViewModel
//class CertificationViewModel @Inject constructor(
//    private val repository: JoinRepository
//) : ViewModel() {
//
//    fun verifyCertification(impUid: String) {
//        viewModelScope.launch {
//            try {
//                val response = repository.verifyCertification(impUid)
//                if (response.isSuccessful && response.body()?.success == true) {
//                    Log.d("Certification", "인증 성공: ${response.body()}")
//                } else {
//                    Log.e("Certification", "인증 실패: ${response.errorBody()?.string()}")
//                }
//            } catch (e: Exception) {
//                Log.e("Certification", "예외 발생", e)
//            }
//        }
//    }
//}
