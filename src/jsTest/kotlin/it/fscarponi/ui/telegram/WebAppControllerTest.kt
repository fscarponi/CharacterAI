package it.fscarponi.ui.telegram

import kotlin.test.*

class WebAppControllerTest {
    private lateinit var controller: WebAppController

    @BeforeTest
    fun setup() {
        controller = WebAppController()
    }

    @Test
    fun testInitialization() {
        controller.initialize()
        // No exception should be thrown
        assertTrue(true, "Initialization completed successfully")
    }

    @Test
    fun testIsDarkTheme() {
        assertTrue(controller.isDarkTheme(), "Default theme should be dark")
    }
}
