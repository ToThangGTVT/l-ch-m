package com.utc.cal

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.utc.cal.databinding.ActivityMainBinding
import com.utc.cal.databinding.DialogSettingsBinding
import com.utc.cal.widget.CalendarWidgetProvider
import com.utc.cal.widget.MonthWidgetProvider
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: WidgetSettingsManager
    private lateinit var pagerAdapter: MonthPagerAdapter

    private var selectedDate: Calendar = Calendar.getInstance()

    private val themeColors = listOf(
        0xFF8C1D18.toInt(),
        0xFF21005D.toInt(),
        0xFF006874.toInt(),
        0xFF1A6C30.toInt(),
        0xFF6C5E0F.toInt(),
        0xFFFFFFFF.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settings = WidgetSettingsManager(this)

        pagerAdapter = MonthPagerAdapter(settings.startOnMonday) { clicked ->
            selectedDate = clicked
            pagerAdapter.selectedDate = clicked
            pagerAdapter.notifyItemChanged(binding.viewPager.currentItem)
            bindDetails(clicked)
        }
        pagerAdapter.selectedDate = selectedDate

        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.setCurrentItem(MonthPagerAdapter.ANCHOR, false)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateHeader(position)
        })

        binding.btnPrev.setOnClickListener {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
        binding.btnNext.setOnClickListener {
            binding.viewPager.currentItem = binding.viewPager.currentItem + 1
        }
        binding.fabSettings.setOnClickListener { showSettingsDialog() }

        buildWeekdayHeader()
        updateHeader(MonthPagerAdapter.ANCHOR)
        bindDetails(selectedDate)
    }

    private fun updateHeader(position: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, position - MonthPagerAdapter.ANCHOR)
        binding.tvMonthYear.text =
            "Tháng ${cal.get(Calendar.MONTH) + 1}, ${cal.get(Calendar.YEAR)}"
    }

    private fun buildWeekdayHeader() {
        val days = if (settings.startOnMonday) {
            listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        } else {
            listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
        }
        binding.llDowHeader.removeAllViews()
        days.forEach { d ->
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
                gravity = Gravity.CENTER
                text = d
                textSize = 12f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                val weekend = d == "CN" || d == "T7"
                setTextColor(
                    com.google.android.material.color.MaterialColors.getColor(
                        this,
                        if (weekend) com.google.android.material.R.attr.colorError
                        else com.google.android.material.R.attr.colorOnSurfaceVariant,
                        Color.BLACK
                    )
                )
            }
            binding.llDowHeader.addView(tv)
        }
    }

    private fun bindDetails(date: Calendar) {
        val dd = date.get(Calendar.DAY_OF_MONTH)
        val mm = date.get(Calendar.MONTH) + 1
        val yyyy = date.get(Calendar.YEAR)

        val lunar = LunarUtils.getLunarDate(dd, mm, yyyy)
        val dayCanChi = LunarUtils.getDayCanChi(dd, mm, yyyy)
        val monthCanChi = LunarUtils.getMonthCanChi(lunar.month, lunar.year)
        val yearCanChi = LunarUtils.getCanChiYear(lunar.year)

        val dowNames = listOf(
            "Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"
        )
        val dow = dowNames[date.get(Calendar.DAY_OF_WEEK) - 1]

        val d = binding.details
        d.tvDayOfWeek.text = dow
        d.tvSolarDay.text = dd.toString()
        d.tvSolarMonthYear.text = "Tháng $mm, $yyyy"
        d.tvLunarDay.text = String.format(Locale.getDefault(), "%02d", lunar.day)
        d.tvLunarMonth.text =
            "/${String.format(Locale.getDefault(), "%02d", lunar.month)}${if (lunar.isLeap) " Nhuận" else ""}"
        d.tvYearCanchi.text = "Năm $yearCanChi"
        d.tvDayCanchi.text = dayCanChi
        d.tvMonthCanchi.text = monthCanChi
    }

    private fun showSettingsDialog() {
        val dialogBinding = DialogSettingsBinding.inflate(layoutInflater)
        val canUseDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        dialogBinding.switchShowLunar.isChecked = settings.showLunarDate
        dialogBinding.switchStartMonday.isChecked = settings.startOnMonday
        dialogBinding.switchDynamicColor.isChecked = settings.useDynamicColor
        if (!canUseDynamic) dialogBinding.rowDynamicColor.visibility = View.GONE

        var selectedColor = settings.widgetThemeColor
        val checkViews = mutableListOf<Pair<Int, ImageView>>()

        fun refreshChecks() {
            checkViews.forEach { (c, check) ->
                check.visibility = if (c == selectedColor) View.VISIBLE else View.GONE
            }
        }

        val sizePx = dp(40)
        val checkPx = dp(24)
        themeColors.forEach { c ->
            val frame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }
            val circle = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(sizePx, sizePx, Gravity.CENTER)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(c)
                    if (c == 0xFFFFFFFF.toInt()) setStroke(dp(1), Color.LTGRAY)
                }
            }
            val check = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(checkPx, checkPx, Gravity.CENTER)
                setImageResource(R.drawable.ic_check)
                setColorFilter(if (c == 0xFFFFFFFF.toInt()) Color.BLACK else Color.WHITE)
                visibility = View.GONE
            }
            frame.addView(circle)
            frame.addView(check)
            frame.setOnClickListener {
                selectedColor = c
                refreshChecks()
            }
            dialogBinding.llColorPicker.addView(frame)
            checkViews.add(c to check)
        }
        refreshChecks()

        fun updateColorVisibility() {
            val showColors = !(canUseDynamic && dialogBinding.switchDynamicColor.isChecked)
            val vis = if (showColors) View.VISIBLE else View.GONE
            dialogBinding.labelThemeColor.visibility = vis
            dialogBinding.llColorPicker.visibility = vis
        }
        dialogBinding.switchDynamicColor.setOnCheckedChangeListener { _, _ -> updateColorVisibility() }
        updateColorVisibility()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                settings.showLunarDate = dialogBinding.switchShowLunar.isChecked
                settings.startOnMonday = dialogBinding.switchStartMonday.isChecked
                if (canUseDynamic) settings.useDynamicColor = dialogBinding.switchDynamicColor.isChecked
                settings.widgetThemeColor = selectedColor

                // Refresh the in-app calendar (week start may have changed)
                pagerAdapter.startOnMonday = settings.startOnMonday
                buildWeekdayHeader()
                pagerAdapter.notifyDataSetChanged()

                updateWidgets()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateWidgets() {
        val manager = AppWidgetManager.getInstance(this)
        listOf(MonthWidgetProvider::class.java, CalendarWidgetProvider::class.java).forEach { cls ->
            val intent = Intent(this, cls).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = manager.getAppWidgetIds(ComponentName(this@MainActivity, cls))
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            sendBroadcast(intent)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
