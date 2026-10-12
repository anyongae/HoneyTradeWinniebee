package com.example.winniebeeapp.ui.seller

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import com.example.winniebeeapp.ui.theme.NavyBlue
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(
    orders: List<Order>,
    onUpdateStatus: (String, OrderStatus) -> Unit,
    onSendDeliveryAlert: (String, Int) -> Unit,
    onTrackOrder: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = NavyBlue,
                    navigationIconContentColor = NavyBlue
                ),
                title = { Text("Manage Orders & Alerts", color = NavyBlue) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyBlue)
                    }
                }
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No orders received yet", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("When a buyer places an order, it will appear here automatically.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(orders, key = { it.id }) { order ->
                    SellerOrderItem(
                        order = order,
                        onUpdateStatus = onUpdateStatus,
                        onSendDeliveryAlert = onSendDeliveryAlert,
                        onTrackOrder = onTrackOrder
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellerOrderItem(
    order: Order,
    onUpdateStatus: (String, OrderStatus) -> Unit,
    onSendDeliveryAlert: (String, Int) -> Unit,
    onTrackOrder: (String) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id.take(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = NavyBlue
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (order.status) {
                        OrderStatus.PAID -> Color(0xFFE3F2FD)
                        OrderStatus.SHIPPED -> Color(0xFFFFF3E0)
                        OrderStatus.DELIVERED -> Color(0xFFE8F5E9)
                        OrderStatus.COMPLETED -> Color(0xFFE8F5E9)
                        else -> Color(0xFFF5F5F5)
                    }
                ) {
                    Text(
                        text = order.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = NavyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val itemsText = order.items.joinToString { "${it.quantity}x ${it.productName}" }.ifEmpty { "Honey Items" }
            Text("Items: $itemsText", style = MaterialTheme.typography.bodyMedium)
            Text("Total Amount: UGX ${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}", style = MaterialTheme.typography.titleSmall, color = NavyBlue)
            if (order.deliveryAddress.isNotEmpty()) {
                Text("Delivery Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Simulation Push Alert Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Simulate Delivery Step Push Alert to Buyer:",
                    style = MaterialTheme.typography.labelMedium,
                    color = NavyBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onSendDeliveryAlert(order.id, 0)
                        Toast.makeText(context, "Alert Sent: 1. Order Placed", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("1. Order Placed 🐝", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        onSendDeliveryAlert(order.id, 1)
                        Toast.makeText(context, "Alert Sent: 2. Processing & Packing", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("2. Packing 📦", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        onSendDeliveryAlert(order.id, 2)
                        Toast.makeText(context, "Alert Sent: 3. Out for Delivery", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("3. Out for Delivery 🚚", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        onSendDeliveryAlert(order.id, 3)
                        Toast.makeText(context, "Alert Sent: 4. Delivered", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("4. Delivered 📍", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.DELIVERED) {
                    Button(onClick = { onTrackOrder(order.id) }) {
                        Text("Track/Update Location")
                    }
                }
            }
        }
    }
}
