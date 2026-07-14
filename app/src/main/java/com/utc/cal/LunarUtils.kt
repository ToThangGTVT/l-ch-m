package com.utc.cal

import java.util.Calendar

object LunarUtils {
    // Array of Can (Stems)
    val CAN = arrayOf("Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ")
    
    // Array of Chi (Branches)
    val CHI = arrayOf("Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi")

    // Simplified Ho Ngoc Duc algorithm port or basic lookup is too large. 
    // We will use a known simplified algorithm to approximate Vietnamese lunar date.
    // For a real production app we'd use the full astronomical algorithm or a library.
    // Since we need to provide a working app quickly without a gigantic math library,
    // let's use a simpler known algorithm for Vietnamese Lunar Calendar.
    
    fun getLunarDate(dd: Int, mm: Int, yyyy: Int): LunarDate {
        return convertSolar2Lunar(dd, mm, yyyy, 7.0)
    }
    
    fun getCanChiYear(lunarYear: Int): String {
        return "${CAN[(lunarYear % 10 + 10) % 10]} ${CHI[(lunarYear % 12 + 12) % 12]}"
    }

    // --- Astronomical Algorithms (Ho Ngoc Duc) ---
    private const val PI = Math.PI
    private fun jdFromDate(dd: Int, mm: Int, yyyy: Int): Int {
        var a = (14 - mm) / 12
        var y = yyyy + 4800 - a
        var m = mm + 12 * a - 3
        return dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private fun jdToDate(jd: Int): IntArray {
        var a = jd + 32044
        var b = (4 * a + 3) / 146097
        var c = a - (146097 * b) / 4
        var d = (4 * c + 3) / 1461
        var e = c - (1461 * d) / 4
        var m = (5 * e + 2) / 153
        var day = e - (153 * m + 2) / 5 + 1
        var month = m + 3 - 12 * (m / 10)
        var year = b * 100 + d - 4800 + m / 10
        return intArrayOf(day, month, year)
    }

    private fun getNewMoonDay(k: Double, timeZone: Double): Int {
        val T = k / 1236.85
        val T2 = T * T
        val T3 = T2 * T
        val dr = PI / 180.0
        var Jd = 2415020.75933 + 29.53058868 * k + 0.0001178 * T2 - 0.000000155 * T3
        Jd += 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr)

        val M = 359.2242 + 29.10535608 * k - 0.0000333 * T2 - 0.00000347 * T3
        val Mprime = 306.0253 + 385.81691806 * k + 0.0107306 * T2 + 0.00001236 * T3
        val F = 21.2964 + 390.67050646 * k - 0.0016528 * T2 - 0.00000239 * T3

        var C = (0.1734 - 0.000393 * T) * Math.sin(M * dr)
        C += 0.0021 * Math.sin(2 * M * dr)
        C -= 0.0004 * Math.sin(Mprime * dr)
        C += 0.0005 * Math.sin(2 * F * dr)

        Jd += C
        val pt = Jd + 0.5 - timeZone / 24.0
        return Math.floor(pt + 0.5).toInt()
    }

    private fun getSunLongitude(dayNumber: Double, timeZone: Double): Double {
        val T = (dayNumber - 2451545.0 + 0.5 - timeZone / 24.0) / 36525.0
        val T2 = T * T
        val dr = PI / 180.0
        val M = 357.52910 + 35999.05030 * T - 0.0001559 * T2 - 0.00000048 * T * T2
        val L0 = 280.46645 + 36000.76983 * T + 0.0003032 * T2
        var dl = ((1.914600 - 0.004817 * T - 0.000014 * T2) * Math.sin(M * dr)
                + (0.019993 - 0.000101 * T) * Math.sin(2 * M * dr)
                + 0.000290 * Math.sin(3 * M * dr))
        val L = L0 + dl
        return (L % 360 + 360) % 360
    }

    private fun getLunarMonth11(yy: Int, timeZone: Double): Int {
        val off = jdFromDate(31, 12, yy) - 2415021.076998695
        var k = Math.floor(off / 29.530588853).toInt()
        var nm = getNewMoonDay(k.toDouble(), timeZone)
        val sunLong = getSunLongitude(nm.toDouble(), timeZone)
        if (sunLong >= 9.0) {
            nm = getNewMoonDay((k - 1).toDouble(), timeZone)
        }
        return nm
    }

    private fun getLeapMonthOffset(a11: Int, timeZone: Double): Int {
        var k: Double
        var last = 0
        var arc: Double
        var i = 1
        var nm = 0
        var sunLong = 0.0
        while (i < 14) {
            val off = a11 - 2415021.076998695
            k = Math.floor(off / 29.530588853) + i
            nm = getNewMoonDay(k, timeZone)
            sunLong = getSunLongitude(nm.toDouble(), timeZone)
            val arcInt = Math.floor(sunLong / 30).toInt()
            if (arcInt == last) return i - 1
            last = arcInt
            i++
        }
        return 0
    }

    private fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Double): LunarDate {
        val dayNumber = jdFromDate(dd, mm, yy)
        var k = Math.floor((dayNumber - 2415021.076998695) / 29.530588853)
        var monthStart = getNewMoonDay(k, timeZone)
        if (dayNumber < monthStart) {
            monthStart = getNewMoonDay(k - 1, timeZone)
        }
        var currentLunarDay = dayNumber - monthStart + 1
        
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        if (a11 >= monthStart) {
            val a11Last = getLunarMonth11(yy - 1, timeZone)
            a11 = a11Last
            b11 = getLunarMonth11(yy, timeZone)
        } else {
            val a11Next = getLunarMonth11(yy + 1, timeZone)
            b11 = a11Next
        }
        
        var dayOffset = Math.floor((monthStart - a11) / 29.530588853).toInt()
        var isLeap = false
        var lunarMonth = dayOffset + 11
        
        val daysBetween11 = b11 - a11
        if (daysBetween11 > 365) {
            val leapMonthOffset = getLeapMonthOffset(a11, timeZone)
            if (dayOffset >= leapMonthOffset) {
                lunarMonth = dayOffset + 10
                if (dayOffset == leapMonthOffset) {
                    isLeap = true
                }
            }
        }
        
        var lunarYear = yy
        if (lunarMonth > 12) {
            lunarMonth -= 12
        }
        if (a11 >= monthStart && lunarMonth < 11) {
            lunarYear++
        }
        if (a11 < monthStart && lunarMonth > 10) {
            lunarYear--
        }

        return LunarDate(currentLunarDay, lunarMonth, lunarYear, isLeap)
    }

    fun getDayCanChi(dd: Int, mm: Int, yyyy: Int): String {
       val jd = jdFromDate(dd, mm, yyyy)
       val can = arrayOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
       val chi = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
       
       val cc = ((jd + 9) % 10 + 10) % 10
       val ch = ((jd + 1) % 12 + 12) % 12
       return "${can[cc]} ${chi[ch]}"
    }

    fun getMonthCanChi(month: Int, year: Int): String {
       val can = arrayOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
       val chi = arrayOf("Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu")
       val firstCanIndex = ((year % 5) * 2) % 10
       val monthCan = can[((firstCanIndex + month - 1) % 10 + 10) % 10]
       val monthChi = chi[((month - 1) % 12 + 12) % 12]
       return "$monthCan $monthChi"
    }

}

data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeap: Boolean = false
)
