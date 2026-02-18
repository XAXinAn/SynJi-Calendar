package com.example.synji_calendar

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.synji_calendar.ui.home.HomeScreen
import com.example.synji_calendar.ui.home.AddScheduleScreen
import com.example.synji_calendar.ui.home.ScheduleDetailScreen
import com.example.synji_calendar.ui.home.Schedule
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.home.BgGradientEnd
import com.example.synji_calendar.ui.home.MoreScreen
import com.example.synji_calendar.ui.home.HomeViewModel
import com.example.synji_calendar.ui.auth.AuthDialogContent
import com.example.synji_calendar.ui.auth.AuthViewModel
import com.example.synji_calendar.ui.group.*
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
                val groupUiState by groupViewModel.uiState.collectAsState()
                val homeViewModel: HomeViewModel = viewModel()
                
                var currentScreen by remember { mutableStateOf("home") }
                var targetDate by remember { mutableStateOf(LocalDate.now()) }
                var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
                var selectedGroup by remember { mutableStateOf<GroupInfo?>(null) }
                var selectedMemberId by remember { mutableStateOf<String?>(null) }
                
                var editingField by remember { mutableStateOf("") }
                var editingFieldValue by remember { mutableStateOf("") }
                
                val focusManager = LocalFocusManager.current
                val context = LocalContext.current
                
                // 加载初始化背景图
                val splashImage = remember {
                    try {
                        context.assets.open("init.png").use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                val screenDepth = remember {
                    mapOf(
                        "home" to 0, "add" to 1, "detail" to 1, "group_list" to 1, "profile" to 1, "more" to 1,
                        "group_create" to 2, "group_join" to 2, "group_detail" to 2, "edit_profile" to 2, "member_detail" to 3
                    )
                }

                val navigateBack: () -> Unit = {
                    focusManager.clearFocus()
                    currentScreen = when(currentScreen) {
                        "member_detail" -> "group_detail"
                        "group_detail" -> "group_list"
                        "group_create" -> "group_list"
                        "group_join" -> "group_list"
                        "group_list" -> "home"
                        "profile" -> "home"
                        "more" -> "home"
                        "edit_profile" -> "profile"
                        else -> "home"
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!authUiState.isCheckingToken) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                val initialDepth = screenDepth[initialState] ?: 0
                                val targetDepth = screenDepth[targetState] ?: 0
                                if (targetDepth < initialDepth) {
                                    slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn() togetherWith
                                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                } else {
                                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)) togetherWith
                                    slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut()
                                }
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                "home" -> HomeScreen(token = authUiState.token ?: "", homeViewModel = homeViewModel, onAddSchedule = { date -> targetDate = date; currentScreen = "add" }, onEditSchedule = { schedule -> editingSchedule = schedule; currentScreen = "detail" }, onGroupClick = { currentScreen = "group_list" }, onProfileClick = { currentScreen = "profile" }, onMoreClick = { currentScreen = "more" })
                                "add" -> { AddScheduleScreen(token = authUiState.token ?: "", initialDate = targetDate, onBack = navigateBack); BackHandler { navigateBack() } }
                                "detail" -> { editingSchedule?.let { ScheduleDetailScreen(token = authUiState.token ?: "", schedule = it, onBack = navigateBack) }; BackHandler { navigateBack() } }
                                "group_list" -> { GroupScreen(token = authUiState.token ?: "", viewModel = groupViewModel, onBack = navigateBack, onGroupClick = { selectedGroup = it; currentScreen = "group_detail" }, onCreateGroup = { currentScreen = "group_create" }, onJoinGroup = { currentScreen = "group_join" }); BackHandler { navigateBack() } }
                                "group_create" -> { CreateGroupScreen(token = authUiState.token ?: "", viewModel = groupViewModel, onBack = navigateBack, onSuccess = navigateBack); BackHandler { navigateBack() } }
                                "group_join" -> { JoinGroupScreen(token = authUiState.token ?: "", viewModel = groupViewModel, onBack = navigateBack, onSuccess = navigateBack); BackHandler { navigateBack() } }
                                "group_detail" -> { GroupDetailScreen(group = selectedGroup, currentUserId = authUiState.user?.userId ?: "", token = authUiState.token ?: "", viewModel = groupViewModel, onBack = navigateBack, onMemberClick = { selectedMemberId = it.userId; currentScreen = "member_detail" }); BackHandler { navigateBack() } }
                                "member_detail" -> { groupUiState.members.find { it.userId == selectedMemberId }?.let { MemberDetailScreen(member = it, groupId = selectedGroup?.groupId ?: "", isOwner = authUiState.user?.userId == selectedGroup?.ownerId, token = authUiState.token ?: "", viewModel = groupViewModel, onBack = navigateBack) }; BackHandler { navigateBack() } }
                                "profile" -> { ProfileScreen(user = authUiState.user, onBack = navigateBack, onEditField = { f, v -> editingField = f; editingFieldValue = v; currentScreen = "edit_profile" }, onLogout = { authViewModel.logout(); currentScreen = "home" }); BackHandler { navigateBack() } }
                                "edit_profile" -> { EditProfileFieldScreen(title = if (editingField == "nickname") "名字" else "信息", initialValue = editingFieldValue, onBack = navigateBack, onSave = { if (editingField == "nickname") authViewModel.updateNickname(it); navigateBack() }); BackHandler { navigateBack() } }
                                "more" -> { MoreScreen(token = authUiState.token ?: "", homeViewModel = homeViewModel, onBack = navigateBack); BackHandler { navigateBack() } }
                            }
                        }
                    }

                    Crossfade(targetState = !authUiState.isLoggedIn && !authUiState.isCheckingToken, label = "auth_overlay") { showAuth ->
                        if (showAuth) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }, contentAlignment = Alignment.Center) {
                                AuthDialogContent(onDismiss = { }, onLoginSuccess = { }, authViewModel = authViewModel)
                            }
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = authUiState.isSplashVisible,
                        enter = fadeIn(),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (splashImage != null) {
                                Image(
                                    bitmap = splashImage,
                                    contentDescription = "Splash Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop // 等比例缩放并裁剪以填充屏幕，不拉伸
                                )
                            } else {
                                // 备选方案：如果图片加载失败，显示原有的或简单的背景
                                Box(
                                    modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(listOf(BgGradientStart, BgGradientEnd))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
