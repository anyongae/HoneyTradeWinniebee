package com.example.winniebeeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    fun getReviewsForProduct(productId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE orderId = :orderId AND productId = :productId LIMIT 1")
    suspend fun getReviewByOrderAndProduct(orderId: String, productId: String): ReviewEntity?

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>
}
