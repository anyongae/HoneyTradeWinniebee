package com.example.winniebeeapp.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.winniebeeapp.data.model.LatLng
import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import com.example.winniebeeapp.ui.theme.NavyBlue

@Composable
fun OrderTrackingScreen(
    order: Order?,
    currentLocation: LatLng?,
    isBuyer: Boolean = true,
    onConfirmReceipt: () -> Unit,
    onUpdateLocation: (LatLng) -> Unit = {}
) {
    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp), color = NavyBlue)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Order Tracking #${order.id.take(8)}", style = MaterialTheme.typography.headlineSmall, color = NavyBlue)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status: ${order.status.name}", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
                Text("Delivery Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)

                if (currentLocation != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current Courier Coordinates: ${currentLocation.latitude}, ${currentLocation.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulate courier movement for seller or test view
        if (!isBuyer && (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.DELIVERED)) {
            Button(
                onClick = {
                    val nextLat = (currentLocation?.latitude ?: 0.3476) + 0.001
                    val nextLng = (currentLocation?.longitude ?: 32.5825) + 0.001
                    onUpdateLocation(LatLng(nextLat, nextLng))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simulate Courier Movement")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Order Journey", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
        Spacer(modifier = Modifier.height(8.dp))

        OrderStatusStepper(currentStatus = order.status)

        Spacer(modifier = Modifier.weight(1f))

        if (order.status == OrderStatus.COMPLETED) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🎉 Order Completed! Escrow funds released to seller.",
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyBlue
                    )
                }
            }
        } else if (isBuyer && order.status == OrderStatus.DELIVERED) {
            Button(
                onClick = onConfirmReceipt,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Receipt (Release Funds)")
            }
        }
    }
}

@Composable
fun OrderStatusStepper(currentStatus: OrderStatus) {
    val statuses = listOf(
        OrderStatus.PENDING,
        OrderStatus.PAID,
        OrderStatus.SHIPPED,
        OrderStatus.DELIVERED,
        OrderStatus.COMPLETED
    )

    Column {
        statuses.forEach { status ->
            val isCompleted = status.ordinal <= currentStatus.ordinal
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = if (isCompleted) "● " else "○ ",
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Text(
                    text = status.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
