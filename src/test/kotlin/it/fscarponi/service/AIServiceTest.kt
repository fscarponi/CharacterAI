package it.fscarponi.service

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import it.fscarponi.config.HttpClientProvider
import it.fscarponi.model.Character
import kotlin.test.AfterTest
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class MockAIService : AIService {
    override suspend fun generateResponse(character: Character, userInput: String, language: String): String {
        val greeting = when(language.lowercase()) {
            "italian" -> "Saluti"
            "spanish" -> "¡Saludos"
            "french" -> "Salutations"
            else -> "Greetings"
        }
        return "$greeting! I am ${character.name}, ${character.role}. ${character.personality}. " +
               "To answer your question: $userInput - I shall respond as a ${character.role} would!"
    }
}

class AIServiceTest {

    @Test
    fun `test mock AI service generates response`() = runBlocking {
        // Given
        val character = Character(
            name = "Test Character",
            role = "warrior",
            personality = "brave and strong",
            background = "A legendary warrior from the north"
        )
        val service = MockAIService()

        // When
        val response = service.generateResponse(character, "Hello, who are you?", "english")

        // Then
        assertNotNull(response, "Response should not be null")
        assertTrue(response.isNotBlank(), "Response should not be empty")
        assertTrue(response.contains(character.name), "Response should contain character name")
        assertTrue(response.contains(character.role), "Response should contain character role")
        println("[DEBUG_LOG] Mock AI Response: $response")
    }

    @Test
    fun `test real HuggingFace AI service generates response`() {
        runBlocking {
            // Given
            val character = Character(
                name = "Archmage Aldrich",
                role = "master wizard",
                personality = "scholarly, wise, and slightly eccentric",
                background = "Once the youngest archmage in the Crystal Tower's history, now a wandering sage who collects magical artifacts.",
                knowledge = listOf(
                    "Expert in ancient runic languages",
                    "Specialist in artifact identification",
                    "Master of experimental spellcraft"
                ),
                secrets = listOf(
                    "Knows the true location of the lost Library of Shadows",
                    "Can communicate with beings from other dimensions"
                ),
                goals = listOf(
                    "Find and preserve dangerous magical artifacts",
                    "Train the next generation of responsible magic users"
                ),
                connections = listOf(
                    "Secret advisor to the Queen on magical matters",
                    "Has a complicated rivalry with the Shadowmancer's Guild"
                )
            )
            val testClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
            }
            val service = HuggingFaceAIService(testClient)

            try {
                // When
                val response = service.generateResponse(character, "Archmage Aldrich, I found some ancient runes that seem to pulse with dark energy. The symbols remind me of those used by the Shadowmancer's Guild.", "english")

                // Then
                assertNotNull(response, "Response should not be null")
                assertTrue(response.isNotBlank(), "Response should not be empty")

                // Log the response for debugging
                println("[DEBUG_LOG] Real AI Response: $response")

                // Verify response incorporates character traits
                val runeExpertise = response.lowercase().contains("rune") || response.lowercase().contains("ancient language")
                val shadowmancerReference = response.lowercase().contains("shadowmancer") || response.lowercase().contains("guild")
                // Check for expertise and connections (these are more directly relevant to the input)
                assertTrue(runeExpertise, "Response should reference character's expertise in runes")
                assertTrue(shadowmancerReference, "Response should acknowledge the Shadowmancer connection")

                // Check for character traits (more flexible matching as these might be expressed in various ways)
                val characterTraits = response.lowercase().run {
                    contains("artifact") || contains("preserve") || 
                    contains("power") || contains("rare") || contains("handle") ||
                    contains("dangerous") || contains("protect")
                }
                assertTrue(characterTraits, "Response should reflect character's traits and concerns about magical items")

                // Print detailed analysis
                println("[DEBUG_LOG] Response Analysis:")
                println("[DEBUG_LOG] - Rune Expertise Referenced: $runeExpertise")
                println("[DEBUG_LOG] - Shadowmancer Connection Used: $shadowmancerReference")
                println("[DEBUG_LOG] - Character Traits Shown: $characterTraits")
            } finally {
                service.cleanup()
                testClient.close()
            }
        }
    }

    @Test
    fun `test mock AI service handles different languages`() = runBlocking {
        // Given
        val character = Character(
            name = "Test Character",
            role = "warrior",
            personality = "brave and strong",
            background = "A legendary warrior from the north"
        )
        val service = MockAIService()

        // Test different languages
        val languages = mapOf(
            "english" to "Greetings",
            "italian" to "Saluti",
            "spanish" to "¡Saludos",
            "french" to "Salutations"
        )

        for ((language, expectedGreeting) in languages) {
            // When
            val response = service.generateResponse(character, "Hello", language)

            // Then
            assertTrue(response.startsWith(expectedGreeting),
                "Response for $language should start with '$expectedGreeting', but was: $response")
            println("[DEBUG_LOG] $language response: $response")
        }
    }
}
