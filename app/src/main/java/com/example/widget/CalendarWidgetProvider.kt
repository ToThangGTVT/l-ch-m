package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.LunarUtils
import com.example.R
import java.util.Calendar

class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_calendar)
    
    val settings = com.example.WidgetSettingsManager(context)
    val showLunar = settings.showLunarDate
    var primaryColor = settings.widgetThemeColor
    if (settings.useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        primaryColor = context.resources.getColor(android.R.color.system_accent1_500, context.theme)
    }
    
    if (settings.widgetThemeColor == 0xFFFFFFFF.toInt() && !(settings.useDynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)) {
        primaryColor = android.graphics.Color.parseColor("#0A56D9")
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
    views.setTextColor(R.id.widget_day, primaryColor)
    views.setTextColor(R.id.widget_month_year, primaryColor)
    views.setTextColor(R.id.widget_lunar_date, primaryColor)

    
    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)
    
    val dayOfWeekList = listOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
    val dayOfWeek = dayOfWeekList[calendar.get(Calendar.DAY_OF_WEEK) - 1]

    val lunarDate = LunarUtils.getLunarDate(day, month, year)
    val lunarText = "${lunarDate.day}/${lunarDate.month}${if (lunarDate.isLeap) " (Nhuận)" else ""} (ÂL)"

    views.setTextViewText(R.id.widget_day, day.toString())
    views.setTextViewText(R.id.widget_month_year, "Tháng $month, $year")
    views.setTextViewText(R.id.widget_day_of_week, dayOfWeek)
    if (showLunar) {
        views.setTextViewText(R.id.widget_lunar_date, lunarText)
        views.setTextViewTextSize(R.id.widget_day, android.util.TypedValue.COMPLEX_UNIT_SP, 64f)
    } else {
        views.setTextViewText(R.id.widget_lunar_date, "")
        views.setTextViewTextSize(R.id.widget_day, android.util.TypedValue.COMPLEX_UNIT_SP, 80f)
    }
    views.setTextColor(R.id.widget_day_of_week, primaryColor)


    // Setup click intent to open main activity
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_day, pendingIntent)
    views.setOnClickPendingIntent(R.id.widget_month_year, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
