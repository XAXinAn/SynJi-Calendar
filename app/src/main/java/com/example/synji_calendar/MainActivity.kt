package com.example.synji_calendar

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synji_calendar.ui.home.HomeScreen
import com.example.synji_calendar.ui.home.AddScheduleScreen
import com.example.synji_calendar.ui.home.ScheduleDetailScreen
import com.example.synji_calendar.ui.home.Schedule
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.auth.AuthDialogContent
import com.example.synji_calendar.ui.auth.AuthViewModel
import com.example.synji_calendar.ui.theme.SynJiCalendarTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SynJiCalendarTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authUiState by authViewModel.uiState.collectAsState()
                
                var currentScreen by remember { mutableStateOf("home") }
                var targetDate by remember { mutableStateOf(LocalDate.now()) }
                var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
                
                val focusManager = LocalFocusManager.current

                val navigateBack: () -> Unit = {
                    focusManager.clearFocus()
                    currentScreen = "home"
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. 主内容层：仅在非 Token 检查期间渲染
                    if (!authUiState.isCheckingToken) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                if (targetState == "add" || targetState == "detail") {
                                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(200, easing = FastOutSlowInEasing)) togetherWith
                                    slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(200, easing = FastOutSlowInEasing))
                                } else {
                                    slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(200, easing = FastOutSlowInEasing)) togetherWith
                                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200, easing = FastOutSlowInEasing))
                                }
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                "home" -> HomeScreen(
                                    token = authUiState.token ?: "",
                                    onAddSchedule = { date -> targetDate = date; currentScreen = "add" },
                                    onEditSchedule = { schedule -> editingSchedule = schedule; currentScreen = "detail" }
                                )
                                "add" -> {
                                    AddScheduleScreen(token = authUiState.token ?: "", initialDate = targetDate, onBack = navigateBack)
                                    BackHandler { navigateBack() }
                                }
                                "detail" -> {
                                    editingSchedule?.let { schedule ->
                                        ScheduleDetailScreen(token = authUiState.token ?: "", schedule = schedule, onBack = navigateBack)
                                    }
                                    BackHandler { navigateBack() }
                                }
                            }
                        }
                    }

                    // 2. 登录注册遮罩层：使用 Crossfade 确保切换稳健，不闪退
                    Crossfade(targetState = !authUiState.isLoggedIn && !authUiState.isCheckingToken, label = "auth_overlay") { showAuth ->
                        if (showAuth) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
                                contentAlignment = Alignment.Center
                            ) {
                                AuthDialogContent(
                                    onDismiss = { },
                                    onLoginSuccess = { /* 状态由 authUiState 驱动 */ },
                                    authViewModel = authViewModel
                                )
                            }
                        }
                    }
                    
                    // 3. 启动时的全屏加载占位
                    if (authUiState.isCheckingToken) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BgGradientStart)
                        }
                    }
                }
            }
        }
    }
}
