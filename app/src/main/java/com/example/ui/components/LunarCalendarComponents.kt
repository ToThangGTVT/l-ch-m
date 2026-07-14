package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LunarUtils
import java.util.Calendar

@Composable
fun Header(month: Int, year: Int, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Tháng trước",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Tháng ${month + 1}, $year".uppercase(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Tháng sau",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DaysOfWeekHeader(startOnMonday: Boolean) {
    val days = if (startOnMonday) listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN") else listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (day == "CN" || day == "T7") androidx.compose.material3.MaterialTheme.colorScheme.error else androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CalendarGrid(modifier: Modifier = Modifier,
    startOnMonday: Boolean,
    calendar: Calendar,
    selectedDate: Calendar,
    onDayClick: (Calendar) -> Unit
) {
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val clone = calendar.clone() as Calendar
    clone.set(Calendar.DAY_OF_MONTH, 1)
    
    var firstDayOfWeek = clone.get(Calendar.DAY_OF_WEEK) - 1
    if (startOnMonday) {
        firstDayOfWeek = (firstDayOfWeek - 1 + 7) % 7
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        for (row in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOfWeek + 1
                    
                    Box(modifier = Modifier.weight(1f)) {
                        if (day in 1..daysInMonth) {
                            val dayCal = calendar.clone() as Calendar
                            dayCal.set(Calendar.DAY_OF_MONTH, day)
                            
                            val isSelected = selectedDate.get(Calendar.DAY_OF_MONTH) == day &&
                                    selectedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                    selectedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                            
                            val todayCal = Calendar.getInstance()
                            val isToday = dayCal.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH) &&
                                    dayCal.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                                    dayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)

                            val lunarDate = LunarUtils.getLunarDate(
                                dayCal.get(Calendar.DAY_OF_MONTH),
                                dayCal.get(Calendar.MONTH) + 1,
                                dayCal.get(Calendar.YEAR)
                            )

                            DayCell(
                                day = day,
                                lunarDay = lunarDate.day,
                                isLunarMonth1 = lunarDate.day == 1,
                                lunarMonth = lunarDate.month,
                                isSelected = isSelected,
                                isToday = isToday,
                                isWeekend = dayCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || dayCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY,
                                onClick = { onDayClick(dayCal) }
                            )
                        } else {
                            Spacer(modifier = Modifier.aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    lunarDay: Int,
    isLunarMonth1: Boolean,
    lunarMonth: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isWeekend: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        isToday -> androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
        isWeekend -> androidx.compose.material3.MaterialTheme.colorScheme.error
        else -> androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    }
    
    val lunarTextColor = when {
        isSelected -> androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        isToday -> androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        else -> androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontWeight = FontWeight.Medium,
                color = textColor,
                fontSize = 18.sp
            )
            Text(
                text = if (isLunarMonth1) "$lunarDay/$lunarMonth" else lunarDay.toString(),
                fontSize = 10.sp,
                color = lunarTextColor
            )
        }
    }
}

@Composable
fun DateDetailsCard(date: Calendar, modifier: Modifier = Modifier) {
    val dd = date.get(Calendar.DAY_OF_MONTH)
    val mm = date.get(Calendar.MONTH) + 1
    val yyyy = date.get(Calendar.YEAR)
    
    val lunarDate = LunarUtils.getLunarDate(dd, mm, yyyy)
    val dayCanChi = LunarUtils.getDayCanChi(dd, mm, yyyy)
    val monthCanChi = LunarUtils.getMonthCanChi(lunarDate.month, lunarDate.year)
    val yearCanChi = LunarUtils.getCanChiYear(lunarDate.year)
    
    val dayOfWeekList = listOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
    val dayOfWeek = dayOfWeekList[date.get(Calendar.DAY_OF_WEEK) - 1]

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Text(
                text = dayOfWeek.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Row(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Solar
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dd.toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        lineHeight = 56.sp
                    )
                    Text(
                        text = "Tháng $mm, $yyyy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.7f)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                )

                // Right: Lunar
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ÂM LỊCH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = String.format("%02d", lunarDate.day),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "/${String.format("%02d", lunarDate.month)}${if (lunarDate.isLeap) " Nhuận" else ""}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "Năm $yearCanChi".uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CanChiCard(modifier = Modifier.weight(1f), label = "NGÀY", value = dayCanChi)
            CanChiCard(modifier = Modifier.weight(1f), label = "THÁNG", value = monthCanChi)
        }
    }
}
@Composable
fun CanChiCard(modifier: Modifier = Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
    }
}
