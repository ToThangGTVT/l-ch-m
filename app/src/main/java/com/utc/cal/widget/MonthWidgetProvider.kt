package com.utc.cal.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Html
import android.widget.RemoteViews
import com.utc.cal.MainActivity
import com.utc.cal.R
import com.utc.cal.LunarUtils
import java.util.Calendar
import android.util.Log
import androidx.core.graphics.toColorInt

class MonthWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateMonthWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateMonthWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    try {
        val views = RemoteViews(context.packageName, R.layout.widget_month)
        
        val settings = com.utc.cal.WidgetSettingsManager(context)
        val showLunar = settings.showLunarDate
        val startOnMonday = settings.startOnMonday
        var primaryColor = settings.widgetThemeColor
        
        if (settings.useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            primaryColor = context.resources.getColor(android.R.color.system_accent1_500, context.theme)
        }
        
        var todayBgResId = R.drawable.widget_today_bg
        if (settings.widgetThemeColor == 0xFFFFFFFF.toInt() && !(settings.useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)) {
            primaryColor = "#0A56D9".toColorInt()
            todayBgResId = R.drawable.widget_today_bg_blue
        }
        
        val bgResId = if (settings.useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            R.drawable.widget_bg_dynamic
        } else {
            when (settings.widgetThemeColor) {
                0xFF8C1D18.toInt() -> R.drawable.widget_bg_0
                0xFF21005D.toInt() -> R.drawable.widget_bg_1
                0xFF006874.toInt() -> R.drawable.widget_bg_2
                0xFF1A6C30.toInt() -> R.drawable.widget_bg_3
                0xFF6C5E0F.toInt() -> R.drawable.widget_bg_4
                0xFFFFFFFF.toInt() -> R.drawable.widget_bg_5
                else -> R.drawable.widget_bg_0
            }
        }
        views.setInt(R.id.widget_root, "setBackgroundResource", bgResId)
        
        val textColor = if (settings.useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.resources.getColor(android.R.color.system_neutral1_900, context.theme)
        } else if (settings.widgetThemeColor == 0xFFFFFFFF.toInt()) {
            Color.BLACK
        } else {
            "#1D1B1E".toColorInt()
        }
        val headerColor = primaryColor
        val weekdayHeaderColor = if (settings.widgetThemeColor == 0xFFFFFFFF.toInt() && !(settings.useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)) {
            Color.BLACK
        } else {
            (primaryColor and 0x00FFFFFF) or -0x40000000
        }
        
        val headerIds = intArrayOf(
            R.id.header_0, R.id.header_1, R.id.header_2, R.id.header_3,
            R.id.header_4, R.id.header_5, R.id.header_6
        )
        val cellIds = arrayOf(
            intArrayOf(R.id.cell_0_0, R.id.cell_0_1, R.id.cell_0_2, R.id.cell_0_3, R.id.cell_0_4, R.id.cell_0_5, R.id.cell_0_6),
            intArrayOf(R.id.cell_1_0, R.id.cell_1_1, R.id.cell_1_2, R.id.cell_1_3, R.id.cell_1_4, R.id.cell_1_5, R.id.cell_1_6),
            intArrayOf(R.id.cell_2_0, R.id.cell_2_1, R.id.cell_2_2, R.id.cell_2_3, R.id.cell_2_4, R.id.cell_2_5, R.id.cell_2_6),
            intArrayOf(R.id.cell_3_0, R.id.cell_3_1, R.id.cell_3_2, R.id.cell_3_3, R.id.cell_3_4, R.id.cell_3_5, R.id.cell_3_6),
            intArrayOf(R.id.cell_4_0, R.id.cell_4_1, R.id.cell_4_2, R.id.cell_4_3, R.id.cell_4_4, R.id.cell_4_5, R.id.cell_4_6),
            intArrayOf(R.id.cell_5_0, R.id.cell_5_1, R.id.cell_5_2, R.id.cell_5_3, R.id.cell_5_4, R.id.cell_5_5, R.id.cell_5_6)
        )
        
        val dayNames = if (startOnMonday) {
            arrayOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        } else {
            arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
        }
        
        for (i in 0..6) {
            views.setTextViewText(headerIds[i], dayNames[i])
            val cColor = if (startOnMonday) {
                if (i == 5 || i == 6) headerColor else weekdayHeaderColor
            } else {
                if (i == 0 || i == 6) headerColor else weekdayHeaderColor
            }
            views.setTextColor(headerIds[i], cColor)
            views.setTextViewTextSize(headerIds[i], android.util.TypedValue.COMPLEX_UNIT_SP, 14F)
        }
        
        views.setTextColor(R.id.widget_month_year, if (settings.widgetThemeColor == 0xFFFFFFFF.toInt() && !(settings.useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)) Color.BLACK else headerColor)
        
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        
        views.setTextViewText(R.id.widget_month_year, "Tháng ${month + 1}, $year")
        
        val clone = calendar.clone() as Calendar
        clone.set(Calendar.DAY_OF_MONTH, 1)
        var firstDayOfWeek = clone.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        
        if (startOnMonday) {
            firstDayOfWeek = (firstDayOfWeek - 1 + 7) % 7 // 0 = Monday
        }
        
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        var dayCounter = 1
        
        for (r in 0..5) {
            for (c in 0..6) {
                val cellId = cellIds[r][c]
                val cellName = context.resources.getResourceEntryName(cellId)
                val bgCellId = context.resources.getIdentifier(cellName + "_bg", "id", context.packageName)
                if (r == 0 && c < firstDayOfWeek) {
                    views.setTextViewText(cellId, "")
                    views.setViewVisibility(bgCellId, android.view.View.INVISIBLE)
                } else if (dayCounter <= daysInMonth) {
                    if (showLunar) {
                        val lunarDate = LunarUtils.getLunarDate(dayCounter, month + 1, year)
                        val lunarStr = if (lunarDate.day == 1) "${lunarDate.day}/${lunarDate.month}" else "${lunarDate.day}"
                        val htmlText = Html.fromHtml("$dayCounter<br><small>$lunarStr</small>", Html.FROM_HTML_MODE_LEGACY)
                        views.setTextViewText(cellId, htmlText)
                        views.setTextViewTextSize(cellId, android.util.TypedValue.COMPLEX_UNIT_SP, 8f)
                    } else {
                        val htmlText = Html.fromHtml("$dayCounter", Html.FROM_HTML_MODE_LEGACY)
                        views.setTextViewText(cellId, htmlText)
                        views.setTextViewTextSize(cellId, android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                    }
                    
                    if (dayCounter == currentDay) {
                        views.setViewVisibility(bgCellId, android.view.View.VISIBLE)
                        views.setImageViewResource(bgCellId, todayBgResId)
                        views.setTextColor(cellId, primaryColor)
                    } else {
                        val isWeekend = if (startOnMonday) (c == 5 || c == 6) else (c == 0 || c == 6)
                        views.setViewVisibility(bgCellId, android.view.View.INVISIBLE)
                        views.setTextColor(cellId, if (isWeekend) primaryColor else textColor)
                    }
                    dayCounter++
                } else {
                    views.setTextViewText(cellId, "")
                    views.setViewVisibility(bgCellId, android.view.View.INVISIBLE)
                }
            }
        }
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    } catch (e: Exception) {
        Log.e("MonthWidgetProvider", "Error updating widget", e)
    }
}
