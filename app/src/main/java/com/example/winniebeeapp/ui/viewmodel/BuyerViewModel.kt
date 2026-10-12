package com.example.winniebeeapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.winniebeeapp.data.local.ProductEntity
import com.example.winniebeeapp.data.local.WinnieBeeDatabase
import com.example.winniebeeapp.data.model.CartItem
import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import com.example.winniebeeapp.data.model.Product
import com.example.winniebeeapp.data.model.Review
import com.example.winniebeeapp.data.repository.CartRepository
import com.example.winniebeeapp.data.repository.OrderRepository
import com.example.winniebeeapp.data.repository.ReviewRepository
import com.example.winniebeeapp.data.service.MomoPaymentServiceImpl
import com.example.winniebeeapp.data.service.PaymentService
import com.example.winniebeeapp.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BuyerViewModel(application: Application) : AndroidViewModel(application) {
    private val cartRepository = CartRepository()
    private val orderRepository = OrderRepository()
    private val reviewRepository = ReviewRepository(application)
    private val paymentService: PaymentService = MomoPaymentServiceImpl()
    private val productDao = WinnieBeeDatabase.getDatabase(application).productDao()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    val cartItems = cartRepository.cartItems

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _productReviews = MutableStateFlow<Map<String, List<Review>>>(emptyMap())
    val productReviews: StateFlow<Map<String, List<Review>>> = _productReviews.asStateFlow()

    private val _isPaymentLoading = MutableStateFlow(false)
    val isPaymentLoading: StateFlow<Boolean> = _isPaymentLoading.asStateFlow()

    private val _paymentError = MutableStateFlow<String?>(null)
    val paymentError: StateFlow<String?> = _paymentError.asStateFlow()

    init {
        viewModelScope.launch {
            productDao.getAllProducts().collectLatest { entities: List<ProductEntity> ->
                val localProducts = entities.map { entity ->
                    Product(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        price = entity.price,
                        category = entity.category,
                        sellerId = entity.sellerId,
                        sellerName = entity.sellerName,
                        imageUrls = if (entity.imageUrl.isNotEmpty()) listOf(entity.imageUrl) else emptyList(),
                        stockQuantity = entity.stockQuantity,
                        isVerified = entity.isVerified,
                        batchQrCodeUrl = entity.batchQrCodeUrl
                    )
                }
                
                // Merge with mock data if list is empty
                if (localProducts.isEmpty()) {
                    _products.value = listOf(
                        Product(
                            id = "1", 
                            name = "Pure Honey", 
                            description = "A natural sweetener, rich in antioxidants.", 
                            price = 15000.0, 
                            sellerName = "Emma's Farm", 
                            isVerified = true,
                            imageUrls = listOf("https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?q=80&w=500"),
                            batchQrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=WinnieBee-Batch-PureHoney-001"
                        ),
                        Product(
                            id = "2", 
                            name = "Golden Amber", 
                            description = "Light and sweet, perfect for tea and baking.", 
                            price = 18000.0, 
                            sellerName = "Central Apiary", 
                            isVerified = true,
                            imageUrls = listOf("https://images.unsplash.com/photo-1555507036-ab1f4038808a?q=80&w=500"),
                            batchQrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=WinnieBee-Batch-GoldenAmber-002"
                        ),
                        Product(
                            id = "3", 
                            name = "Propolis", 
                            description = "Known for its natural healing properties.", 
                            price = 25000.0, 
                            sellerName = "Nature's Cure",
                            imageUrls = listOf("https://images.unsplash.com/photo-1596719815613-43187c3e7215?q=80&w=500")
                        ),
                        Product(
                            id = "4", 
                            name = "Bee Wax",
                            description = "Pure beeswax for candles, balms, and crafts.", 
                            price = 12000.0, 
                            sellerName = "Wax Works",
                            isVerified = true,
                            imageUrls = listOf("https://images.unsplash.com/photo-1581441363689-1f3c3c414635?q=80&w=500"),
                            batchQrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=WinnieBee-Batch-BeeWax-004"
                        )
                    )
                } else {
                    _products.value = localProducts
                }
            }
        }

        viewModelScope.launch {
            reviewRepository.getAllReviews().collectLatest { reviewsList ->
                val grouped = reviewsList.groupBy { it.productId }
                _productReviews.value = grouped
            }
        }
    }

    fun addToCart(product: Product) {
        cartRepository.addToCart(
            CartItem(
                productId = product.id,
                productName = product.name,
                quantity = 1,
                unitPrice = product.price,
                imageUrl = product.imageUrls.firstOrNull(),
                batchQrCodeUrl = product.batchQrCodeUrl,
                sellerId = product.sellerId
            )
        )
    }

    fun removeFromCart(productId: String) {
        cartRepository.removeFromCart(productId)
    }

    fun checkout(address: String, phoneNumber: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isPaymentLoading.value = true
            _paymentError.value = null
            val total = cartRepository.getTotalAmount()
            val sellerIdFromCart = cartRepository.cartItems.value.firstOrNull { it.sellerId.isNotEmpty() }?.sellerId ?: ""

            val paymentResult = paymentService.initiateMobileMoneyPayment(phoneNumber, total)

            if (paymentResult.isSuccess) {
                val transactionId = paymentResult.getOrNull()

                val order = Order(
                    items = cartRepository.cartItems.value,
                    totalAmount = total,
                    deliveryAddress = address,
                    paymentTransactionId = transactionId,
                    status = OrderStatus.PENDING,
                    sellerId = sellerIdFromCart
                )
                
                val result = orderRepository.createOrder(order)
                if (result.isSuccess) {
                    val orderId = result.getOrNull() ?: ""
                    val createdOrder = order.copy(id = orderId)
                    
                    val verification = paymentService.verifyPayment(transactionId ?: "")
                    val finalOrder = if (verification.isSuccess) {
                        orderRepository.updateOrderStatus(orderId, OrderStatus.PAID)
                        val paidOrder = createdOrder.copy(status = OrderStatus.PAID)
                        _orders.update { current -> current + paidOrder }
                        paidOrder
                    } else {
                        _orders.update { current -> current + createdOrder }
                        createdOrder
                    }

                    // Issue push notification for order placed
                    NotificationHelper.showOrderPlacedNotification(getApplication(), finalOrder)

                    cartRepository.clearCart()
                    onComplete()
                } else {
                    _paymentError.value = "Failed to create order locally"
                }
            } else {
                _paymentError.value = paymentResult.exceptionOrNull()?.message ?: "Payment initiation failed"
            }
            _isPaymentLoading.value = false
        }
    }

    fun clearPaymentError() {
        _paymentError.value = null
    }

    fun confirmOrderReceipt(orderId: String) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, OrderStatus.COMPLETED)
            if (result.isSuccess) {
                _orders.update { current ->
                    current.map { if (it.id == orderId) it.copy(status = OrderStatus.COMPLETED) else it }
                }
                NotificationHelper.showOrderStatusUpdateNotification(getApplication(), orderId, OrderStatus.COMPLETED)
            }
        }
    }

    fun submitVerifiedReview(
        order: Order,
        productId: String,
        productName: String,
        buyerId: String,
        buyerName: String,
        rating: Float,
        comment: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = reviewRepository.submitReviewForOrder(
                order = order,
                productId = productId,
                productName = productName,
                buyerId = buyerId,
                buyerName = buyerName,
                rating = rating,
                comment = comment
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to submit review")
            }
        }
    }
    
    fun getTotalAmount() = cartRepository.getTotalAmount()
}
