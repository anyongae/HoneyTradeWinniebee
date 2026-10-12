package com.example.winniebeeapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.winniebeeapp.data.local.ProductEntity
import com.example.winniebeeapp.data.local.UserEntity
import com.example.winniebeeapp.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val adminRepository = AdminRepository(application)

    private val _unverifiedSellers = MutableStateFlow<List<UserEntity>>(emptyList())
    val unverifiedSellers: StateFlow<List<UserEntity>> = _unverifiedSellers.asStateFlow()

    private val _unverifiedProducts = MutableStateFlow<List<ProductEntity>>(emptyList())
    val unverifiedProducts: StateFlow<List<ProductEntity>> = _unverifiedProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPendingVerifications()
    }

    fun loadPendingVerifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _unverifiedSellers.value = adminRepository.getUnverifiedSellers()
                _unverifiedProducts.value = adminRepository.getUnverifiedProducts()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyUser(userId: String) {
        viewModelScope.launch {
            try {
                adminRepository.verifyUser(userId)
                loadPendingVerifications()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun verifyProduct(productId: String) {
        viewModelScope.launch {
            try {
                adminRepository.verifyProduct(productId)
                loadPendingVerifications()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
