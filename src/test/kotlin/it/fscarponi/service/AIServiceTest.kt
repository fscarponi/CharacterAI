package it.fscarponi.service

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import it.fscarponi.model.Character

class MockAIService : AIService {
    override suspend fun generateResponse(character: Character, userInput: String): String {
        return "Greetings! I am ${character.name}, ${character.role}. ${character.personality}. " +
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
        val response = service.generateResponse(character, "Hello, who are you?")

        // Then
        assertNotNull(response, "Response should not be null")
        assertTrue(response.isNotBlank(), "Response should not be empty")
        assertTrue(response.contains(character.name), "Response should contain character name")
        assertTrue(response.contains(character.role), "Response should contain character role")
        println("[DEBUG_LOG] Mock AI Response: $response")
    }

    @Test
    fun `test real HuggingFace AI service generates response`() = runBlocking {
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
        val service = HuggingFaceAIService()

        // When
        val response = service.generateResponse(character, "Archmage Aldrich, I found some ancient runes that seem to pulse with dark energy. The symbols remind me of those used by the Shadowmancer's Guild.")

        // Then
        assertNotNull(response, "Response should not be null")
        assertTrue(response.isNotBlank(), "Response should not be empty")

        // Log the response for debugging
        println("[DEBUG_LOG] Real AI Response: $response")

        // Verify response incorporates character traits
        val runeExpertise = response.lowercase().contains("rune") || response.lowercase().contains("ancient language")
        val shadowmancerReference = response.lowercase().contains("shadowmancer") || response.lowercase().contains("guild")
        val artifactGoal = response.lowercase().contains("artifact") || response.lowercase().contains("preserve")

        assertTrue(runeExpertise, "Response should reference character's expertise in runes")
        assertTrue(shadowmancerReference, "Response should acknowledge the Shadowmancer connection")
        assertTrue(artifactGoal, "Response should reflect character's goal of preserving artifacts")

        // Print detailed analysis
        println("[DEBUG_LOG] Response Analysis:")
        println("[DEBUG_LOG] - Rune Expertise Referenced: $runeExpertise")
        println("[DEBUG_LOG] - Shadowmancer Connection Used: $shadowmancerReference")
        println("[DEBUG_LOG] - Artifact Preservation Goal Shown: $artifactGoal")
    }
}
