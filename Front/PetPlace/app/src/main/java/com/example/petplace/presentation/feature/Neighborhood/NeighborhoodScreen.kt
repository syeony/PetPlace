package com.example.petplace.presentation.feature.Neighborhood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.petplace.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodScreen() {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { true }
    )
    val isSheetVisible = remember { mutableStateOf(true) }

    val tags = listOf("#식당", "#카페", "#병원", "#용품샵", "#동물병원")
    val selectedTag = remember { mutableStateOf("#식당") }

    val buttons = listOf(
        Pair("실종견 등록", R.drawable.outline_exclamation_24),
        Pair("실종견 신고", R.drawable.outline_search_24),
        Pair("실종견 리스트", R.drawable.ic_feed),
        Pair("돌봄/산책", R.drawable.outline_sound_detection_dog_barking_24),
        Pair("입양처", Icons.Default.Favorite),
        Pair("애견호텔", R.drawable.outline_home_work_24),
    )

    // 바텀시트 호출부
    if (isSheetVisible.value) {
        NeighborhoodBottomSheet(
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    isSheetVisible.value = false
                }
            },
            sheetState = sheetState
        )
    }

    // 🧷 지도 + 검색 UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // 검색창
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("애견 동반 장소를 검색하세요") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp)),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(30.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 해시태그
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            tags.forEach { tag ->
                val isSelected = selectedTag.value == tag
                val backgroundColor = if (isSelected) Color(0xFFFFA500) else Color(0xFFF5F5F5)
                val textColor = if (isSelected) Color.White else Color.Black

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(backgroundColor)
                        .clickable { selectedTag.value = tag }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(tag, color = textColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Gray)
//        ) {
//            Text("지도 화면", modifier = Modifier.align(Alignment.Center))
//        }
    }
}
