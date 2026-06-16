package com.dailystrength.presentation.workout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.dailystrength.presentation.theme.DailyStrengthTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI test for the workout completion screen. Runs on a device/emulator via
 * `connectedDebugAndroidTest` (not part of the headless CI build).
 */
class WorkoutCompleteScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsStreakAndInvokesDone() {
        var doneClicked = false
        composeRule.setContent {
            DailyStrengthTheme {
                WorkoutCompleteScreen(streak = 7, onDone = { doneClicked = true })
            }
        }

        composeRule.onNodeWithText("7").assertIsDisplayed()
        composeRule.onNodeWithText("días de racha").assertIsDisplayed()
        composeRule.onNodeWithText("Hecho").performClick()

        assertTrue(doneClicked)
    }
}
