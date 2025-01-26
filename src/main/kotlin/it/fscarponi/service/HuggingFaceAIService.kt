package it.fscarponi.service

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import it.fscarponi.ai.SystemPrompt
import it.fscarponi.config.AIConfig
import it.fscarponi.config.HttpClientProvider
import it.fscarponi.model.Character

class HuggingFaceAIService(
    private val client: io.ktor.client.HttpClient = HttpClientProvider.client
) : AIService {

    // Store conversation history
    private val messageHistory = mutableListOf<Message>()

    // Initialize system message when starting conversation with a character
    private fun initializeSystemMessage(character: Character, language: String = "english") {
        val systemPrompt = SystemPrompt.generatePrompt(character, language)
        messageHistory.clear() // Clear previous history
        messageHistory.add(Message(role = "system", content = systemPrompt))
    }

    override suspend fun generateResponse(character: Character, userInput: String, language: String): String {
        // Initialize or reinitialize if system message is different
        if (messageHistory.isEmpty()) {
            initializeSystemMessage(character, language)
        }

        // Add user message to history
        messageHistory.add(Message(role = "user", content = userInput))

        try {
            val response = client.post("${AIConfig.HUGGINGFACE_API_URL}${AIConfig.DEFAULT_MODEL}/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${AIConfig.API_TOKEN}")
                setBody(
                    ChatRequest(
                        messages = messageHistory.toList()
                    )
                )
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
        messageHistory.clear()
    }
}
