package com.example.synji_calendar.ui.group

import com.example.synji_calendar.ui.auth.ApiResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GroupRepository {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = "http://192.168.43.227:8080"

    suspend fun fetchGroups(token: String): ApiResponse<List<GroupInfo>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/group/list")
            .get()
            .header("Authorization", token)
            .build()
        val type = object : TypeToken<ApiResponse<List<GroupInfo>>>() {}.type
        executeRequest(request, type)
    }

    suspend fun createGroup(token: String, name: String): ApiResponse<GroupInfo> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(CreateGroupRequest(name)).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/api/group/create")
            .post(requestBody)
            .header("Authorization", token)
            .build()
        val type = object : TypeToken<ApiResponse<GroupInfo>>() {}.type
        executeRequest(request, type)
    }

    suspend fun joinGroup(token: String, inviteCode: String): ApiResponse<GroupInfo> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(JoinGroupRequest(inviteCode)).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/api/group/join")
            .post(requestBody)
            .header("Authorization", token)
            .build()
        val type = object : TypeToken<ApiResponse<GroupInfo>>() {}.type
        executeRequest(request, type)
    }

    /**
     * 获取群组成员列表
     */
    suspend fun fetchGroupMembers(token: String, groupId: String): ApiResponse<List<GroupMember>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/group/members?groupId=$groupId")
            .get()
            .header("Authorization", token)
            .build()
        val type = object : TypeToken<ApiResponse<List<GroupMember>>>() {}.type
        executeRequest(request, type)
    }

    /**
     * 设置管理员逻辑
     */
    suspend fun setMemberAdmin(token: String, groupId: String, userId: String, isAdmin: Boolean): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        val requestBody = gson.toJson(SetAdminRequest(groupId, userId, isAdmin)).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/api/group/set-admin")
            .post(requestBody)
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
