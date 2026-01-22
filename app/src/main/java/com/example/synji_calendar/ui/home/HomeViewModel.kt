package com.example.synji_calendar.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _holidays = MutableStateFlow<Map<LocalDate, Holiday>>(emptyMap())
    val holidays = _holidays.asStateFlow()

    init {
        loadHolidays()
    }

    private fun loadHolidays() {
        viewModelScope.launch {
            val map = HolidayService.fetchHolidays(getApplication())
            _holidays.value = map
        }
    }
}
