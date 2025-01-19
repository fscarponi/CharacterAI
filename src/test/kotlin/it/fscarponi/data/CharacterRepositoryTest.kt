package it.fscarponi.data

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import it.fscarponi.model.Character
import kotlin.test.*
import java.io.File

class CharacterRepositoryTest {
    private lateinit var repository: SQLiteCharacterRepository
    private lateinit var dbFile: String

    companion object {
        private var testCounter = 0

        @Synchronized
        private fun getNextTestId(): Int = ++testCounter
    }

    private fun cleanupDatabase() {
        runCatching {
            if (this::repository.isInitialized) {
                runBlocking {
                    repository.cleanup()
                }
            }
            // Wait a bit to ensure all connections are properly closed
            Thread.sleep(100)
            // Delete all database files
            File(dbFile).delete()
            File("$dbFile-shm").delete()
            File("$dbFile-wal").delete()
        }.onFailure { e ->
            println("[DEBUG_LOG] Error during database cleanup: ${e.message}")
        }
    }

    @BeforeTest
    fun setup() = runBlocking {
        // Create a unique database file for each test
        dbFile = "test_db_${getNextTestId()}.db"
        cleanupDatabase()

        repository = SQLiteCharacterRepository(dbFile)

        // Add a test character with retry logic
        repeat(3) { attempt ->
            try {
                repository.addCharacter(
                    Character(
                        name = "Test Wizard",
                        role = "wizard",
                        personality = "wise and mysterious",
                        background = "A test character for unit tests",
                        knowledge = listOf("magic", "testing"),
                        goals = listOf("help with tests"),
                        connections = listOf("test framework")
                    )
                )
                return@repeat
            } catch (e: Exception) {
                if (attempt == 2) throw e
                println("[DEBUG_LOG] Retry attempt ${attempt + 1} due to: ${e.message}")
                Thread.sleep(100)
            }
        }
    }

    @AfterTest
    fun teardown() = runBlocking {
        repository.cleanup()
        cleanupDatabase()
    }

    @Test
    fun `test repository initialization and character retrieval`() = runBlocking {
        // Given
        repository.deleteAllCharacters() // Ensure clean state

        // When
        repository.initialize()
        val characters = repository.getAllCharacters()

        // Then
        assertTrue(characters.isNotEmpty(), "Repository should contain pregenerated characters")
        assertTrue(characters.any { it.name == "Test Wizard" }, "Should contain test wizard character")

        val testWizard = characters.find { it.name == "Test Wizard" }
        assertNotNull(testWizard, "Test wizard should exist")
        assertEquals("wizard", testWizard.role)
        assertEquals("wise and mysterious", testWizard.personality)
        assertTrue(testWizard.knowledge.contains("magic"), "Should have magic knowledge")
        assertTrue(testWizard.goals.contains("help with tests"), "Should have test-related goals")
    }

    @Test
    fun `test adding and retrieving custom character`() = runBlocking {
        // Given
        repository.deleteAllCharacters() // Ensure clean state
        val customCharacter = Character(
            name = "Test Character",
            role = "test role",
            personality = "test personality",
            background = "test background",
            knowledge = listOf("test knowledge"),
            goals = listOf("test goal"),
            connections = listOf("test connection")
        )

        // When
        repository.addCharacter(customCharacter)
        val retrievedCharacter = repository.getCharacterByName("Test Character")

        // Then
        assertNotNull(retrievedCharacter, "Should retrieve the added character")
        assertEquals(customCharacter.name, retrievedCharacter.name)
        assertEquals(customCharacter.role, retrievedCharacter.role)
        assertEquals(customCharacter.personality, retrievedCharacter.personality)
        assertEquals(customCharacter.background, retrievedCharacter.background)
        assertEquals(customCharacter.knowledge, retrievedCharacter.knowledge)
        assertEquals(customCharacter.goals, retrievedCharacter.goals)
        assertEquals(customCharacter.connections, retrievedCharacter.connections)
    }

    @Test
    fun `test character deletion`() = runBlocking {
        // Given
        val characterName = "Character To Delete"
        val character = Character(
            name = characterName,
            role = "test role",
            personality = "test personality",
            background = "test background"
        )
        repository.addCharacter(character)

        // When
        repository.deleteCharacter(characterName)
        val retrievedCharacter = repository.getCharacterByName(characterName)

        // Then
        assertTrue(retrievedCharacter == null, "Character should be deleted")
    }

    @Test
    fun `test error handling and repository consistency`() = runBlocking {
        // Given
        val validCharacter = Character(
            name = "Valid Character",
            role = "test role",
            personality = "test personality",
            background = "test background",
            knowledge = listOf("error handling"),
            goals = listOf("maintain consistency"),
            connections = listOf("test framework")
        )

        // When - First add a valid character
        repository.addCharacter(validCharacter)

        // Then - Try to add an invalid character (same name)
        val duplicateCharacter = validCharacter.copy()
        try {
            repository.addCharacter(duplicateCharacter)
            fail("Should not allow duplicate character names")
        } catch (_: IllegalArgumentException) {
            // Expected exception
        }

        // Verify repository is still in a consistent state
        val allCharacters = repository.getAllCharacters()
        assertEquals(2, allCharacters.size, "Should contain only initial test character and one valid character")
        assertTrue(allCharacters.any { it.name == "Valid Character" }, "Valid character should still exist")
        assertTrue(allCharacters.any { it.name == "Test Wizard" }, "Test wizard should still exist")
    }

    @Test
    fun `test concurrent character operations`() = runBlocking {
        coroutineScope {
            // Given
            val charactersToAdd = (1..5).map { index ->
                Character(
                    name = "Concurrent Character $index",
                    role = "test role $index",
                    personality = "test personality $index",
                    background = "test background $index",
                    knowledge = listOf("concurrent testing"),
                    goals = listOf("test concurrent operations"),
                    connections = listOf("other test characters")
                )
            }

            // When - Add characters concurrently
            val additionJobs = charactersToAdd.map { character ->
                async {
                    repository.addCharacter(character)
                }
            }
            additionJobs.awaitAll()

            // Then
            val allCharacters = repository.getAllCharacters()
            charactersToAdd.forEach { character ->
                assertTrue(
                    allCharacters.any { it.name == character.name },
                    "Should contain character ${character.name}"
                )
            }

            // Verify concurrent retrieval
            val retrievalJobs = charactersToAdd.map { character ->
                async {
                    repository.getCharacterByName(character.name)
                }
            }
            val retrievalResults = retrievalJobs.awaitAll()

            assertTrue(
                retrievalResults.all { it != null },
                "All characters should be retrieved successfully"
            )
        }
    }
}
