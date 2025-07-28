package com.example.petplace.presentation.feature.chat
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

@Composable
fun ChatScreen(userName: String) {
    val messages = remember { sampleMessages }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(userName) }, navigationIcon = {
                IconButton(onClick = { /* 뒤로가기 */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            })
        },
        bottomBar = { MessageInputBar() }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(messages) { message ->
                ChatBubble(message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isMine = message.isMine
    Row(
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = if (isMine) Color(0xFFFFE4B5) else Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
                .widthIn(max = 250.dp)
        ) {
            Text(text = message.content)
            Text(text = message.time, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
fun MessageInputBar() {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("메시지를 입력하세요...") },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            // 전송 로직
            text = ""
        }) {
            Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFFF9C56F))
        }
    }
}
