package com.example.synji_calendar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    initialDate: LocalDate = LocalDate.now(),
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "日程详情",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTitle
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp),
                            tint = IconColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Delete logic */ }) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                    TextButton(onClick = { /* Save logic */ }) {
                        Text(
                            "完成",
                            color = CalendarSelectBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = ContainerGrey
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 0.5.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Category Section - Optimized Visuals
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(CalendarSelectBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = CalendarSelectBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        onClick = { /* Switch category logic */ },
                        shape = RoundedCornerShape(12.dp),
                        color = ContainerGrey
                    ) {
                        Text(
                            text = "学习",
                            color = CalendarSelectBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Detail List
                    DetailViewRow(Icons.AutoMirrored.Filled.Notes, "标题", "计算机网络-期末考试-\nA23计算机-2026")
                    DetailViewDivider()
                    
                    DetailViewRow(Icons.Default.CalendarToday, "日期", initialDate.toString())
                    DetailViewDivider()
                    
                    DetailViewRow(Icons.Default.AccessTime, "时间", "上午 9:30")
                    DetailViewDivider()
                    
                    DetailViewRow(Icons.Default.LocationOn, "地点", "未设置地点", isPlaceholder = true)
                    DetailViewDivider()
                    
                    DetailViewRow(Icons.Default.Groups, "归属", "个人")
                    DetailViewDivider()
                    
                    DetailImportantToggleRow()
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Optional: Add a memo/notes section if needed in the future
            Text(
                "日程由「5群」自动同步",
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DetailViewDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 14.dp),
        thickness = 0.5.dp,
        color = Color(0xFFF0F0F0)
    )
}

@Composable
private fun DetailViewRow(icon: ImageVector, label: String, value: String, isPlaceholder: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ContainerGrey, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IconColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color.Gray,
            modifier = Modifier.width(60.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (isPlaceholder) Color.LightGray else TextTitle,
            textAlign = TextAlign.End,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun DetailImportantToggleRow() {
    var isImportant by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ContainerGrey, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (isImportant) Color(0xFFFFB300) else IconColor.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "重要",
            fontSize = 15.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Switch(
            checked = isImportant,
            onCheckedChange = { isImportant = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CalendarSelectBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleDetailPreview() {
    ScheduleDetailScreen()
}
