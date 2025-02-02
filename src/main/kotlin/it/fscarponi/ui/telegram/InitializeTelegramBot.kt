package it.fscarponi.ui.telegram

import it.fscarponi.data.CharacterRepository
import it.fscarponi.service.HuggingFaceAIService
import it.fscarponi.telegram.CharacterAIBot
import it.fscarponi.ui.COLOR_ERROR
import it.fscarponi.ui.COLOR_INFO
import it.fscarponi.ui.COLOR_SUCCESS
import it.fscarponi.ui.printColored
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

internal fun initializeTelegramBot(repository: CharacterRepository, aiService: HuggingFaceAIService) {
    try {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        val bot = CharacterAIBot(repository, aiService)
        botsApi.registerBot(bot)
        printColored("\n=== Telegram Bot Started ===", COLOR_SUCCESS)
        printColored("The bot is now available on Telegram", COLOR_INFO)
    } catch (e: Exception) {
        printColored("Failed to start Telegram bot: ${e.message}", COLOR_ERROR)
        e.printStackTrace()
    }
}
