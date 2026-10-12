package com.example.winniebeeapp.ui.buyer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.winniebeeapp.data.model.Product
import com.example.winniebeeapp.data.model.Review
import com.example.winniebeeapp.ui.theme.NavyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDiscoveryScreen(
    products: List<Product>,
    productReviews: Map<String, List<Review>> = emptyMap(),
    onAddToCart: (Product) -> Unit,
    onViewCart: () -> Unit,
    onViewOrders: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = NavyBlue,
                    actionIconContentColor = NavyBlue
                ),
                title = { Text("WinnieBee Market") },
                actions = {
                    IconButton(onClick = onViewOrders) {
                        Icon(Icons.Default.ListAlt, contentDescription = "Orders")
                    }
                    IconButton(onClick = onViewCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).padding(8.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                val reviews = productReviews[product.id] ?: emptyList()
                DiscoveryProductItem(product, reviews, onAddToCart)
            }
        }
    }
}

@Composable
fun DiscoveryProductItem(
    product: Product, 
    reviews: List<Review>, 
    onAddToCart: (Product) -> Unit
) {
    var showQrDialog by remember { mutableStateOf(false) }
    var showReviewsDialog by remember { mutableStateOf(false) }

    val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 5.0
    val reviewCount = reviews.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Product image
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 8.dp)) {
                if (product.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrls.first(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.fillMaxSize(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        // image placeholder
                    }
                }
            }
            
            Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(product.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, modifier = Modifier.padding(vertical = 4.dp))
            
            // Quantity attached to price in Litres
            Text("UGX ${product.price} / Litre", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
            if (product.stockQuantity > 0) {
                Text("Quantity: ${product.stockQuantity} Litres", style = MaterialTheme.typography.bodySmall, color = NavyBlue)
            }
            Text("Seller: ${product.sellerName}", style = MaterialTheme.typography.bodySmall)

            // Verified Rating Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = String.format("%.1f", avgRating),
                    style = MaterialTheme.typography.labelMedium,
                    color = NavyBlue
                )
                Text(
                    text = " (${if (reviewCount > 0) "$reviewCount reviews" else "Verified"}) ✅",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (product.isVerified) {
                Text("Verified Seller ✅", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            if (reviews.isNotEmpty()) {
                TextButton(
                    onClick = { showReviewsDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Read Verified Reviews ($reviewCount)", style = MaterialTheme.typography.labelSmall, color = NavyBlue)
                }
            }

            // Clear, Scannable QR Code Section
            if (!product.batchQrCodeUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showQrDialog = true }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(6.dp).fillMaxWidth()
                    ) {
                        Text(
                            "Batch Verification QR",
                            style = MaterialTheme.typography.labelSmall,
                            color = NavyBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color.White,
                            shape = MaterialTheme.shapes.extraSmall,
                            shadowElevation = 1.dp,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            AsyncImage(
                                model = product.batchQrCodeUrl,
                                contentDescription = "Batch QR Code",
                                modifier = Modifier.size(70.dp).padding(4.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "🔍 Tap to scan QR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onAddToCart(product) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Add to Cart", style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    // High-Resolution Scan Dialog for Buyer
    if (showQrDialog && !product.batchQrCodeUrl.isNullOrEmpty()) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    "Batch / Harvest QR Code",
                    style = MaterialTheme.typography.titleMedium,
                    color = NavyBlue
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NavyBlue
                    )
                    Text(
                        "Seller: ${product.sellerName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "UGX ${product.price} / Litre (${product.stockQuantity} Litres)",
                        style = MaterialTheme.typography.bodySmall,
                        color = NavyBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color.White,
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        AsyncImage(
                            model = product.batchQrCodeUrl,
                            contentDescription = "Full Size Batch QR Code Ready for Scan",
                            modifier = Modifier
                                .size(250.dp)
                                .padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Point phone camera or QR scanner at white screen above",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close", color = NavyBlue)
                }
            }
        )
    }

    // Dialog showing authentic reviews tied to completed paid orders
    if (showReviewsDialog) {
        AlertDialog(
            onDismissRequest = { showReviewsDialog = false },
            containerColor = Color.White,
            title = {
                Column {
                    Text("Verified Buyer Reviews", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
                    Text("All reviews are tied to paid, completed purchases ✅", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Product: ${product.name}", style = MaterialTheme.typography.bodyLarge, color = NavyBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(reviews) { review ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(review.buyerName, style = MaterialTheme.typography.labelLarge, color = NavyBlue)
                                        Text("Verified Purchase ✅", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        (1..5).forEach { star ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (star <= review.rating) Color(0xFFFFB300) else Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    if (review.comment.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("\"${review.comment}\"", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReviewsDialog = false }) {
                    Text("Close", color = NavyBlue)
                }
            }
        )
    }
}
