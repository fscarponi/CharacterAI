package it.fscarponi.ui.compose

import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import androidx.compose.runtime.*
import it.fscarponi.ui.compose.components.ChatMessage
import it.fscarponi.ui.compose.state.ChatState
import it.fscarponi.ui.compose.service.MockBotService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.w3c.dom.HTMLDivElement

fun main() {
    val scope = MainScope()
    val botService = MockBotService()

    renderComposable(rootElementId = "root") {
        val chatState = remember { ChatState(botService, scope) }
        Div({ 
            style {
                padding(16.px)
                maxWidth(800.px)
                property("margin", "0 auto")
                backgroundColor(Color("#FFFFFF"))
                property("box-shadow", "0 0 5px rgba(128, 128, 128, 0.5)")
                borderRadius(8.px)
            }
        }) {
            H1({ 
                style {
                    property("text-align", "center")
                    color(Color("#333333"))
                }
            }) {
                Text("Character AI Chat")
            }

            Div({ 
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    property("gap", "16px")
                }
            }) {
                // Chat messages area
                Div({ 
                    id("chat-messages")
                    style {
                        height(400.px)
                        border(1.px, LineStyle.Solid, Color("#DDDDDD"))
                        padding(16.px)
                        property("overflow-y", "auto")
                        borderRadius(4.px)
                        backgroundColor(Color("#FAFAFA"))
                    }
                }) {
                    LaunchedEffect(chatState.messages.size) {
                        kotlinx.browser.document.getElementById("chat-messages")?.let { element ->
                            element.scrollTop = element.scrollHeight.toDouble()
                        }
                    }
                    if (chatState.messages.isEmpty()) {
                        P { 
                            Text("Welcome to Character AI Chat! Start a conversation...")
                        }
                    } else {
                        for (message in chatState.messages) {
                            ChatMessage(message)
                        }
                    }
                }

                // Input area
                Div({ 
                    style {
                        display(DisplayStyle.Flex)
                        property("gap", "8px")
                    }
                }) {
                    Input(InputType.Text) {
                        style {
                            property("flex", "1")
                            padding(12.px)
                            borderRadius(4.px)
                            border(1.px, LineStyle.Solid, Color("#DDDDDD"))
                            property("outline", "none")
                            if (chatState.isLoading) {
                                property("opacity", "0.7")
                            }
                        }
                        value(chatState.currentInput)
                        onInput { event ->
                            chatState.updateInput(event.target.value)
                        }
                        placeholder("Type your message...")
                        attr("onkeydown", "if(event.key==='Enter') { this.dispatchEvent(new Event('submit')); return false; }")
                        addEventListener("submit") {
                            if (!chatState.isLoading) {
                                chatState.sendMessage()
                            }
                        }
                        if (chatState.isLoading) {
                            attr("disabled", "true")
                        }
                    }
                    Button({
                        style {
                            padding(12.px, 24.px)
                            backgroundColor(if (chatState.isLoading) Color("#CCCCCC") else Color("#1976D2"))
                            color(Color("#FFFFFF"))
                            border(0.px)
                            borderRadius(4.px)
                            property("cursor", if (chatState.isLoading) "default" else "pointer")
                        }
                        onClick { 
                            if (!chatState.isLoading) {
                                chatState.sendMessage()
                            }
                        }
                        if (chatState.isLoading) {
                            attr("disabled", "true")
                        }
                    }) {
                        Text(if (chatState.isLoading) "Sending..." else "Send")
                    }
                }
            }
        }
    }
}
