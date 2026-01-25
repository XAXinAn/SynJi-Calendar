package com.example.synji_calendar.ui.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synji_calendar.ui.home.BgGradientEnd
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.home.ContainerGrey
import com.example.synji_calendar.ui.home.TextTitle
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialogContent(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(0) }
    
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown -= 1
        }
    }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            authViewModel.clearError()
        }
    }

    // 登录框主体
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight()
            .clickable(enabled = false) { }, // 防止点击穿透
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(brush = Brush.horizontalGradient(listOf(BgGradientStart, BgGradientEnd)))
                    .padding(horizontal = 16.dp)
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ChevronLeft, "Back", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(32.dp), shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.9f)) {
                        Box(contentAlignment = Alignment.Center) { Text("SJ", color = BgGradientStart, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("讯极日历", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("SYN JI CALENDAR", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Phone Input
                Surface(modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), color = ContainerGrey, border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("+86", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextTitle)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.LightGray))
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = phoneNumber, onValueChange = { phoneNumber = it }, modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, color = TextTitle),
                            decorationBox = { if (phoneNumber.isEmpty()) Text("请输入手机号", color = Color.Gray.copy(alpha = 0.4f), fontSize = 16.sp); it() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Code Input
                Surface(modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), color = ContainerGrey, border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, end = 6.dp)) {
                        BasicTextField(
                            value = verificationCode, onValueChange = { verificationCode = it }, modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontSize = 16.sp, color = TextTitle),
                            decorationBox = { if (verificationCode.isEmpty()) Text("请输入验证码", color = Color.Gray.copy(alpha = 0.4f), fontSize = 16.sp); it() }
                        )
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.width(100.dp)) {
                            if (uiState.isLoading && verificationCode.isEmpty()) {
                                CircularProgressIndicator(color = BgGradientStart, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                TextButton(
                                    onClick = { if (phoneNumber.length == 11) { authViewModel.sendVerificationCode(phoneNumber); countdown = 60 } else { Toast.makeText(context, "请输入正确的手机号", Toast.LENGTH_SHORT).show() } },
                                    enabled = countdown == 0, colors = ButtonDefaults.textButtonColors(contentColor = BgGradientStart, disabledContentColor = Color.Gray)
                                ) { Text(text = if (countdown > 0) "${countdown}s" else "获取验证码", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Agreement
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                        Checkbox(checked = isAgreed, onCheckedChange = { isAgreed = it }, colors = CheckboxDefaults.colors(checkedColor = BgGradientStart), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("我已阅读并同意", fontSize = 12.sp, color = Color.Gray)
                    Text("《用户协议》", fontSize = 12.sp, color = BgGradientStart, modifier = Modifier.clickable { })
                    Text("《隐私政策》", fontSize = 12.sp, color = BgGradientStart, modifier = Modifier.clickable { })
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp))
                        .background(brush = Brush.horizontalGradient(if (isAgreed && phoneNumber.isNotEmpty() && verificationCode.isNotEmpty()) listOf(BgGradientStart, BgGradientEnd) else listOf(Color.LightGray, Color.LightGray)))
                        .clickable(enabled = isAgreed && phoneNumber.isNotEmpty() && verificationCode.isNotEmpty() && !uiState.isLoading) { authViewModel.login(phoneNumber, verificationCode) },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading && verificationCode.isNotEmpty()) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("登录/注册", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
