package it.fscarponi.ui.compose.components

import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import androidx.compose.runtime.Composable
import kotlin.js.Date

data class Message(
    val content: String,
    val isBot: Boolean,
    val timestamp: Double = Date.now()
)

@Composable
fun ChatMessage(message: Message) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(if (message.isBot) FlexDirection.Row else FlexDirection.RowReverse)
            marginBottom(8.px)
        }
    }) {
        Div({
            style {
                maxWidth(70.percent)
                padding(12.px)
                borderRadius(8.px)
                backgroundColor(if (message.isBot) Color("#E3F2FD") else Color("#E8F5E9"))
                property("word-wrap", "break-word")
                property("margin-${if (message.isBot) "right" else "left"}", "auto")
            }
        }) {
            P({
                style {
                    margin(0.px)
                }
            }) {
                Text(message.content)
            }
            Span({
                style {
                    fontSize(12.px)
                    color(Color("#757575"))
                    display(DisplayStyle.Block)
                    marginTop(4.px)
                }
            }) {
                Text(formatTimestamp(message.timestamp))
            }
        }
    }
}

private fun formatTimestamp(timestamp: Double): String {
    val date = Date(timestamp)
    return date.toLocaleTimeString(
        "default",
        dateLocaleOptions {
            hour = "2-digit"
            minute = "2-digit"
        }
    )
}

private fun dateLocaleOptions(init: Date.LocaleOptions.() -> Unit): Date.LocaleOptions {
    return (js("{}") as Date.LocaleOptions).apply(init)
}
