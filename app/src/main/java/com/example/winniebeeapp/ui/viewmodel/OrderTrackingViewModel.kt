package com.example.winniebeeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.winniebeeapp.data.model.LatLng
import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import com.example.winniebeeapp.data.repository.OrderRepository
import com.example.winniebeeapp.data.repository.TrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrderTrackingViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val trackingRepository = TrackingRepository()

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location.asStateFlow()

    fun startTracking(orderId: String) {
        viewModelScope.launch {
            orderRepository.observeOrder(orderId).collectLatest { order ->
                _order.value = order
            }
        }
        viewModelScope.launch {
            trackingRepository.observeLocation(orderId).collectLatest { location ->
                _location.value = location
            }
        }
    }

    fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
        }
    }

    fun updateCourierLocation(orderId: String, latLng: LatLng) {
        viewModelScope.launch {
            trackingRepository.updateLocation(orderId, latLng)
        }
    }
    
    fun confirmReceipt(orderId: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, OrderStatus.COMPLETED)
        }
    }
}
