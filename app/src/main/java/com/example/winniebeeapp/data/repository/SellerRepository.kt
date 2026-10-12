package com.example.winniebeeapp.data.repository

import android.content.Context
import com.example.winniebeeapp.data.local.ProductEntity
import com.example.winniebeeapp.data.local.WinnieBeeDatabase
import com.example.winniebeeapp.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SellerRepository(context: Context) {
    private val productDao = WinnieBeeDatabase.getDatabase(context).productDao()

    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    suspend fun listProduct(product: Product): Result<String> {
        val productId = UUID.randomUUID().toString()
        val productWithId = product.copy(id = productId)

        // 1. Save Locally
        try {
            productDao.insertProduct(ProductEntity(
                id = productId,
                name = product.name,
                description = product.description,
                price = product.price,
                category = product.category,
                sellerId = product.sellerId,
                sellerName = product.sellerName,
                imageUrl = product.imageUrls.firstOrNull() ?: "",
                stockQuantity = product.stockQuantity,
                isVerified = product.isVerified,
                batchQrCodeUrl = product.batchQrCodeUrl
            ))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        // 2. Optional Firebase Sync
        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("products").document(productId).set(productWithId).await()
            } catch (e: Exception) {
                // Ignore sync errors for now
            }
        }

        return Result.success(productId)
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        try {
            productDao.updateProduct(ProductEntity(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                category = product.category,
                sellerId = product.sellerId,
                sellerName = product.sellerName,
                imageUrl = product.imageUrls.firstOrNull() ?: "",
                stockQuantity = product.stockQuantity,
                isVerified = product.isVerified,
                batchQrCodeUrl = product.batchQrCodeUrl
            ))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("products").document(product.id).set(product).await()
            } catch (e: Exception) {
                // Ignore sync errors
            }
        }

        return Result.success(Unit)
    }

    suspend fun delistProduct(productId: String): Result<Unit> {
        try {
            productDao.deleteProductById(productId)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("products").document(productId).delete().await()
            } catch (e: Exception) {
                // Ignore sync errors
            }
        }

        return Result.success(Unit)
    }

    suspend fun submitVerificationRequest(sellerId: String, documents: List<String>): Result<Unit> {
        val fs = firestore ?: return Result.failure(Exception("Firebase not initialized"))
        return try {
            val request = mapOf(
                "sellerId" to sellerId,
                "documents" to documents,
                "status" to "PENDING",
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            fs.collection("verification_requests").add(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitBatchForVerification(productIds: List<String>, batchInfo: String): Result<String> {
        val fs = firestore ?: return Result.failure(Exception("Firebase not initialized"))
        return try {
            val batchId = UUID.randomUUID().toString()
            val batchData = mapOf(
                "batchId" to batchId,
                "productIds" to productIds,
                "batchInfo" to batchInfo,
                "status" to "PENDING",
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            fs.collection("batches").document(batchId).set(batchData).await()
            
            // Update products with batchId
            val batch = fs.batch()
            productIds.forEach { id ->
                val ref = fs.collection("products").document(id)
                batch.update(ref, "batchId", batchId)
            }
            batch.commit().await()
            
            Result.success(batchId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
