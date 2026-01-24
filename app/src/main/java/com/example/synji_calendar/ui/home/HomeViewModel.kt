package com.example.synji_calendar.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import com.nlf.calendar.Solar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

data class Schedule(
    val id: Long? = null, 
    val title: String,
    val date: LocalDate,
    val time: LocalTime = LocalTime.now(),
    val isAllDay: Boolean = false,
    val location: String = "",
    val belonging: String = "个人",
    @SerializedName("important")
    val isImportant: Boolean = false,
    val notes: String = ""
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HomeRepository()
    
    private val _holidays = MutableStateFlow<Map<LocalDate, Holiday>>(emptyMap())
    val holidays = _holidays.asStateFlow()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val allSchedules = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val calendarCache = mutableMapOf<YearMonth, List<DayDisplayInfo>>()
    private val _currentMonthData = MutableStateFlow<Map<YearMonth, List<DayDisplayInfo>>>(emptyMap())
    val currentMonthData = _currentMonthData.asStateFlow()

    init {
        loadHolidays()
        getOrComputeMonthData(YearMonth.now())
    }

    private fun loadHolidays() {
        viewModelScope.launch {
            val map = HolidayService.fetchHolidays(getApplication())
            _holidays.value = map
        }
    }

    fun getOrComputeMonthData(month: YearMonth) {
        if (calendarCache.containsKey(month)) return
        viewModelScope.launch(Dispatchers.Default) {
            val days = mutableListOf<LocalDate>()
            val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
            val prevMonth = month.minusMonths(1)
            for (i in firstDayOfWeek - 1 downTo 0) days.add(prevMonth.atDay(prevMonth.lengthOfMonth() - i))
            for (i in 1..month.lengthOfMonth()) days.add(month.atDay(i))
            while (days.size < 42) days.add(month.plusMonths(1).atDay(days.size - firstDayOfWeek - month.lengthOfMonth() + 1))
            
            val data = days.map { date ->
                val solar = Solar.fromYmd(date.year, date.monthValue, date.dayOfMonth)
                val lunar = solar.lunar
                DayDisplayInfo(date, lunar.dayInChinese, date == LocalDate.now(), YearMonth.from(date) == month, null)
            }
            calendarCache[month] = data
            _currentMonthData.value = _currentMonthData.value + (month to data)
        }
    }

    fun refreshSchedules(token: String) {
        if (token.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.fetchSchedules(token)
            if (response.code == 200 && response.data != null) {
                _schedules.value = response.data
            }
            _isLoading.value = false
        }
    }

    fun addSchedule(token: String, schedule: Schedule, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.addSchedule(token, schedule.copy(id = null))
            if (response.code == 200) {
                refreshSchedules(token)
                onComplete()
            } else {
                _isLoading.value = false
            }
        }
    }

    fun deleteSchedule(token: String, scheduleId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.deleteSchedule(token, scheduleId)
            if (response.code == 200) {
                refreshSchedules(token)
                onComplete()
            } else { _isLoading.value = false }
        }
    }

    fun updateSchedule(token: String, updatedSchedule: Schedule, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.updateSchedule(token, updatedSchedule)
            if (response.code == 200) {
                refreshSchedules(token)
                onComplete()
            } else { _isLoading.value = false }
        }
    }
}
