package cl.duoc.deepsource.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import cl.duoc.deepsource.ui.theme.DeepSourceTheme
import cl.duoc.deepsource.ui.viewmodel.TimeReceipt
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TimeReceiptDialogTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun displaysSavedValuesAndAcceptsOnce() {
        val acceptCount = AtomicInteger(0)
        val receipt = TimeReceipt("entry-42", "App Boreal", "Diseño de pantalla", "2026-11-02", 75)
        composeRule.setContent {
            DeepSourceTheme {
                TimeReceiptDialog(receipt) { acceptCount.incrementAndGet() }
            }
        }

        composeRule.onNodeWithText("REGISTRO EXITOSO").assertIsDisplayed()
        composeRule.onNodeWithText("App Boreal").assertIsDisplayed()
        composeRule.onNodeWithText("Diseño de pantalla").assertIsDisplayed()
        composeRule.onNodeWithText("02 nov", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("1 h 15 min").assertIsDisplayed()
        composeRule.onNodeWithText("Aceptar").performClick()
        composeRule.waitForIdle()
        assertEquals(1, acceptCount.get())
    }
}
