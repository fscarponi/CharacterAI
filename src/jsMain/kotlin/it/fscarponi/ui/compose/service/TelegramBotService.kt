package it.fscarponi.ui.compose.service

import kotlinx.coroutines.delay

class TelegramBotService(private val chatId: String) : BotService {
    override suspend fun sendMessage(message: String): String {
        // TODO: Implement actual integration with Telegram bot
        // For now, we'll simulate the response
        delay(500) // Simulate network delay
        return "Message sent to Telegram chat $chatId: $message"
    }

    companion object {
        private var instance: TelegramBotService? = null

        fun getInstance(chatId: String): TelegramBotService {
            return instance ?: TelegramBotService(chatId).also { instance = it }
        }
    }
}