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
    fun navigationItems_areDisplayed() {
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun clickFavorites_navigatesToFavorites() {
        composeTestRule.onNodeWithText("Favorites").performClick()
        composeTestRule.onNodeWithText("Favorites").assertIsDisplayed()
    }
}
