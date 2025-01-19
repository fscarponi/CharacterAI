package it.fscarponi.telegram

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import it.fscarponi.data.CharacterRepository
import it.fscarponi.service.HuggingFaceAIService
import kotlinx.coroutines.runBlocking
import it.fscarponi.model.Character

class CharacterAIBot(
    private val characterRepository: CharacterRepository,
    private val aiService: HuggingFaceAIService
) : TelegramLongPollingBot() {
    private val sessionManager = TelegramSessionManager()

    override fun getBotToken(): String = TelegramConfig.BOT_TOKEN

    override fun getBotUsername(): String = TelegramConfig.BOT_USERNAME

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage() || !update.message.hasText()) return

        val chatId = update.message.chatId.toString()
        val messageText = update.message.text

        // Handle creation flow if user is in creation state
        if (sessionManager.getCreationState(chatId) != CreationState.NONE && !messageText.startsWith("/")) {
            handleCreationFlow(chatId, messageText)
            return
        }

        when {
            messageText.startsWith("/start") -> handleStart(chatId)
            messageText.startsWith("/help") -> handleHelp(chatId)
            messageText.startsWith("/create") -> handleCreate(chatId)
            messageText.startsWith("/select") -> handleSelect(chatId, messageText.removePrefix("/select").trim())
            messageText.startsWith("/chat") -> handleChat(chatId, messageText.removePrefix("/chat").trim())
            messageText.startsWith("/cancel") -> handleCancel(chatId)
            else -> sendMessage(chatId, "Please use one of the available commands. Type /help for more information.")
        }
    }

    private fun handleStart(chatId: String) {
        val welcomeMessage = """
            Welcome to Character AI Bot! 🤖

            This bot allows you to interact with AI-powered characters.

            Available commands:
            /help - Show available commands
            /create - Create a new character
            /select - Select an existing character
            /chat <message> - Chat with the selected character

            Type /help to get started!
        """.trimIndent()

        sendMessage(chatId, welcomeMessage)
    }

    private fun handleHelp(chatId: String) {
        val helpMessage = """
            Available commands:

            /start - Start the bot
            /help - Show this help message
            /create - Create a new character
            /select - Select an existing character
            /chat <message> - Chat with the selected character

            How to use:
            1. First create a character using /create or select an existing one with /select
            2. Start chatting with your character using /chat followed by your message
        """.trimIndent()

        sendMessage(chatId, helpMessage)
    }

    private fun handleCreate(chatId: String) {
        sessionManager.setCreationState(chatId, CreationState.AWAITING_NAME)
        sendMessage(
            chatId, """
            Let's create a new character! 

            Please enter the character's name.
            (Type /cancel to abort character creation)
        """.trimIndent()
        )
    }

    private fun handleCreationFlow(chatId: String, message: String) {
        val session = sessionManager.getSession(chatId)

        when (sessionManager.getCreationState(chatId)) {
            CreationState.AWAITING_NAME -> {
                session.tempCharacterData["name"] = message
                sessionManager.setCreationState(chatId, CreationState.AWAITING_ROLE)
                sendMessage(
                    chatId, """
                    Great! Now please provide the character's role.
                    This could be their occupation, position, or primary function (e.g., "Medieval Knight", "Space Explorer", "Village Healer").
                """.trimIndent()
                )
            }

            CreationState.AWAITING_ROLE -> {
                session.tempCharacterData["role"] = message
                sessionManager.setCreationState(chatId, CreationState.AWAITING_BACKGROUND)
                sendMessage(
                    chatId, """
                    Excellent! Now please provide the character's background story.
                    This should include their history, significant events, and any relevant details.
                """.trimIndent()
                )
            }

            CreationState.AWAITING_BACKGROUND -> {
                session.tempCharacterData["background"] = message
                sessionManager.setCreationState(chatId, CreationState.AWAITING_PERSONALITY)
                sendMessage(
                    chatId, """
                    Excellent! Now describe the character's personality traits.
                    Include their temperament, habits, and typical behavior.
                """.trimIndent()
                )
            }

            CreationState.AWAITING_PERSONALITY -> {
                session.tempCharacterData["personality"] = message
                sessionManager.setCreationState(chatId, CreationState.AWAITING_CONFIRMATION)

                // Show character summary and ask for confirmation
                val summary = """
                    Please review your character:

                    Name: ${session.tempCharacterData["name"]}
                    Role: ${session.tempCharacterData["role"]}

                    Background:
                    ${session.tempCharacterData["background"]}

                    Personality:
                    ${session.tempCharacterData["personality"]}

                    Type 'confirm' to create this character or /cancel to start over.
                """.trimIndent()
                sendMessage(chatId, summary)
            }

            CreationState.AWAITING_CONFIRMATION -> {
                if (message.lowercase() == "confirm") {
                    // Create the character
                    runBlocking {
                        val character = Character(
                            name = session.tempCharacterData["name"] ?: "",
                            role = session.tempCharacterData["role"] ?: "",
                            background = session.tempCharacterData["background"] ?: "",
                            personality = session.tempCharacterData["personality"] ?: "",
                            knowledge = emptyList(),
                            secrets = emptyList(),
                            goals = emptyList(),
                            connections = emptyList()
                        )
                        characterRepository.addCharacter(character)
                        sessionManager.setSelectedCharacter(chatId, character)
                        sessionManager.setCreationState(chatId, CreationState.NONE)
                        session.tempCharacterData.clear()

                        sendMessage(
                            chatId, """
                            Character created successfully! 
                            You can now start chatting with ${character.name} using the /chat command.
                        """.trimIndent()
                        )
                    }
                } else {
                    sendMessage(chatId, "Please type 'confirm' to create the character or /cancel to start over.")
                }
            }

            CreationState.NONE -> {
                // This shouldn't happen as we check the state before calling handleCreationFlow
                sendMessage(chatId, "Something went wrong. Please try again with /create command.")
            }
        }
    }

    private fun handleCancel(chatId: String) {
        val session = sessionManager.getSession(chatId)
        session.tempCharacterData.clear()
        sessionManager.setCreationState(chatId, CreationState.NONE)
        sendMessage(chatId, "Operation cancelled. You can start over with /create or use other commands.")
    }

    private fun handleSelect(chatId: String, message: String) {
        runBlocking {
            val characters = characterRepository.getAllCharacters()
            if (characters.isEmpty()) {
                sendMessage(chatId, "No characters found. Create one using /create command.")
                return@runBlocking
            }

            if (message.isNotEmpty()) {
                message.toIntOrNull()?.let {
                    characters.getOrNull(it-1)?.let { character ->
                        sendMessage(chatId, "The character $it has been selected successfully.")
                        sessionManager.setSelectedCharacter(chatId, character = character)
                        sendMessage(chatId, "use /chat to start interaction!")
                    }
                } ?: sendMessage(chatId, "The character id not found...")
            } else {
                val message = StringBuilder("Available characters:\n\n")
                characters.forEachIndexed { index, character ->
                    message.append("${index + 1}. ${character.name}\n")
                    message.append("   Background: ${character.background}\n\n")
                }
                message.append("\nUse /select <number> to choose a character.")

                sendMessage(chatId, message.toString())
            }

        }
    }

    private fun handleChat(chatId: String, message: String) {
        if (message.isEmpty()) {
            sendMessage(chatId, "Please provide a message after /chat command.")
            return
        }

        val selectedCharacter = sessionManager.getSelectedCharacter(chatId)
        if (selectedCharacter == null) {
            sendMessage(
                chatId, """
                No character selected. Please either:
                1. Select an existing character using /select
                2. Create a new character using /create
            """.trimIndent()
            )
            return
        }

        runBlocking {
            try {
                val response = aiService.generateResponse(selectedCharacter, message)
                sendMessage(chatId, response)
            } catch (e: Exception) {
                sendMessage(chatId, "Sorry, I encountered an error while generating the response. Please try again.")
                e.printStackTrace() // For debugging purposes
            }
        }
    }

    private fun sendMessage(chatId: String, text: String) {
        val message = SendMessage()
        message.chatId = chatId
        message.text = text
        execute(message)
    }
}
