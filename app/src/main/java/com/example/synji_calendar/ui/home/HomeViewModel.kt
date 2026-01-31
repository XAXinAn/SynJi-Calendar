package com.example.synji_calendar.ui.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.synji_calendar.utils.OcrEngine
import com.google.gson.annotations.SerializedName
import com.nlf.calendar.Solar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 下拉刷新状态枚举
 */
enum class RefreshStatus {
    IDLE, REFRESHING, COMPLETE
}

/**
 * 日期显示模型
 */
data class DayDisplayInfo(
    val date: LocalDate,
    val subText: String,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val holiday: Holiday?
)

/**
 * 根据 v1.7 接口文档对齐的日程数据模型
 */
data class Schedule(
    val id: Long? = null, 
    val title: String,
    val date: LocalDate,
    val time: LocalTime = LocalTime.of(0, 0, 0),
    val isAllDay: Boolean = false,
    val location: String? = null, 
    val belonging: String = "个人", // 文档 4.2: 默认为"个人"
    @SerializedName("important")
    val isImportant: Boolean = false,
    val notes: String? = null,
    @SerializedName("isAiGenerated")
    val isAiGenerated: Boolean = false,
    @SerializedName("isViewed")
    val isViewed: Boolean = true, // 初始默认为已读，AI识别时会改为false
    @SerializedName("createdAt")
    val createdAt: String? = null 
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HomeRepository()
    
    private val _holidays = MutableStateFlow<Map<LocalDate, Holiday>>(emptyMap())
    val holidays = _holidays.asStateFlow()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val allSchedules = _schedules.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Schedule>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _refreshStatus = MutableStateFlow(RefreshStatus.IDLE)
    val refreshStatus = _refreshStatus.asStateFlow()

    private val _lastUpdateRaw = MutableStateFlow<LocalDateTime?>(null)
    val lastUpdateRaw = _lastUpdateRaw.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _loadingMessage = MutableStateFlow("")
    val loadingMessage = _loadingMessage.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val _ocrResult = MutableStateFlow("")
    val ocrResult = _ocrResult.asStateFlow()

    private val calendarCache = mutableMapOf<YearMonth, List<DayDisplayInfo>>()
    private val _currentMonthData = MutableStateFlow<Map<YearMonth, List<DayDisplayInfo>>>(emptyMap())
    val currentMonthData = _currentMonthData.asStateFlow()

    init {
        loadHolidays()
        getOrComputeMonthData(YearMonth.now())
        viewModelScope.launch(Dispatchers.IO) {
            OcrEngine.init(getApplication())
        }
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

    fun refreshSchedules(token: String, isBackground: Boolean = false) {
        if (token.isEmpty()) return
        viewModelScope.launch {
            if (!isBackground) {
                _refreshStatus.value = RefreshStatus.REFRESHING
            }
            val response = repository.fetchSchedules(token)
            if (response.code == 200 && response.data != null) {
                _schedules.value = response.data
                _lastUpdateRaw.value = LocalDateTime.now()
            }
            if (!isBackground) {
                _refreshStatus.value = RefreshStatus.COMPLETE
                delay(1500)
                _refreshStatus.value = RefreshStatus.IDLE
            }
        }
    }

    fun searchSchedules(token: String, query: String) {
        if (token.isEmpty()) return
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val response = repository.searchSchedules(token, query)
            if (response.code == 200 && response.data != null) {
                _searchResults.value = response.data
            } else {
                _searchResults.value = emptyList()
            }
            _isSearching.value = false
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun addSchedule(token: String, schedule: Schedule, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "正在保存日程..."
            val scheduleToSave = schedule.copy(id = null, isAiGenerated = false, isViewed = true)
            val response = repository.addSchedule(token, scheduleToSave)
            if (response.code == 200) {
                refreshSchedules(token)
                _message.emit("日程添加成功")
                onComplete()
            } else {
                _message.emit("添加失败: ${response.message}")
            }
            _isLoading.value = false
        }
    }

    fun deleteSchedule(token: String, scheduleId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "正在删除..."
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
            _loadingMessage.value = "正在更新..."
            val response = repository.updateSchedule(token, updatedSchedule)
            if (response.code == 200) {
                refreshSchedules(token)
                onComplete()
            } else { _isLoading.value = false }
        }
    }

    fun markAsViewed(token: String, schedule: Schedule) {
        if (!schedule.isViewed) {
            viewModelScope.launch {
                val response = repository.updateSchedule(token, schedule.copy(isViewed = true))
                if (response.code == 200) {
                    refreshSchedules(token, isBackground = true)
                }
            }
        }
    }

    fun performAutoScheduleFromImage(token: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _loadingMessage.value = "正在识别并解析日程..."
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(getApplication<Application>().contentResolver, uri)) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    MediaStore.Images.Media.getBitmap(getApplication<Application>().contentResolver, uri)
                }

                val extractedText = OcrEngine.recognize(bitmap)
                if (extractedText.isEmpty()) {
                    _message.emit("未识别到文字")
                    _isLoading.value = false
                    return@launch
                }

                val aiResponse = repository.parseScheduleWithAi(token, extractedText)
                if (aiResponse.code == 200 && aiResponse.data != null) {
                    val schedules = aiResponse.data
                    var successCount = 0
                    schedules.forEach { extraction ->
                        val scheduleToSave = extraction.copy(isAiGenerated = true, isViewed = false)
                        val addResp = repository.addSchedule(token, scheduleToSave)
                        if (addResp.code == 200) successCount++
                    }
                    if (successCount > 0) {
                        _message.emit("成功添加 $successCount 条日程")
                        refreshSchedules(token)
                    }
                } else {
                    _message.emit("AI 解析失败: ${aiResponse.message}")
                }
            } catch (e: Exception) {
                _message.emit("处理异常: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        OcrEngine.destroy()
    }
}
