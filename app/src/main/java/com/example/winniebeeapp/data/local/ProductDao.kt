package com.example.winniebeeapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity): Int

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sellerId = :sellerId")
    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isVerified = 0")
    suspend fun getUnverifiedProducts(): List<ProductEntity>

    @Query("UPDATE products SET isVerified = 1 WHERE id = :productId")
    suspend fun verifyProduct(productId: String): Int

    @Delete
    suspend fun deleteProduct(product: ProductEntity): Int

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String): Int
}
