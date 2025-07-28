package com.example.petplace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.petplace.presentation.common.navigation.MainScaffold
import com.example.petplace.presentation.common.theme.PetPlaceTheme
import dagger.hilt.android.AndroidEntryPoint

//@AndroidEntryPoint 이게 hilt인데 쓸래??
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PetPlaceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScaffold()
                }
            }
        }
    }
}
