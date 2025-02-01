package it.fscarponi.ai

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import it.fscarponi.config.AIConfig
import it.fscarponi.config.HttpClientProvider
import it.fscarponi.model.Character
import it.fscarponi.service.ChatRequest
import it.fscarponi.service.ChatResponse
import it.fscarponi.service.Message
import kotlinx.coroutines.runBlocking

object SystemPrompt {
    private val messageStructure = "" //todo

    private val guidelines = """
        Guidelines:
            - Use the language used by the user (if the user say something in italian, use italian as language)
            - Stay in character and use first person
            - Express actions in *asterisks*
            - Use character-specific speech patterns
            - Reference your background naturally
            - Show personality through responses
            - Maintain consistent behavior
            - For direct questions, please provide direct and concise answers
            - Don't be verbose
            - Don't repeat yourself
        """

    private fun generateCharacterPrompt(character: Character) =
        """
            You are roleplaying as a character with these traits:
            Name: ${character.name}
            Role: ${character.role}
            Personality: ${character.personality}
            Background: ${character.background}
            Knowledge: ${character.knowledge.joinToString("\n            - ", prefix = "\n            - ")}
            Goals: ${character.goals.joinToString("\n            - ", prefix = "\n            - ")}
            Secrets: ${character.secrets.joinToString("\n            - ", prefix = "\n            - ")}
            Connections: ${character.connections.joinToString("\n            - ", prefix = "\n            - ")}
        
            Roleplay is now starting! don't leave your character!
        """.trimIndent()


    private fun fullPrompt(character: Character): String =
        """
            ${
            if (messageStructure.isNotBlank())
                "Messages should have the following structure: $messageStructure" else ""
        }
            
            $guidelines
            
            ${generateCharacterPrompt(character)}
           
        """.trimIndent()


    fun generatePrompt(character: Character, language: String? = null): String {
        val prompt = fullPrompt(character)
        return if (language != null) {
            localizePrompt(prompt, language)
        } else prompt
    }

    private fun localizePrompt(prompt: String, language: String): String = runBlocking {
        val response =
            HttpClientProvider.client.post("${AIConfig.HUGGINGFACE_API_URL}${AIConfig.DEFAULT_MODEL}/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${AIConfig.API_TOKEN}")
                setBody(
                    body = ChatRequest(
                        messages = listOf(
                            Message(
                                role = "user",
                                content = "Translate in ${language} the following text: ${prompt}"
                            )
                        )
                    )
                )
            }
        val chatResponse: ChatResponse = response.body()
        val assistantTranslation = chatResponse.choices.firstOrNull()?.message?.content

//        printColored("translated prompt", COLOR_PROMPT)
//        printColored(assistantTranslation ?: "no translation provided", COLOR_PROMPT)

        return@runBlocking assistantTranslation ?: prompt
    }


}





