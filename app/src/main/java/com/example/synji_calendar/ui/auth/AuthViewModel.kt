package com.example.synji_calendar.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val token: String? = null,
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: UserInfo? = null,
    val isCheckingToken: Boolean = true,
    val isSplashVisible: Boolean = true
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val sharedPrefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            // 启动时同时开始检查 Token 和 5秒计时
            val startTime = System.currentTimeMillis()
            
            // 1. 检查本地 Token
            val savedToken = sharedPrefs.getString("token", null)
            if (savedToken != null) {
                val response = repository.getUserInfo(savedToken)
                if (response.code == 200 && response.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        token = savedToken,
                        user = response.data
                    )
                } else {
                    sharedPrefs.edit().remove("token").apply()
                }
            }
            
            // 2. 计算剩余需要等待的时间，确保总时长至少 5000ms
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = 5000L - elapsedTime
            if (remainingTime > 0) {
                delay(remainingTime)
            }
            
            // 3. 结束初始化
            _uiState.value = _uiState.value.copy(
                isCheckingToken = false,
                isSplashVisible = false
            )
        }
    }

    fun sendVerificationCode(phone: String) {
        if (phone.length != 11) {
            _uiState.value = _uiState.value.copy(error = "请输入正确的11位手机号")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val response = repository.sendVerifyCode(phone)
            
            val displayMsg = when {
                response.code == 200 -> "内测期间验证码请填写：111111"
                response.code == 500 -> "短信发送失败，请稍后重试"
                else -> response.message ?: "发送失败，请稍后重试"
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                phoneNumber = phone,
                error = displayMsg
            )
        }
    }

    fun login(phone: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val response = repository.login(phone, code)
            
            if (response.code == 200 && response.data != null) {
                sharedPrefs.edit().putString("token", response.data.token).apply()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    token = response.data.token,
                    user = response.data.user,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = response.message
                )
            }
        }
    }

    fun updateNickname(nickname: String) {
        val token = _uiState.value.token ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = repository.updateUserInfo(token, nickname)
            if (response.code == 200 && response.data != null) {
                _uiState.value = _uiState.value.copy(
                    user = response.data,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = response.message,
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun logout() {
        sharedPrefs.edit().remove("token").apply()
        _uiState.value = AuthUiState(isCheckingToken = false, isSplashVisible = false)
    }
}
