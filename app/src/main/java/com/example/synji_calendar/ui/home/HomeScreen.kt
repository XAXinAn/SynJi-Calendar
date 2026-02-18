package com.example.synji_calendar.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synji_calendar.service.FloatingService
import com.nlf.calendar.Solar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

// --- COLOR CONSTANTS ---
val BgGradientStart = Color(0xFFF2994A)
val BgGradientEnd = Color(0xFFFBD6B7)
val ContainerGrey = Color(0xFFF4F5F9)
val CalendarSelectBlue = Color(0xFF2B92E4)
val IconColor = Color(0xFF535353)
val TextTitle = Color(0xFF333333)
val RestBlue = Color(0xFF2B92E4)
val WorkRed = Color(0xFFE66767)
val ImportantRed = Color(0xFFE94E4E)
val BrandOrange = Color(0xFFF2994A)

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
    val refreshStatus by homeViewModel.refreshStatus.collectAsState()
    val lastUpdateRaw by homeViewModel.lastUpdateRaw.collectAsState()
    val allSchedules by homeViewModel.allSchedules.collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showHistoryList by remember { mutableStateOf(false) }
    var showSearchOverlay by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (token.isNotEmpty()) homeViewModel.refreshSchedules(token, isBackground = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        homeViewModel.message.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { homeViewModel.performAutoScheduleFromImage(token, it) }
    }

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

    LaunchedEffect(selectedDate) {
        if (token.isNotEmpty() && selectedDate != null) {
            homeViewModel.refreshSchedules(token, isBackground = true)
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

    val pullToRefreshState = rememberPullToRefreshState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd), 0f, 1000f))) {
            // --- Header (Search Bar) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Scan */ }) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Surface(
                    modifier = Modifier.weight(1f).height(42.dp).clickable { showSearchOverlay = true },
                    shape = RoundedCornerShape(21.dp),
                    color = Color.White
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("输入日程标题搜索", color = Color.Gray, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.2f), CircleShape).clickable { onProfileClick() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
            }

            // --- Action Items ---
            Row(modifier = Modifier.fillMaxWidth().padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                ActionItem(Icons.Default.AddBox, "快速添加") { 
                    galleryLauncher.launch("image/*") 
                }
                ActionItem(Icons.Default.DashboardCustomize, "悬浮窗") {
                    if (Settings.canDrawOverlays(context)) {
                        context.startService(Intent(context, FloatingService::class.java).apply { putExtra(FloatingService.EXTRA_TOKEN, token) })
                    } else {
                        overlayPermissionLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri()))
                    }
                }
                ActionItem(Icons.Default.Groups, "群组共享") { onGroupClick() }
                ActionItem(Icons.Default.Apps, "更多") { onMoreClick() }
            }

            // --- Main Content (Calendar) ---
            Surface(modifier = Modifier.fillMaxSize(), color = ContainerGrey, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) {
                PullToRefreshBox(
                    isRefreshing = refreshStatus == RefreshStatus.REFRESHING || refreshStatus == RefreshStatus.COMPLETE,
                    onRefresh = { homeViewModel.refreshSchedules(token) },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize(),
                    indicator = {}
                ) {
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        val progress = pullToRefreshState.distanceFraction
                        Box(
                            modifier = Modifier.fillMaxWidth().height(if (progress > 0f || refreshStatus != RefreshStatus.IDLE) (progress * 80).dp else 0.dp).graphicsLayer { translationY = (progress * 40f).coerceAtMost(40f) - 40f; alpha = progress.coerceIn(0f, 1f) },
                            contentAlignment = Alignment.Center
                        ) { RefreshStatusHeader(refreshStatus, progress, lastUpdateRaw) }
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { CalendarHeader(currentMonth = displayMonth, onMenuClick = { showHistoryList = true }, onAddClick = { onAddSchedule(selectedDate ?: LocalDate.now()) }) }
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            val config = LocalConfiguration.current
                            val contentWidth = config.screenWidthDp.dp - 32.dp
                            val daySize = (contentWidth - 24.dp) / 7
                            val dynamicCalendarHeight = (daySize * interpolatedRowCount.coerceAtLeast(1f)) + 20.dp + 32.dp + 12.dp
                            Box(modifier = Modifier.fillMaxWidth().height(dynamicCalendarHeight)) {
                                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Top, beyondViewportPageCount = 0, pageSpacing = 16.dp) { page ->
                                    val month = initialMonth.plusMonths((page - initialPage).toLong())
                                    val data = monthDataMap[month] ?: emptyList()
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                                        Surface(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 0.5.dp) {
                                            if (data.isNotEmpty()) { LiveCalendar(data, holidayMap, selectedDate) { selectedDate = it } }
                                            else { Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = CalendarSelectBlue, modifier = Modifier.size(24.dp)) } }
                                        }
                                    }
                                }
                            }
                            selectedDate?.let { ScheduleSection(selectedDate = it, homeViewModel = homeViewModel, onEditSchedule = onEditSchedule, onJumpClick = { showWheelPicker = true }, onTodayClick = { scope.launch { pagerState.animateScrollToPage(initialPage); selectedDate = LocalDate.now() } }) }
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        // --- Search Overlay ---
        AnimatedVisibility(visible = showSearchOverlay, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            BackHandler { showSearchOverlay = false; searchQuery = "" }
            Surface(modifier = Modifier.fillMaxSize(), color = ContainerGrey) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(22.dp), color = Color.White, border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.3f))) {
                            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, null, tint = BrandOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                BasicTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.weight(1f).focusRequester(searchFocusRequester), textStyle = TextStyle(fontSize = 15.sp, color = TextTitle), singleLine = true, decorationBox = { if (searchQuery.isEmpty()) Text("搜索日程", color = Color.Gray.copy(alpha = 0.5f), fontSize = 15.sp); it() })
                                if (searchQuery.isNotEmpty()) { IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) { Icon(Icons.Default.Close, null, tint = Color.Gray) } }
                            }
                        }
                        TextButton(onClick = { showSearchOverlay = false; searchQuery = "" }) { Text("取消", color = Color.Gray) }
                    }
                    val filteredSchedules = remember(searchQuery, allSchedules) { if (searchQuery.isBlank()) emptyList() else allSchedules.filter { it.title.contains(searchQuery, ignoreCase = true) } }
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredSchedules) { schedule -> SearchScheduleResultItem(schedule = schedule, onClick = { showSearchOverlay = false; searchQuery = ""; onEditSchedule(schedule) }) }
                        if (searchQuery.isNotEmpty() && filteredSchedules.isEmpty()) { item { Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) { Text("未找到相关日程", color = Color.Gray) } } }
                    }
                }
            }
            LaunchedEffect(showSearchOverlay) { if (showSearchOverlay) searchFocusRequester.requestFocus() }
        }

        // --- History Overlay ---
        AnimatedVisibility(visible = showHistoryList, enter = slideInHorizontally(initialOffsetX = { -it }), exit = slideOutHorizontally(targetOffsetX = { -it })) {
            Surface(modifier = Modifier.fillMaxSize(), color = ContainerGrey) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().height(100.dp).background(Brush.horizontalGradient(listOf(BgGradientStart, BgGradientEnd))).statusBarsPadding().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showHistoryList = false }) { Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                        Text("添加日程记录", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    }
                    val historySchedules = remember(allSchedules) { allSchedules.filter { it.isAiGenerated }.sortedByDescending { it.id ?: 0L } }
                    if (historySchedules.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无自动添加记录", color = Color.Gray) } }
                    else { LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(historySchedules) { schedule -> HistoryScheduleCard(schedule = schedule, isUnviewed = !schedule.isViewed, onClick = { homeViewModel.markAsViewed(token, schedule); onEditSchedule(schedule) }) } } }
                }
            }
        }

        // --- Wheel Picker (Optimized Animation) ---
        AnimatedVisibility(
            visible = showWheelPicker,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Background layer
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showWheelPicker = false }
                )
                // Content layer with combined animations
                Surface(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .animateEnterExit(
                            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)),
                            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400))
                        ),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = Color.White
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
            BackHandler { showWheelPicker = false }
        }

        if (isLoading) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) } }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun RefreshStatusHeader(status: RefreshStatus, progress: Float, lastUpdate: LocalDateTime?) {
    Column(modifier = Modifier.fillMaxWidth().animateContentSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (status) {
                RefreshStatus.REFRESHING -> { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Gray); Spacer(modifier = Modifier.width(8.dp)); Text("正在刷新...", fontSize = 14.sp, color = Color.Gray) }
                RefreshStatus.COMPLETE -> { Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF52C41A), modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("刷新完成", fontSize = 14.sp, color = Color.Gray) }
                else -> { Text(text = if (progress >= 1f) "释放立即刷新" else "下拉刷新", fontSize = 14.sp, color = Color.Gray) }
            }
        }
        if (lastUpdate != null) { val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); Text(text = "上次更新 ${lastUpdate.format(formatter)}", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.padding(top = 2.dp)) }
    }
}

@Composable
fun CalendarHeader(currentMonth: YearMonth, onMenuClick: () -> Unit = {}, onAddClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMenuClick, modifier = Modifier.size(26.dp)) { Icon(Icons.Default.Menu, null, tint = Color.Black) }
        Spacer(modifier = Modifier.weight(1f))
        Text(currentMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onAddClick, modifier = Modifier.size(26.dp)) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black) }
    }
}

@Composable
fun LiveCalendar(monthData: List<DayDisplayInfo>, holidayMap: Map<LocalDate, Holiday>, selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp).fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) { listOf("日", "一", "二", "三", "四", "五", "六").forEach { Box(modifier = Modifier.weight(1f).height(20.dp), contentAlignment = Alignment.Center) { Text(it, fontSize = 12.sp, color = Color(0xFF999999), fontWeight = FontWeight.Medium) } } }
        Spacer(modifier = Modifier.height(12.dp))
        monthData.chunked(7).forEach { week -> if (week.any { it.isCurrentMonth }) { Row(modifier = Modifier.fillMaxWidth()) { week.forEach { info -> val infoWithHoliday = info.copy(holiday = holidayMap[info.date]); Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) { CalendarDay(infoWithHoliday, info.date == selectedDate, onDateSelected) } } } } }
    }
}

@Composable
fun CalendarDay(info: DayDisplayInfo, isSelected: Boolean, onDateSelected: (LocalDate) -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = info.isCurrentMonth) { onDateSelected(info.date) }) {
        if (isSelected && info.isCurrentMonth) { if (info.isToday) Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = CalendarSelectBlue) {} else Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, border = BorderStroke(1.5.dp, CalendarSelectBlue), color = Color.Transparent) {} }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val textColor = when { isSelected && info.isCurrentMonth && info.isToday -> Color.White; info.isToday && info.isCurrentMonth -> CalendarSelectBlue; !info.isCurrentMonth -> Color.LightGray; else -> Color(0xFF333333) }
            Text(text = info.date.dayOfMonth.toString(), fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)), color = textColor); Spacer(modifier = Modifier.height(2.dp)); Text(text = info.subText, fontSize = 10.sp, maxLines = 1, lineHeight = 10.sp, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)), color = if (textColor == Color.White) Color.White else (if (info.isToday && !isSelected) CalendarSelectBlue else Color(0xFF999999)))
        }
        if (info.isCurrentMonth && info.holiday != null) { Text(text = if (info.holiday.isRest) "休" else "班", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSelected && info.isToday) Color.White else (if (info.holiday.isRest) RestBlue else WorkRed), modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp)) }
    }
}

@Composable
fun SearchScheduleResultItem(schedule: Schedule, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = Color.White) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text(text = schedule.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextTitle); Spacer(modifier = Modifier.height(4.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text(text = schedule.date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")), fontSize = 13.sp, color = Color.Gray); Spacer(modifier = Modifier.width(8.dp)); Text(text = if (schedule.isAllDay) "全天" else schedule.time.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 13.sp, color = BrandOrange, fontWeight = FontWeight.Medium) } }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun HistoryScheduleCard(schedule: Schedule, isUnviewed: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 0.5.dp) {
        Box(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Text(text = schedule.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333)); Spacer(modifier = Modifier.height(4.dp)); Text(text = "${schedule.date.format(DateTimeFormatter.ofPattern("MM月dd日"))} ${if (schedule.isAllDay) "全天" else schedule.time.format(DateTimeFormatter.ofPattern("HH:mm"))}", fontSize = 13.sp, color = Color.Gray) }; Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray) }
            if (isUnviewed) { Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WorkRed).align(Alignment.TopEnd)) }
        }
    }
}

@Composable
fun ScheduleSection(selectedDate: LocalDate, homeViewModel: HomeViewModel, onEditSchedule: (Schedule) -> Unit, onJumpClick: () -> Unit, onTodayClick: () -> Unit) {
    val allSchedules by homeViewModel.allSchedules.collectAsState()
    val schedules = remember(allSchedules, selectedDate) { allSchedules.filter { it.date == selectedDate }.sortedBy { if(it.isAllDay) LocalTime.MIN else it.time } }
    val solar = remember(selectedDate) { Solar.fromYmd(selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth) }
    val lunar = remember(solar) { solar.lunar }
    val dateStr = when (val diff = ChronoUnit.DAYS.between(LocalDate.now(), selectedDate)) { 0L -> "今天"; 1L -> "明天"; 2L -> "后天"; -1L -> "昨天"; -2L -> "前天"; else -> if (diff > 0) "${diff}天后" else "${-diff}天前" }.let { prefix -> val monthInt = abs(lunar.month); val chineseMonthDigits = when(monthInt) { 1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; 6 -> "六"; 7 -> "七"; 8 -> "八"; 9 -> "九"; 10 -> "十"; 11 -> "十一"; 12 -> "十二"; else -> "" }; val alias = when(monthInt) { 1 -> "(正月)"; 11 -> "(冬月)"; 12 -> "(腊月)"; else -> "" }; "$prefix 农历${chineseMonthDigits}月${alias}${lunar.dayInChinese}" }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp, end = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text(text = dateStr, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF666666), modifier = Modifier.weight(1f)); Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onJumpClick, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.CalendarMonth, null, tint = BrandOrange, modifier = Modifier.size(20.dp)) }; Spacer(modifier = Modifier.width(8.dp)); Surface(modifier = Modifier.size(28.dp).clickable { onTodayClick() }, shape = CircleShape, color = BrandOrange.copy(alpha = 0.1f), border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.2f))) { Box(contentAlignment = Alignment.Center) { Text("今", color = BrandOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold) } } } }
        if (schedules.isEmpty()) { Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) { Text("暂无日程", color = Color.Gray, fontSize = 14.sp) } }
        else { schedules.forEach { schedule -> ScheduleCard(schedule, onClick = { onEditSchedule(schedule) }); Spacer(modifier = Modifier.height(10.dp)) } }
    }
}

@Composable
fun ScheduleCard(item: Schedule, onClick: () -> Unit = {}) {
    val normalGradient = Brush.verticalGradient(listOf(BrandOrange, BrandOrange.copy(alpha = 0.6f))); val urgentGradient = Brush.verticalGradient(listOf(ImportantRed, ImportantRed.copy(alpha = 0.6f))); val barGradient = if (item.isImportant) urgentGradient else normalGradient; val accentColor = if (item.isImportant) ImportantRed else BrandOrange; Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, border = BorderStroke(0.5.dp, Color(0xFFEEEEEE))) { Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.width(4.dp).fillMaxHeight().clip(CircleShape).background(barGradient)); Spacer(modifier = Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) { Icon(Icons.AutoMirrored.Filled.EventNote, null, tint = accentColor, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text(text = item.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextTitle, maxLines = 1) }; Text(text = if (item.isAllDay) "全天" else item.time.format(DateTimeFormatter.ofPattern("HH:mm")), color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold) }; Spacer(modifier = Modifier.height(6.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = Color.LightGray, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(text = if (item.location.isNullOrEmpty()) "待定" else item.location, color = Color.Gray, fontSize = 13.sp, maxLines = 1); Spacer(modifier = Modifier.width(16.dp)); Icon(Icons.AutoMirrored.Outlined.Assignment, null, tint = Color.LightGray, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(text = item.belonging, color = Color.Gray, fontSize = 13.sp, maxLines = 1) } } } }
}

@Composable
fun ActionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)) { Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(32.dp)); Spacer(modifier = Modifier.height(8.dp)); Text(text = label, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium) }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() { HomeScreen(token = "") }
