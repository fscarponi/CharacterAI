package it.fscarponi.telegram

import kotlin.test.Test
import kotlin.test.assertEquals
import it.fscarponi.ui.UIType
import kotlin.test.BeforeTest

class TelegramSessionManagerTest {
    private lateinit var sessionManager: TelegramSessionManager

    @BeforeTest
    fun setup() {
        sessionManager = TelegramSessionManager()
    }

    @Test
    fun `test UI selection and state transitions`() {
        val chatId = "test_chat_1"
        
        // Initial state should be NONE
        assertEquals(CreationState.NONE, sessionManager.getCreationState(chatId))
        
        // Set state to AWAITING_UI_SELECTION
        sessionManager.setCreationState(chatId, CreationState.AWAITING_UI_SELECTION)
        assertEquals(CreationState.AWAITING_UI_SELECTION, sessionManager.getCreationState(chatId))
        
        // Select UI type
        sessionManager.setSelectedUIType(chatId, UIType.NEW)
        assertEquals(UIType.NEW, sessionManager.getSelectedUIType(chatId))
        
        // Transition to language selection
        sessionManager.setCreationState(chatId, CreationState.AWAITING_LANGUAGE_SELECTION)
        assertEquals(CreationState.AWAITING_LANGUAGE_SELECTION, sessionManager.getCreationState(chatId))
    }

    @Test
    fun `test default UI type`() {
        val chatId = "test_chat_2"
        
        // New session should default to CURRENT UI
        assertEquals(UIType.CURRENT, sessionManager.getSelectedUIType(chatId))
    }

    @Test
    fun `test UI type persistence`() {
        val chatId = "test_chat_3"
        
        // Set UI type
        sessionManager.setSelectedUIType(chatId, UIType.NEW)
        
        // Change states
        sessionManager.setCreationState(chatId, CreationState.AWAITING_LANGUAGE_SELECTION)
        sessionManager.setCreationState(chatId, CreationState.NONE)
        
        // UI type should persist
        assertEquals(UIType.NEW, sessionManager.getSelectedUIType(chatId))
    }
}