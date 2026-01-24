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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

// 对齐文档 3.2：除了 location 和 notes 外均设为非空，并配置默认值
data class Schedule(
    val id: Long? = null, 
    val title: String,
    val date: LocalDate,
    val time: LocalTime = LocalTime.of(0, 0, 0),
    val isAllDay: Boolean = false,
    val location: String? = null, 
    val belonging: String = "默认", 
    @SerializedName("important")
    val isImportant: Boolean = false,
    val notes: String? = null 
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HomeRepository()
    
    private val _holidays = MutableStateFlow<Map<LocalDate, Holiday>>(emptyMap())
    val holidays = _holidays.asStateFlow()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val allSchedules = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 加载进度描述
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

    fun refreshSchedules(token: String) {
        if (token.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "正在同步日程..."
            val response = repository.fetchSchedules(token)
            if (response.code == 200 && response.data != null) {
                _schedules.value = response.data
                Log.d("HomeViewModel", "成功接收结构化数据(列表): ${response.data.size} 条日程")
            }
            _isLoading.value = false
        }
    }

    /**
     * 内部使用的挂起函数：添加单条日程，不带 Scope
     */
    private suspend fun addSingleScheduleInternal(token: String, schedule: Schedule): Boolean {
        val response = repository.addSchedule(token, schedule.copy(id = null))
        return if (response.code == 200) {
            true
        } else {
            _message.emit("添加失败(${schedule.title}): ${response.message}")
            false
        }
    }

    fun addSchedule(token: String, schedule: Schedule, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "正在保存日程..."
            if (addSingleScheduleInternal(token, schedule)) {
                refreshSchedules(token)
                _message.emit("日程添加成功")
                onComplete()
            } else {
                _isLoading.value = false
            }
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

    fun clearOcrResult() {
        _ocrResult.value = ""
    }

    /**
     * 全自动流程：对齐文档 3.3，支持遍历解析到的日程列表
     */
    fun performAutoScheduleFromImage(token: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                // 1. 读取图片
                _loadingMessage.value = "正在处理图片..."
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(getApplication<Application>().contentResolver, uri)) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    MediaStore.Images.Media.getBitmap(getApplication<Application>().contentResolver, uri)
                }

                // 2. OCR 识别
                _loadingMessage.value = "正在识别文字..."
                val extractedText = OcrEngine.recognize(bitmap)
                if (extractedText.isEmpty()) {
                    _message.emit("未识别到文字，请确保图片清晰")
                    _isLoading.value = false
                    return@launch
                }

                // 3. AI 解析
                _loadingMessage.value = "AI 正在理解内容..."
                val aiResponse = repository.parseScheduleWithAi(token, extractedText)
                if (aiResponse.code == 200 && aiResponse.data != null) {
                    val schedules = aiResponse.data
                    Log.d("HomeViewModel", "AI 解析成功，发现 ${schedules.size} 条日程")
                    
                    // 4. 自动遍历添加
                    var successCount = 0
                    schedules.forEachIndexed { index, schedule ->
                        _loadingMessage.value = "正在存入第 ${index + 1}/${schedules.size} 条日程..."
                        if (addSingleScheduleInternal(token, schedule)) {
                            successCount++
                        }
                    }
                    
                    if (successCount > 0) {
                        _message.emit("成功添加 $successCount 条日程")
                        refreshSchedules(token)
                    } else {
                        _isLoading.value = false
                    }
                } else {
                    _message.emit("AI 解析失败: ${aiResponse.message}")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Auto schedule failed", e)
                _message.emit("处理异常: ${e.localizedMessage}")
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        OcrEngine.destroy()
    }
}
