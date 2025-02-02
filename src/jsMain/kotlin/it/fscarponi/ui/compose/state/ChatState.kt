package it.fscarponi.ui.compose.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import it.fscarponi.ui.compose.components.Message
import it.fscarponi.ui.compose.service.BotService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ChatState(
    private val botService: BotService,
    private val scope: CoroutineScope
) {
    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> get() = _messages

    var currentInput by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun updateInput(text: String) {
        currentInput = text
    }

    fun sendMessage() {
        if (currentInput.isBlank() || isLoading) return

        val userMessage = currentInput

        // Add user message
        _messages.add(Message(
            content = userMessage,
            isBot = false
        ))

        // Clear input
        currentInput = ""

        // Set loading state
        isLoading = true

        // Get bot response
        scope.launch {
            try {
                val response = botService.sendMessage(userMessage)
                _messages.add(Message(
                    content = response,
                    isBot = true
                ))
            } catch (e: Exception) {
                _messages.add(Message(
                    content = "Error: Failed to get response from bot",
                    isBot = true
                ))
            } finally {
                isLoading = false
            }
        }
    }
}
