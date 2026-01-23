package com.example.synji_calendar.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val isCheckingToken: Boolean = true
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val sharedPrefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkPersistedToken()
    }

    /**
     * 启动时检查本地是否存有 Token
     */
    private fun checkPersistedToken() {
        val savedToken = sharedPrefs.getString("token", null)
        if (savedToken != null) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isCheckingToken = true)
                val response = repository.getUserInfo(savedToken)
                if (response.code == 200 && response.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        token = savedToken,
                        user = response.data,
                        isCheckingToken = false
                    )
                } else {
                    // Token 失效，清除本地存储
                    sharedPrefs.edit().remove("token").apply()
                    _uiState.value = _uiState.value.copy(isCheckingToken = false)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(isCheckingToken = false)
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
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                phoneNumber = phone,
                error = if (response.code != 200) response.message else null
            )
        }
    }

    fun login(phone: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val response = repository.login(phone, code)
            
            if (response.code == 200 && response.data != null) {
                // 保存 Token 到本地
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun logout() {
        sharedPrefs.edit().remove("token").apply()
        _uiState.value = AuthUiState(isCheckingToken = false)
    }
}
