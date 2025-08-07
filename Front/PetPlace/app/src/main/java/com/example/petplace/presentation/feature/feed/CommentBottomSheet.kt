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
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petplace.R
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    feedId: Long,
    onDismiss: () -> Unit,
    viewModel: BoardViewModel = hiltViewModel()
) {
    // 1) 댓글 리스트 구독
    val comments by viewModel.commentList.collectAsState()

    // 2) 시트 열릴 때 댓글 API 호출
    LaunchedEffect(feedId) {
        viewModel.refreshComments(feedId)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 최상위 댓글 필터
    val topLevelComments = comments.filter { it.parentId == null }

    // 토글 상태: 댓글 개수만큼
    val expandedStates = remember(comments) {
        mutableStateListOf<Boolean>().apply {
            repeat(topLevelComments.size) { add(false) }
        }
    }

    // 입력창 & 포커싱
    var commentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var replyingTo by remember { mutableStateOf<Long?>(null) }

    // 삭제 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCommentId by remember { mutableStateOf<Long?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // 삭제 확인 Dialog
    if (showDeleteDialog && selectedCommentId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("댓글 삭제") },
            text = { Text("정말 삭제하시겠습니까?") },
            confirmButton = {
                Button(onClick = {
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

    // 시트 자동 표시
    LaunchedEffect(Unit) { sheetState.show() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Scaffold(
            containerColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding(),
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
                        placeholder = {
                            Text(
                                if (replyingTo == null) "댓글을 입력하세요"
                                else "답글을 입력하세요"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (commentText.isNotBlank()) {
                                    coroutineScope.launch {
                                        viewModel.addComment(feedId, replyingTo, commentText)
                                        commentText = ""
                                        replyingTo = null
                                        focusManager.clearFocus()
                                    }
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
                            .clickable {
                                if (commentText.isNotBlank()) {
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
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(
                    "댓글",
                    fontSize = 17.sp,
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
                                modifier = Modifier.pointerInput(comment.id) {
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

                            TextButton(onClick = {
                                commentText = ""
                                replyingTo = comment.id
                                focusRequester.requestFocus()
                            }) { Text("답글 달기", fontSize = 10.sp) }

                            if (comment.replies.isNotEmpty()) {
                                TextButton(onClick = {
                                    expandedStates[idx] = !expandedStates[idx]
                                }) {
                                    Text(
                                        if (expandedStates[idx]) "답글 숨기기" else "답글 더 보기",
                                        fontSize = 10.sp
                                    )
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
}
