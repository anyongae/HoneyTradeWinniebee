package com.example.winniebeeapp.data.repository

import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderRepository {
    companion object {
        private val _localOrders = MutableStateFlow<List<Order>>(emptyList())
        val localOrders: StateFlow<List<Order>> = _localOrders.asStateFlow()
    }

    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    suspend fun createOrder(order: Order): Result<String> {
        val orderId = if (order.id.isNotEmpty()) order.id else java.util.UUID.randomUUID().toString()
        val orderWithId = order.copy(id = orderId, status = OrderStatus.PENDING)

        _localOrders.update { current ->
            val existing = current.filter { it.id != orderId }
            existing + orderWithId
        }

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("orders").document(orderId).set(orderWithId).await()
            } catch (e: Exception) {
                // Ignore sync errors for local-first architecture
            }
        }

        return Result.success(orderId)
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        _localOrders.update { current ->
            current.map {
                if (it.id == orderId) it.copy(status = newStatus, updatedAt = com.google.firebase.Timestamp.now()) else it
            }
        }

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("orders").document(orderId)
                    .update("status", newStatus, "updatedAt", com.google.firebase.Timestamp.now())
                    .await()
            } catch (e: Exception) {
                // Ignore sync errors for local-first architecture
            }
        }
        return Result.success(Unit)
    }

    fun observeOrder(orderId: String): Flow<Order?> = callbackFlow {
        val job = CoroutineScope(Dispatchers.IO).launch {
            _localOrders.collect { list ->
                val found = list.find { it.id == orderId }
                if (found != null) {
                    trySend(found)
                }
            }
        }

        val fs = firestore
        if (fs != null) {
            val subscription = fs.collection("orders").document(orderId)
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val order = snapshot.toObject(Order::class.java)
                        if (order != null) {
                            trySend(order)
                        }
                    }
                }
            awaitClose {
                subscription.remove()
                job.cancel()
            }
        } else {
            awaitClose { job.cancel() }
        }
    }

    fun observeBuyerOrders(buyerId: String): Flow<List<Order>> = callbackFlow {
        val job = CoroutineScope(Dispatchers.IO).launch {
            _localOrders.collect { list ->
                val buyerList = if (buyerId.isEmpty()) list else list.filter { it.buyerId.isEmpty() || it.buyerId == buyerId }
                trySend(buyerList)
            }
        }

        val fs = firestore
        if (fs != null) {
            val subscription = fs.collection("orders")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val remoteOrders = snapshot.toObjects(Order::class.java)
                        val combined = (_localOrders.value + remoteOrders).distinctBy { it.id }
                        val filtered = if (buyerId.isEmpty()) combined else combined.filter { it.buyerId.isEmpty() || it.buyerId == buyerId }
                        trySend(filtered)
                    }
                }
            awaitClose {
                subscription.remove()
                job.cancel()
            }
        } else {
            awaitClose { job.cancel() }
        }
    }

    fun observeSellerOrders(sellerId: String): Flow<List<Order>> = callbackFlow {
        val job = CoroutineScope(Dispatchers.IO).launch {
            _localOrders.collect { list ->
                val sellerList = if (sellerId.isEmpty()) list else list.filter { it.sellerId.isEmpty() || it.sellerId == sellerId }
                trySend(sellerList)
            }
        }

        val fs = firestore
        if (fs != null) {
            val subscription = fs.collection("orders")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        val remoteOrders = snapshot.toObjects(Order::class.java)
                        val combined = (_localOrders.value + remoteOrders).distinctBy { it.id }
                        val filtered = if (sellerId.isEmpty()) combined else combined.filter { it.sellerId.isEmpty() || it.sellerId == sellerId }
                        trySend(filtered)
                    }
                }
            awaitClose {
                subscription.remove()
                job.cancel()
            }
        } else {
            awaitClose { job.cancel() }
        }
    }
}
