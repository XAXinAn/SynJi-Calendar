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
fun CreateGroupScreen(
    token: String,
    viewModel: GroupViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(BgGradientStart, BgGradientEnd)))) {
                TopAppBar(
                    title = { Text("创建群组", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.Close, contentDescription = "取消", tint = Color.White)
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                if (groupName.isNotBlank()) {
                                    viewModel.createGroup(token, groupName) {
                                        onSuccess()
                                    }
                                }
                            },
                            enabled = groupName.isNotBlank() && !uiState.isLoading,
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
                            Text("完成", fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                value = groupName,
                onValueChange = { groupName = it },
                placeholder = { Text("群组名称", color = Color.Gray) },
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
                text = "好的群组名称能让大家更快找到组织",
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
