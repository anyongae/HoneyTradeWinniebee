package com.example.winniebeeapp.data.repository

import android.content.Context
import com.example.winniebeeapp.data.local.ReviewEntity
import com.example.winniebeeapp.data.local.WinnieBeeDatabase
import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import com.example.winniebeeapp.data.model.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ReviewRepository(context: Context) {
    private val reviewDao = WinnieBeeDatabase.getDatabase(context).reviewDao()

    suspend fun submitReviewForOrder(
        order: Order,
        productId: String,
        productName: String,
        buyerId: String,
        buyerName: String,
        rating: Float,
        comment: String
    ): Result<Review> {
        // STRICT RULE: Verified purchase validation!
        // Reviews can ONLY be submitted against a paid & completed order!
        if (order.status != OrderStatus.COMPLETED && order.status != OrderStatus.PAID && order.status != OrderStatus.DELIVERED) {
            return Result.failure(Exception("Reviews are only allowed for paid/completed orders to prevent fake reviews."))
        }

        // Check if item was part of the order
        val hasProductInOrder = order.items.any { it.productId == productId }
        if (!hasProductInOrder) {
            return Result.failure(Exception("This product was not part of the verified order."))
        }

        val reviewId = UUID.randomUUID().toString()
        val reviewEntity = ReviewEntity(
            id = reviewId,
            orderId = order.id,
            productId = productId,
            productName = productName,
            buyerId = buyerId,
            buyerName = buyerName,
            rating = rating,
            comment = comment,
            isVerifiedPurchase = true,
            createdAt = System.currentTimeMillis()
        )

        return try {
            reviewDao.insertReview(reviewEntity)
            Result.success(
                Review(
                    id = reviewEntity.id,
                    orderId = reviewEntity.orderId,
                    productId = reviewEntity.productId,
                    productName = reviewEntity.productName,
                    buyerId = reviewEntity.buyerId,
                    buyerName = reviewEntity.buyerName,
                    rating = reviewEntity.rating,
                    comment = reviewEntity.comment,
                    isVerifiedPurchase = true,
                    createdAt = reviewEntity.createdAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReviewsForProduct(productId: String): Flow<List<Review>> {
        return reviewDao.getReviewsForProduct(productId).map { entities ->
            entities.map { entity ->
                Review(
                    id = entity.id,
                    orderId = entity.orderId,
                    productId = entity.productId,
                    productName = entity.productName,
                    buyerId = entity.buyerId,
                    buyerName = entity.buyerName,
                    rating = entity.rating,
                    comment = entity.comment,
                    isVerifiedPurchase = entity.isVerifiedPurchase,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    fun getAllReviews(): Flow<List<Review>> {
        return reviewDao.getAllReviews().map { entities ->
            entities.map { entity ->
                Review(
                    id = entity.id,
                    orderId = entity.orderId,
                    productId = entity.productId,
                    productName = entity.productName,
                    buyerId = entity.buyerId,
                    buyerName = entity.buyerName,
                    rating = entity.rating,
                    comment = entity.comment,
                    isVerifiedPurchase = entity.isVerifiedPurchase,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    suspend fun hasUserReviewed(orderId: String, productId: String): Boolean {
        return reviewDao.getReviewByOrderAndProduct(orderId, productId) != null
    }
}
