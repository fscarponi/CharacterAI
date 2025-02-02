package it.fscarponi.telegram

import it.fscarponi.ui.UIType
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.methods.ActionType
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

        // Handle language selection
        if (sessionManager.getCreationState(chatId) == CreationState.AWAITING_LANGUAGE_SELECTION && !messageText.startsWith("/")) {
            handleLanguageSelection(chatId, messageText)
            return
        }

        // Handle creation flow if user is in creation state
        if (sessionManager.getCreationState(chatId) != CreationState.NONE && 
            sessionManager.getCreationState(chatId) != CreationState.AWAITING_LANGUAGE_SELECTION && 
            !messageText.startsWith("/")) {
            handleCreationFlow(chatId, messageText)
            return
        }

        when {
            messageText.startsWith("/start") -> handleStart(chatId)
            messageText.startsWith("/help") -> handleHelp(chatId)
            messageText.startsWith("/create") -> handleCreate(chatId)
            messageText.startsWith("/select") -> handleSelect(chatId, messageText.removePrefix("/select").trim())
            messageText.startsWith("/chat") -> handleChat(chatId, messageText.removePrefix("/chat").trim())
            messageText.startsWith("/stopchat") -> handleStopChat(chatId)
            messageText.startsWith("/cancel") -> handleCancel(chatId)
            sessionManager.isChatMode(chatId) -> handleChat(chatId, messageText)
            else -> sendMessage(chatId, "Please use one of the available commands. Type /help for more information.")
        }
    }

    private fun handleStart(chatId: String) {
        sessionManager.setCreationState(chatId, CreationState.AWAITING_LANGUAGE_SELECTION)
        val welcomeMessage = """
            Welcome to Character AI Bot! 🤖

            Please select your preferred language:

            1. English
            2. Italian
            3. Spanish
            4. French

            Reply with the number of your choice (1-4).
            (Default: English)
        """.trimIndent()

        sendMessage(chatId, welcomeMessage)
    }

    private fun handleUISelection(chatId: String, message: String) {
        val uiType = when (message.trim()) {
            "1" -> UIType.CURRENT
            "2" -> UIType.NEW
            else -> UIType.CURRENT // Default to Current UI for invalid inputs
        }

        sessionManager.setSelectedUIType(chatId, uiType)
        sessionManager.setCreationState(chatId, CreationState.AWAITING_LANGUAGE_SELECTION)

        val uiConfirmationMessage = """
            UI version set to: $uiType

            Now, please select your preferred language:

            1. English
            2. Italian
            3. Spanish
            4. French

            Reply with the number of your choice (1-4).
            (Default: English)
        """.trimIndent()

        sendMessage(chatId, uiConfirmationMessage)
    }

    private fun handleLanguageSelection(chatId: String, message: String) {
        val languageMap = mapOf(
            "1" to "english",
            "2" to "italian",
            "3" to "spanish",
            "4" to "french"
        )

        val selectedLanguage = when {
            message.trim() in languageMap.keys -> languageMap[message.trim()]!!
            message.trim().lowercase() in languageMap.values -> message.trim().lowercase()
            else -> "english" // Default to English for invalid inputs
        }

        sessionManager.setSelectedLanguage(chatId, selectedLanguage)
        sessionManager.setCreationState(chatId, CreationState.NONE)

        val confirmationMessage = """
            Language set to: ${selectedLanguage.capitalize()}

            This bot allows you to interact with AI-powered characters.

            Available commands:
            /help - Show available commands
            /create - Create a new character
            /select - Select an existing character
            /stopchat - Exit chat mode and return to character selection

            Type /help to get started!
        """.trimIndent()

        sendMessage(chatId, confirmationMessage)
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
            CreationState.NONE -> {
                sendMessage(chatId, "No active creation flow. Use /create to start creating a character.")
            }

            CreationState.AWAITING_UI_SELECTION -> {
                handleUISelection(chatId, message)
            }

            CreationState.AWAITING_LANGUAGE_SELECTION -> {
                handleLanguageSelection(chatId, message)
            }

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
                            The curtain opens!
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
                        sessionManager.setChatMode(chatId, true)
                        sendMessage(chatId, "You can now start chatting directly with ${character.name}! Use /stopchat to exit chat mode.")
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
            sendMessage(chatId, "Please provide a message to chat.")
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
                val selectedLanguage = sessionManager.getSelectedLanguage(chatId)
                val response = aiService.generateResponse(selectedCharacter, message, selectedLanguage)
                sendMessage(chatId, response)
            } catch (e: Exception) {
                sendMessage(chatId, "Sorry, I encountered an error while generating the response. Please try again.")
                e.printStackTrace() // For debugging purposes
            }
        }
    }

    private fun handleStopChat(chatId: String) {
        sessionManager.setChatMode(chatId, false)
        sendMessage(chatId, "Chat mode disabled. You can select another character with /select or use other commands.")
    }

    private fun sendTypingAction(chatId: String) {
        val action = SendChatAction()
        action.setChatId(chatId)
        action.setAction(ActionType.TYPING)
        execute(action)
    }

    private fun sendMessage(chatId: String, text: String) {
        sendTypingAction(chatId)
        val message = SendMessage()
        message.chatId = chatId
        message.text = text
        execute(message)
    }
}
