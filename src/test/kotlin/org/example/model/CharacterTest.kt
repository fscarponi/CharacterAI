package org.example.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharacterTest {
    @Test
    fun `test character creation and toString format`() {
        // Given
        val character = Character(
            name = "Test Hero",
            role = "warrior",
            personality = "brave and strong",
            background = "A legendary warrior from the north"
        )

        // When
        val stringRepresentation = character.toString()

        // Then
        assertTrue(stringRepresentation.contains("Name: Test Hero"))
        assertTrue(stringRepresentation.contains("Role: warrior"))
        assertTrue(stringRepresentation.contains("Personality: brave and strong"))
        assertTrue(stringRepresentation.contains("Background: A legendary warrior from the north"))
    }

    @Test
    fun `test character template creation`() {
        // Given
        val template = Character(
            name = "Gandalf",
            role = "wizard",
            personality = "wise and mysterious",
            background = "An ancient wizard who guides others"
        )

        // Then
        assertEquals("Gandalf", template.name)
        assertEquals("wizard", template.role)
        assertEquals("wise and mysterious", template.personality)
        assertEquals("An ancient wizard who guides others", template.background)
    }
}