package com.example.synji_calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.example.synji_calendar.ui.home.HomeScreen
import com.example.synji_calendar.ui.home.AddScheduleScreen
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

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState == "add") {
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
                    }
                }
            }
        }
    }
}
