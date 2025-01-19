package it.fscarponi.model

data class Character(
    val name: String,
    val role: String,
    val personality: String,
    val background: String,
    val knowledge: List<String> = emptyList(),  // Areas of expertise and specific knowledge
    val secrets: List<String> = emptyList(),    // Hidden aspects of the character's past or nature
    val goals: List<String> = emptyList(),      // Current motivations and aspirations
    val connections: List<String> = emptyList()  // Important relationships and affiliations
) {
    override fun toString(): String {
        val base = """
            Name: $name
            Role: $role
            Personality: $personality
            Background: $background
            """.trimIndent()

        val details = buildString {
            if (knowledge.isNotEmpty()) {
                append("\nKnowledge & Expertise:")
                knowledge.forEach { append("\n- $it") }
            }
            if (goals.isNotEmpty()) {
                append("\nPersonal Goals:")
                goals.forEach { append("\n- $it") }
            }
            if (connections.isNotEmpty()) {
                append("\nConnections:")
                connections.forEach { append("\n- $it") }
            }
        }

        return base + details
    }
}
