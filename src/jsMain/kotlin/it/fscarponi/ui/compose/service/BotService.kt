package it.fscarponi.ui.compose.service

interface BotService {
    suspend fun sendMessage(message: String): String
}

class MockBotService : BotService {
    override suspend fun sendMessage(message: String): String {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)
        return "This is a mock response to: $message"
    }
}