package com.example.synji_calendar.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class Schedule(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val date: LocalDate,
    val time: LocalTime = LocalTime.now(),
    val isAllDay: Boolean = false,
    val location: String = "",
    val belonging: String = "个人",
    val isImportant: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _holidays = MutableStateFlow<Map<LocalDate, Holiday>>(emptyMap())
    val holidays = _holidays.asStateFlow()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val allSchedules = _schedules.asStateFlow()

    init {
        loadHolidays()
        // 已清空所有虚拟日程，等待手动添加测试
        _schedules.value = emptyList()
    }

    private fun loadHolidays() {
        viewModelScope.launch {
            val map = HolidayService.fetchHolidays(getApplication())
            _holidays.value = map
        }
    }

    fun addSchedule(schedule: Schedule) {
        _schedules.value = _schedules.value + schedule
    }

    fun deleteSchedule(schedule: Schedule) {
        _schedules.value = _schedules.value.filter { it.id != schedule.id }
    }

    fun updateSchedule(updatedSchedule: Schedule) {
        _schedules.value = _schedules.value.map {
            if (it.id == updatedSchedule.id) updatedSchedule else it
        }
    }
}
