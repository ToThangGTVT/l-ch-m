package com.utc.cal.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.os.Build

@Composable
fun SettingsDialog(
    showLunarInit: Boolean,
    useDynamicColorInit: Boolean,
    themeColorInit: Int,
    startOnMondayInit: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, Boolean, Int, Boolean) -> Unit
) {
    var showLunar by remember { mutableStateOf(showLunarInit) }
    var useDynamic by remember { mutableStateOf(useDynamicColorInit) }
    var themeColor by remember { mutableStateOf(themeColorInit) }
    var startOnMonday by remember { mutableStateOf(startOnMondayInit) }
    
    val canUseDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        val colors = listOf(
        0xFF8C1D18.toInt(),
        0xFF21005D.toInt(),
        0xFF006874.toInt(),
        0xFF1A6C30.toInt(),
        0xFF6C5E0F.toInt(),
        0xFFFFFFFF.toInt()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cài đặt Widget") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hiện âm lịch")
                    Switch(checked = showLunar, onCheckedChange = { showLunar = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bắt đầu tuần bằng Thứ Hai")
                    Switch(checked = startOnMonday, onCheckedChange = { startOnMonday = it })
                }
                if (canUseDynamic) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dùng màu hệ thống (Android 12+)")
                        Switch(checked = useDynamic, onCheckedChange = { useDynamic = it })
                    }
                }
                
                if (!useDynamic || !canUseDynamic) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Màu chủ đạo")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colors.forEach { color ->
                                                        Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .then(if (color == 0xFFFFFFFF.toInt()) Modifier.border(1.dp, Color.LightGray, CircleShape) else Modifier)
                                    .clickable { themeColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (themeColor == color) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = if (color == 0xFFFFFFFF.toInt()) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(showLunar, useDynamic, themeColor, startOnMonday) }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
