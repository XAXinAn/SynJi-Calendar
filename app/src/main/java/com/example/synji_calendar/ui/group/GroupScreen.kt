package com.example.synji_calendar.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synji_calendar.ui.home.BgGradientEnd
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.home.ContainerGrey
import com.example.synji_calendar.ui.home.IconColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    token: String,
    viewModel: GroupViewModel = viewModel(),
    onBack: () -> Unit,
    onGroupClick: (GroupInfo) -> Unit,
    onCreateGroup: () -> Unit,
    onJoinGroup: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 初始加载
    LaunchedEffect(token) {
        if (token.isNotEmpty()) viewModel.loadGroups(token)
    }

    // 根据名称首字母分组
    val groupedItems = remember(uiState.groups) {
        uiState.groups.groupBy { 
            it.name.firstOrNull()?.uppercaseChar() ?: '#' 
        }.toSortedMap()
    }

    Column(modifier = Modifier.fillMaxSize().background(ContainerGrey)) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Brush.horizontalGradient(listOf(BgGradientStart, BgGradientEnd)))
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White)
                }
                Text(
                    "群组列表",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // 搜索框
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("搜索群组", color = Color.Gray, fontSize = 15.sp)
                        }
                    }
                }

                // 功能选项
                item {
                    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                        GroupActionRow(Icons.Default.Add, "创建群组", BgGradientStart, onClick = onCreateGroup)
                        Divider(modifier = Modifier.padding(horizontal = 60.dp), color = ContainerGrey, thickness = 0.5.dp)
                        GroupActionRow(Icons.Default.GroupAdd, "加入群组", Color(0xFF52C41A), onClick = onJoinGroup)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.groups.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("暂无加入的群组", color = Color.Gray)
                        }
                    }
                }

                // 真实群组列表
                groupedItems.forEach { (initial, groups) ->
                    item {
                        Text(
                            text = initial.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ContainerGrey)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(groups) { group ->
                        GroupListItem(group) { onGroupClick(group) }
                        Divider(modifier = Modifier.padding(horizontal = 68.dp), color = ContainerGrey, thickness = 0.5.dp)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (uiState.groups.isNotEmpty()) {
                            Text("${uiState.groups.size}个群组", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BgGradientStart
                )
            }
        }
    }
}

@Composable
fun GroupActionRow(icon: ImageVector, label: String, iconBg: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(8.dp),
            color = iconBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
    }
}

@Composable
fun GroupListItem(group: GroupInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            color = IconColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Groups, null, tint = IconColor, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(group.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
    }
}
