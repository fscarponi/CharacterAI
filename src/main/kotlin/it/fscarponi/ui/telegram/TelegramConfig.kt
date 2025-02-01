package it.fscarponi.telegram

object TelegramConfig {
    val BOT_TOKEN: String by lazy {
        System.getenv("TELEGRAM_BOT_TOKEN") ?: run {
            val properties = java.util.Properties()
            val propertiesFile = java.io.File("local.properties")
            if (!propertiesFile.exists()) {
                throw IllegalStateException("Neither TELEGRAM_BOT_TOKEN environment variable nor local.properties file found")
            }
            properties.load(propertiesFile.inputStream())
            properties.getProperty("telegram.bot.token") ?: throw IllegalStateException(
                "telegram.bot.token not found in local.properties and TELEGRAM_BOT_TOKEN not set in environment"
            )
        }
    }

    val BOT_USERNAME: String by lazy {
        System.getenv("TELEGRAM_BOT_USERNAME") ?: run {
            val properties = java.util.Properties()
            val propertiesFile = java.io.File("local.properties")
            if (!propertiesFile.exists()) {
                throw IllegalStateException("Neither TELEGRAM_BOT_USERNAME environment variable nor local.properties file found")
            }
            properties.load(propertiesFile.inputStream())
            properties.getProperty("telegram.bot.username") ?: throw IllegalStateException(
                "telegram.bot.username not found in local.properties and TELEGRAM_BOT_USERNAME not set in environment"
            )
        }
    }
}
