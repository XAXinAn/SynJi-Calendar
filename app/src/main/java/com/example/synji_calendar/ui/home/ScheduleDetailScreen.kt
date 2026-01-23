package com.example.synji_calendar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("日程详情", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", modifier = Modifier.size(32.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Delete */ }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                    }
                    IconButton(onClick = { /* Save */ }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = ContainerGrey
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Category Icon & Label
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "学习 (点击切换分类)",
                            color = Color(0xFF4CAF50),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        // Detail Rows
                        DetailRow(Icons.AutoMirrored.Filled.Notes, "标题", "计算机网络-期末考试-\nA23计算机-2026")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        DetailRow(Icons.Default.CalendarToday, "日期", "2026-01-12")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        DetailRow(Icons.Default.AccessTime, "时间", "上午 9:30")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        DetailRow(Icons.Default.LocationOn, "地点", "未设置地点", isPlaceholder = true)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        DetailRow(Icons.Default.Groups, "归属", "个人")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                        
                        ImportantToggleRow()
                    }

                    // Floating Camera Icon inside the card
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = 80.dp)
                            .size(48.dp),
                        shape = CircleShape,
                        color = Color(0xFF535353).copy(alpha = 0.8f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String, isPlaceholder: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.width(60.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 16.sp,
            color = if (isPlaceholder) Color.LightGray else Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun ImportantToggleRow() {
    var isImportant by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "重要",
            fontSize = 16.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isImportant,
            onCheckedChange = { isImportant = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleDetailPreview() {
    ScheduleDetailScreen()
}
