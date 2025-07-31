package com.example.petplace.presentation.feature.Missing_register

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.presentation.common.navigation.BottomNavItem

private val BgColor      = Color(0xFFFEF9F0)
private val AccentOrange = Color(0xFFFFA500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    /* ① 상태 수집 */
    val detail     by viewModel.detail.collectAsState()
    val imageList  by viewModel.imageList.collectAsState()
    val date       by viewModel.date.collectAsState()
    val time       by viewModel.time.collectAsState()
    val place      by viewModel.place.collectAsState()

    /* ② 갤러리·카메라 launcher */
    val launcherGallery =
        rememberLauncherForActivityResult(PickMultipleVisualMedia(5)) { uris ->
            if (!uris.isNullOrEmpty()) viewModel.addImages(uris)
        }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("실종 등록") },
                navigationIcon = {
                    IconButton(onClick = { /* nav.popBackStack() */ }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgColor)
            )
        },
        containerColor = BgColor
    ) { pad ->

        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {

            /* 🐶 반려동물 카드 – 기존 그대로 */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("family/select")
                    }
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.pp_logo),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("코코", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("골든 리트리버 • 3살", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }

            /* 📸 사진 업로드 + 미리보기 */
            Row(
                Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // + 버튼 → 갤러리 바로 실행
                Box(
                    Modifier
                        .size(100.dp)
                        .border(BorderStroke(1.dp, Color(0xFFD7D7D7)), RoundedCornerShape(8.dp))
                        .clickable {
                            launcherGallery.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(id = R.drawable.outline_photo_camera_24),
                        null,
                        tint = Color(0xFF8C8C8C)
                    )
                }

                /* 선택된 이미지 썸네일 */
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(imageList) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, Color(0xFFD7D7D7)), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "한마리의 동물만 나오게 해주세요.\n얼굴이 잘 나온 사진을 등록해주세요.",
                fontSize = 12.sp,
                color = Color(0xFF8C8C8C),
                lineHeight = 16.sp
            )

            /* ✍️ 상세 내용 */
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = detail,
                onValueChange = viewModel::setDetail,
                placeholder = {
                    Text("실종 장소, 상황, 특징 등을 작성해주세요.", color = Color(0xFFADAEBC))
                },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )

            /* 🗓 실종 일시 – 기존 그대로 */
            Spacer(Modifier.height(24.dp))
            Text("실종 일시", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(date to { /* date picker */ }, time to { /* time picker */ }).forEach { (v, _) ->
                    OutlinedTextField(
                        value = v,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }

            /* 📍 실종 장소 – 기존 그대로 */
            Spacer(Modifier.height(24.dp))
            Text("실종 장소", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = place,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            Spacer(Modifier.height(4.dp))
            Text("작성 완료 후에는 장소를 변경할 수 없어요.", fontSize = 12.sp, color = Color(0xFF8C8C8C))

            /* ✔ 완료 버튼 */
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    navController.navigate("${BottomNavItem.Neighborhood.route}?showDialog=true") {
                        popUpTo("Missing_register") { inclusive = true }   // 뒤로가기로 등록화면 안 보이게
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("작성완료", color = Color.White)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}