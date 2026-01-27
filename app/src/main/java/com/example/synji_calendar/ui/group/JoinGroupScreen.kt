package com.example.synji_calendar.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGroupScreen(
    token: String,
    viewModel: GroupViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(BgGradientStart, BgGradientEnd)))) {
                TopAppBar(
                    title = { Text("加入群组", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, contentDescription = "取消", tint = Color.White)
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                if (inviteCode.isNotBlank()) {
                                    viewModel.joinGroup(token, inviteCode) {
                                        onSuccess()
                                    }
                                }
                            },
                            enabled = inviteCode.length >= 4 && !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = BgGradientStart,
                                disabledContainerColor = Color.White.copy(alpha = 0.5f),
                                disabledContentColor = BgGradientStart.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.padding(end = 8.dp).height(32.dp)
                        ) {
                            Text("确定", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = ContainerGrey
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            TextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.uppercase() },
                placeholder = { Text("输入邀请码", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = BgGradientStart,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 1,
                singleLine = true
            )
            
            Text(
                text = "输入6位邀请码加入已有的群组",
                modifier = Modifier.padding(16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BgGradientStart)
                }
            }
        }
    }
}
