package com.example.petplace.presentation.feature.feed

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.petplace.R
import com.example.petplace.data.local.feed.CommentDto

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    comments: List<CommentDto>,
    onDismiss: () -> Unit
) {
    /* ───────── bottom sheet state ───────── */
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    /* 답글 더보기 토글 상태 → 최상위 댓글 수만큼 */
    val expandedStates = remember {
        mutableStateListOf<Boolean>().apply { repeat(comments.size) { add(false) } }
    }

    /* 입력창 & 포커싱 */
    var commentText   by remember { mutableStateOf("") }
    val focusManager   = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    /* 첫 표시 때 바로 Expanded */
    LaunchedEffect(Unit) { sheetState.show() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.White,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Scaffold(
            containerColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding(),
            /* ───────── bottom bar : 댓글 입력 ───────── */
            bottomBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder   = { Text("댓글을 입력하세요") },
                        singleLine    = true,
                        modifier      = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions  = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions  = KeyboardActions(
                            onSend = {
                                if (commentText.isNotBlank()) {
                                    /* 실제 전송 로직 자리에 println 만 */
                                    println("전송: $commentText")
                                    commentText = ""
                                    focusManager.clearFocus()
                                }
                            }
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.paper_airplane),
                        contentDescription = "전송",
                        modifier = Modifier
                            .size(35.dp)
                            .padding(end = 12.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        ) { inner ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(16.dp)
            ) {
                Text(
                    "댓글",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))

                LazyColumn {
                    items(comments.size) { idx ->
                        val comment = comments[idx]

                        /* ---------- 최상위 댓글 ---------- */
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProfileImage(comment.userImg)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(comment.userNick, fontWeight = FontWeight.Bold)
                                    Text(comment.content)
                                }
                            }

                            /* 답글 버튼 → 입력창 포커스 */
                            TextButton(
                                onClick = {
                                    commentText = ""
                                    focusRequester.requestFocus()
                                }
                            ) { Text("답글 달기", fontSize = 10.sp) }

                            /* 답글 더보기 버튼 */
                            if (comment.replies.isNotEmpty()) {
                                TextButton(onClick = { expandedStates[idx] = !expandedStates[idx] }) {
                                    Text(
                                        if (expandedStates[idx]) "          답글 숨기기"
                                        else                     "          답글 더 보기",
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            /* ---------- 대댓글 영역 ---------- */
                            if (expandedStates[idx]) {
                                comment.replies.forEach { reply ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(start = 36.dp, top = 4.dp, bottom = 20.dp)
                                    ) {
                                        ProfileImage(reply.userImg)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(reply.userNick, fontWeight = FontWeight.Bold)
                                            Text(reply.content)
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
