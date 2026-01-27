package com.example.synji_calendar.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.synji_calendar.ui.home.BgGradientStart
import com.example.synji_calendar.ui.home.ContainerGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileFieldScreen(
    title: String,
    initialValue: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(initialValue) }

    Column(modifier = Modifier.fillMaxSize().background(ContainerGrey)) {
        // Header
        CenterAlignedTopAppBar(
            title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "取消")
                }
            },
            actions = {
                Button(
                    onClick = { onSave(textValue) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF07C160), // 微信绿
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.padding(end = 8.dp).height(32.dp),
                    enabled = textValue.isNotBlank() && textValue != initialValue
                ) {
                    Text("完成", fontSize = 14.sp)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = ContainerGrey)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 输入框
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            TextField(
                value = textValue,
                onValueChange = { textValue = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = { Text("请输入$title") },
                singleLine = true
            )
        }
        
        Text(
            text = "好的名字可以让朋友更容易记住你",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    }
}
