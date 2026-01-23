package com.example.synji_calendar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

// --- 提取自各页面的公共归属选择界面 ---
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 15.sp, color = TextTitle),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) Text("搜索归属选项", color = Color.LightGray, fontSize = 15.sp)
                            innerTextField()
                        }
                    )
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(filteredOptions) { option ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        onClick = { onSelected(option) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(CalendarSelectBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = CalendarSelectBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = option, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextTitle, modifier = Modifier.weight(1f))
                            if (option == currentBelonging) Icon(Icons.Default.Check, null, tint = CalendarSelectBlue)
                        }
                    }
                }
            }
        }
    }
}

// --- 提取自各页面的滚轮选择器基础组件 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> WheelPicker(
    items: List<T>,
    initialItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val itemHeight = 44.dp
    val visibleItems = 5
    val startIndex = items.indexOf(initialItem).coerceAtLeast(0)
    val state = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)

    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            val centerIndex = state.firstVisibleItemIndex
            if (centerIndex in items.indices) onItemSelected(items[centerIndex])
        }
    }

    Box(modifier = modifier.height(itemHeight * visibleItems), contentAlignment = Alignment.Center) {
        HorizontalDivider(modifier = Modifier.offset(y = -itemHeight/2).fillMaxWidth(0.85f), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
        HorizontalDivider(modifier = Modifier.offset(y = itemHeight/2).fillMaxWidth(0.85f), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
        if (label.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = label, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.offset(x = if(label == "年") 48.dp else 28.dp))
            }
        }
        LazyColumn(state = state, flingBehavior = flingBehavior, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = itemHeight * 2)) {
            items(items.size) { index ->
                val item = items[index]
                val isSelected = remember { derivedStateOf { state.firstVisibleItemIndex == index } }
                Box(modifier = Modifier.fillMaxWidth().height(itemHeight), contentAlignment = Alignment.Center) {
                    Text(
                        text = item.toString(),
                        fontSize = if (isSelected.value) 22.sp else 18.sp,
                        fontWeight = if (isSelected.value) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected.value) Color.Black else Color.LightGray.copy(alpha = 0.8f),
                        modifier = Modifier.offset(x = if(label.isNotEmpty()) (-12).dp else 0.dp)
                    )
                }
            }
        }
    }
}
