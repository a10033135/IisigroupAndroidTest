package idv.fan.iisigroup.android.test.feature.exchangeRate

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CalculatorBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initialDisplay_showsZero() {
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorBottomSheet(
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun pressingNumberButtons_updatesExpression() {
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorBottomSheet(
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("3").performClick()

        composeTestRule.onNodeWithText("123").assertIsDisplayed()
    }

    @Test
    fun pressingOperator_showsResult() {
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorBottomSheet(
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("2").performClick()

        composeTestRule.onNodeWithText("1+2").assertIsDisplayed()
        composeTestRule.onNodeWithText("= 3", substring = true).assertIsDisplayed()
    }

    @Test
    fun pressingC_deletesLastCharacter() {
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorBottomSheet(
                    onDismiss = {},
                    onConfirm = {},
                )
            }
        }

        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("c").performClick()

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun confirmButton_callsOnConfirmWithExpressionValue() {
        var confirmedValue: Double? = null
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorBottomSheet(
                    onDismiss = {},
                    onConfirm = { confirmedValue = it },
                )
            }
        }

        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("輸入").performClick()

        assertEquals(5.0, confirmedValue)
    }

    @Test
    fun confirmWithCalculatedResult_passesEvaluatedValue() {
        var confirmedValue: Double? = null
        composeTestRule.setContent {
            MaterialTheme {
                CalculatorBottomSheet(
                    onDismiss = {},
                    onConfirm = { confirmedValue = it },
                )
            }
        }

        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("輸入").performClick()

        assertEquals(3.0, confirmedValue)
    }
}
