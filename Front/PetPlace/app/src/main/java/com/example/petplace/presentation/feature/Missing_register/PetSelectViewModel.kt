package com.example.petplace.presentation.feature.missing_register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petplace.data.model.pet.PetRes
import com.example.petplace.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetSelectViewModel @Inject constructor(
    private val repository: PetRepository
): ViewModel(){

    private val _pets = MutableStateFlow<List<PetRes>>(emptyList())
    val pets = _pets.asStateFlow()

    private val _selectedId = MutableStateFlow<Long?>(null)
    val selectedId = _selectedId.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        try {
            _pets.value = repository.getMyPets()
        } catch (e: Exception) {
            _error.value = e.message ?: "알 수 없는 오류"
        } finally {
            _loading.value = false
        }
    }

    fun selectPet(id: Long) { _selectedId.value = id }
}
