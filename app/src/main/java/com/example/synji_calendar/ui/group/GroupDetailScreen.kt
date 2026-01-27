package com.example.synji_calendar.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

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
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // 群组信息卡片
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

                Spacer(modifier = Modifier.height(12.dp))

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
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BgGradientStart)
            }
        }
    }
}
