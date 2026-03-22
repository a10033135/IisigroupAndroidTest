package idv.fan.iisigroup.android.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MainActivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun appStartsWithFlightTabSelected() {
        // The app starts on the flight screen — verify the flight title is displayed
        composeTestRule.onNodeWithText("航班資訊").assertIsDisplayed()
    }

    @Test
    fun clickingExchangeRateTab_navigatesToExchangeRateScreen() {
        composeTestRule.onNodeWithText("匯率").performClick()
        composeTestRule.onNodeWithText("匯率換算").assertIsDisplayed()
    }

    @Test
    fun clickingSettingTab_navigatesToSettingScreen() {
        composeTestRule.onNodeWithText("設定").performClick()
        composeTestRule.onNodeWithText("設定頁面").assertIsDisplayed()
    }

    @Test
    fun allNavigationTabs_areDisplayed() {
        composeTestRule.onNodeWithText("航班").assertIsDisplayed()
        composeTestRule.onNodeWithText("匯率").assertIsDisplayed()
        composeTestRule.onNodeWithText("設定").assertIsDisplayed()
    }
}
