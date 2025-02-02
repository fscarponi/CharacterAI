package it.fscarponi.telegram

import it.fscarponi.model.Character

class TelegramSessionManager {
    private val userSessions = mutableMapOf<String, UserSession>()

    fun getSession(chatId: String): UserSession {
        return userSessions.getOrPut(chatId) { UserSession() }
    }

    fun setSelectedCharacter(chatId: String, character: Character) {
        getSession(chatId).selectedCharacter = character
    }

    fun getSelectedCharacter(chatId: String): Character? {
        return getSession(chatId).selectedCharacter
    }

    fun setCreationState(chatId: String, state: CreationState) {
        getSession(chatId).creationState = state
    }

    fun getCreationState(chatId: String): CreationState {
        return getSession(chatId).creationState
    }

    fun setSelectedLanguage(chatId: String, language: String) {
        getSession(chatId).selectedLanguage = language
    }

    fun getSelectedLanguage(chatId: String): String {
        return getSession(chatId).selectedLanguage
    }
}

data class UserSession(
    var selectedCharacter: Character? = null,
    var creationState: CreationState = CreationState.NONE,
    var tempCharacterData: MutableMap<String, String> = mutableMapOf(),
    var selectedLanguage: String = "english"
)

enum class CreationState {
    NONE,
    AWAITING_LANGUAGE_SELECTION,
    AWAITING_NAME,
    AWAITING_ROLE,
    AWAITING_BACKGROUND,
    AWAITING_PERSONALITY,
    AWAITING_CONFIRMATION
}
