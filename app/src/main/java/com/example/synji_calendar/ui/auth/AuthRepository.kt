package com.example.synji_calendar.ui.auth

import android.util.Log
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AuthRepository {
    // 增加 Cookie 存储，解决后端使用 Session 导致验证码找不到的问题
    private val cookieStore = mutableMapOf<String, List<Cookie>>()
    
    private val client = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: listOf()
            }
        })
        .build()
    
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
    private val baseUrl = "http://192.168.43.227:8080"

    suspend fun sendVerifyCode(phoneNumber: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim()
        val requestBody = gson.toJson(mapOf("phoneNumber" to cleanPhone)).toRequestBody(jsonMediaType)
        val request = Request.Builder().url("$baseUrl/api/auth/send-code").post(requestBody).build()
        val type = object : TypeToken<ApiResponse<String>>() {}.type
        executeRequest(request, type)
    }

    suspend fun login(phoneNumber: String, verifyCode: String): ApiResponse<LoginData> = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim()
        val cleanCode = verifyCode.trim()
        
        // 双字段保险：同时发送 code 和 verifyCode
        val payload = mapOf(
            "phoneNumber" to cleanPhone,
            "code" to cleanCode,
            "verifyCode" to cleanCode
        )
        
        Log.d("AuthRepository", "发送登录请求: $payload")
        
        val requestBody = gson.toJson(payload).toRequestBody(jsonMediaType)
        val request = Request.Builder().url("$baseUrl/api/auth/login").post(requestBody).build()
        val type = object : TypeToken<ApiResponse<LoginData>>() {}.type
        executeRequest(request, type)
    }

    suspend fun getUserInfo(token: String): ApiResponse<UserInfo> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url("$baseUrl/api/user/info").get().header("Authorization", token).build()
        val type = object : TypeToken<ApiResponse<UserInfo>>() {}.type
        executeRequest(request, type)
    }

    suspend fun updateUserInfo(token: String, nickname: String): ApiResponse<UserInfo> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(mapOf("nickname" to nickname)).toRequestBody(jsonMediaType)
        val request = Request.Builder().url("$baseUrl/api/user/update").put(requestBody).header("Authorization", token).build()
        val type = object : TypeToken<ApiResponse<UserInfo>>() {}.type
        executeRequest(request, type)
    }

    private fun <T> executeRequest(request: Request, responseType: java.lang.reflect.Type): ApiResponse<T> {
        return try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                Log.d("AuthRepository", "接口: ${request.url}, 响应: $bodyString")
                gson.fromJson(bodyString, responseType)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "网络请求失败", e)
            ApiResponse(500, "网络异常: ${e.message}", null)
        }
    }
}
