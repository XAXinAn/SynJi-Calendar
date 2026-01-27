package com.example.synji_calendar.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.synji_calendar.ui.home.BgGradientEnd
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.home.ContainerGrey
import com.example.synji_calendar.ui.home.IconColor

@Composable
fun MemberDetailScreen(
    member: GroupMember,
    groupId: String,
    isOwner: Boolean,
    token: String,
    viewModel: GroupViewModel,
    onBack: () -> Unit
) {
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
                    "成员资料",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 成员资料列表 (仿个人信息风格)
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            // 头像
            MemberInfoItem(label = "头像") {
                Surface(
                    modifier = Modifier.size(60.dp), 
                    shape = RoundedCornerShape(8.dp), 
                    color = IconColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = IconColor, modifier = Modifier.size(40.dp))
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = ContainerGrey, thickness = 0.5.dp)
            
            MemberInfoItem(label = "名字", value = member.nickname)
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = ContainerGrey, thickness = 0.5.dp)
            
            MemberInfoItem(label = "手机号", value = member.phoneNumber)

            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = ContainerGrey, thickness = 0.5.dp)

            MemberInfoItem(
                label = "角色", 
                value = when(member.role) {
                    "OWNER" -> "群主"
                    "ADMIN" -> "管理员"
                    else -> "普通成员"
                }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = ContainerGrey, thickness = 0.5.dp)

            MemberInfoItem(label = "加入时间", value = member.joinedAt ?: "未知")
        }

        Spacer(modifier = Modifier.weight(1f))

        // 管理操作按钮 (仅群主可见，且不能对自己操作)
        if (isOwner && member.role != "OWNER") {
            Button(
                onClick = { 
                    viewModel.toggleAdmin(token, groupId, member.userId, member.role == "ADMIN")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, 
                    contentColor = if (member.role == "ADMIN") Color.Red else BgGradientStart
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    if (member.role == "ADMIN") "解除管理员" else "设为管理员",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (member.role == "MEMBER") {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "管理员上限为2人",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MemberInfoItem(
    label: String,
    value: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, color = Color(0xFF333333))
        Spacer(modifier = Modifier.weight(1f))
        
        if (content != null) {
            content()
        } else if (value != null) {
            Text(value, fontSize = 16.sp, color = Color.Gray)
        }
    }
}
