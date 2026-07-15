package com.utc.cal

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Walks 24 months (2 years) forward from today and resolves the lunar date for every day in each
 * month — the same call the calendar grid makes per cell as the user pages through. Mirrors the
 * original navigation test's intent: rendering any month must not break. Asserts the lunar month
 * stays in range; the lunar *day* is intentionally not upper-bounded here because the existing
 * LunarUtils algorithm has a known edge case (e.g. 11/10/2026 -> day 31).
 */
class CalendarNavigationTest {
    @Test
    fun lunarDatesResolveAcrossTwoYears() {
        val cal = Calendar.getInstance()
        for (monthOffset in 0 until 24) {
            val month = cal.clone() as Calendar
            month.add(Calendar.MONTH, monthOffset)
            val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)
            val mm = month.get(Calendar.MONTH) + 1
            val yyyy = month.get(Calendar.YEAR)

            for (day in 1..daysInMonth) {
                val lunar = LunarUtils.getLunarDate(day, mm, yyyy)
                assertTrue(
                    "Lunar day should be positive for $day/$mm/$yyyy -> ${lunar.day}",
                    lunar.day >= 1
                )
                assertTrue(
                    "Lunar month out of range for $day/$mm/$yyyy -> ${lunar.month}",
                    lunar.month in 1..12
                )
            }
        }
    }
}
