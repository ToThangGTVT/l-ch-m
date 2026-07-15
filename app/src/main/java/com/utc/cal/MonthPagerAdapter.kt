package com.utc.cal

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors
import java.util.Calendar

/**
 * Backs the [androidx.viewpager2.widget.ViewPager2] of months. Uses the same infinite-scroll
 * trick as the previous Compose screen: [ANCHOR] represents the current month and each page is
 * that many months away. Each page is a 6x7 grid of weighted day cells so it fills the pager area.
 */
class MonthPagerAdapter(
    var startOnMonday: Boolean,
    private val onDayClick: (Calendar) -> Unit
) : RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder>() {

    companion object {
        const val ANCHOR = 6000
        const val PAGE_COUNT = 12000
    }

    var selectedDate: Calendar = Calendar.getInstance()

    override fun getItemCount() = PAGE_COUNT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month_page, parent, false)
        return MonthViewHolder(view as LinearLayout)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, position - ANCHOR)
        holder.bind(cal)
    }

    inner class MonthViewHolder(private val container: LinearLayout) :
        RecyclerView.ViewHolder(container) {

        fun bind(monthCal: Calendar) {
            container.removeAllViews()
            val ctx = container.context
            val inflater = LayoutInflater.from(ctx)

            val month = monthCal.get(Calendar.MONTH)
            val year = monthCal.get(Calendar.YEAR)
            val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val first = monthCal.clone() as Calendar
            first.set(Calendar.DAY_OF_MONTH, 1)
            var firstDayOfWeek = first.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
            if (startOnMonday) firstDayOfWeek = (firstDayOfWeek - 1 + 7) % 7

            val today = Calendar.getInstance()

            for (row in 0 until 6) {
                val rowLayout = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                    )
                }
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOfWeek + 1

                    val cell = inflater.inflate(R.layout.item_day_cell, rowLayout, false)
                    cell.layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                    )
                    val cellBg = cell.findViewById<LinearLayout>(R.id.cell_bg)
                    val tvSolar = cell.findViewById<TextView>(R.id.tv_solar)
                    val tvLunar = cell.findViewById<TextView>(R.id.tv_lunar)

                    if (day in 1..daysInMonth) {
                        val dayCal = monthCal.clone() as Calendar
                        dayCal.set(Calendar.DAY_OF_MONTH, day)

                        val isSelected = isSameDay(dayCal, selectedDate)
                        val isToday = isSameDay(dayCal, today)
                        val dow = dayCal.get(Calendar.DAY_OF_WEEK)
                        val isWeekend = dow == Calendar.SUNDAY || dow == Calendar.SATURDAY

                        val lunar = LunarUtils.getLunarDate(day, month + 1, year)
                        tvSolar.text = day.toString()
                        tvLunar.text =
                            if (lunar.day == 1) "${lunar.day}/${lunar.month}" else lunar.day.toString()

                        when {
                            isSelected -> {
                                cellBg.setBackgroundResource(R.drawable.bg_day_selected)
                                val c = color(MaterialR.attr.colorOnPrimaryContainer)
                                tvSolar.setTextColor(c)
                                tvLunar.setTextColor(c)
                            }
                            isToday -> {
                                cellBg.setBackgroundResource(R.drawable.bg_day_today)
                                val c = color(MaterialR.attr.colorOnSecondaryContainer)
                                tvSolar.setTextColor(c)
                                tvLunar.setTextColor(c)
                            }
                            else -> {
                                cellBg.background = null
                                tvSolar.setTextColor(
                                    color(if (isWeekend) MaterialR.attr.colorError else MaterialR.attr.colorOnSurface)
                                )
                                tvLunar.setTextColor(color(MaterialR.attr.colorOnSurfaceVariant))
                            }
                        }
                        cellBg.setOnClickListener { onDayClick(dayCal) }
                    } else {
                        tvSolar.text = ""
                        tvLunar.text = ""
                        cellBg.background = null
                        cellBg.isClickable = false
                        cellBg.setOnClickListener(null)
                    }
                    rowLayout.addView(cell)
                }
                container.addView(rowLayout)
            }
        }

        private fun color(attr: Int): Int =
            MaterialColors.getColor(container, attr, Color.BLACK)
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH) &&
        a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
