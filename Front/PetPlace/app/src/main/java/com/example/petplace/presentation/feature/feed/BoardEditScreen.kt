package com.example.petplace.presentation.feature.feed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoardEditScreen(
    navController: NavController,
    feedId: Long,
    regionId: Long,
    viewModel: BoardEditViewModel = hiltViewModel()
) {
    val feed by viewModel.feed.collectAsState()
    val content by viewModel.content.collectAsState()
    val category by viewModel.category.collectAsState()
    val tagIds by viewModel.tagIds.collectAsState()
    val images by viewModel.images.collectAsState()
    val scope = rememberCoroutineScope()

    // 최초 1회 피드 데이터 불러오기
    LaunchedEffect(feedId) {
        viewModel.loadFeedDetail(feedId)
    }

    val allCategories = listOf(
        "내새꾸자랑" to "MYPET",
        "정보" to "INFO",
        "나눔" to "SHARE",
        "후기" to "REVIEW",
        "자유" to "ANY"
    )

    val allTags = listOf(
        "산책", "목욕", "미용", "사료", "간식", "놀이", "훈련", "건강관리", "동물병원",
        "호텔", "유치원", "캣타워", "펫시터", "입양", "보험", "장난감", "케어",
        "리드줄", "하네스", "이동장", "실종"
    )

    // 이미지 추가 (갤러리)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.appendUploadImages(uris)   // 로컬 Uri → 업로드 → 절대 URL로 images 상태 갱신
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .padding(bottom = 160.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 카테고리 선택
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp)
            ) {
                allCategories.forEach { (label, apiValue) ->
                    val selected = category == apiValue
                    val bgColor = if (selected) Color(0xFFF79800) else Color.White
                    val textColor = if (selected) Color.White else Color.DarkGray
                    val borderColor = if (selected) Color.Transparent else Color.DarkGray

                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                            .background(bgColor, RoundedCornerShape(20.dp))
                            .clickable { viewModel.pickCategory(apiValue) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = label, color = textColor, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "카테고리를 하나 선택해주세요.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("내용을 입력해 주세요...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Color(0xFFF79800),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("해시태그를 선택해주세요.(4개 이내)", fontSize = 14.sp, modifier = Modifier.padding(start = 16.dp))
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                allTags.forEachIndexed { idx, tag ->
                    val tagId = idx + 1  // 1부터 시작
                    val selected = tagIds.contains(tagId.toLong())
                    val bgColor = if (selected) Color(0xFFF79800) else Color.White
                    val borderColor = if (selected) Color(0xFFF79800) else Color.LightGray
                    val textColor = if (selected) Color.White else Color.DarkGray

                    Text(
                        text = tag,
                        color = textColor,
                        modifier = Modifier
                            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                            .background(bgColor, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.toggleTag(tagId.toLong())
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 15.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // 이미지 미리보기 + 삭제 버튼
            if (images.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    images.forEachIndexed { idx, img ->
                        Box(modifier = Modifier.size(100.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(model = img.src),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            IconButton(
                                onClick = { viewModel.removeImage(idx) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(Color(0x66000000), RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "삭제",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 이미지 선택 버튼
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_photo_camera_24),
                    contentDescription = "사진 촬영",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.saveEdits(
                            feedId = feedId,
                            regionId = regionId,
                            onSuccess = {
                                navController.previousBackStackEntry?.savedStateHandle?.set("feedEdited", true)
                                navController.popBackStack()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF79800))
            ) {
                Text("수정하기", color = Color.White)
            }
        }
    }
}
