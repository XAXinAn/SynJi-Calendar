package com.example.synji_calendar.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.synji_calendar.ui.auth.UserInfo
import com.example.synji_calendar.ui.home.BgGradientEnd
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.home.ContainerGrey
import com.example.synji_calendar.ui.home.IconColor

@Composable
fun ProfileScreen(
    user: UserInfo?,
    onBack: () -> Unit,
    onEditField: (field: String, currentValue: String) -> Unit,
    onLogout: () -> Unit
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
                    "个人信息",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 个人资料列表
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            // 头像 (微信风格，通常点击可看大图或更换，这里暂时只展示)
            ProfileItem(label = "头像", showChevron = true, onClick = { /* TODO: 更换头像 */ }) {
                Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(8.dp), color = IconColor.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = IconColor, modifier = Modifier.size(40.dp))
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = ContainerGrey, thickness = 0.5.dp)
            
            ProfileItem(label = "名字", value = user?.nickname ?: "未设置", onClick = {
                onEditField("nickname", user?.nickname ?: "")
            })
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = ContainerGrey, thickness = 0.5.dp)
            
            ProfileItem(label = "账号", value = user?.phoneNumber ?: "", showChevron = false)
        }

        Spacer(modifier = Modifier.weight(1f))

        // 退出登录按钮
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Red),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("退出登录", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileItem(
    label: String,
    value: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit = {},
    content: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != {}) { onClick() }
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
        
        if (showChevron) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}
