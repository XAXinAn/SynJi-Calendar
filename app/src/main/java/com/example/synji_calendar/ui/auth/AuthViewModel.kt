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
    val verificationCodeTip: String? = null, // 新增：持久显示的验证码提示
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
            val startTime = System.currentTimeMillis()
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
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = 5000L - elapsedTime
            if (remainingTime > 0) delay(remainingTime)
            _uiState.value = _uiState.value.copy(isCheckingToken = false, isSplashVisible = false)
        }
    }

    fun sendVerificationCode(phone: String) {
        if (phone.length != 11) {
            _uiState.value = _uiState.value.copy(error = "请输入正确的11位手机号")
            return
        }

        viewModelScope.launch {
            // 重新获取时，先清空旧的提示
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, verificationCodeTip = null)
            val response = repository.sendVerifyCode(phone)
            
            if (response.code == 200) {
                val codeFromServer = response.data?.toString() ?: "111111"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    phoneNumber = phone,
                    verificationCodeTip = "内测验证码：$codeFromServer" // 设置持久提示
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = response.message ?: "发送失败，请稍后重试"
                )
            }
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
                    error = null,
                    verificationCodeTip = null // 登录成功清空提示
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
                _uiState.value = _uiState.value.copy(user = response.data, isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(error = response.message, isLoading = false)
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
