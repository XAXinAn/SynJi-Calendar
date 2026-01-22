package com.example.synji_calendar.ui.home

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import biweekly.Biweekly
import biweekly.component.VEvent
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

data class Holiday(
    val date: LocalDate,
    val isWork: Boolean,
    val isRest: Boolean,
    val name: String
)

object HolidayService {
    private const val TAG = "HolidayService"
    private const val CACHE_FILE_NAME = "holidays_cache_v2.ics"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var holidayMap: Map<LocalDate, Holiday> = emptyMap()

    suspend fun fetchHolidays(context: Context): Map<LocalDate, Holiday> {
        if (holidayMap.isNotEmpty()) return holidayMap
        
        return withContext(Dispatchers.IO) {
            // 1. 尝试加载缓存
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            if (cacheFile.exists()) {
                val cached = parseIcsString(cacheFile.readText())
                if (cached.isNotEmpty()) {
                    holidayMap = cached
                    Log.i(TAG, "Loaded ${cached.size} items from cache")
                    return@withContext holidayMap
                }
            }

            // 2. 联网下载
            val networkMap = downloadAndCache(context)
            
            // 3. 如果联网也失败，内置部分 2025 数据供测试 UI
            if (networkMap.isEmpty()) {
                Log.w(TAG, "Network failed, using internal test data")
                val testData = mutableMapOf<LocalDate, Holiday>()
                // 2025 元旦
                testData[LocalDate.of(2025, 1, 1)] = Holiday(LocalDate.of(2025, 1, 1), false, true, "元旦")
                // 2025 春节调休示例
                testData[LocalDate.of(2025, 1, 26)] = Holiday(LocalDate.of(2025, 1, 26), true, false, "春节")
                testData[LocalDate.of(2025, 1, 28)] = Holiday(LocalDate.of(2025, 1, 28), false, true, "春节")
                holidayMap = testData
            } else {
                holidayMap = networkMap
            }
            
            return@withContext holidayMap
        }
    }

    private fun downloadAndCache(context: Context): Map<LocalDate, Holiday> {
        return try {
            val request = Request.Builder().url("https://cdn.jsdelivr.net/npm/chinese-days/dist/holidays.ics").build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val icalString = response.body?.string() ?: ""
                File(context.filesDir, CACHE_FILE_NAME).writeText(icalString)
                parseIcsString(icalString)
            } else emptyMap()
        } catch (e: Exception) { emptyMap() }
    }

    private fun parseIcsString(icalString: String): Map<LocalDate, Holiday> {
        val map = mutableMapOf<LocalDate, Holiday>()
        try {
            // 使用 .all() 获取 List 再取第一个，解决 firstOrNull 编译错误
            val icals = Biweekly.parse(icalString).all()
            if (icals.isEmpty()) return map
            val ical = icals[0]
            
            val systemZone = ZoneId.systemDefault()
            ical.getComponents(VEvent::class.java).forEach { event ->
                val summary = event.summary?.value ?: ""
                val isRest = summary.contains("休")
                val isWork = summary.contains("班")
                if (isRest || isWork) {
                    val name = summary.replace(Regex("[\\(（\\[].*?[\\)）\\]]"), "").trim()
                    val startDate = event.dateStart?.value?.toInstant()?.atZone(systemZone)?.toLocalDate() ?: return@forEach
                    val endDate = event.dateEnd?.value?.toInstant()?.atZone(systemZone)?.toLocalDate() ?: startDate.plusDays(1)
                    
                    var curr = startDate
                    // 修复变量名错误: curr.isBefore
                    while (curr.isBefore(endDate)) {
                        map[curr] = Holiday(curr, isWork, isRest, name)
                        curr = curr.plusDays(1)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: ${e.message}")
        }
        return map
    }
}
