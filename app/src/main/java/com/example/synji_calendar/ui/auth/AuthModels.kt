package com.example.synji_calendar.ui.auth

/**
 * 接口统一响应包装
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

/**
 * 登录成功的响应数据
 */
data class LoginData(
    val token: String,
    val user: UserInfo
)

/**
 * 用户基础信息
 */
data class UserInfo(
    val userId: String,
    val phoneNumber: String,
    val nickname: String,
    val isNewUser: Boolean
)

/**
 * 发送验证码请求体
 */
data class SendCodeRequest(
    val phoneNumber: String
)

/**
 * 登录请求体
 */
data class LoginRequest(
    val phoneNumber: String,
    val verifyCode: String
)
