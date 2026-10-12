package com.example.winniebeeapp.ui.seller

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.winniebeeapp.ui.theme.LightBlue
import com.example.winniebeeapp.ui.theme.NavyBlue

data class ProductTemplate(
    val name: String,
    val description: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onAddProduct: (String, String, Double, Int, String, String, String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val categories = mapOf(
        "Pure Honey" to ProductTemplate(
            "Pure Honey", 
            "A natural sweetener, rich in antioxidants.",
            "https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?q=80&w=500"
        ),
        "Golden Amber" to ProductTemplate(
            "Golden Amber", 
            "Light and sweet, perfect for tea and baking.",
            "https://images.unsplash.com/photo-1555507036-ab1f4038808a?q=80&w=500"
        ),
        "Propolis" to ProductTemplate(
            "Propolis", 
            "Known for its natural healing properties.",
            "https://images.unsplash.com/photo-1596719815613-43187c3e7215?q=80&w=500"
        ),
        "Bee Wax" to ProductTemplate(
            "Bee Wax", 
            "Pure beeswax for candles, balms, and crafts.",
            "https://images.unsplash.com/photo-1581441363689-1f3c3c414635?q=80&w=500"
        )
    )

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var batchQrCodeUri by remember { mutableStateOf<Uri?>(null) }

    val batchQrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> batchQrCodeUri = uri }
    
    var expanded by remember { mutableStateOf(false) }

    val navyBlueTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = NavyBlue,
        unfocusedTextColor = NavyBlue,
        focusedLabelColor = NavyBlue,
        unfocusedLabelColor = NavyBlue,
        focusedBorderColor = NavyBlue,
        unfocusedBorderColor = NavyBlue
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("List New Product", style = MaterialTheme.typography.headlineMedium, color = NavyBlue)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = navyBlueTextFieldColors,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.keys.forEach { categoryName ->
                    DropdownMenuItem(
                        text = { Text(categoryName) },
                        onClick = {
                            category = categoryName
                            val template = categories[categoryName]
                            if (template != null) {
                                name = template.name
                                description = template.description
                                imageUrl = template.imageUrl
                            }
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = navyBlueTextFieldColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Importance)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = navyBlueTextFieldColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) price = it },
            label = { Text("Price per Litre (UGX)") },
            prefix = { Text("UGX ", color = NavyBlue) },
            suffix = { Text("/ Litre", color = NavyBlue) },
            modifier = Modifier.fillMaxWidth(),
            colors = navyBlueTextFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) quantity = it },
            label = { Text("Quantity Provision (in Litres)") },
            suffix = { Text("Litres", color = NavyBlue) },
            supportingText = { Text("Attach available volume in litres to price per litre") },
            modifier = Modifier.fillMaxWidth(),
            colors = navyBlueTextFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        val priceVal = price.toDoubleOrNull() ?: 0.0
        val qtyVal = quantity.toDoubleOrNull() ?: 0.0
        if (priceVal > 0 && qtyVal > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF0F4F8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Price & Quantity Provision:",
                        style = MaterialTheme.typography.labelLarge,
                        color = NavyBlue
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "• Listing Rate: UGX $priceVal / Litre",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Available Volume: $qtyVal Litres",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "• Total Batch Value: UGX ${priceVal * qtyVal}",
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Batch/Harvest Verification QR Code", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { batchQrLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBlue)
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (batchQrCodeUri == null) "Upload Batch/Harvest QR Code" else "Batch/Harvest QR Code Attached ✅")
        }

        if (batchQrCodeUri != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = batchQrCodeUri,
                    contentDescription = "Batch QR Code Preview",
                    modifier = Modifier.fillMaxHeight(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                onAddProduct(
                    name, 
                    description, 
                    price.toDoubleOrNull() ?: 0.0, 
                    quantity.toDoubleOrNull()?.toInt() ?: quantity.toIntOrNull() ?: 0, 
                    category,
                    imageUrl,
                    batchQrCodeUri?.toString()
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightBlue)
        ) {
            Text("List Product")
        }

        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel", color = NavyBlue)
        }
    }
}
