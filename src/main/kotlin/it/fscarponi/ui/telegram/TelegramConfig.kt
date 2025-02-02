package it.fscarponi.ui.telegram

import java.io.File
import java.io.FileInputStream
import java.util.Properties

object TelegramConfig {
    private val properties = Properties().apply {
        val file = File("local.properties")
        if (file.exists()) {
            FileInputStream(file).use { load(it) }
        }
    }

    val BOT_TOKEN: String = System.getenv("TELEGRAM_BOT_TOKEN")
        ?: properties.getProperty("telegram.bot.token")
        ?: throw IllegalStateException("TELEGRAM_BOT_TOKEN not found in environment or local.properties")

    val BOT_USERNAME: String = System.getenv("TELEGRAM_BOT_USERNAME")
        ?: properties.getProperty("telegram.bot.username")
        ?: throw IllegalStateException("TELEGRAM_BOT_USERNAME not found in environment or local.properties")
}
