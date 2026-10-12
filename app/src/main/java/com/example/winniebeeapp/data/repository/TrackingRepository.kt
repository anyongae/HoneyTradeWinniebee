package com.example.winniebeeapp.data.repository

import com.example.winniebeeapp.data.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TrackingRepository {
    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    suspend fun updateLocation(orderId: String, location: LatLng): Result<Unit> {
        val fs = firestore ?: return Result.failure(Exception("Firebase not initialized"))
        return try {
            fs.collection("orders").document(orderId)
                .update("deliveryCoordinates", location)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeLocation(orderId: String): Flow<LatLng?> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val subscription = fs.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val location = snapshot?.get("deliveryCoordinates") as? Map<String, Double>
                val latLng = location?.let {
                    LatLng(it["latitude"] ?: 0.0, it["longitude"] ?: 0.0)
                }
                trySend(latLng)
            }
        awaitClose { subscription.remove() }
    }
}
