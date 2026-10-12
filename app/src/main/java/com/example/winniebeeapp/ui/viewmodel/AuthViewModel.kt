package com.example.winniebeeapp.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.winniebeeapp.data.model.User
import com.example.winniebeeapp.data.model.UserRole
import com.example.winniebeeapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun login(email: String, pass: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.login(email.trim(), pass)
            if (result.isSuccess) {
                val loggedInUser = result.getOrNull()
                _user.value = loggedInUser
                if (loggedInUser != null) onSuccess(loggedInUser)
            } else {
                _error.value = "Wrong email/password entered"
            }
            _isLoading.value = false
        }
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        phone: String,
        region: String,
        age: Int,
        sex: String,
        maaif: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.registerUser(name, email, pass, role, phone, region, age, sex, maaif)
            if (result.isSuccess) {
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
    }
}
