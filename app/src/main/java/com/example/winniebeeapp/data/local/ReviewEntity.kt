package com.example.winniebeeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val buyerId: String,
    val buyerName: String,
    val rating: Float,
    val comment: String,
    val isVerifiedPurchase: Boolean = true,
    val createdAt: Long
)
