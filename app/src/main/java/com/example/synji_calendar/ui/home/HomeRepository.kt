package com.example.synji_calendar.ui.home

import com.example.synji_calendar.ui.auth.ApiResponse
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
    
    // 统一配置支持 Java 8 时间类的 Gson
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
        .create()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = "http://192.168.0.102:8080"

    suspend fun fetchSchedules(token: String): ApiResponse<List<Schedule>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/schedule/list")
            .get()
            .header("Authorization", token)
            .build()
        val type = object : TypeToken<ApiResponse<List<Schedule>>>() {}.type
        executeRequest(request, type)
    }

    suspend fun addSchedule(token: String, schedule: Schedule): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(schedule).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/api/schedule/add")
            .post(requestBody)
            .header("Authorization", token)
            .build()
        val type = object : TypeToken<ApiResponse<Unit>>() {}.type
        executeRequest(request, type)
    }

    suspend fun updateSchedule(token: String, schedule: Schedule): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(schedule).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/api/schedule/update")
            .put(requestBody)
            .header("Authorization", token)
            .build()
        val type = object : TypeToken<ApiResponse<Unit>>() {}.type
        executeRequest(request, type)
    }

    suspend fun deleteSchedule(token: String, scheduleId: Long): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/schedule/delete/$scheduleId")
            .delete()
            .header("Authorization", token)
            .build()
        val type = object : TypeToken<ApiResponse<Unit>>() {}.type
        executeRequest(request, type)
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
