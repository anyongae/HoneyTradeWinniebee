package com.example.winniebeeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.winniebeeapp.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val pass: String,
    val role: String, // Changed to String
    val phoneNumber: String,
    val region: String,
    val age: Int,
    val sex: String,
    val isVerified: Boolean,
    val maaifCertificateUrl: String? = null,
    val batchQrCodeUrl: String? = null
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val sellerId: String,
    val sellerName: String,
    val imageUrl: String,
    val stockQuantity: Int,
    val isVerified: Boolean,
    val batchQrCodeUrl: String? = null
)
