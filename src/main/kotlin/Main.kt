package it.fscarponi

import it.fscarponi.data.CharacterRepository
import it.fscarponi.data.SQLiteCharacterRepository
import it.fscarponi.model.Character
import it.fscarponi.service.HuggingFaceAIService
import it.fscarponi.telegram.CharacterAIBot
import it.fscarponi.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

fun createCustomCharacter(): Character {
    printColored("\n=== Character Creation ===", COLOR_HEADER)

    printColored("Enter character name: ", COLOR_PROMPT)
    val name = readlnOrNull() ?: "Unknown"

    printColored("Enter character role (e.g., warrior, wizard, merchant): ", COLOR_PROMPT)
    val role = readlnOrNull() ?: "adventurer"

    printColored("Enter character personality traits: ", COLOR_PROMPT)
    val personality = readlnOrNull() ?: "friendly and helpful"

    printColored("Enter character background story: ", COLOR_PROMPT)
    val background = readlnOrNull() ?: "A mysterious traveler"

    printColored("\nNow let's add some knowledge areas (enter 'done' when finished)", COLOR_HEADER)
    val knowledge = mutableListOf<String>()
    while (true) {
        printColored("Enter an area of expertise: ", COLOR_PROMPT)
        val input = readlnOrNull() ?: "done"
        if (input.lowercase() == "done") break
        knowledge.add(input)
        printColored("Added: $input", COLOR_INFO)
    }

    printColored("\nLet's set some goals (enter 'done' when finished)", COLOR_HEADER)
    val goals = mutableListOf<String>()
    while (true) {
        printColored("Enter a goal: ", COLOR_PROMPT)
        val input = readlnOrNull() ?: "done"
        if (input.lowercase() == "done") break
        goals.add(input)
        printColored("Added: $input", COLOR_INFO)
    }

    printColored("\nFinally, let's add some connections (enter 'done' when finished)", COLOR_HEADER)
    val connections = mutableListOf<String>()
    while (true) {
        printColored("Enter a connection: ", COLOR_PROMPT)
        val input = readlnOrNull() ?: "done"
        if (input.lowercase() == "done") break
        connections.add(input)
        printColored("Added: $input", COLOR_INFO)
    }

    return Character(
        name = name,
        role = role,
        personality = personality,
        background = background,
        knowledge = if (knowledge.isEmpty()) listOf("Basic knowledge of their profession") else knowledge,
        goals = if (goals.isEmpty()) listOf("Seek adventure and knowledge") else goals,
        connections = if (connections.isEmpty()) listOf("Various acquaintances in their hometown") else connections
    )
}

fun displayCharacterPreview(character: Character) {
    printColored("\n=== Character Preview ===", COLOR_HEADER)
    printColored("Name: ${character.name}", COLOR_INFO)
    printColored("Role: ${character.role}", COLOR_INFO)
    printColored("Personality: ${character.personality}", COLOR_INFO)
    printColored("Background: ${character.background}", COLOR_INFO)

    if (character.knowledge.isNotEmpty()) {
        printColored("\nKnowledge & Expertise:", COLOR_PROMPT)
        character.knowledge.forEach { printColored("- $it", COLOR_INFO) }
    }

    if (character.goals.isNotEmpty()) {
        printColored("\nGoals:", COLOR_PROMPT)
        character.goals.forEach { printColored("- $it", COLOR_INFO) }
    }

    if (character.connections.isNotEmpty()) {
        printColored("\nConnections:", COLOR_PROMPT)
        character.connections.forEach { printColored("- $it", COLOR_INFO) }
    }
    printColored("=====================", COLOR_HEADER)
}

fun showHelp() {
    printColored("\n=== Help & Commands ===", COLOR_HEADER)

    printColored("\nAvailable Commands:", COLOR_PROMPT)
    printColored("- help   : Show this help message", COLOR_INFO)
    printColored("- status : Display current character information", COLOR_INFO)
    printColored("- clear  : Clear the conversation history", COLOR_INFO)
    printColored("- exit   : End the conversation", COLOR_INFO)

    printColored("\nInteraction Tips:", COLOR_PROMPT)
    printColored("1. Address the character by their name or title", COLOR_INFO)
    printColored("2. Ask about their knowledge areas and experiences", COLOR_INFO)
    printColored("3. Reference their background and connections", COLOR_INFO)
    printColored("4. Stay in character during conversations", COLOR_INFO)
    printColored("5. Use 'status' to remind yourself of character details", COLOR_INFO)

    printColored("\nConversation:", COLOR_PROMPT)
    printColored("Just type your message and press Enter to talk to the character", COLOR_INFO)
    printColored("The character will respond based on their personality and knowledge", COLOR_INFO)

    printColored("\n=====================", COLOR_HEADER)
}

suspend fun selectCharacter(repository: CharacterRepository): Character {
    val availableCharacters = repository.getAllCharacters()

    while (true) {
        printColored("\n=== Available Characters ===", COLOR_HEADER)
        availableCharacters.forEachIndexed { index, char ->
            printColored("\n${index + 1}. ${char.name} (${char.role})", COLOR_PROMPT)
            displayCharacterPreview(char)
        }
        printColored("\nOptions:", COLOR_HEADER)
        printColored("- Enter a number to select a character", COLOR_INFO)
        printColored("- Type 'custom' to create your own", COLOR_INFO)
        printColored("- Type 'help' for more information", COLOR_INFO)
        printColored("\nYour choice: ", COLOR_PROMPT)

        when (val input = readlnOrNull()?.lowercase() ?: "") {
            "help" -> {
                printColored("\n=== Help ===", COLOR_HEADER)
                printColored("1. Select an existing character by entering its number", COLOR_INFO)
                printColored("2. Create your own character by typing 'custom'", COLOR_INFO)
                printColored("3. Each character has unique traits, knowledge, and goals", COLOR_INFO)
                printColored("4. You can preview a character before confirming", COLOR_INFO)
                continue
            }

            "custom" -> {
                val newCharacter = createCustomCharacter()
                printColored("\nConfirm character creation? (yes/no):", COLOR_PROMPT)
                if (readlnOrNull()?.lowercase() in listOf("yes", "y")) {
                    repository.addCharacter(newCharacter)
                    printColored("Character created successfully!", COLOR_SUCCESS)
                    return newCharacter
                } else {
                    printColored("Character creation cancelled. Please try again.", COLOR_WARNING)
                    continue
                }
            }

            else -> {
                val index = input.toIntOrNull()?.let { it - 1 }
                if (index != null && index in availableCharacters.indices) {
                    val selectedCharacter = availableCharacters[index]
                    printColored("\nYou selected:", COLOR_PROMPT)
                    displayCharacterPreview(selectedCharacter)
                    printColored("\nConfirm selection? (yes/no):", COLOR_PROMPT)
                    if (readlnOrNull()?.lowercase() in listOf("yes", "y")) {
                        printColored("Character selected successfully!", COLOR_SUCCESS)
                        return selectedCharacter
                    } else {
                        printColored("Selection cancelled. Please try again.", COLOR_WARNING)
                        continue
                    }
                } else {
                    printColored("Invalid choice. Please try again.", COLOR_ERROR)
                    continue
                }
            }
        }
    }
}

private fun initializeTelegramBot(repository: CharacterRepository, aiService: HuggingFaceAIService) {
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

private suspend fun startCliInterface(repository: CharacterRepository, aiService: HuggingFaceAIService) {
    // Display welcome banner
    printColored("\n=== Welcome to Character AI ===", COLOR_HEADER)
    printColored("An immersive role-playing experience with AI-powered characters", COLOR_INFO)
    printColored("============================================", COLOR_HEADER)

    val character = selectCharacter(repository)

    printColored("\n=== Session Started ===", COLOR_HEADER)
    printColored("You are now interacting with:", COLOR_INFO)
    displayCharacterPreview(character)
    printColored("\nType your messages to interact with the character.", COLOR_INFO)
    showHelp()

    while (true) {
        printColored("\nYou: ", COLOR_PROMPT)
        val input = readlnOrNull() ?: continue

        when (input.lowercase()) {
            "exit" -> {
                printColored("\n=== Session Ended ===", COLOR_HEADER)
                printColored("Thank you for using Character AI!", COLOR_SUCCESS)
                break
            }

            "help" -> showHelp()
            "status" -> {
                printColored("\n=== Character Status ===", COLOR_HEADER)
                displayCharacterPreview(character)
            }

            "clear" -> {
                println("\n".repeat(50))
                printColored("=== Conversation Cleared ===", COLOR_SUCCESS)
            }

            else -> try {
                val response = aiService.generateResponse(character, input)
                printColored("\n${character.name}: ", COLOR_PROMPT)
                printColored(response, COLOR_INFO)
            } catch (e: Exception) {
                printColored("Error: ${e.message}", COLOR_ERROR)
                printColored("Please try again or type 'help' for available commands.", COLOR_WARNING)
            }
        }
    }
}

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
}
