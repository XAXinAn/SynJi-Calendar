package com.example.synji_calendar.ui.auth

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

class AuthRepository {
    private val client = OkHttpClient()
    
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
    
    // 根据最新服务器 IP 更新服务地址
    private val baseUrl = "http://192.168.0.100:8080"

    suspend fun sendVerifyCode(phoneNumber: String): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(mapOf("phoneNumber" to phoneNumber)).toRequestBody(jsonMediaType)
        val request = Request.Builder().url("$baseUrl/api/auth/send-code").post(requestBody).build()
        val type = object : TypeToken<ApiResponse<Unit>>() {}.type
        executeRequest(request, type)
    }

    suspend fun login(phoneNumber: String, verifyCode: String): ApiResponse<LoginData> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(mapOf("phoneNumber" to phoneNumber, "verifyCode" to verifyCode)).toRequestBody(jsonMediaType)
        val request = Request.Builder().url("$baseUrl/api/auth/login").post(requestBody).build()
        val type = object : TypeToken<ApiResponse<LoginData>>() {}.type
        executeRequest(request, type)
    }

    suspend fun getUserInfo(token: String): ApiResponse<UserInfo> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url("$baseUrl/api/user/info").get().header("Authorization", token).build()
        val type = object : TypeToken<ApiResponse<UserInfo>>() {}.type
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
