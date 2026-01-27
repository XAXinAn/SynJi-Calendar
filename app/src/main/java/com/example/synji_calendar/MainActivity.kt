package com.example.synji_calendar

import android.os.Bundle
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
import com.example.synji_calendar.ui.group.GroupScreen
import com.example.synji_calendar.ui.group.GroupDetailScreen
import com.example.synji_calendar.ui.group.GroupInfo
import com.example.synji_calendar.ui.group.GroupViewModel
import com.example.synji_calendar.ui.group.CreateGroupScreen
import com.example.synji_calendar.ui.group.JoinGroupScreen
import com.example.synji_calendar.ui.profile.ProfileScreen
import com.example.synji_calendar.ui.profile.EditProfileFieldScreen
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
                val groupViewModel: GroupViewModel = viewModel()
                
                var currentScreen by remember { mutableStateOf("home") }
                var targetDate by remember { mutableStateOf(LocalDate.now()) }
                var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
                var selectedGroup by remember { mutableStateOf<GroupInfo?>(null) }
                
                // 个人中心编辑相关状态
                var editingField by remember { mutableStateOf("") }
                var editingFieldValue by remember { mutableStateOf("") }
                
                val focusManager = LocalFocusManager.current

                val navigateBack: () -> Unit = {
                    focusManager.clearFocus()
                    currentScreen = when(currentScreen) {
                        "group_detail" -> "group_list"
                        "group_create" -> "group_list"
                        "group_join" -> "group_list"
                        "group_list" -> "home"
                        "profile" -> "home"
                        "edit_profile" -> "profile"
                        else -> "home"
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. 主内容层
                    if (!authUiState.isCheckingToken) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                if (targetState == "home") {
                                    slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(200, easing = FastOutSlowInEasing)) togetherWith
                                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200, easing = FastOutSlowInEasing))
                                } else {
                                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(200, easing = FastOutSlowInEasing)) togetherWith
                                    slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(200, easing = FastOutSlowInEasing))
                                }
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                "home" -> HomeScreen(
                                    token = authUiState.token ?: "",
                                    onAddSchedule = { date -> targetDate = date; currentScreen = "add" },
                                    onEditSchedule = { schedule -> editingSchedule = schedule; currentScreen = "detail" },
                                    onGroupClick = { currentScreen = "group_list" },
                                    onProfileClick = { currentScreen = "profile" }
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
                                "group_list" -> {
                                    GroupScreen(
                                        token = authUiState.token ?: "",
                                        viewModel = groupViewModel,
                                        onBack = navigateBack,
                                        onGroupClick = { group ->
                                            selectedGroup = group
                                            currentScreen = "group_detail"
                                        },
                                        onCreateGroup = { currentScreen = "group_create" },
                                        onJoinGroup = { currentScreen = "group_join" }
                                    )
                                    BackHandler { navigateBack() }
                                }
                                "group_create" -> {
                                    CreateGroupScreen(
                                        token = authUiState.token ?: "",
                                        viewModel = groupViewModel,
                                        onBack = navigateBack,
                                        onSuccess = navigateBack
                                    )
                                    BackHandler { navigateBack() }
                                }
                                "group_join" -> {
                                    JoinGroupScreen(
                                        token = authUiState.token ?: "",
                                        viewModel = groupViewModel,
                                        onBack = navigateBack,
                                        onSuccess = navigateBack
                                    )
                                    BackHandler { navigateBack() }
                                }
                                "group_detail" -> {
                                    GroupDetailScreen(
                                        group = selectedGroup,
                                        onBack = navigateBack
                                    )
                                    BackHandler { navigateBack() }
                                }
                                "profile" -> {
                                    ProfileScreen(
                                        user = authUiState.user,
                                        onBack = navigateBack,
                                        onEditField = { field, value ->
                                            editingField = field
                                            editingFieldValue = value
                                            currentScreen = "edit_profile"
                                        },
                                        onLogout = {
                                            authViewModel.logout()
                                            currentScreen = "home"
                                        }
                                    )
                                    BackHandler { navigateBack() }
                                }
                                "edit_profile" -> {
                                    EditProfileFieldScreen(
                                        title = if (editingField == "nickname") "名字" else "信息",
                                        initialValue = editingFieldValue,
                                        onBack = navigateBack,
                                        onSave = { newValue ->
                                            if (editingField == "nickname") {
                                                authViewModel.updateNickname(newValue)
                                            }
                                            navigateBack()
                                        }
                                    )
                                    BackHandler { navigateBack() }
                                }
                            }
                        }
                    }

                    // 2. 登录注册遮罩层
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
                                    onLoginSuccess = { },
                                    authViewModel = authViewModel
                                )
                            }
                        }
                    }
                    
                    // 3. 启动加载
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
