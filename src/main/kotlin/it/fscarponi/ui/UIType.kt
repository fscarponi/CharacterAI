package it.fscarponi.ui

enum class UIType(val displayName: String, val isExperimental: Boolean) {
    CURRENT("Current UI", false),
    NEW("New UI (Experimental)", true);

    override fun toString(): String {
        return if (isExperimental) {
            "$displayName 🧪"
        } else {
            displayName
        }
    }
}