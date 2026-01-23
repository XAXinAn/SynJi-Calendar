package com.example.synji_calendar.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    initialDate: LocalDate = LocalDate.now(),
    onBack: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }
    var isAllDay by remember { mutableStateOf(false) }
    
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedBelonging by remember { mutableStateOf("个人") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isSelectingBelonging by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize()) {
        // 主内容页面
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "新建日程",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTitle
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text("取消", color = Color.Gray, fontSize = 16.sp)
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            if (title.isNotBlank()) {
                                homeViewModel.addSchedule(
                                    Schedule(
                                        title = title,
                                        date = selectedDate,
                                        time = if (isAllDay) LocalTime.MIN else selectedTime,
                                        isAllDay = isAllDay,
                                        location = location,
                                        belonging = selectedBelonging,
                                        isImportant = isImportant
                                    )
                                )
                                onBack()
                            }
                        }) {
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
                        // Title Input
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("请输入日程标题", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextTitle)
                        )

                        AddScheduleDivider()

                        // All Day Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AddIconWithBg(Icons.Default.AccessTimeFilled)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("全天", fontSize = 15.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = isAllDay,
                                onCheckedChange = { isAllDay = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CalendarSelectBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                        AddScheduleDivider()

                        // Date
                        AddEditRow(
                            Icons.Default.CalendarToday, 
                            "日期", 
                            selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            onClick = { showDatePicker = true }
                        )
                        AddScheduleDivider()
                        
                        // Time (Only show if not All Day)
                        if (!isAllDay) {
                            AddEditRow(
                                Icons.Default.AccessTime, 
                                "时间", 
                                selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                onClick = { showTimePicker = true }
                            )
                            AddScheduleDivider()
                        }
                        
                        // Location Input
                        Row(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AddIconWithBg(Icons.Default.LocationOn)
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
                                    textStyle = TextStyle(
                                        fontSize = 15.sp, 
                                        textAlign = TextAlign.End,
                                        color = TextTitle
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(CalendarSelectBlue)
                                )
                            }
                        }
                        AddScheduleDivider()
                        
                        AddEditRow(
                            Icons.Default.Groups, 
                            "归属", 
                            selectedBelonging,
                            onClick = { isSelectingBelonging = true }
                        )
                        AddScheduleDivider()
                        
                        // Important Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AddIconWithBg(Icons.Default.Star, if (isImportant) Color(0xFFFFB300) else IconColor.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("重要", fontSize = 15.sp, color = Color.Gray)
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
                }
            }
        }

        // 归属选择新界面 (Overlay)
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
                onBack = { isSelectingBelonging = false }
            )
        }
    }

    // 日期选择器
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

    // 时间选择器
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BelongingSelectionScreen(
    currentBelonging: String,
    onSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allOptions = listOf("个人", "5群", "工作", "生活")
    val filteredOptions = allOptions.filter { it.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("选择归属", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronLeft, "Back", modifier = Modifier.size(32.dp), tint = IconColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = ContainerGrey
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 15.sp, color = TextTitle),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("搜索归属选项", color = Color.LightGray, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Options List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredOptions) { option ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        onClick = { onSelected(option) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(CalendarSelectBlue.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = CalendarSelectBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextTitle,
                                modifier = Modifier.weight(1f)
                            )
                            if (option == currentBelonging) {
                                Icon(Icons.Default.Check, null, tint = CalendarSelectBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 时间选择器内容 ---
@Composable
fun WheelTimePickerContent(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onCancel: () -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableIntStateOf(initialTime.minute) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text("取消", color = Color.Gray, fontSize = 16.sp) }
            Text("选择时间", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            TextButton(onClick = { onConfirm(LocalTime.of(selectedHour, selectedMinute)) }) {
                Text("确定", color = CalendarSelectBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WheelPicker(
                items = (0..23).toList(),
                initialItem = selectedHour,
                onItemSelected = { selectedHour = it },
                modifier = Modifier.weight(1f),
                label = "时"
            )
            WheelPicker(
                items = (0..59).toList(),
                initialItem = selectedMinute,
                onItemSelected = { selectedMinute = it },
                modifier = Modifier.weight(1f),
                label = "分"
            )
        }
    }
}

@Composable
private fun AddScheduleDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        thickness = 0.5.dp,
        color = Color(0xFFF0F0F0)
    )
}

@Composable
private fun AddIconWithBg(icon: ImageVector, tint: Color = IconColor.copy(alpha = 0.6f)) {
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
private fun AddEditRow(icon: ImageVector, label: String, value: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AddIconWithBg(icon)
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

@Preview(showBackground = true)
@Composable
fun AddSchedulePreview() {
    AddScheduleScreen()
}
