package com.example.skolar20.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Add welcome message
    LaunchedEffect(Unit) {
        messages = listOf(
            ChatMessage(
                text = "Hi! I'm your study assistant. I can help you with:\n\n" +
                        "📚 Explaining concepts and topics\n" +
                        "✏️ Homework and assignment help\n" +
                        "🧮 Math and science problems\n" +
                        "📖 Study tips and techniques\n" +
                        "🎯 Exam preparation strategies\n\n" +
                        "What would you like help with today?",
                isFromUser = false
            )
        )
    }

    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        messages = messages + ChatMessage(text = userMessage, isFromUser = true)
        currentMessage = ""
        isLoading = true

        // Simple rule-based responses for demo
        scope.launch {
            kotlinx.coroutines.delay(1000) // Simulate thinking

            val response = when {
                userMessage.contains("math", ignoreCase = true) ->
                    "I can help with math! What specific topic are you working on? (e.g., algebra, calculus, geometry)"
                userMessage.contains("science", ignoreCase = true) ->
                    "Science is fascinating! Which branch? Physics, Chemistry, or Biology?"
                userMessage.contains("study", ignoreCase = true) ->
                    "Here are some effective study tips:\n\n" +
                            "1. Use the Pomodoro Technique (25 min study, 5 min break)\n" +
                            "2. Create flashcards for key concepts\n" +
                            "3. Teach the material to someone else\n" +
                            "4. Practice active recall\n" +
                            "5. Get enough sleep before exams!"
                userMessage.contains("exam", ignoreCase = true) ->
                    "Exam prep tips:\n\n" +
                            "• Start studying at least 2 weeks ahead\n" +
                            "• Make a study schedule\n" +
                            "• Do practice problems\n" +
                            "• Review past papers\n" +
                            "• Stay calm and confident!"
                userMessage.contains("help", ignoreCase = true) || userMessage.contains("homework", ignoreCase = true) ->
                    "I'm here to help! Please share your specific question or the topic you're struggling with, and I'll guide you through it step by step."
                else ->
                    "That's a great question! For detailed help with specific subjects, I recommend:\n\n" +
                            "1. Booking a tutor through the Tutors tab\n" +
                            "2. Breaking down your question into smaller parts\n" +
                            "3. Checking your textbook or course materials\n\n" +
                            "Feel free to ask me about study strategies, exam tips, or general academic guidance!"
            }

            messages = messages + ChatMessage(text = response, isFromUser = false)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Study Assistant",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Ask me anything about your studies!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Thinking...")
                            }
                        }
                    }
                }
            }
        }

        // Input area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                TextField(
                    value = currentMessage,
                    onValueChange = { currentMessage = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask a question...") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = { sendMessage(currentMessage) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message"
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            color = if (message.isFromUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isFromUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}