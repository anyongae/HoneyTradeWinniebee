package com.example.winniebeeapp.ui.buyer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.winniebeeapp.data.model.CartItem
import com.example.winniebeeapp.ui.theme.NavyBlue

@Composable
fun CartScreen(
    items: List<CartItem>,
    totalAmount: Double,
    isPaymentLoading: Boolean,
    paymentError: String?,
    onRemoveItem: (String) -> Unit,
    onCheckout: (String, String) -> Unit,
    onDismissError: () -> Unit
) {
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    if (paymentError != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Payment Failed") },
            text = { Text(paymentError) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text("OK")
                }
            }
        )
    }

    val navyBlueTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = NavyBlue,
        unfocusedTextColor = NavyBlue,
        focusedLabelColor = NavyBlue,
        unfocusedLabelColor = NavyBlue,
        focusedBorderColor = NavyBlue,
        unfocusedBorderColor = NavyBlue
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Cart", style = MaterialTheme.typography.headlineMedium, color = NavyBlue)
        
        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty", color = NavyBlue)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    CartItemRow(item, onRemoveItem)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Delivery Address") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPaymentLoading,
                colors = navyBlueTextFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Mobile Money Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !isPaymentLoading,
                colors = navyBlueTextFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Amount:", style = MaterialTheme.typography.titleLarge, color = NavyBlue)
                Text("UGX ${String.format("%.2f", totalAmount)}", style = MaterialTheme.typography.titleLarge, color = NavyBlue)
            }

            Button(
                onClick = { 
                    if (address.isNotBlank() && phoneNumber.isNotBlank()) {
                        onCheckout(address, phoneNumber) 
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPaymentLoading && address.isNotBlank() && phoneNumber.isNotBlank()
            ) {
                if (isPaymentLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Pay UGX $totalAmount with Mobile Money")
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onRemove: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.productName, style = MaterialTheme.typography.titleMedium, color = NavyBlue)
            Text("Qty: ${item.quantity} x UGX ${item.unitPrice}", style = MaterialTheme.typography.bodyMedium)
            
            if (!item.batchQrCodeUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Batch QR Code:", style = MaterialTheme.typography.labelSmall, color = NavyBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    AsyncImage(
                        model = item.batchQrCodeUrl,
                        contentDescription = "Batch QR Code",
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        IconButton(onClick = { onRemove(item.productId) }) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
        }
    }
}
