package com.example.synji_calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.synji_calendar.ui.home.HomeScreen
import com.example.synji_calendar.ui.home.AddScheduleScreen
import com.example.synji_calendar.ui.home.ScheduleDetailScreen
import com.example.synji_calendar.ui.home.Schedule
import com.example.synji_calendar.ui.auth.AuthDialog
import com.example.synji_calendar.ui.theme.SynJiCalendarTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SynJiCalendarTheme {
                var currentScreen by remember { mutableStateOf("home") }
                var targetDate by remember { mutableStateOf(LocalDate.now()) }
                var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
                var showAuthDialog by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState == "add" || targetState == "detail") {
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> fullWidth },
                                    animationSpec = tween(300)
                                ) togetherWith slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                    animationSpec = tween(300)
                                )
                            } else {
                                slideInHorizontally(
                                    initialOffsetX = { fullWidth -> -fullWidth / 3 },
                                    animationSpec = tween(300)
                                ) togetherWith slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> fullWidth },
                                    animationSpec = tween(300)
                                )
                            }
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            "home" -> HomeScreen(
                                onAddSchedule = { date ->
                                    targetDate = date
                                    currentScreen = "add"
                                },
                                onEditSchedule = { schedule ->
                                    editingSchedule = schedule
                                    currentScreen = "detail"
                                }
                            )
                            "add" -> {
                                AddScheduleScreen(
                                    initialDate = targetDate,
                                    onBack = { currentScreen = "home" }
                                )
                                BackHandler {
                                    currentScreen = "home"
                                }
                            }
                            "detail" -> {
                                editingSchedule?.let { schedule ->
                                    ScheduleDetailScreen(
                                        schedule = schedule,
                                        onBack = { currentScreen = "home" }
                                    )
                                }
                                BackHandler {
                                    currentScreen = "home"
                                }
                            }
                        }
                    }

                    // 登录注册弹窗
                    if (showAuthDialog) {
                        AuthDialog(
                            onDismiss = { showAuthDialog = false },
                            onLoginSuccess = { showAuthDialog = false }
                        )
                    }
                }
            }
        }
    }
}
