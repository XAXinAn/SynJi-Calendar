package com.example.synji_calendar.ui.home

import android.util.Log
import com.example.synji_calendar.ui.auth.ApiResponse
import com.example.synji_calendar.utils.NetworkConfig
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HomeRepository {
    private val client = OkHttpClient()
    
    private val scheduleAdapter = JsonDeserializer<Schedule> { json, _, context ->
        try {
            val obj = json.asJsonObject
            Schedule(
                id = if (obj.has("id") && !obj.get("id").isJsonNull) obj.get("id").asLong else null,
                title = if (obj.has("title") && !obj.get("title").isJsonNull) obj.get("title").asString else "未命名日程",
                date = context.deserialize(obj.get("date"), LocalDate::class.java),
                time = if (obj.has("time") && !obj.get("time").isJsonNull) context.deserialize(obj.get("time"), LocalTime::class.java) else LocalTime.of(0, 0, 0),
                isAllDay = if (obj.has("isAllDay") && !obj.get("isAllDay").isJsonNull) obj.get("isAllDay").asBoolean else false,
                location = if (obj.has("location") && !obj.get("location").isJsonNull) obj.get("location").asString else null,
                belonging = if (obj.has("belonging") && !obj.get("belonging").isJsonNull) obj.get("belonging").asString else "默认",
                isImportant = if (obj.has("important") && !obj.get("important").isJsonNull) obj.get("important").asBoolean else if (obj.has("isImportant") && !obj.get("isImportant").isJsonNull) obj.get("isImportant").asBoolean else false,
                notes = if (obj.has("notes") && !obj.get("notes").isJsonNull) obj.get("notes").asString else null,
                // 兼容下划线和驼峰命名
                isAiGenerated = when {
                    obj.has("isAiGenerated") -> obj.get("isAiGenerated").asBoolean
                    obj.has("is_ai_generated") -> obj.get("is_ai_generated").asBoolean
                    else -> false
                },
                isViewed = when {
                    obj.has("isViewed") -> obj.get("isViewed").asBoolean
                    obj.has("is_viewed") -> obj.get("is_viewed").asBoolean
                    else -> true
                }
            )
        } catch (e: Exception) {
            Schedule(title = "解析异常", date = LocalDate.now())
        }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
            JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
        })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
            LocalDate.parse(json.asString)
        })
        .registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime> { src, _, _ ->
            JsonPrimitive(src.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        })
        .registerTypeAdapter(LocalTime::class.java, JsonDeserializer { json, _, _ ->
            LocalTime.parse(json.asString)
        })
        .registerTypeAdapter(Schedule::class.java, scheduleAdapter)
        .create()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = NetworkConfig.BASE_URL

    suspend fun ping(): ApiResponse<String> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url("$baseUrl/api/ping").get().build()
        executeRequest(request, object : TypeToken<ApiResponse<String>>() {}.type)
    }

    suspend fun fetchSchedules(token: String): ApiResponse<List<Schedule>> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url("$baseUrl/api/schedule/list").get().header("Authorization", token).build()
        executeRequest(request, object : TypeToken<ApiResponse<List<Schedule>>>() {}.type)
    }

    // 新增：后端协同搜索接口
    suspend fun searchSchedules(token: String, keyword: String): ApiResponse<List<Schedule>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/schedule/search?keyword=$keyword")
            .get()
            .header("Authorization", token)
            .build()
        executeRequest(request, object : TypeToken<ApiResponse<List<Schedule>>>() {}.type)
    }

    suspend fun addSchedule(token: String, schedule: Schedule): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(schedule).toRequestBody(jsonMediaType)
        val request = Request.Builder().url("$baseUrl/api/schedule/add").post(requestBody).header("Authorization", token).build()
        executeRequest(request, object : TypeToken<ApiResponse<Unit>>() {}.type)
    }

    suspend fun parseScheduleWithAi(token: String, text: String): ApiResponse<List<Schedule>> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(mapOf("text" to text)).toRequestBody(jsonMediaType)
        val request = Request.Builder().url("$baseUrl/api/schedule/ai-parse").post(requestBody).header("Authorization", token).build()
        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                val type = object : TypeToken<ApiResponse<List<Schedule>>>() {}.type
                gson.fromJson<ApiResponse<List<Schedule>>>(bodyString, type)
            }
        } catch (e: Exception) {
            ApiResponse(500, "AI解析异常: ${e.message}", null)
        }
    }

    suspend fun updateSchedule(token: String, schedule: Schedule): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(schedule).toRequestBody(jsonMediaType)
        val request = Request.Builder().url("$baseUrl/api/schedule/update").put(requestBody).header("Authorization", token).build()
        executeRequest(request, object : TypeToken<ApiResponse<Unit>>() {}.type)
    }

    suspend fun deleteSchedule(token: String, scheduleId: Long): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url("$baseUrl/api/schedule/delete/$scheduleId").delete().header("Authorization", token).build()
        executeRequest(request, object : TypeToken<ApiResponse<Unit>>() {}.type)
    }

    private fun <T> executeRequest(request: Request, responseType: java.lang.reflect.Type): ApiResponse<T> {
        return try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                gson.fromJson(bodyString, responseType)
            }
        } catch (e: Exception) {
            ApiResponse(500, "网络异常: ${e.message}", null)
        }
    }
}
