package com.example.winniebeeapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.winniebeeapp.data.local.WinnieBeeDatabase
import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import com.example.winniebeeapp.data.model.Product
import com.example.winniebeeapp.data.repository.OrderRepository
import com.example.winniebeeapp.data.repository.SellerRepository
import com.example.winniebeeapp.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SellerViewModel(application: Application) : AndroidViewModel(application) {
    private val sellerRepository = SellerRepository(application)
    private val orderRepository = OrderRepository()
    private val productDao = WinnieBeeDatabase.getDatabase(application).productDao()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun loadProducts(sellerId: String) {
        viewModelScope.launch {
            productDao.getProductsBySeller(sellerId).collectLatest { entities ->
                _products.value = entities.map { entity ->
                    Product(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        price = entity.price,
                        category = entity.category,
                        sellerId = entity.sellerId,
                        sellerName = entity.sellerName,
                        imageUrls = if (entity.imageUrl.isNotEmpty()) listOf(entity.imageUrl) else emptyList(),
                        stockQuantity = entity.stockQuantity,
                        isVerified = entity.isVerified,
                        batchQrCodeUrl = entity.batchQrCodeUrl
                    )
                }
            }
        }
    }

    fun loadOrders(sellerId: String) {
        viewModelScope.launch {
            orderRepository.observeSellerOrders(sellerId).collectLatest { orders ->
                _orders.value = orders
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, newStatus)
            NotificationHelper.showOrderStatusUpdateNotification(getApplication(), orderId, newStatus)
        }
    }

    fun sendDeliveryStepAlert(orderId: String, stepIndex: Int) {
        viewModelScope.launch {
            val newStatus = when (stepIndex) {
                0 -> OrderStatus.PAID
                1 -> OrderStatus.PAID
                2 -> OrderStatus.SHIPPED
                3 -> OrderStatus.DELIVERED
                else -> OrderStatus.PAID
            }
            orderRepository.updateOrderStatus(orderId, newStatus)
            NotificationHelper.sendDeliveryStepAlert(getApplication(), orderId, stepIndex)
        }
    }

    fun addProduct(
        name: String,
        desc: String,
        price: Double,
        qty: Int,
        cat: String,
        img: String,
        batchQrCodeUrl: String?,
        sellerId: String,
        sellerName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                description = desc,
                price = price,
                stockQuantity = qty,
                category = cat,
                imageUrls = listOf(img),
                batchQrCodeUrl = batchQrCodeUrl,
                sellerId = sellerId,
                sellerName = sellerName
            )
            val result = sellerRepository.listProduct(product)
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }

    fun updateProduct(
        id: String,
        name: String,
        desc: String,
        price: Double,
        qty: Int,
        cat: String,
        img: String,
        batchQrCodeUrl: String?,
        sellerId: String,
        sellerName: String,
        isVerified: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val product = Product(
                id = id,
                name = name,
                description = desc,
                price = price,
                stockQuantity = qty,
                category = cat,
                imageUrls = if (img.isNotEmpty()) listOf(img) else emptyList(),
                batchQrCodeUrl = batchQrCodeUrl,
                sellerId = sellerId,
                sellerName = sellerName,
                isVerified = isVerified
            )
            val result = sellerRepository.updateProduct(product)
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }

    fun delistProduct(productId: String) {
        viewModelScope.launch {
            sellerRepository.delistProduct(productId)
        }
    }
}
