package com.example.winniebeeapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.winniebeeapp.MainActivity
import com.example.winniebeeapp.R
import com.example.winniebeeapp.data.model.Order
import com.example.winniebeeapp.data.model.OrderStatus
import java.util.Locale

object NotificationHelper {
    private const val CHANNEL_ID = "order_tracking_channel"
    private const val CHANNEL_NAME = "Order Tracking Notifications"
    private const val CHANNEL_DESC = "Notifications for WinnieBee order status updates and tracking"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOrderPlacedNotification(context: Context, order: Order) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("orderId", order.id)
            putExtra("navigate_to", "order_tracking")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            order.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val itemNames = order.items.joinToString { it.productName }.ifEmpty { "Honey Products" }
        val statusText = getStatusMessage(order.status)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🐝 Order Placed: #${order.id.take(8)}")
            .setContentText("Your order for $itemNames is $statusText. Tap to track.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your order for $itemNames (UGX ${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}) has been placed successfully!\n\nStatus: $statusText\nDelivery Address: ${order.deliveryAddress}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(order.id.hashCode(), notification)
    }

    fun showOrderStatusUpdateNotification(context: Context, orderId: String, newStatus: OrderStatus) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("orderId", orderId)
            putExtra("navigate_to", "order_tracking")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusMessage = getStatusMessage(newStatus)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚚 Order Status Updated: #${orderId.take(8)}")
            .setContentText("Status is now $statusMessage.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Order #${orderId.take(8)} Status Update: $statusMessage. Tap to view real-time tracking.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(orderId.hashCode(), notification)
    }

    fun sendDeliveryStepAlert(context: Context, orderId: String, stepIndex: Int) {
        createNotificationChannel(context)

        val (title, message) = when (stepIndex) {
            0 -> Pair(
                "🐝 Step 1: Order Placed & Received",
                "Seller has received your order #${orderId.take(8)}. Payment confirmed in escrow."
            )
            1 -> Pair(
                "📦 Step 2: Processing & Packing",
                "Your honey products for order #${orderId.take(8)} are being quality inspected and packaged."
            )
            2 -> Pair(
                "🚚 Step 3: Out for Delivery",
                "Order #${orderId.take(8)} is on the way! Courier has picked up your package."
            )
            3 -> Pair(
                "📍 Step 4: Delivered Successfully",
                "Order #${orderId.take(8)} has arrived at your destination address. Enjoy your WinnieBee honey!"
            )
            else -> Pair(
                "🔔 Delivery Update: Order #${orderId.take(8)}",
                "Your order status has been updated by the seller."
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("orderId", orderId)
            putExtra("navigate_to", "order_tracking")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (orderId + stepIndex).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$title\n\n$message\n\nTap to view live tracking and courier details.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((orderId + stepIndex).hashCode(), notification)
    }

    private fun getStatusMessage(status: OrderStatus): String = when (status) {
        OrderStatus.PENDING -> "Pending Payment"
        OrderStatus.PAID -> "Payment Confirmed (Escrow Held) ✅"
        OrderStatus.SHIPPED -> "Dispatched & On the Way 🚚"
        OrderStatus.DELIVERED -> "Delivered to Location 📍"
        OrderStatus.COMPLETED -> "Completed & Funds Released 🎉"
        OrderStatus.DISPUTED -> "Under Dispute Resolution ⚠️"
        OrderStatus.CANCELLED -> "Cancelled ❌"
    }
}
