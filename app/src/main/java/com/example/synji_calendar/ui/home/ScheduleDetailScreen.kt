package com.example.synji_calendar.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synji_calendar.ui.group.GroupViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    token: String,
    schedule: Schedule,
    onBack: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    // 关键修复点：一进入详情页，立即触发标记已读逻辑
    LaunchedEffect(Unit) {
        if (!schedule.isViewed) {
            homeViewModel.markAsViewed(token, schedule)
        }
        if (token.isNotEmpty()) {
            groupViewModel.loadGroups(token)
        }
    }

    var title by remember { mutableStateOf(schedule.title) }
    var location by remember { mutableStateOf(schedule.location ?: "") }
    var notes by remember { mutableStateOf(schedule.notes ?: "") }
    var isImportant by remember { mutableStateOf(schedule.isImportant) }
    var isAllDay by remember { mutableStateOf(schedule.isAllDay) }
    
    var selectedDate by remember { mutableStateOf(schedule.date) }
    var selectedTime by remember { mutableStateOf(schedule.time) }
    var selectedBelonging by remember { mutableStateOf(schedule.belonging ?: "个人") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isSelectingBelonging by remember { mutableStateOf(false) }
    
    val isLoading by homeViewModel.isLoading.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(BgGradientStart, BgGradientEnd))).statusBarsPadding()) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "日程详情",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack, enabled = !isLoading) {
                                Icon(Icons.Default.ChevronLeft, "Back", modifier = Modifier.size(32.dp), tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(
                                enabled = !isLoading,
                                onClick = {
                                    schedule.id?.let { id ->
                                        homeViewModel.deleteSchedule(token, id, onComplete = onBack)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.DeleteOutline, "Delete", tint = Color.White.copy(alpha = 0.8f))
                            }
                            Button(
                                enabled = !isLoading && title.isNotBlank(),
                                onClick = {
                                    homeViewModel.updateSchedule(
                                        token = token,
                                        updatedSchedule = schedule.copy(
                                            title = title,
                                            date = selectedDate,
                                            time = if (isAllDay) LocalTime.MIN else selectedTime,
                                            isAllDay = isAllDay,
                                            location = location,
                                            belonging = selectedBelonging,
                                            isImportant = isImportant,
                                            notes = notes,
                                            isViewed = true // 更新时强制已读
                                        ),
                                        onComplete = onBack
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = BgGradientStart,
                                    disabledContainerColor = Color.White.copy(alpha = 0.5f),
                                    disabledContentColor = BgGradientStart.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier.padding(end = 8.dp).height(32.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BgGradientStart)
                                } else {
                                    Text("完成", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            containerColor = ContainerGrey
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
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
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("请输入日程标题", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = BgGradientStart,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextTitle)
                        )

                        DetailDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DetailIconWithBg(Icons.Default.AccessTimeFilled)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("全天", fontSize = 15.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = isAllDay,
                                onCheckedChange = { isAllDay = it },
                                enabled = !isLoading,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BgGradientStart,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                        DetailDivider()

                        DetailEditRow(
                            Icons.Default.CalendarToday, 
                            "日期", 
                            selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            onClick = { if (!isLoading) showDatePicker = true }
                        )
                        DetailDivider()
                        
                        if (!isAllDay) {
                            DetailEditRow(
                                Icons.Default.AccessTime, 
                                "时间", 
                                selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                onClick = { if (!isLoading) showTimePicker = true }
                            )
                            DetailDivider()
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DetailIconWithBg(Icons.Default.LocationOn)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("地点", fontSize = 15.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                                if (location.isEmpty()) {
                                    Text("未设置地点", color = Color.LightGray, fontSize = 15.sp)
                                }
                                BasicTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isLoading,
                                    textStyle = TextStyle(
                                        fontSize = 15.sp, 
                                        textAlign = TextAlign.End,
                                        color = TextTitle
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(BgGradientStart)
                                )
                            }
                        }
                        DetailDivider()
                        
                        DetailEditRow(
                            Icons.Default.Groups, 
                            "归属", 
                            selectedBelonging,
                            onClick = { if (!isLoading) isSelectingBelonging = true }
                        )
                        DetailDivider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DetailIconWithBg(Icons.Default.Star, if (isImportant) Color(0xFFFFB300) else IconColor.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("重要", fontSize = 15.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = isImportant,
                                onCheckedChange = { isImportant = it },
                                enabled = !isLoading,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BgGradientStart,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 备注卡片
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 0.5.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DetailIconWithBg(Icons.AutoMirrored.Filled.Notes)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("备注", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextTitle)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("无备注内容", color = Color.LightGray, fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            enabled = !isLoading,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = BgGradientStart,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontSize = 15.sp, color = TextTitle, lineHeight = 22.sp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isSelectingBelonging,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
        ) {
            BelongingSelectionScreen(
                currentBelonging = selectedBelonging,
                onSelected = {
                    selectedBelonging = it
                    isSelectingBelonging = false
                },
                onBack = { isSelectingBelonging = false },
                groupViewModel = groupViewModel
            )
        }
    }

    if (showDatePicker) {
        ModalBottomSheet(
            onDismissRequest = { showDatePicker = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = null
        ) {
            WheelDatePickerContent(
                initialDate = selectedDate,
                onConfirm = { date ->
                    selectedDate = date
                    showDatePicker = false
                },
                onCancel = { showDatePicker = false }
            )
        }
    }

    if (showTimePicker) {
        ModalBottomSheet(
            onDismissRequest = { showTimePicker = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            dragHandle = null
        ) {
            WheelTimePickerContent(
                initialTime = selectedTime,
                onConfirm = { time ->
                    selectedTime = time
                    showTimePicker = false
                },
                onCancel = { showTimePicker = false }
            )
        }
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        thickness = 0.5.dp,
        color = Color(0xFFF0F0F0)
    )
}

@Composable
private fun DetailIconWithBg(icon: ImageVector, tint: Color = IconColor.copy(alpha = 0.6f)) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(ContainerGrey, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun DetailEditRow(icon: ImageVector, label: String, value: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        DetailIconWithBg(icon)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 15.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = if (value.contains("选择")) Color.LightGray else TextTitle,
            textAlign = TextAlign.End
        )
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}
