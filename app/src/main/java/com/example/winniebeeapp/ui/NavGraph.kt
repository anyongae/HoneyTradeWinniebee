package com.example.winniebeeapp.ui

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.winniebeeapp.data.model.UserRole
import com.example.winniebeeapp.ui.admin.AdminDashboard
import com.example.winniebeeapp.ui.auth.LoginScreen
import com.example.winniebeeapp.ui.auth.RegistrationScreen
import com.example.winniebeeapp.ui.buyer.BuyerOrdersScreen
import com.example.winniebeeapp.ui.buyer.CartScreen
import com.example.winniebeeapp.ui.buyer.ProductDiscoveryScreen
import com.example.winniebeeapp.ui.common.OrderTrackingScreen
import com.example.winniebeeapp.ui.seller.AddProductScreen
import com.example.winniebeeapp.ui.seller.EditProductScreen
import com.example.winniebeeapp.ui.seller.SellerDashboard
import com.example.winniebeeapp.ui.seller.SellerOrdersScreen
import com.example.winniebeeapp.ui.viewmodel.AuthViewModel
import com.example.winniebeeapp.ui.viewmodel.BuyerViewModel
import com.example.winniebeeapp.ui.viewmodel.OrderTrackingViewModel
import com.example.winniebeeapp.ui.viewmodel.SellerViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registration : Screen("registration")
    object BuyerHome : Screen("buyer_home")
    object SellerDashboard : Screen("seller_dashboard")
    object SellerOrders : Screen("seller_orders")
    object AddProduct : Screen("add_product")
    object EditProduct : Screen("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
    object Cart : Screen("cart")
    object BuyerOrders : Screen("buyer_orders")
    object AdminDashboard : Screen("admin_dashboard")
    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }
}

@Composable
fun WinnieBeeNavGraph(
    navController: NavHostController = rememberNavController(),
    initialOrderId: String? = null
) {
    val authViewModel: AuthViewModel = viewModel()
    val buyerViewModel: BuyerViewModel = viewModel()
    val sellerViewModel: SellerViewModel = viewModel()
    val trackingViewModel: OrderTrackingViewModel = viewModel()
    val currentUser by authViewModel.user.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(initialOrderId) {
        if (!initialOrderId.isNullOrEmpty()) {
            navController.navigate(Screen.OrderTracking.createRoute(initialOrderId))
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLogin = { email, pass ->
                    authViewModel.login(email, pass) { user ->
                        when (user.role) {
                            UserRole.ADMIN -> {
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            UserRole.SELLER -> {
                                navController.navigate(Screen.SellerDashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            else -> {
                                navController.navigate(Screen.BuyerHome.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Registration.route)
                }
            )
        }

        composable(Screen.Registration.route) {
            RegistrationScreen(
                authViewModel = authViewModel,
                onRegister = { name, email, pass, role, mobile, region, age, sex, maaifUrl ->
                    authViewModel.register(name, email, pass, role, mobile, region, age, sex, maaifUrl) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Registration.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.BuyerHome.route) {
            val products by buyerViewModel.products.collectAsState()
            val productReviews by buyerViewModel.productReviews.collectAsState()
            ProductDiscoveryScreen(
                products = products,
                productReviews = productReviews,
                onAddToCart = { product -> buyerViewModel.addToCart(product) },
                onViewCart = { navController.navigate(Screen.Cart.route) },
                onViewOrders = { navController.navigate(Screen.BuyerOrders.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.BuyerHome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SellerDashboard.route) {
            val products by sellerViewModel.products.collectAsState()
            
            LaunchedEffect(currentUser) {
                currentUser?.let { sellerViewModel.loadProducts(it.id) }
            }

            SellerDashboard(
                products = products, 
                onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onEditProduct = { product -> navController.navigate(Screen.EditProduct.createRoute(product.id)) },
                onDelistProduct = { productId -> sellerViewModel.delistProduct(productId) },
                onVerifyBatch = { /* Trigger batch verification */ },
                onViewOrders = { navController.navigate(Screen.SellerOrders.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SellerDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SellerOrders.route) {
            val orders by sellerViewModel.orders.collectAsState()
            
            LaunchedEffect(currentUser) {
                currentUser?.let { sellerViewModel.loadOrders(it.id) }
            }

            SellerOrdersScreen(
                orders = orders,
                onUpdateStatus = { id, status -> sellerViewModel.updateOrderStatus(id, status) },
                onSendDeliveryAlert = { id, stepIndex -> sellerViewModel.sendDeliveryStepAlert(id, stepIndex) },
                onTrackOrder = { id -> navController.navigate(Screen.OrderTracking.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddProduct.route) {
            AddProductScreen(
                onAddProduct = { name, desc, price, qty, cat, img, batchQrUrl ->
                    currentUser?.let { user ->
                        sellerViewModel.addProduct(name, desc, price, qty, cat, img, batchQrUrl, user.id, user.name) {
                            navController.popBackStack()
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProduct.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val products by sellerViewModel.products.collectAsState()
            val productToEdit = products.find { it.id == productId }

            if (productToEdit != null) {
                EditProductScreen(
                    product = productToEdit,
                    onUpdateProduct = { id, name, desc, price, qty, cat, img, batchQrUrl ->
                        currentUser?.let { user ->
                            sellerViewModel.updateProduct(
                                id = id,
                                name = name,
                                desc = desc,
                                price = price,
                                qty = qty,
                                cat = cat,
                                img = img,
                                batchQrCodeUrl = batchQrUrl,
                                sellerId = user.id,
                                sellerName = user.name,
                                isVerified = productToEdit.isVerified,
                                onSuccess = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Cart.route) {
            val cartItems by buyerViewModel.cartItems.collectAsState()
            val isPaymentLoading by buyerViewModel.isPaymentLoading.collectAsState()
            val paymentError by buyerViewModel.paymentError.collectAsState()
            CartScreen(
                items = cartItems,
                totalAmount = buyerViewModel.getTotalAmount(),
                isPaymentLoading = isPaymentLoading,
                paymentError = paymentError,
                onRemoveItem = { id -> buyerViewModel.removeFromCart(id) },
                onCheckout = { address, phone ->
                    buyerViewModel.checkout(address, phone) {
                        Toast.makeText(context, "Thank you for shopping with WinnieBee App", Toast.LENGTH_LONG).show()
                        navController.navigate(Screen.BuyerHome.route) {
                            popUpTo(Screen.BuyerHome.route) { inclusive = true }
                        }
                    }
                },
                onDismissError = { buyerViewModel.clearPaymentError() }
            )
        }

        composable(Screen.BuyerOrders.route) {
            val orders by buyerViewModel.orders.collectAsState()
            BuyerOrdersScreen(
                orders = orders,
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderTracking.createRoute(orderId))
                },
                onSubmitReview = { order, productId, productName, rating, comment, onSuccess, onError ->
                    currentUser?.let { user ->
                        buyerViewModel.submitVerifiedReview(
                            order = order,
                            productId = productId,
                            productName = productName,
                            buyerId = user.id,
                            buyerName = user.name.ifEmpty { "Verified Buyer" },
                            rating = rating,
                            comment = comment,
                            onSuccess = onSuccess,
                            onError = onError
                        )
                    } ?: onError("Please log in to submit a review.")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OrderTracking.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val order by trackingViewModel.order.collectAsState()
            val location by trackingViewModel.location.collectAsState()
            val isBuyer = currentUser?.role != UserRole.SELLER

            LaunchedEffect(orderId) {
                trackingViewModel.startTracking(orderId)
            }

            OrderTrackingScreen(
                order = order,
                currentLocation = location,
                isBuyer = isBuyer,
                onConfirmReceipt = {
                    trackingViewModel.confirmReceipt(orderId)
                    buyerViewModel.confirmOrderReceipt(orderId)
                    Toast.makeText(context, "Receipt Confirmed! Funds released to seller. 🎉", Toast.LENGTH_LONG).show()
                },
                onUpdateLocation = { latLng ->
                    trackingViewModel.updateCourierLocation(orderId, latLng)
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
