package com.example.petplace.presentation.feature.Missing_register

import androidx.lifecycle.ViewModel
import com.example.petplace.R
import com.example.petplace.data.local.Missing_register.FamilyPet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PetSelectViewModel : ViewModel() {

    /* 실제 프로젝트라면 Repository 주입 */
    private val _pets = MutableStateFlow(
        listOf(
            FamilyPet(1, "코코", "골든 리트리버", "남아 3살", R.drawable.pp_logo),
            FamilyPet(2, "고등어", "코리안숏헤어",   "여아 5살", R.drawable.pp_logo),
            FamilyPet(3, "두부", "말티즈",        "여아 8살", R.drawable.pp_logo)
        )
    )
    val pets = _pets.asStateFlow()

    private val _selectedId = MutableStateFlow<Int?>(null)
    val selectedId = _selectedId.asStateFlow()

    /* ---------- public API ---------- */
    fun selectPet(id: Int) {
        _selectedId.value = id
    }
}
