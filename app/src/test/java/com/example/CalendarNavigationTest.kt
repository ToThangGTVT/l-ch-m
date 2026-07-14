package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.LunarCalendarScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CalendarNavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNavigation() {
        composeTestRule.setContent {
            MyApplicationTheme {
                LunarCalendarScreen()
            }
        }
        
        // Find next month button and click many times to see if any month breaks
        for (i in 1..24) { // Test across 2 years
            composeTestRule.onNodeWithContentDescription("Tháng sau").performClick()
            composeTestRule.waitForIdle()
        }
    }
}
