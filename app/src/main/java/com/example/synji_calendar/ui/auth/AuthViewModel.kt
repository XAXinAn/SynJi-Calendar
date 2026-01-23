package com.example.synji_calendar.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class AuthState(
    val isLoggedIn: Boolean = false,
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val generatedCode: String? = null
)

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    // 模拟已注册用户的数据库
    private val registeredUsers = mutableSetOf<String>()

    fun sendVerificationCode(phone: String) {
        if (phone.length != 11) {
            _authState.value = _authState.value.copy(error = "请输入正确的11位手机号")
            return
        }

        val code = (100000..999999).random().toString()
        _authState.value = _authState.value.copy(
            phoneNumber = phone,
            generatedCode = code,
            error = null
        )
        
        // 在控制台模拟发送验证码
        Log.d("AuthService", "========================================")
        Log.d("AuthService", "【讯极日历】验证码发送成功！")
        Log.d("AuthService", "手机号：$phone")
        Log.d("AuthService", "验证码：$code")
        Log.d("AuthService", "========================================")
        
        println("【模拟后端】向手机号 $phone 发送验证码: $code")
    }

    fun login(phone: String, code: String) {
        val currentCode = _authState.value.generatedCode
        
        if (currentCode == null) {
            _authState.value = _authState.value.copy(error = "请先获取验证码")
            return
        }

        if (code != currentCode) {
            _authState.value = _authState.value.copy(error = "验证码错误")
            return
        }

        // 登录成功逻辑
        val isNewUser = !registeredUsers.contains(phone)
        if (isNewUser) {
            registeredUsers.add(phone)
            Log.d("AuthService", "新用户注册并登录：$phone")
        } else {
            Log.d("AuthService", "用户登录成功：$phone")
        }

        _authState.value = _authState.value.copy(
            isLoggedIn = true,
            error = null
        )
    }

    fun logout() {
        _authState.value = AuthState()
    }
}
