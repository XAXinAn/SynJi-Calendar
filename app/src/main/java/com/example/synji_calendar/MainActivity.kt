package com.example.synji_calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.synji_calendar.ui.home.HomeScreen
import com.example.synji_calendar.ui.theme.SynJiCalendarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SynJiCalendarTheme {
                HomeScreen()
            }
        }
    }
}
