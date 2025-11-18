// app/src/main/java/com/example/skolar20/ui/screens/MessagesScreen.kt
package com.example.skolar20.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.skolar20.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(val id: Long, val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // simple in-memory message list
    var messages by remember { mutableStateOf<List<ChatMessage>>(listOf(
        ChatMessage(1L, context.getString(R.string.bot_greeting), false)
    )) }

    var inputText by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    fun appendMessage(msg: ChatMessage) {
        messages = messages + msg
    }

    fun nextId(): Long = (messages.maxOfOrNull { it.id } ?: 0L) + 1L

    // Simple bot logic
    suspend fun botReply(userText: String) {
        delay(600) // simulate think time
        val lower = userText.lowercase()
        val reply = when {
            "hour" in lower || "time" in lower -> context.getString(R.string.bot_hours)
            "price" in lower || "cost" in lower -> context.getString(R.string.bot_price)
            "help" in lower || "support" in lower -> context.getString(R.string.bot_default_reply)
            userText.isBlank() -> context.getString(R.string.bot_default_reply)
            else -> context.getString(R.string.bot_default_reply)
        }
        appendMessage(ChatMessage(nextId(), reply, false))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(text = stringResource(id = R.string.messages_title)) })

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(messages) { m ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (m.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        tonalElevation = 2.dp,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = m.text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Input row
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(text = stringResource(id = R.string.messages_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (inputText.isBlank()) return@Button
                    // append user message
                    val userMsg = ChatMessage(nextId(), inputText.trim(), true)
                    appendMessage(userMsg)
                    val toSend = inputText.trim()
                    inputText = ""
                    sending = true
                    scope.launch {
                        botReply(toSend)
                        sending = false
                    }
                },
                enabled = !sending
            ) {
                Text(if (sending) "..." else "Send")
            }
        }
    }
}
