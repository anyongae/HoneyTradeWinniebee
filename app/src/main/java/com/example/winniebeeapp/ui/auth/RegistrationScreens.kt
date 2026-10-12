package com.example.winniebeeapp.ui.auth

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.winniebeeapp.data.model.UserRole
import com.example.winniebeeapp.ui.theme.LightBlue
import com.example.winniebeeapp.ui.theme.NavyBlue
import com.example.winniebeeapp.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegister: (String, String, String, UserRole, String, String, Int, String, String?) -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.BUYER) }
    var mobileNumber by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("Male") }
    var region by remember { mutableStateOf("") }

    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    // Seller-specific files
    var maaifCertificateUri by remember { mutableStateOf<Uri?>(null) }

    val maaifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> maaifCertificateUri = uri }

    val regions = listOf("Central", "Eastern", "Northern", "Western")
    val sexes = listOf("Male", "Female", "Other")
    var regionExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }

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
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("WinnieBee Registration", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = navyBlueTextFieldColors,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = navyBlueTextFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = navyBlueTextFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = password.isNotEmpty() && password.length < 8,
            supportingText = {
                if (password.isNotEmpty() && password.length < 8) {
                    Text("Password must be at least 8 characters", color = MaterialTheme.colorScheme.error)
                }
            },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = navyBlueTextFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            colors = navyBlueTextFieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Region Selection
        ExposedDropdownMenuBox(
            expanded = regionExpanded,
            onExpandedChange = { if (!isLoading) regionExpanded = !regionExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = region,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Region") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                colors = navyBlueTextFieldColors,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                enabled = !isLoading
            )

            ExposedDropdownMenu(
                expanded = regionExpanded,
                onDismissRequest = { regionExpanded = false }
            ) {
                regions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            region = selectionOption
                            regionExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sex Selection
        ExposedDropdownMenuBox(
            expanded = sexExpanded,
            onExpandedChange = { if (!isLoading) sexExpanded = !sexExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = sex,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sex") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                colors = navyBlueTextFieldColors,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                enabled = !isLoading
            )

            ExposedDropdownMenu(
                expanded = sexExpanded,
                onDismissRequest = { sexExpanded = false }
            ) {
                sexes.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            sex = selectionOption
                            sexExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role Selection
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Account Type", style = MaterialTheme.typography.bodyMedium, color = NavyBlue)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == UserRole.BUYER,
                    onClick = { if (!isLoading) selectedRole = UserRole.BUYER },
                    colors = RadioButtonDefaults.colors(selectedColor = NavyBlue),
                    enabled = !isLoading
                )
                Text("Buyer")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = selectedRole == UserRole.SELLER,
                    onClick = { if (!isLoading) selectedRole = UserRole.SELLER },
                    colors = RadioButtonDefaults.colors(selectedColor = NavyBlue),
                    enabled = !isLoading
                )
                Text("Seller")
            }
        }

        // Seller Specific Uploads
        if (selectedRole == UserRole.SELLER) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Seller Verification Documents", style = MaterialTheme.typography.titleMedium, color = NavyBlue)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { if (!isLoading) maaifLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyBlue),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (maaifCertificateUri == null) "Upload MAAIF/UNBS Certificate" else "Certificate Attached ✅")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isFormValid = name.isNotBlank() && 
                         email.isNotBlank() && 
                         password.length >= 8 && 
                         mobileNumber.isNotBlank() && 
                         age.isNotBlank() && 
                         region.isNotBlank() && 
                         sex.isNotBlank() &&
                         (if (selectedRole == UserRole.SELLER) maaifCertificateUri != null else true)

        Button(
            onClick = { 
                onRegister(
                    name, 
                    email, 
                    password, 
                    selectedRole, 
                    mobileNumber, 
                    region, 
                    age.toIntOrNull() ?: 0, 
                    sex,
                    maaifCertificateUri?.toString()
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = LightBlue),
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Register")
            }
        }

        TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
            Text("Already have an account? Login", color = NavyBlue)
        }
    }
}
