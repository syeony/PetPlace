package com.example.petplace.presentation.feature.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BoardWriteScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val allCategories = viewModel.allCategories
    val allTags = listOf("1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999", "1010")

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf(listOf<String>()) }

    Column(modifier = modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(16.dp)) {

        // 뒤로가기
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.width(8.dp))
        //카테고리 선택
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
        ) {
            allCategories.forEach { category ->
                val selected = selectedCategory == category
                val bgColor = if (selected) Color(0xFFF79800) else Color.White
                val textColor = if (selected) Color.White else Color.DarkGray
                val borderColor = if (selected) Color.Transparent else Color(0xFF4B5563)

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .background(bgColor, RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category,
                        color = textColor,
                        fontSize = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("카테고리를 하나 선택해주세요.", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("내용을 입력해 주세요...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = Color(0xFFF79800),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text("해시태그를 선택해주세요.(4개 이내)", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
//            mainAxisSpacing = 8.dp,
//            crossAxisSpacing = 8.dp
        ) {
            allTags.forEach { tag ->
                val selected = tag in selectedTags
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
                            selectedTags = if (selected) {
                                selectedTags - tag
                            } else if (selectedTags.size < 4) {
                                selectedTags + tag
                            } else selectedTags
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row {
            selectedTags.forEach {
                Text(
                    text = "#${it}",
                    color = Color(0xFFF79800),
                    modifier = Modifier.padding(end = 8.dp),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            imageUris.forEach { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            IconButton(onClick = {
                // TODO: 이미지 추가 처리 (갤러리 선택 등)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Image")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // TODO: 등록 처리 후 이동
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF79800))
        ) {
            Text("등록하기", color = Color.White)
        }
    }
}
