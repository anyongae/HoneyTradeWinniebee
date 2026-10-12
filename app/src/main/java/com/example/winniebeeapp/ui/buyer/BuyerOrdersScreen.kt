package com.example.winniebeeapp.ui.buyer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.winniebeeapp.data.model.CartItem
import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import com.example.winniebeeapp.ui.theme.NavyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerOrdersScreen(
    orders: List<Order>,
    onOrderClick: (String) -> Unit,
    onSubmitReview: (Order, String, String, Float, String, () -> Unit, (String) -> Unit) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("My Orders & Tracking", color = NavyBlue) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("You haven't placed any orders yet.", color = NavyBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderItemCard(order, onOrderClick = { onOrderClick(order.id) }, onSubmitReview = onSubmitReview)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemCard(
    order: Order,
    onOrderClick: () -> Unit,
    onSubmitReview: (Order, String, String, Float, String, () -> Unit, (String) -> Unit) -> Unit
) {
    var selectedItemForReview by remember { mutableStateOf<CartItem?>(null) }
    val isVerifiedForReview = order.status == OrderStatus.COMPLETED || order.status == OrderStatus.PAID || order.status == OrderStatus.DELIVERED
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Order #${order.id.take(8)}", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
                Text(order.status.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Total: UGX ${order.totalAmount}", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text("Order Items:", style = MaterialTheme.typography.labelLarge, color = NavyBlue)
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.productName, style = MaterialTheme.typography.bodyMedium)
                        Text("Qty: ${item.quantity} x UGX ${item.unitPrice}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    if (isVerifiedForReview) {
                        OutlinedButton(
                            onClick = { selectedItemForReview = item },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Rate & Review ✅", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (order.status == OrderStatus.DELIVERED) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOrderClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Receipt (Release Funds) 📍")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOrderClick, modifier = Modifier.align(Alignment.End)) {
                    Text("Track Order Details", color = NavyBlue)
                }
            }
        }
    }

    if (selectedItemForReview != null) {
        val item = selectedItemForReview!!
        LeaveReviewDialog(
            productName = item.productName,
            onDismiss = { selectedItemForReview = null },
            onSubmit = { rating, comment ->
                onSubmitReview(
                    order,
                    item.productId,
                    item.productName,
                    rating,
                    comment,
                    {
                        Toast.makeText(context, "Verified Review Submitted ✅", Toast.LENGTH_SHORT).show()
                        selectedItemForReview = null
                    },
                    { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun LeaveReviewDialog(
    productName: String,
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(5.0f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column {
                Text("Rate & Review Product", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
                Text("Verified Purchase ✅", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Product: $productName", style = MaterialTheme.typography.bodyMedium, color = NavyBlue)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Your Rating:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { rating = star.toFloat() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= rating) Color(0xFFFFB300) else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Write your verified review") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = comment.isNotBlank()
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NavyBlue)
            }
        }
    )
}
