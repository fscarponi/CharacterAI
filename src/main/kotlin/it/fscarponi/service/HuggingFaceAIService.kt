package it.fscarponi.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import it.fscarponi.config.AIConfig
import it.fscarponi.model.Character

class HuggingFaceAIService : AIService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
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
            Name: ${character.name}
            Role: ${character.role}
            Personality: ${character.personality}
            Background: ${character.background}
            Knowledge: ${character.knowledge.joinToString("\n            - ", prefix = "\n            - ")}
            Goals: ${character.goals.joinToString("\n            - ", prefix = "\n            - ")}
            Secrets: ${character.secrets.joinToString("\n            - ", prefix = "\n            - ")}
            Connections: ${character.connections.joinToString("\n            - ", prefix = "\n            - ")}

            Guidelines:
            - Stay in character and use first person
            - Express actions in *asterisks*
            - Use character-specific speech patterns
            - Reference your background naturally
            - Show personality through responses
            - Maintain consistent behavior
            - For direct questions, please provide direct and concise answers
            - Try to answer direct questions directly and briefly.
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

    fun cleanup() {
        client.close()
        messageHistory.clear()
    }
}
