package it.fscarponi.ui.telegram

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.Window
import kotlin.js.JsName

external val window: Window

private val telegramWebAppCheck: Boolean = js("typeof window.Telegram!=='undefined'&&typeof window.Telegram.WebApp!=='undefined'")

private fun checkTelegramWebApp(): Boolean = try {
    telegramWebAppCheck
} catch (e: Throwable) {
    false
}

class WebAppController {
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val webApp: WebApp
        get() = Telegram.WebApp

    fun initialize() {
        try {
            // Verify we're in Telegram WebApp environment
            if (!checkTelegramWebApp()) {
                _error.value = "This app can only be opened in Telegram"
                return
            }

            // Set theme colors
            with(webApp) {
                setBackgroundColor(themeParams.bg_color)
                setHeaderColor(if (colorScheme == "dark") "#2B2B2B" else "#FFFFFF")
            }

            _isInitialized.value = true
            webApp.ready()
        } catch (e: Exception) {
            _error.value = "Failed to initialize: ${e.message}"
        }
    }

    fun getCurrentUser(): WebAppUser? = try {
        webApp.initDataUnsafe.user
    } catch (e: Exception) {
        _error.value = "Failed to get user data: ${e.message}"
        null
    }

    fun isDarkTheme(): Boolean = webApp.colorScheme == "dark"

    fun closeApp() {
        try {
            webApp.close()
        } catch (e: Exception) {
            _error.value = "Failed to close app: ${e.message}"
        }
    }

    fun clearError() {
        _error.value = null
    }
}
