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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.ContentPasteSearch
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nlf.calendar.Solar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

// --- COLOR CONSTANTS ---
val BgGradientStart = Color(0xFFF7B07E)
val BgGradientEnd = Color(0xFFFBD6B7)
val ContainerGrey = Color(0xFFF4F5F9)
val CalendarSelectBlue = Color(0xFF2B92E4)
val IconColor = Color(0xFF535353)
val TextTitle = Color(0xFF535353)
val RestBlue = Color(0xFF2B92E4)
val WorkRed = Color(0xFFE66767)

data class DayDisplayInfo(
    val date: LocalDate,
    val subText: String,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val holiday: Holiday?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    token: String,
    homeViewModel: HomeViewModel = viewModel(),
    onAddSchedule: (LocalDate) -> Unit = {},
    onEditSchedule: (Schedule) -> Unit = {}
) {
    val holidayMap by homeViewModel.holidays.collectAsState()
    val monthDataMap by homeViewModel.currentMonthData.collectAsState()
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { }
    val scope = rememberCoroutineScope()

    LaunchedEffect(token) {
        if (token.isNotEmpty()) homeViewModel.refreshSchedules(token)
    }

    val initialMonth = YearMonth.now()
    val pageCount = 20000
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(pageCount = { pageCount }, initialPage = initialPage)
    
    val displayMonth = remember(pagerState.currentPage) {
        initialMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    // 预加载当前月和相邻月数据到 ViewModel 缓存
    LaunchedEffect(displayMonth) {
        homeViewModel.getOrComputeMonthData(displayMonth)
        homeViewModel.getOrComputeMonthData(displayMonth.plusMonths(1))
        homeViewModel.getOrComputeMonthData(displayMonth.minusMonths(1))
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var showWheelPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showWheelPicker) {
        ModalBottomSheet(
            onDismissRequest = { showWheelPicker = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = null
        ) {
            WheelDatePickerContent(
                initialDate = selectedDate ?: LocalDate.now(),
                onConfirm = { date ->
                    selectedDate = date
                    val targetPage = initialPage + ChronoUnit.MONTHS.between(initialMonth, YearMonth.from(date)).toInt()
                    scope.launch { pagerState.animateScrollToPage(targetPage) }
                    showWheelPicker = false
                },
                onCancel = { showWheelPicker = false }
            )
        }
    }

    fun getRowCount(month: YearMonth): Int {
        val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
        val totalDays = month.lengthOfMonth()
        return (firstDayOfWeek + totalDays + 6) / 7
    }

    val interpolatedRowCount = remember(pagerState.currentPage, pagerState.currentPageOffsetFraction) {
        val currentRows = getRowCount(displayMonth)
        val offset = pagerState.currentPageOffsetFraction
        val nextMonth = if (offset > 0) displayMonth.plusMonths(1) else displayMonth.minusMonths(1)
        val nextRows = getRowCount(nextMonth)
        currentRows + (nextRows - currentRows) * abs(offset)
    }

    LaunchedEffect(pagerState.settledPage) {
        val settledMonth = initialMonth.plusMonths((pagerState.settledPage - initialPage).toLong())
        if (selectedDate == null || YearMonth.from(selectedDate!!) != settledMonth) {
            selectedDate = if (settledMonth == YearMonth.now()) LocalDate.now() else settledMonth.atDay(1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd), 0f, 1000f))) {
            // 1. Search Bar
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 20.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(22.dp), color = Color.White) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("点击搜索日程", color = Color(0xFF888888), fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color(0xFFEEEEEE)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("搜索", color = BgGradientStart, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = IconColor) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                ActionItem(Icons.Outlined.Collections, "图片上传") { galleryLauncher.launch("image/*") }
                ActionItem(Icons.Outlined.ContentPasteSearch, "悬浮窗") { }
                ActionItem(Icons.Outlined.Groups, "群组") { }
                ActionItem(Icons.Default.MoreVert, "更多") { }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = ContainerGrey, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        CalendarHeader(displayMonth, onAddClick = { onAddSchedule(selectedDate ?: LocalDate.now()) })
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
                        val config = LocalConfiguration.current
                        val contentWidth = config.screenWidthDp.dp - 32.dp
                        val daySize = (contentWidth - 24.dp) / 7
                        val dynamicCalendarHeight = (daySize * interpolatedRowCount.toFloat().coerceAtLeast(1f)) + 20.dp + 32.dp + 12.dp

                        Box(modifier = Modifier.fillMaxWidth().height(dynamicCalendarHeight)) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.Top,
                                beyondViewportPageCount = 0, // 减少预加载负担
                                pageSpacing = 16.dp
                            ) { page ->
                                val month = initialMonth.plusMonths((page - initialPage).toLong())
                                val data = monthDataMap[month] ?: emptyList()
                                
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                                    Surface(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 0.5.dp) {
                                        if (data.isNotEmpty()) {
                                            LiveCalendar(data, holidayMap, selectedDate) { selectedDate = it }
                                        } else {
                                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(color = CalendarSelectBlue, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        selectedDate?.let { ScheduleSection(it, homeViewModel, onEditSchedule) }
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FloatingActionButton(onClick = { showWheelPicker = true }, containerColor = Color.White, contentColor = IconColor, shape = RoundedCornerShape(18.dp), modifier = Modifier.size(56.dp)) { Icon(Icons.Default.CalendarMonth, null) }
            FloatingActionButton(onClick = { scope.launch { pagerState.animateScrollToPage(initialPage); selectedDate = LocalDate.now() } }, containerColor = Color.White, contentColor = CalendarSelectBlue, shape = RoundedCornerShape(18.dp), modifier = Modifier.size(56.dp)) { Text("今", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun LiveCalendar(monthData: List<DayDisplayInfo>, holidayMap: Map<LocalDate, Holiday>, selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp).fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                Box(modifier = Modifier.weight(1f).height(20.dp), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 12.sp, color = Color(0xFF999999), fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        monthData.chunked(7).forEach { week ->
            if (week.any { it.isCurrentMonth }) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { info ->
                        val infoWithHoliday = info.copy(holiday = holidayMap[info.date])
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            CalendarDay(infoWithHoliday, info.date == selectedDate, onDateSelected)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleSection(selectedDate: LocalDate, homeViewModel: HomeViewModel, onEditSchedule: (Schedule) -> Unit) {
    val allSchedules by homeViewModel.allSchedules.collectAsState()
    val schedules = remember(allSchedules, selectedDate) {
        allSchedules.filter { it.date == selectedDate }.sortedBy { if(it.isAllDay) LocalTime.MIN else it.time }
    }
    
    val solar = remember(selectedDate) { Solar.fromYmd(selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth) }
    val lunar = remember(solar) { solar.lunar }
    val diff = ChronoUnit.DAYS.between(LocalDate.now(), selectedDate)
    val prefix = when (diff) {
        0L -> "今天"
        1L -> "明天"
        2L -> "后天"
        -1L -> "昨天"
        -2L -> "前天"
        else -> if (diff > 0) "${diff}天后" else "${-diff}天前"
    }
    val dateStr = "$prefix 农历${lunar.monthInChinese}月${lunar.dayInChinese}"

    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp, end = 4.dp)) {
        Text(text = dateStr, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF666666), modifier = Modifier.padding(bottom = 12.dp))
        if (schedules.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) { Text("暂无日程", color = Color.Gray, fontSize = 14.sp) }
        } else {
            schedules.forEach { schedule ->
                ScheduleCard(schedule, onClick = { onEditSchedule(schedule) })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ScheduleCard(item: Schedule, onClick: () -> Unit = {}) {
    val accentColor = if (item.isImportant) WorkRed else CalendarSelectBlue
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间展示区域
            Column(
                modifier = Modifier.width(60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (item.isAllDay) "全天" else item.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isAllDay) CalendarSelectBlue else Color(0xFF333333)
                )
                if (!item.isAllDay) {
                    Text(
                        text = "开始",
                        fontSize = 10.sp,
                        color = Color.Gray.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 垂直指示条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .background(accentColor, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 内容区域
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 归属标签
                    Surface(
                        color = accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.belonging,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    
                    if (item.isImportant) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = WorkRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = item.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    lineHeight = 22.sp
                )
                
                if (item.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.location,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CalendarHeader(currentMonth: YearMonth, onAddClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Menu, null, modifier = Modifier.size(26.dp), tint = Color.Black)
        Spacer(modifier = Modifier.weight(1f))
        Text(currentMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onAddClick, modifier = Modifier.size(26.dp)) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black) }
    }
}

@Composable
fun CalendarDay(info: DayDisplayInfo, isSelected: Boolean, onDateSelected: (LocalDate) -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = info.isCurrentMonth) { onDateSelected(info.date) }) {
        if (isSelected && info.isCurrentMonth) {
            if (info.isToday) Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = CalendarSelectBlue) {}
            else Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, border = BorderStroke(1.5.dp, CalendarSelectBlue), color = Color.Transparent) {}
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val textColor = when {
                isSelected && info.isCurrentMonth && info.isToday -> Color.White
                info.isToday && info.isCurrentMonth -> CalendarSelectBlue
                !info.isCurrentMonth -> Color.LightGray
                else -> Color(0xFF333333)
            }
            Text(text = info.date.dayOfMonth.toString(), fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)), color = textColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = info.subText, fontSize = 10.sp, maxLines = 1, lineHeight = 10.sp, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)), color = if (textColor == Color.White) Color.White else (if (info.isToday && !isSelected) CalendarSelectBlue else Color(0xFF999999)))
        }
        if (info.isCurrentMonth && info.holiday != null) {
            Text(text = if (info.holiday.isRest) "休" else "班", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected && info.isToday) Color.White else (if (info.holiday.isRest) RestBlue else WorkRed), modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp))
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)) {
        Surface(modifier = Modifier.size(54.dp), shape = RoundedCornerShape(14.dp), color = Color.White) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = IconColor, modifier = Modifier.size(26.dp)) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 13.sp, color = TextTitle, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() { HomeScreen(token = "") }
