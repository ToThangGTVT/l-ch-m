package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LunarCalendarScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunarCalendarScreen(modifier: Modifier = Modifier) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    val bgColor = Color(0xFFFCF8F8)
    
    Column(modifier = modifier.fillMaxSize().background(bgColor).padding(16.dp)) {
        Header(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
            onPreviousMonth = {
                val newCal = calendar.clone() as Calendar
                newCal.add(Calendar.MONTH, -1)
                calendar = newCal
            },
            onNextMonth = {
                val newCal = calendar.clone() as Calendar
                newCal.add(Calendar.MONTH, 1)
                calendar = newCal
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        DaysOfWeekHeader()
        
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            CalendarGrid(
                calendar = calendar,
                selectedDate = selectedDate,
                onDayClick = { selectedDate = it }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        DateDetailsCard(selectedDate)
    }
}

@Composable
fun Header(month: Int, year: Int, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tháng trước", tint = Color(0xFF1D1B1E))
        }
        Text(
            text = "Tháng ${month + 1}, $year".uppercase(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFF8C1D18)
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Tháng sau", tint = Color(0xFF1D1B1E))
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    val days = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (day == "CN" || day == "T7") Color(0xFF8C1D18) else Color(0xFF1D1B1E).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CalendarGrid(
    calendar: Calendar,
    selectedDate: Calendar,
    onDayClick: (Calendar) -> Unit
) {
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val clone = calendar.clone() as Calendar
    clone.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = clone.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    
    val totalDays = daysInMonth + firstDayOfWeek
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(firstDayOfWeek) {
            Spacer(modifier = Modifier.size(48.dp))
        }
        items(daysInMonth) { dayIndex ->
            val day = dayIndex + 1
            val dayCal = calendar.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, day)
            
            val isSelected = selectedDate.get(Calendar.DAY_OF_MONTH) == day &&
                    selectedDate.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            
            val isToday = dayCal.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                    dayCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                    dayCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)

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
        isSelected -> Color(0xFFF9DEDC)
        isToday -> Color(0xFFEADDFF)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> Color(0xFF410E0B)
        isToday -> Color(0xFF21005D)
        isWeekend -> Color(0xFF8C1D18)
        else -> Color(0xFF1D1B1E)
    }
    
    val lunarTextColor = when {
        isSelected -> Color(0xFF410E0B).copy(alpha = 0.7f)
        isToday -> Color(0xFF21005D).copy(alpha = 0.7f)
        else -> Color(0xFF1D1B1E).copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 16.sp
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
fun DateDetailsCard(date: Calendar) {
    val dd = date.get(Calendar.DAY_OF_MONTH)
    val mm = date.get(Calendar.MONTH) + 1
    val yyyy = date.get(Calendar.YEAR)
    
    val lunarDate = LunarUtils.getLunarDate(dd, mm, yyyy)
    val dayCanChi = LunarUtils.getDayCanChi(dd, mm, yyyy)
    val monthCanChi = LunarUtils.getMonthCanChi(lunarDate.month, lunarDate.year)
    val yearCanChi = LunarUtils.getCanChiYear(lunarDate.year)
    
    val dayOfWeekList = listOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
    val dayOfWeek = dayOfWeekList[date.get(Calendar.DAY_OF_WEEK) - 1]

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF1F0), RoundedCornerShape(32.dp))
                .border(1.dp, Color(0xFFF2B8B5), RoundedCornerShape(32.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayOfWeek.uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8C1D18).copy(alpha = 0.6f),
                letterSpacing = 2.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = dd.toString(),
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8C1D18),
                    lineHeight = 80.sp
                )
                Text(
                    text = "Dương lịch",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF410E0B),
                    modifier = Modifier.offset(y = (-8).dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF2B8B5))
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = String.format("%02d", lunarDate.day),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB3261E)
                    )
                    Text(
                        text = "tháng ${String.format("%02d", lunarDate.month)}${if (lunarDate.isLeap) " (Nhuận)" else ""}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB3261E)
                    )
                }
                Text(
                    text = "Năm $yearCanChi".uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF601410),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CanChiCard(modifier = Modifier.weight(1f), label = "NGÀY", value = dayCanChi)
            CanChiCard(modifier = Modifier.weight(1f), label = "THÁNG", value = monthCanChi)
        }
    }
}

@Composable
fun CanChiCard(modifier: Modifier = Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6750A4),
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1D1B1E)
        )
    }
}
