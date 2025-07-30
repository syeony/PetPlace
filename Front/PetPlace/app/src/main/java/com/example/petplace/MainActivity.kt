package com.example.petplace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.petplace.presentation.common.navigation.MainScaffold
import com.example.petplace.presentation.common.theme.PetPlaceTheme
import android.util.Log
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var keyHash = Utility.getKeyHash(this)
        Log.e("KeyHash", "해쉬값 : ${keyHash}")
        setContent {
            PetPlaceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScaffold()
                }
            }
        }
    }
}
