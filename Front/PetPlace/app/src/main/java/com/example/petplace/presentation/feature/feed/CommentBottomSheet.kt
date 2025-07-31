package com.example.petplace.presentation.feature.feed

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager // 키보드 포커스 제어
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.presentation.feature.feed.model.Comment

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    comments: List<Comment>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true, // 부분 확장을 건너뜀
        confirmValueChange = { true },
    )
    //채팅 펼치기
    val expandedStates = remember { mutableStateListOf<Boolean>().apply {
        repeat(comments.size) { add(false) }
    } }
    var commentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        sheetState.show() // 강제로 Expanded 상태로
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f) // 화면 75% 정도 차지
                .navigationBarsPadding(), // 하단 시스템바까지 패딩

            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("댓글을 입력하세요") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (commentText.isNotBlank()) {
                                    // 전송 로직
                                    println("전송: $commentText")

                                    // 입력 초기화
                                    commentText = ""

                                    // 키보드 내리기
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        singleLine = true
                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Button(onClick = { /* 전송 */ }) {
//                        Text("전송")
//                    }
                }
            }
        ){innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "댓글",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
//                verticalArrangement = Arrangement.spacedBy(12.dp) // 아이템 간격 12dp
                ) {
                    items(comments.size) { index ->
                        val comment = comments[index]

                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = rememberAsyncImagePainter(comment.profileImage),
                                    contentDescription = null,
                                    modifier = Modifier.size(35.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = comment.author, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = comment.town, fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Text(text = comment.text)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            // 답글 버튼
                            if(comment.replies.isEmpty()){
                                TextButton(
                                    onClick = {

                                    }
                                ) {
                                    Text("답글 달기", fontSize = 10.sp)
                                }
                            }
                            else{
                                TextButton(
                                    onClick = {
                                        expandedStates[index] = !expandedStates[index]
                                    }
                                ) {
                                    Text("댓글 보기", fontSize = 10.sp)
                                }
                            }
                            // 대댓글 표시 (토글 상태일 때만)
                            if (expandedStates[index]) {
                                Column {
                                    comment.replies.forEach { reply ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 36.dp, top = 4.dp, bottom = 4.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_reply),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Image(
                                                painter = rememberAsyncImagePainter(reply.profileImage),
                                                contentDescription = null,
                                                modifier = Modifier.size(30.dp).clip(CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = reply.author, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(text = reply.town, fontSize = 12.sp, color = Color.Gray)
                                                }
                                                Text(text = reply.text)
                                            }

                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {  }
                                        ) {
                                            Text("답글 달기", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

    }
}