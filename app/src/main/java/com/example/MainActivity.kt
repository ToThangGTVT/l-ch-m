package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.*
import androidx.compose.ui.platform.LocalContext
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.ComponentName
import com.example.widget.MonthWidgetProvider
import com.example.widget.CalendarWidgetProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.example.WidgetSettingsManager
import java.util.Calendar
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                
                val context = LocalContext.current
                val settingsManager = remember { WidgetSettingsManager(context) }
                var showSettings by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showSettings = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                ) { innerPadding ->
                    if (showSettings) {
                        SettingsDialog(
                            showLunarInit = settingsManager.showLunarDate,
                            useDynamicColorInit = settingsManager.useDynamicColor,
                            themeColorInit = settingsManager.widgetThemeColor,
                            startOnMondayInit = settingsManager.startOnMonday,
                            onDismiss = { showSettings = false },
                            onSave = { showLunar, useDyn, color, startMonday ->
                                settingsManager.startOnMonday = startMonday
                                settingsManager.useDynamicColor = useDyn
                                settingsManager.showLunarDate = showLunar
                                settingsManager.widgetThemeColor = color
                                showSettings = false
                                
                                val intent1 = Intent(context, MonthWidgetProvider::class.java).apply {
                                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                    val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, MonthWidgetProvider::class.java))
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                                }
                                context.sendBroadcast(intent1)
                                
                                val intent2 = Intent(context, CalendarWidgetProvider::class.java).apply {
                                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                    val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, CalendarWidgetProvider::class.java))
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                                }
                                context.sendBroadcast(intent2)
                            }
                        )
                    }

                    LunarCalendarScreen(startOnMonday = settingsManager.startOnMonday, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LunarCalendarScreen(startOnMonday: Boolean, modifier: Modifier = Modifier) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    val initialPage = 6000 // 500 years
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 12000 })
    val coroutineScope = rememberCoroutineScope()
    
    val currentMonthCalendar = remember(pagerState.currentPage) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, pagerState.currentPage - initialPage)
        cal
    }
    
    val bgColor = MaterialTheme.colorScheme.background
    
    Column(modifier = modifier.fillMaxSize().background(bgColor).padding(16.dp)) {
        Header(currentMonthCalendar.get(Calendar.MONTH), currentMonthCalendar.get(Calendar.YEAR),
            onPreviousMonth = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            onNextMonth = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        DaysOfWeekHeader(startOnMonday)
        
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.weight(1.3f)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageCalendar = remember(page) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, page - initialPage)
                    cal
                }
                CalendarGrid(
                    modifier = Modifier.fillMaxSize(),
                    startOnMonday = startOnMonday,
                    calendar = pageCalendar,
                    selectedDate = selectedDate,
                    onDayClick = { selectedDate = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
            DateDetailsCard(selectedDate, modifier = Modifier.fillMaxSize())
        }
    }
}
