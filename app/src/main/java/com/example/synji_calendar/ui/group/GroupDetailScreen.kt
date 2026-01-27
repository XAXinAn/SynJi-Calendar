package com.example.synji_calendar.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.synji_calendar.ui.home.BgGradientEnd
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.home.ContainerGrey

@Composable
fun GroupDetailScreen(
    group: GroupInfo?,
    currentUserId: String,
    token: String,
    viewModel: GroupViewModel,
    onBack: () -> Unit,
    onMemberClick: (GroupMember) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()
    
    // 是否拥有管理权限 (群主或管理员)
    val currentUserMember = uiState.members.find { it.userId == currentUserId }
    val hasManagementPrivilege = currentUserMember?.role == "OWNER" || currentUserMember?.role == "ADMIN"

    LaunchedEffect(group?.groupId) {
        group?.groupId?.let {
            viewModel.loadMembers(token, it)
        }
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
                    group?.name ?: "群组详情",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (group != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 群组信息卡片
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("群组名称", fontSize = 14.sp, color = Color.Gray)
                            Text(group.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("邀请码", fontSize = 14.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    group.inviteCode, 
                                    fontSize = 24.sp, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = BgGradientStart,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(onClick = { 
                                    clipboardManager.setText(AnnotatedString(group.inviteCode))
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.ContentCopy, "复制", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text("将此代码发送给好友，邀请他们加入群组", fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                }

                // 成员统计
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("成员数量", fontSize = 16.sp, color = Color(0xFF333333))
                            Text("${group.memberCount} 人", fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                }

                // 成员列表 (仅群主或管理员可见)
                if (hasManagementPrivilege) {
                    item {
                        Text(
                            "成员列表", 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold, 
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            color = Color(0xFF666666)
                        )
                    }

                    val sortedMembers = uiState.members.sortedBy { it.nickname }
                    items(sortedMembers) { member ->
                        MemberItem(member = member) {
                            onMemberClick(member)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BgGradientStart)
            }
        }
    }
}

@Composable
fun MemberItem(member: GroupMember, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ContainerGrey),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color.Gray)
            }
            
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.nickname, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    if (member.role == "OWNER" || member.role == "ADMIN") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Shield, 
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (member.role == "OWNER") Color(0xFFFFA500) else BgGradientStart
                        )
                    }
                }
                Text(member.phoneNumber, fontSize = 12.sp, color = Color.Gray)
            }

            Text(
                when(member.role) {
                    "OWNER" -> "群主"
                    "ADMIN" -> "管理员"
                    else -> ""
                },
                fontSize = 12.sp,
                color = BgGradientStart
            )
        }
    }
}
