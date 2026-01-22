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
    private const val CACHE_FILE_NAME = "holidays_cache.ics"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var holidayMap: Map<LocalDate, Holiday> = emptyMap()

    /**
     * 获取节假日数据：优先内存 -> 其次磁盘 -> 最后网络
     */
    suspend fun fetchHolidays(context: Context): Map<LocalDate, Holiday> {
        // 1. 内存缓存
        if (holidayMap.isNotEmpty()) return holidayMap
        
        return withContext(Dispatchers.IO) {
            // 2. 尝试从本地磁盘读取
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            if (cacheFile.exists()) {
                Log.d(TAG, "Loading from disk cache...")
                val cachedIcs = cacheFile.readText()
                holidayMap = parseIcsString(cachedIcs)
                
                // 如果磁盘有数据，直接返回展示，但后台依然静默更新一下网络数据
                if (holidayMap.isNotEmpty()) {
                    Log.i(TAG, "Disk cache loaded successfully: ${holidayMap.size} items")
                    // 启动异步静默更新（不阻塞当前返回）
                    updateCacheInBackground(context)
                    return@withContext holidayMap
                }
            }

            // 3. 磁盘没数据，必须从网络获取
            return@withContext downloadAndCache(context)
        }
    }

    private suspend fun downloadAndCache(context: Context): Map<LocalDate, Holiday> {
        try {
            Log.d(TAG, "Fetching from network...")
            val request = Request.Builder()
                .url("https://cdn.jsdelivr.net/npm/chinese-days/dist/holidays.ics")
                .build()
            
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val icalString = response.body?.string() ?: ""
                // 保存到磁盘
                File(context.filesDir, CACHE_FILE_NAME).writeText(icalString)
                holidayMap = parseIcsString(icalString)
                Log.i(TAG, "Network download successful and cached")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network fetch failed", e)
        }
        return holidayMap
    }

    private fun updateCacheInBackground(context: Context) {
        // 实际项目中可以这里启动协程更新，目前简单起见可暂不处理，
        // 或者在下一次启动时自然会更新。
    }

    private fun parseIcsString(icalString: String): Map<LocalDate, Holiday> {
        val map = mutableMapOf<LocalDate, Holiday>()
        try {
            val icals = Biweekly.parse(icalString).all()
            if (icals.isEmpty()) return map
            
            val ical = icals[0]
            val systemZone = ZoneId.systemDefault()

            ical.getComponents(VEvent::class.java).forEach { event ->
                val summary = event.summary?.value ?: ""
                val isRest = summary.contains("休")
                val isWork = summary.contains("班")

                if (isRest || isWork) {
                    val name = summary.replace(Regex("[\\(（].*?[\\)）]"), "").trim()
                    val startDate = event.dateStart?.value?.toInstant()?.atZone(systemZone)?.toLocalDate() ?: return@forEach
                    val endDate = event.dateEnd?.value?.toInstant()?.atZone(systemZone)?.toLocalDate() ?: startDate.plusDays(1)

                    var current = startDate
                    while (current.isBefore(endDate)) {
                        map[current] = Holiday(current, isWork, isRest, name)
                        current = current.plusDays(1)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error", e)
        }
        return map
    }
}
