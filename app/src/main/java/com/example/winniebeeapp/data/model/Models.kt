package com.example.winniebeeapp.data.model

import com.google.firebase.Timestamp

enum class UserRole {
    BUYER, SELLER, ADMIN
}

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.BUYER,
    val isVerified: Boolean = false,
    val phoneNumber: String = "",
    val region: String = "",
    val age: Int = 0,
    val sex: String = "",
    val maaifCertificateUrl: String? = null,
    val batchQrCodeUrl: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val imageUrls: List<String> = emptyList(),
    val stockQuantity: Int = 0,
    val isVerified: Boolean = false,
    val batchId: String? = null,
    val batchQrCodeUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)

enum class OrderStatus {
    PENDING,    // Created, awaiting payment initiation
    PAID,       // Payment confirmed, held in escrow
    SHIPPED,    // Seller has dispatched
    DELIVERED,  // Arrived at buyer's location
    COMPLETED,  // Buyer confirmed receipt, funds released to seller
    DISPUTED,   // Buyer raised an issue
    CANCELLED   // Order cancelled before completion
}

data class Order(
    val id: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val deliveryAddress: String = "",
    val deliveryCoordinates: LatLng? = null,
    val courierId: String? = null,
    val paymentTransactionId: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val imageUrl: String? = null,
    val batchQrCodeUrl: String? = null,
    val sellerId: String = ""
)

data class LatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class Review(
    val id: String = "",
    val orderId: String = "",
    val productId: String = "",
    val productName: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val rating: Float = 5.0f,
    val comment: String = "",
    val isVerifiedPurchase: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
