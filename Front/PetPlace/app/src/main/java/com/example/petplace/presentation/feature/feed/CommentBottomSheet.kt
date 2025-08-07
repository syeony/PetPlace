package com.example.petplace.presentation.feature.feed

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petplace.R
import com.example.petplace.data.model.feed.CommentRes
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    feedId: Long,
    comments: List<CommentRes>,
    onDismiss: () -> Unit,
    viewModel: BoardViewModel
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

    var replyingTo by remember { mutableStateOf<Long?>(null) }  // ★ 답글 다는 대상 id
    val topLevelComments = comments.filter { it.parentId == null }

    //댓글 전송버튼
    val coroutineScope = rememberCoroutineScope()

    //댓글 삭제할때
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCommentId by remember { mutableStateOf<Long?>(null) }

    // AlertDialog - 댓글 삭제 버튼
    if (showDeleteDialog && selectedCommentId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("댓글 삭제") },
            text = { Text("정말 이 댓글을 삭제할까요?") },
            confirmButton = {
                Button(onClick = {
                    // 댓글 삭제
                    coroutineScope.launch {
                        viewModel.removeComment(selectedCommentId!!, feedId)
                        showDeleteDialog = false
                        selectedCommentId = null
                    }
                }) { Text("삭제") }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    selectedCommentId = null
                }) { Text("취소") }
            }
        )
    }

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
                        placeholder   = { Text(if (replyingTo == null) "댓글을 입력하세요" else "답글을 입력하세요") },
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
                                    if (replyingTo == null) {
                                        // ★ 최상위 댓글 등록 로직
                                        println("최상위 댓글 등록: $commentText")
                                        // 서버에 parentCommentId = null로 전송
                                    } else {
                                        // ★ 대댓글 등록 로직
                                        println("대댓글 등록: $commentText, parentId=${replyingTo}")
                                        // 서버에 parentCommentId = replyingTo로 전송
                                    }
                                    commentText = ""
                                    replyingTo = null // 입력 후 리셋
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
                            .padding(end = 12.dp)
                            .clickable {
                                if (commentText.isNotBlank()) {
                                    // 댓글 등록
                                    coroutineScope.launch {
                                        viewModel.addComment(feedId, replyingTo, commentText)
                                        commentText = ""
                                        replyingTo = null
                                        focusManager.clearFocus()
                                    }
                                }
                            },
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
                    items(topLevelComments.size) { idx ->
                        val comment = topLevelComments[idx]

                        Column(Modifier.padding(vertical = 8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .pointerInput(comment.id) {
                                        detectTapGestures(
                                            onLongPress = {
                                                selectedCommentId = comment.id
                                                showDeleteDialog = true
                                            }
                                        )
                                    }
                            ) {
                                ProfileImage(comment.userImg)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(comment.userNick, fontWeight = FontWeight.Bold)
                                    Text(comment.content)
                                }
                            }

                            // "답글 달기" 클릭 시 parent id를 기억!
                            TextButton(
                                onClick = {
                                    commentText = ""
                                    replyingTo = comment.id    // ★
                                    focusRequester.requestFocus()
                                }
                            ) { Text("답글 달기", fontSize = 10.sp) }

                            // 답글 더보기
                            if (comment.replies.isNotEmpty()) {
                                TextButton(onClick = { expandedStates[idx] = !expandedStates[idx] }) {
                                    Text(
                                        if (expandedStates[idx]) "          답글 숨기기"
                                        else                     "          답글 더 보기",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            if (expandedStates[idx]) {
                                comment.replies.forEach { reply ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(start = 36.dp, top = 4.dp, bottom = 20.dp)
                                            .pointerInput(reply.id) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        selectedCommentId = reply.id
                                                        showDeleteDialog = true
                                                    }
                                                )
                                            }
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