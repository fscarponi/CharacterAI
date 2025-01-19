package it.fscarponi.config

object AIConfig {
    // Hugging Face API endpoint for Mistral model
    const val HUGGINGFACE_API_URL = "https://api-inference.huggingface.co/models/"

    // Mistral-Nemo-Instruct-2407: Advanced instruction-following model optimized for:
    // - High-quality conversational responses
    // - Strong context understanding
    // - Consistent character role-playing
    // - Natural language generation
    const val DEFAULT_MODEL = "mistralai/Mistral-Nemo-Instruct-2407"

    // Read API token from local.properties
    val API_TOKEN: String by lazy {
        val properties = java.util.Properties()
        val propertiesFile = java.io.File("local.properties")
        if (!propertiesFile.exists()) {
            throw IllegalStateException("local.properties file not found")
        }
        properties.load(propertiesFile.inputStream())
        properties.getProperty("huggingface.api.token") ?: throw IllegalStateException(
            "huggingface.api.token not found in local.properties"
        )
    }
}
