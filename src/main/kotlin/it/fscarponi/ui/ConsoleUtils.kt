package it.fscarponi.ui

// Color constants for consistent UI
const val COLOR_HEADER = "32"    // Green
const val COLOR_PROMPT = "36"    // Cyan
const val COLOR_INFO = "37"      // White
const val COLOR_WARNING = "33"   // Yellow
const val COLOR_ERROR = "31"     // Red
const val COLOR_SUCCESS = "32"   // Green

fun printColored(text: String, color: String) {
    println("\u001B[${color}m$text\u001B[0m")
}
