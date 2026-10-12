package com.example.winniebeeapp.data.repository

import android.content.Context
import com.example.winniebeeapp.data.local.ProductEntity
import com.example.winniebeeapp.data.local.UserEntity
import com.example.winniebeeapp.data.local.WinnieBeeDatabase

class AdminRepository(context: Context) {
    private val database = WinnieBeeDatabase.getDatabase(context)
    private val userDao = database.userDao()
    private val productDao = database.productDao()

    suspend fun getUnverifiedSellers(): List<UserEntity> {
        return userDao.getUnverifiedSellers()
    }

    suspend fun verifyUser(userId: String): Int {
        return userDao.verifyUser(userId)
    }

    suspend fun getUnverifiedProducts(): List<ProductEntity> {
        return productDao.getUnverifiedProducts()
    }

    suspend fun verifyProduct(productId: String): Int {
        return productDao.verifyProduct(productId)
    }
}
