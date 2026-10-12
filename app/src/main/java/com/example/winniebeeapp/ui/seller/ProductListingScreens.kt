package com.example.winniebeeapp.ui.seller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.winniebeeapp.data.model.Product
import com.example.winniebeeapp.ui.theme.NavyBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboard(
    products: List<Product>,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDelistProduct: (String) -> Unit,
    onVerifyBatch: (List<String>) -> Unit,
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
                title = { Text("Seller Dashboard", color = NavyBlue) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = NavyBlue)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Products", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onViewOrders) {
                    Text("View Orders")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onVerifyBatch(products.map { it.id }) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verify Current Batch")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No products listed yet. Tap '+' to add a product.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(products, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onEdit = { onEditProduct(product) },
                            onDelist = { onDelistProduct(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onEdit: () -> Unit,
    onDelist: () -> Unit
) {
    var showDelistDialog by remember { mutableStateOf(false) }

    if (showDelistDialog) {
        AlertDialog(
            onDismissRequest = { showDelistDialog = false },
            title = { Text("Delist Product") },
            text = { Text("Are you sure you want to delist \"${product.name}\"? This product will no longer be visible to buyers.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDelistDialog = false
                        onDelist()
                    }
                ) {
                    Text("Delist", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text("Price: UGX ${product.price}", style = MaterialTheme.typography.bodyMedium)
                Text("Stock: ${product.stockQuantity} Litres", style = MaterialTheme.typography.bodySmall)
                if (product.isVerified) {
                    Text("Verified ✅", color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("Pending Verification ⏳", color = MaterialTheme.colorScheme.secondary)
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Product",
                        tint = NavyBlue
                    )
                }
                IconButton(onClick = { showDelistDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delist Product",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
