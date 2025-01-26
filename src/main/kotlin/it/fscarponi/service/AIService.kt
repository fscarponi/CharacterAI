package it.fscarponi.service

import it.fscarponi.model.Character
import kotlinx.serialization.Serializable

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
    suspend fun generateResponse(character: Character, userInput: String, language: String = "english"): String
}
