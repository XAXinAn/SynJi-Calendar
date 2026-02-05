package com.example.synji_calendar.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoreScreen(
    token: String,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val isAutoDetectionEnabled by homeViewModel.isAutoDetectionEnabled.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(ContainerGrey)) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Brush.horizontalGradient(listOf(BgGradientStart, BgGradientEnd)))
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White)
                }
                Text(
                    "更多",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // 内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 自动检测开关项
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandOrange.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    null,
                                    tint = BrandOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "自动检测日程",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                    Switch(
                        checked = isAutoDetectionEnabled,
                        onCheckedChange = { homeViewModel.setAutoDetectionEnabled(token, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BrandOrange,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
