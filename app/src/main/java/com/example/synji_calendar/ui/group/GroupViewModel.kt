package com.example.synji_calendar.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupUiState(
    val groups: List<GroupInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupViewModel : ViewModel() {
    private val repository = GroupRepository()
    
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState = _uiState.asStateFlow()

    fun loadGroups(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = repository.fetchGroups(token)
            if (response.code == 200 && response.data != null) {
                _uiState.value = _uiState.value.copy(
                    groups = response.data,
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

    fun createGroup(token: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = repository.createGroup(token, name)
            if (response.code == 200) {
                loadGroups(token)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = response.message,
                    isLoading = false
                )
            }
        }
    }

    fun joinGroup(token: String, inviteCode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val response = repository.joinGroup(token, inviteCode)
            if (response.code == 200) {
                loadGroups(token)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = response.message,
                    isLoading = false
                )
            }
        }
    }
}
