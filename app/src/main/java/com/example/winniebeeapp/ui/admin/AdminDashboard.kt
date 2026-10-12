package com.example.winniebeeapp.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.winniebeeapp.data.local.ProductEntity
import com.example.winniebeeapp.data.local.UserEntity
import com.example.winniebeeapp.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    viewModel: AdminViewModel? = if (LocalInspectionMode.current) null else viewModel(),
    onLogout: () -> Unit
) {
    val unverifiedSellers by viewModel?.unverifiedSellers?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val unverifiedProducts by viewModel?.unverifiedProducts?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val isLoading by viewModel?.isLoading?.collectAsState() ?: remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                item {
                    Text("Pending Seller Verifications", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (unverifiedSellers.isEmpty()) {
                    item {
                        Text("No pending sellers", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    items(unverifiedSellers) { seller ->
                        SellerVerificationItem(
                            seller = seller,
                            onVerify = { viewModel?.verifyUser(seller.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Pending Product Verifications", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (unverifiedProducts.isEmpty()) {
                    item {
                        Text("No pending products", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    items(unverifiedProducts) { product ->
                        ProductVerificationItem(
                            product = product,
                            onVerify = { viewModel?.verifyProduct(product.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SellerVerificationItem(seller: UserEntity, onVerify: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(seller.name, style = MaterialTheme.typography.titleMedium)
            Text(seller.email, style = MaterialTheme.typography.bodySmall)
            Text("Region: ${seller.region}", style = MaterialTheme.typography.bodySmall)
            
            if (seller.maaifCertificateUrl != null) {
                Text("MAAIF Cert: ${seller.maaifCertificateUrl}", color = MaterialTheme.colorScheme.primary)
            }
            if (seller.batchQrCodeUrl != null) {
                Text("Batch QR: ${seller.batchQrCodeUrl}", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onVerify, modifier = Modifier.align(Alignment.End)) {
                Text("Verify Seller")
            }
        }
    }
}

@Composable
fun ProductVerificationItem(product: ProductEntity, onVerify: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text(product.description, style = MaterialTheme.typography.bodySmall)
            Text("Price: UGX ${product.price}", style = MaterialTheme.typography.bodySmall)
            Text("Seller ID: ${product.sellerId}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onVerify, modifier = Modifier.align(Alignment.End)) {
                Text("Verify Product")
            }
        }
    }
}
