package com.example.synji_calendar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.ContentPasteSearch
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nlf.calendar.Solar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    val holidayMap by homeViewModel.holidays.collectAsState()
    
    // Initial month setup
    val initialMonth = YearMonth.now()
    val pageCount = 20000
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(pageCount = { pageCount }, initialPage = initialPage)
    
    // Current month derived from pager state
    val currentMonth = remember(pagerState.currentPage) {
        initialMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
    }

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
            // 1. Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 54.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { },
                    shape = RoundedCornerShape(23.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF535353), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("点击搜索日程", color = Color(0xFF535353), fontSize = 18.sp, modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFEEEEEE)))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("搜索", color = BgGradientStart, fontWeight = FontWeight.Medium, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = Color(0xFF2C3E50)) {
                    Box(contentAlignment = Alignment.Center) { Text("👑", fontSize = 20.sp) }
                }
            }

            // 2. Actions
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionItem(Icons.Outlined.Collections, "图片上传")
                ActionItem(Icons.Outlined.ContentPasteSearch, "悬浮窗")
                ActionItem(Icons.Outlined.Groups, "群组")
                ActionItem(Icons.Default.MoreHoriz, "更多")
            }

            // 3. Bottom Container
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ContainerGrey,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally 
                ) {
                    CalendarHeader(currentMonth)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        modifier = Modifier.wrapContentSize(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 0.dp
                    ) {
                        // SWIPABLE CALENDAR
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.wrapContentSize(),
                            verticalAlignment = Alignment.Top
                        ) { page ->
                            val month = initialMonth.plusMonths((page - initialPage).toLong())
                            LiveCalendar(holidayMap, month)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(currentMonth: YearMonth) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Menu, null, modifier = Modifier.size(28.dp), tint = Color.Black)
        Spacer(modifier = Modifier.width(16.dp))
        Text(currentMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun LiveCalendar(holidayMap: Map<LocalDate, Holiday>, currentMonth: YearMonth) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val calendarDays = remember(currentMonth) {
        val days = mutableListOf<LocalDate>()
        val firstOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7
        val prevMonth = currentMonth.minusMonths(1)
        for (i in firstDayOfWeek - 1 downTo 0) days.add(prevMonth.atDay(prevMonth.lengthOfMonth() - i))
        for (i in 1..currentMonth.lengthOfMonth()) days.add(currentMonth.atDay(i))
        var nextMonthDay = 1
        while (days.size < 42) days.add(currentMonth.plusMonths(1).atDay(nextMonthDay++))
        days
    }

    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp).wrapContentSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 13.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        calendarDays.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CalendarDay(date, date == selectedDate, YearMonth.from(date) == currentMonth, holidayMap[date]) { selectedDate = it }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(date: LocalDate, isSelected: Boolean, isCurrentMonth: Boolean, holiday: Holiday?, onDateSelected: (LocalDate) -> Unit) {
    val solar = remember(date) { Solar.fromYmd(date.year, date.monthValue, date.dayOfMonth) }
    val lunar = remember(solar) { solar.lunar }
    val isToday = remember(date) { date == LocalDate.now() }
    val subText = remember(solar, lunar) {
        val sf = solar.festivals; val lf = lunar.festivals; val of = lunar.otherFestivals; val jq = lunar.jieQi
        when {
            sf.isNotEmpty() -> sf[0]; lf.isNotEmpty() -> lf[0]; of.isNotEmpty() -> of[0]; jq.isNotEmpty() -> jq
            lunar.day == 1 -> lunar.monthInChinese + "月"; else -> lunar.dayInChinese
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(46.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = isCurrentMonth) { onDateSelected(date) }
    ) {
        if (isSelected && isCurrentMonth) Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = CalendarSelectBlue) {}
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal, 
                lineHeight = 16.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                color = when { isSelected && isCurrentMonth -> Color.White; isToday && isCurrentMonth -> CalendarSelectBlue; !isCurrentMonth -> Color.LightGray; else -> Color(0xFF333333) }
            )
            Text(
                text = subText, fontSize = 10.sp, maxLines = 1, lineHeight = 10.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                color = when { isSelected && isCurrentMonth -> Color.White; isToday && isCurrentMonth -> CalendarSelectBlue; !isCurrentMonth -> Color.LightGray; else -> Color(0xFF999999) }
            )
        }
        if (isCurrentMonth && holiday != null) {
            Text(
                text = if (holiday.isRest) "休" else "班",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else (if (holiday.isRest) RestBlue else WorkRed),
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 2.dp)
            )
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, label: String) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { /* TODO: Navigate to feature */ }
    ) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = IconColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = TextTitle,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() { HomeScreen() }
