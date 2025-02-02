package it.fscarponi

import it.fscarponi.config.HttpClientProvider
import it.fscarponi.data.CharacterRepository
import it.fscarponi.data.SQLiteCharacterRepository
import it.fscarponi.service.HuggingFaceAIService
import it.fscarponi.ui.COLOR_ERROR
import it.fscarponi.ui.COLOR_INFO
import it.fscarponi.ui.cli.startCliInterface
import it.fscarponi.ui.printColored
import it.fscarponi.ui.telegram.initializeTelegramBot
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) = runBlocking {
    val mode = if (args.isEmpty()) {
        printColored("no argoment provided-> running as telegram bot", COLOR_ERROR)
        "--telegram"
    } else args[0]

    val repository: CharacterRepository = SQLiteCharacterRepository()
    repository.initialize()
    val aiService = HuggingFaceAIService()

    when (mode) {
        "--cli" -> {
            printColored("Starting CLI interface...", COLOR_INFO)
            startCliInterface(repository, aiService)
        }

        else -> {
            printColored("Starting Telegram bot...", COLOR_INFO)
            initializeTelegramBot(repository, aiService)
            // Keep the application running
            while (true) {
                delay(1000)
            }
        }

    }
    HttpClientProvider.cleanup()
}
