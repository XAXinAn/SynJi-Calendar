package com.example.synji_calendar.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.synji_calendar.ui.home.BgGradientEnd
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.home.ContainerGrey
import com.example.synji_calendar.ui.home.TextTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header - 使用软件主题渐变色
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(BgGradientStart, BgGradientEnd)
                            )
                        )
                        .padding(horizontal = 16.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo 区域
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.9f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("SJ", color = BgGradientStart, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("讯极日历", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("SYN JI CALENDAR", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Phone Input
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = ContainerGrey,
                        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text("+86", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextTitle)
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.LightGray))
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 16.sp, color = TextTitle),
                                decorationBox = { innerTextField ->
                                    if (phoneNumber.isEmpty()) {
                                        Text("请输入手机号", color = Color.Gray.copy(alpha = 0.4f), fontSize = 16.sp)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verification Code Input
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = ContainerGrey,
                        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp, end = 6.dp)
                        ) {
                            BasicTextField(
                                value = verificationCode,
                                onValueChange = { verificationCode = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 16.sp, color = TextTitle),
                                decorationBox = { innerTextField ->
                                    if (verificationCode.isEmpty()) {
                                        Text("请输入验证码", color = Color.Gray.copy(alpha = 0.4f), fontSize = 16.sp)
                                    }
                                    innerTextField()
                                }
                            )
                            
                            TextButton(
                                onClick = { /* 发送验证码逻辑 */ },
                                colors = ButtonDefaults.textButtonColors(contentColor = BgGradientStart),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("获取验证码", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Agreement
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                            Checkbox(
                                checked = isAgreed,
                                onCheckedChange = { isAgreed = it },
                                colors = CheckboxDefaults.colors(checkedColor = BgGradientStart),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "我已阅读并同意",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "《用户协议》",
                            fontSize = 12.sp,
                            color = BgGradientStart,
                            modifier = Modifier.clickable { /* 跳转协议 */ }
                        )
                        Text(
                            text = "《隐私政策》",
                            fontSize = 12.sp,
                            color = BgGradientStart,
                            modifier = Modifier.clickable { /* 跳转政策 */ }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // 登录按钮 - 文字改为“登录/注册”
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    if (isAgreed) listOf(BgGradientStart, BgGradientEnd)
                                    else listOf(Color.LightGray, Color.LightGray)
                                )
                            )
                            .clickable(enabled = isAgreed) { onLoginSuccess() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "登录/注册", 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
