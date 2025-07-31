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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborhoodScreen(
    navController: NavController,
    initialShowDialog: Boolean = false,
    viewModel: NeighborhoodViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    /* -------- ViewModel state 수집 -------- */
    val tags          = viewModel.tags
    val selectedTag   by viewModel.selectedTag.collectAsState()
    val showSheet     by viewModel.showBottomSheet.collectAsState()
    val showThanks    by viewModel.showThanksDialog.collectAsState()

    /* 🔸 최초 진입 시 한 번만 true 로 세팅 */
    LaunchedEffect(Unit) {
        if (initialShowDialog) viewModel.setThanksDialog(true)
    }

    /* ---------- 다이얼로그 ---------- */
    if (showThanks) {
        MatchingThanksDialog { viewModel.setThanksDialog(false) }
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    /* -------- BottomSheet -------- */
    if (showSheet) {
        NeighborhoodBottomSheet(
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    viewModel.hideBottomSheet()
                }
            },
            sheetState = sheetState,
            navController = navController
        )
    }

    /* -------- 검색 UI -------- */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("애견 동반 장소를 검색하세요") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp)),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Row(Modifier.horizontalScroll(rememberScrollState())) {
            tags.forEach { tag ->
                val isSel = selectedTag == tag
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) Color(0xFFF79800) else Color(0xFFF5F5F5))
                        .clickable { viewModel.selectTag(tag) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) { Text(tag, color = if (isSel) Color.White else Color.Black) }
            }
        }
    }
}
