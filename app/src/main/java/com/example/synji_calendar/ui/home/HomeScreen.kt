package com.example.synji_calendar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.ContentPasteSearch
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color constants
val BgGradientStart = Color(0xFFF7B07E)
val BgGradientEnd = Color(0xFFFBD6B7)
val ContainerGrey = Color(0xFFF4F5F9) 
val CalendarSelectBlue = Color(0xFF2B92E4)
val IconColor = Color(0xFF535353)
val TextTitle = Color(0xFF535353)
val RestBlue = Color(0xFF2B92E4)
val WorkRed = Color(0xFFE66767)

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BgGradientStart, BgGradientEnd),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Search Bar & Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 54.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF535353),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "点击搜索日程",
                            color = Color(0xFF535353),
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(Color(0xFFEEEEEE))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "搜索",
                            color = BgGradientStart,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = Color(0xFF2C3E50)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👑", fontSize = 20.sp)
                    }
                }
            }

            // 2. Top Quick Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(Icons.Outlined.Collections, "图片上传")
                ActionItem(Icons.Outlined.ContentPasteSearch, "悬浮窗")
                ActionItem(Icons.Outlined.Groups, "群组")
                ActionItem(Icons.Default.MoreHoriz, "更多")
            }

            // 3. Bottom Rounded Container
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ContainerGrey,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    // Calendar Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 0.dp
                    ) {
                        CalendarComponent()
                    }
                }
            }
        }
    }
}

@Composable
fun ActionItem(
    icon: ImageVector,
    label: String,
    containerColor: Color = Color.White,
    iconColor: Color = IconColor,
    labelColor: Color = TextTitle
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = RoundedCornerShape(14.dp),
            color = containerColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = labelColor,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun CalendarComponent() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Text(it, fontSize = 14.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Dec 2024 / Jan 2025 rows as per the image
        CalendarRow(
            days = listOf("28", "29", "30", "31", "1", "2", "3"),
            lunars = listOf("初九", "初十", "十一", "十二", "元旦", "十四", "十五"),
            isInactive = listOf(0, 1, 2, 3),
            selectedIdx = 5,
            restIdxs = listOf(4, 5, 6)
        )
        CalendarRow(
            days = listOf("4", "5", "6", "7", "8", "9", "10"),
            lunars = listOf("十六", "小寒", "十八", "十九", "二十", "廿一", "廿二"),
            workIdxs = listOf(0)
        )
        CalendarRow(
            days = listOf("11", "12", "13", "14", "15", "16", "17"),
            lunars = listOf("廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九")
        )
        CalendarRow(
            days = listOf("18", "19", "20", "21", "22", "23", "24"),
            lunars = listOf("三十", "腊月", "大寒", "初三", "初四", "初五", "初六")
        )
        CalendarRow(
            days = listOf("25", "26", "27", "28", "29", "30", "31"),
            lunars = listOf("初七", "腊八节", "初九", "初十", "十一", "十二", "十三")
        )
    }
}

@Composable
fun CalendarRow(
    days: List<String>, 
    lunars: List<String>, 
    isInactive: List<Int> = emptyList(),
    selectedIdx: Int? = null,
    restIdxs: List<Int> = emptyList(),
    workIdxs: List<Int> = emptyList()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = index == selectedIdx
            val isGrey = isInactive.contains(index)
            val hasRest = restIdxs.contains(index)
            val hasWork = workIdxs.contains(index)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = CalendarSelectBlue
                        ) {}
                    }
                    Text(
                        text = day,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isSelected -> Color.White
                            isGrey -> Color.LightGray
                            else -> Color(0xFF333333)
                        }
                    )
                    
                    if (hasRest) {
                        Text(
                            text = "休",
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White else RestBlue,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-4).dp)
                        )
                    }
                    if (hasWork) {
                        Text(
                            text = "班",
                            fontSize = 10.sp,
                            color = WorkRed,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-4).dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lunars[index], 
                    fontSize = 12.sp, 
                    color = when {
                        isSelected -> Color.White
                        isGrey -> Color.LightGray
                        else -> Color(0xFF777777)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
