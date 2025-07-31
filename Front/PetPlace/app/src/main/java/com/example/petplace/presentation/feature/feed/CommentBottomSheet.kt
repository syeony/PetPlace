package com.example.petplace.presentation.feature.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.data.local.feed.Comment

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

    LaunchedEffect(Unit) {
        sheetState.show() // 강제로 Expanded 상태로
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(2f / 3f) // 화면의 2/3만 차지하도록 설정
            .padding(16.dp)) {
            Text(
                text = "댓글",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(comments) { comment ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Image(
                                painter = rememberAsyncImagePainter(comment.profileImage),
                                contentDescription = null,
                                modifier = Modifier.size(35.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text(text = comment.author, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = comment.town, fontSize = 12.sp, color = Color.Gray)
                                }
                                Text(text = comment.text)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (comment.isMine) {
                                IconButton(onClick = { /* 수정 */ }) {
                                    Icon(Icons.Default.Create, contentDescription = "수정")
                                }
                                IconButton(onClick = { /* 삭제 */ }) {
                                    Icon(Icons.Default.Clear, contentDescription = "삭제")
                                }
                            }
                        }
                        if (comment.replies.isEmpty()) {
                            TextButton(onClick = { /* 답글 */ }) {
                                Text("답글 달기", fontSize = 10.sp)
                            }
                        } else {
                            comment.replies.forEach { reply ->
                                Row(modifier = Modifier.padding(start = 36.dp)) {
                                    Text("${reply.author}: ${reply.text}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}