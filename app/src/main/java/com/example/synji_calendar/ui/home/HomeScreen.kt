package com.example.synji_calendar.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.ContentPasteSearch
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synji_calendar.service.FloatingService
import com.nlf.calendar.Solar
import kotlinx.coroutines.flow.collectLatest
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
    onEditSchedule: (Schedule) -> Unit = {},
    onGroupClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMoreClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val holidayMap by homeViewModel.holidays.collectAsState()
    val monthDataMap by homeViewModel.currentMonthData.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showHistoryList by remember { mutableStateOf(false) }

    // 监听生命周期，从其他软件切回时刷新
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (token.isNotEmpty()) homeViewModel.refreshSchedules(token, isBackground = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 监听 ViewModel 消息
    LaunchedEffect(Unit) {
        homeViewModel.message.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { homeViewModel.performAutoScheduleFromImage(token, it) }
    }

    // 处理悬浮窗权限申请
    val overlayPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Settings.canDrawOverlays(context)) {
            val intent = Intent(context, FloatingService::class.java).apply {
                putExtra(FloatingService.EXTRA_TOKEN, token)
            }
            context.startService(intent)
        }
    }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) homeViewModel.refreshSchedules(token, isBackground = true)
    }

    val initialMonth = YearMonth.now()
    val pageCount = 20000
    val initialPage = pageCount / 2
    val pagerState = rememberPagerState(pageCount = { pageCount }, initialPage = initialPage)
    
    val displayMonth = remember(pagerState.currentPage) {
        initialMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    LaunchedEffect(displayMonth) {
        homeViewModel.getOrComputeMonthData(displayMonth)
        homeViewModel.getOrComputeMonthData(displayMonth.plusMonths(1))
        homeViewModel.getOrComputeMonthData(displayMonth.minusMonths(1))
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var showWheelPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // 切换日期静默刷新
    LaunchedEffect(selectedDate) {
        if (token.isNotEmpty() && selectedDate != null) {
            homeViewModel.refreshSchedules(token, isBackground = true)
        }
    }

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
                Surface(
                    modifier = Modifier.size(40.dp).clickable { onProfileClick() },
                    shape = CircleShape,
                    color = IconColor
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                ActionItem(Icons.Outlined.Collections, "图片上传") { galleryLauncher.launch("image/*") }
                ActionItem(Icons.Outlined.ContentPasteSearch, "悬浮窗") {
                    if (Settings.canDrawOverlays(context)) {
                        val intent = Intent(context, FloatingService::class.java).apply {
                            putExtra(FloatingService.EXTRA_TOKEN, token)
                        }
                        context.startService(intent)
                    } else {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        overlayPermissionLauncher.launch(intent)
                    }
                }
                ActionItem(Icons.Outlined.Groups, "群组") { onGroupClick() }
                ActionItem(Icons.Default.MoreHoriz, "更多") { onMoreClick() }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = ContainerGrey, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        CalendarHeader(
                            currentMonth = displayMonth,
                            onMenuClick = { showHistoryList = true },
                            onAddClick = { onAddSchedule(selectedDate ?: LocalDate.now()) },
                            onRefreshClick = { if (token.isNotEmpty()) homeViewModel.refreshSchedules(token, isBackground = true) }
                        )
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
                                beyondViewportPageCount = 0,
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

        // --- 全屏添加日程记录页面 ---
        AnimatedVisibility(
            visible = showHistoryList,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = ContainerGrey) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp).background(Brush.horizontalGradient(listOf(BgGradientStart, BgGradientEnd))).statusBarsPadding().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showHistoryList = false }) {
                            Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Text("添加日程记录", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    val allSchedules by homeViewModel.allSchedules.collectAsState()
                    
                    // 仅显示通过 AI 添加的日程
                    val historySchedules = remember(allSchedules) {
                        allSchedules.filter { it.isAiGenerated }.sortedByDescending { it.id ?: 0L } // 以ID倒序，即添加时间倒序
                    }
                    
                    if (historySchedules.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("暂无自动添加记录", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(historySchedules) { schedule ->
                                HistoryScheduleCard(
                                    schedule = schedule,
                                    isUnviewed = !schedule.isViewed,
                                    onClick = {
                                        homeViewModel.markAsViewed(token, schedule)
                                        onEditSchedule(schedule)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FloatingActionButton(onClick = { showWheelPicker = true }, containerColor = Color.White, contentColor = IconColor, shape = RoundedCornerShape(18.dp), modifier = Modifier.size(56.dp)) { Icon(Icons.Default.CalendarMonth, null) }
            FloatingActionButton(onClick = { scope.launch { pagerState.animateScrollToPage(initialPage); selectedDate = LocalDate.now() } }, containerColor = Color.White, contentColor = CalendarSelectBlue, shape = RoundedCornerShape(18.dp), modifier = Modifier.size(56.dp)) { Text("今", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }
        
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun HistoryScheduleCard(schedule: Schedule, isUnviewed: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.5.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = schedule.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${schedule.date.format(DateTimeFormatter.ofPattern("MM月dd日"))} ${if (schedule.isAllDay) "全天" else schedule.time.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
            }
            
            // 红点标记
            if (isUnviewed) {
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape).background(WorkRed).align(Alignment.TopEnd)
                )
            }
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

    // 格式化农历月份展示
    val monthInt = kotlin.math.abs(lunar.month)
    val chineseMonthDigits = when(monthInt) {
        1 -> "一"
        2 -> "二"
        3 -> "三"
        4 -> "四"
        5 -> "五"
        6 -> "六"
        7 -> "七"
        8 -> "八"
        9 -> "九"
        10 -> "十"
        11 -> "十一"
        12 -> "十二"
        else -> ""
    }
    val alias = when(monthInt) {
        1 -> "(正月)"
        11 -> "(冬月)"
        12 -> "(腊月)"
        else -> ""
    }
    val dateStr = "$prefix 农历${chineseMonthDigits}月${alias}${lunar.dayInChinese}"

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
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .background(accentColor, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                
                if (!item.location.isNullOrEmpty()) {
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
                            text = item.location ?: "",
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
fun CalendarHeader(currentMonth: YearMonth, onMenuClick: () -> Unit = {}, onAddClick: () -> Unit = {}, onRefreshClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMenuClick, modifier = Modifier.size(26.dp)) {
            Icon(Icons.Default.Menu, null, tint = Color.Black)
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = onRefreshClick, modifier = Modifier.size(26.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Black)
        }
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
