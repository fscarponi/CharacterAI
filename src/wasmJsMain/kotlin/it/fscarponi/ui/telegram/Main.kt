package it.fscarponi.ui.telegram

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.w3c.dom.*
import kotlinx.browser.document
import org.w3c.dom.HTMLImageElement


private val controller = WebAppController()

@Composable
fun App() {
    val isInitialized by controller.isInitialized.collectAsState()
    val error by controller.error.collectAsState()

    LaunchedEffect(Unit) {
        controller.initialize()
    }

    MaterialTheme(
        colorScheme = if (controller.isDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !isInitialized -> {
                        CircularProgressIndicator()
                    }
                    error != null -> {
                        ErrorMessage(error!!) {
                            controller.clearError()
                        }
                    }
                    else -> {
                        MainContent()
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(error: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onDismiss) {
            Text("Dismiss")
        }
    }
}

@Composable
fun MainContent() {
    val user = controller.getCurrentUser()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (user != null) {
            if (!user.photo_url.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DisposableEffect(Unit) {
                        val containerId = "user-photo-${user.id}"
                        val container = document.createElement("div")
                        container.id = containerId

                        val img = document.createElement("img") as HTMLImageElement
                        img.src = user.photo_url!!
                        img.style.apply {
                            width = "64px"
                            height = "64px"
                            borderRadius = "50%"
                            objectFit = "cover"
                        }

                        container.appendChild(img)
                        document.getElementById("root")?.appendChild(container)

                        onDispose {
                            document.getElementById(containerId)?.remove()
                        }
                    }
                }
            }

            Text(
                text = "Welcome, ${user.first_name}!",
                style = MaterialTheme.typography.headlineMedium
            )
        } else {
            Text(
                text = "Welcome to Character AI!",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Button(
            onClick = { controller.closeApp() }
        ) {
            Text("Close WebApp")
        }
    }
}
