package com.example.winniebeeapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.winniebeeapp.ui.WinnieBeeNavGraph
import com.example.winniebeeapp.ui.theme.WinnieBeeAppTheme
import com.example.winniebeeapp.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create Notification Channel for order tracking
        NotificationHelper.createNotificationChannel(this)

        // Request runtime permission for notifications on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val initialOrderId = intent?.getStringExtra("orderId")

        setContent {
            WinnieBeeAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    WinnieBeeNavGraph(initialOrderId = initialOrderId)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationPreview() {
    WinnieBeeAppTheme {
        WinnieBeeNavGraph()
    }
}
