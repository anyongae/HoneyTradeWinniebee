package com.example.winniebeeapp.data.repository

import com.example.winniebeeapp.data.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    fun addToCart(item: CartItem) {
        _cartItems.update { current ->
            val existing = current.find { it.productId == item.productId }
            if (existing != null) {
                current.map { if (it.productId == item.productId) it.copy(quantity = it.quantity + item.quantity) else it }
            } else {
                current + item
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.update { current -> current.filter { it.productId != productId } }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getTotalAmount(): Double {
        return _cartItems.value.sumOf { it.unitPrice * it.quantity }
    }
}
