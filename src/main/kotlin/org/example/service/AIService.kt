package org.example.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.example.config.AIConfig
import org.example.model.Character

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class ChatChoice(
    val message: Message
)

@Serializable
data class ChatResponse(
    val choices: List<ChatChoice>
)

@Serializable
data class ChatRequest(
    val model: String = "mistralai/Mistral-Nemo-Instruct-2407",
    val messages: List<Message>,
    val temperature: Double = 0.5,    // Default from example
    val max_tokens: Int = 2048,       // Default from example
    val top_p: Double = 0.7          // Default from example
)

interface AIService {
    suspend fun generateResponse(character: Character, userInput: String): String
}

class HuggingFaceAIService : AIService {
    private val client = HttpClient(CIO) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(kotlinx.serialization.json.Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    // Store conversation history
    private val messageHistory = mutableListOf<Message>()

    // Initialize system message when starting conversation with a character
    private fun initializeSystemMessage(character: Character) {
        val systemPrompt = """
            You are roleplaying as a character with these traits:
            ${character.toString()}

            Guidelines:
            - Stay in character and use first person
            - Express actions in *asterisks*
            - Use character-specific speech patterns
            - Reference your background naturally
            - Show personality through responses
            - Maintain consistent behavior
        """.trimIndent()

        messageHistory.clear() // Clear previous history
        messageHistory.add(Message(role = "system", content = systemPrompt))
    }

    override suspend fun generateResponse(character: Character, userInput: String): String {
        // Initialize or reinitialize if system message is different
        if (messageHistory.isEmpty()) {
            initializeSystemMessage(character)
        }

        // Add user message to history
        messageHistory.add(Message(role = "user", content = userInput))

        try {
            val response = client.post("${AIConfig.HUGGINGFACE_API_URL}${AIConfig.DEFAULT_MODEL}/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${AIConfig.API_TOKEN}")
                setBody(ChatRequest(
                    messages = messageHistory.toList()
                ))
            }

            val chatResponse: ChatResponse = response.body()
            val assistantMessage = chatResponse.choices.firstOrNull()?.message

            if (assistantMessage != null) {
                // Add assistant's response to history
                messageHistory.add(assistantMessage)
                return assistantMessage.content
            }

            return "I apologize, but I couldn't generate a proper response."
        } catch (e: Exception) {
            // In case of error, remove the last user message from history
            if (messageHistory.isNotEmpty()) {
                messageHistory.removeAt(messageHistory.lastIndex)
            }
            throw e
        }
    }
}
