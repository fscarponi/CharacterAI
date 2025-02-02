@file:JsModule("@twa-dev/sdk")

package it.fscarponi.ui.telegram

import kotlin.js.Promise

external interface WebAppUser {
    val id: Double
    val is_bot: Boolean?
    val first_name: String
    val last_name: String?
    val username: String?
    val language_code: String?
    val photo_url: String?
}

external interface ThemeParams {
    val bg_color: String
    val text_color: String
    val hint_color: String
    val link_color: String
    val button_color: String
    val button_text_color: String
}

external interface WebApp {
    val initData: String
    val initDataUnsafe: WebAppInitData
    val version: String
    val platform: String
    val colorScheme: String
    val themeParams: ThemeParams
    val isExpanded: Boolean
    val viewportHeight: Double
    val viewportStableHeight: Double

    fun ready()
    fun expand()
    fun close()
    fun sendData(data: String)
    fun enableClosingConfirmation()
    fun disableClosingConfirmation()
    fun setHeaderColor(color: String)
    fun setBackgroundColor(color: String)
}

external interface WebAppInitData {
    val query_id: String?
    val user: WebAppUser?
    val receiver: WebAppUser?
    val start_param: String?
}

external interface TelegramWebAppNamespace {
    val WebApp: WebApp
}

external val Telegram: TelegramWebAppNamespace
