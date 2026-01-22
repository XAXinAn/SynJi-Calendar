package com.example.synji_calendar.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.ContentPasteSearch
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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

// --- COLOR CONSTANTS ---
val BgGradientStart = Color(0xFFF7B07E)
val BgGradientEnd = Color(0xFFFBD6B7)
val ContainerGrey = Color(0xFFF4F5F9)
val CalendarSelectBlue = Color(0xFF2B92E4)
val IconColor = Color(0xFF535353)
val TextTitle = Color(0xFF535353)
val RestBlue = Color(0xFF2B92E4)
val WorkRed = Color(0xFFE66767)

// Pre-calculated day info for performance
data class DayDisplayInfo(
    val date: LocalDate,
    val subText: String,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val holiday: Holiday?
)

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel()) {
    val holidayMap by homeViewModel.holidays.collectAsState()
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { }

    val initialMonth = YearMonth.now()
    val pageCount = 20000
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(pageCount = { pageCount }, initialPage = initialPage)
    
    val currentMonth = remember(pagerState.currentPage) {
        initialMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    LaunchedEffect(currentMonth) {
        if (selectedDate != null && YearMonth.from(selectedDate!!) != currentMonth) {
            selectedDate = currentMonth.atDay(1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(
        brush = Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd), 0f, 1000f)
    )) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Search Bar - Scaled Down
            Row(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 20.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.weight(1f).height(40.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
                    shape = RoundedCornerShape(20.dp), color = Color.White
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF535353), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("点击搜索日程", color = Color(0xFF535353), fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFFEEEEEE)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("搜索", color = BgGradientStart, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color(0xFF2C3E50)) {
                    Box(contentAlignment = Alignment.Center) { Text("👑", fontSize = 16.sp) }
                }
            }

            // 2. Quick Actions - Scaled Down
            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                ActionItem(Icons.Outlined.Collections, "图片上传") { galleryLauncher.launch("image/*") }
                ActionItem(Icons.Outlined.ContentPasteSearch, "悬浮窗") { }
                ActionItem(Icons.Outlined.Groups, "群组") { }
                ActionItem(Icons.Default.MoreVert, "更多") { }
            }

            // 3. Bottom Container
            Surface(modifier = Modifier.fillMaxSize(), color = ContainerGrey, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp)) {
                    CalendarHeader(currentMonth)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Fixed Height Swiping Area with content-based height animation
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight().animateContentSize(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
                        ),
                        verticalAlignment = Alignment.Top,
                        beyondViewportPageCount = 1
                    ) { page ->
                        val month = initialMonth.plusMonths((page - initialPage).toLong())
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(0.96f).wrapContentHeight(),
                                shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 0.dp
                            ) {
                                LiveCalendar(holidayMap, month, selectedDate) { selectedDate = it }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(currentMonth: YearMonth) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Menu, null, modifier = Modifier.size(28.dp), tint = Color.Black)
        Spacer(modifier = Modifier.width(16.dp))
        Text(currentMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun LiveCalendar(holidayMap: Map<LocalDate, Holiday>, currentMonth: YearMonth, selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val monthData = remember(currentMonth, holidayMap) {
        val days = mutableListOf<LocalDate>()
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
        val prevMonth = currentMonth.minusMonths(1)
        for (i in firstDayOfWeek - 1 downTo 0) days.add(prevMonth.atDay(prevMonth.lengthOfMonth() - i))
        for (i in 1..currentMonth.lengthOfMonth()) days.add(currentMonth.atDay(i))
        while (days.size < 42) days.add(currentMonth.plusMonths(1).atDay(days.size - firstDayOfWeek - currentMonth.lengthOfMonth() + 1))
        
        days.map { date ->
            val solar = Solar.fromYmd(date.year, date.monthValue, date.dayOfMonth)
            val lunar = solar.lunar
            val sf = solar.festivals; val lf = lunar.festivals; val of = lunar.otherFestivals; val jq = lunar.jieQi
            val subText = when {
                sf.isNotEmpty() -> sf[0]; lf.isNotEmpty() -> lf[0]; of.isNotEmpty() -> of[0]; jq.isNotEmpty() -> jq
                lunar.day == 1 -> lunar.monthInChinese + "月"; else -> lunar.dayInChinese
            }
            DayDisplayInfo(date, subText, date == LocalDate.now(), YearMonth.from(date) == currentMonth, holidayMap[date])
        }
    }

    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp).fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 13.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        monthData.chunked(7).forEach { week ->
            if (week.any { it.isCurrentMonth }) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { info ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            CalendarDay(info, info.date == selectedDate, onDateSelected)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(info: DayDisplayInfo, isSelected: Boolean, onDateSelected: (LocalDate) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(46.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = info.isCurrentMonth) { onDateSelected(info.date) }
    ) {
        if (isSelected && info.isCurrentMonth) {
            if (info.isToday) {
                Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = CalendarSelectBlue) {}
            } else {
                Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = Color.Transparent, border = BorderStroke(1.5.dp, CalendarSelectBlue)) {}
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
            val textColor = when {
                isSelected && info.isCurrentMonth && info.isToday -> Color.White
                info.isToday && info.isCurrentMonth -> CalendarSelectBlue
                !info.isCurrentMonth -> Color.LightGray
                else -> Color(0xFF333333)
            }
            Text(
                text = info.date.dayOfMonth.toString(), fontSize = 17.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)), color = textColor
            )
            Text(
                text = info.subText, fontSize = 10.sp, maxLines = 1, lineHeight = 10.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                color = if (textColor == Color.White) Color.White else (if (info.isToday && !isSelected) CalendarSelectBlue else Color(0xFF999999))
            )
        }
        if (info.isCurrentMonth && info.holiday != null) {
            Text(
                text = if (info.holiday.isRest) "休" else "班", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                color = if (isSelected && info.isToday) Color.White else (if (info.holiday.isRest) RestBlue else WorkRed),
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)) {
        Surface(modifier = Modifier.size(56.dp), shape = RoundedCornerShape(12.dp), color = Color.White) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = IconColor, modifier = Modifier.size(24.dp)) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 13.sp, color = TextTitle, fontWeight = FontWeight.Normal)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() { HomeScreen() }
