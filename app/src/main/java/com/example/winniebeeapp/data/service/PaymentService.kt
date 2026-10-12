package com.example.winniebeeapp.data.service

import kotlinx.coroutines.delay

interface PaymentService {
    suspend fun initiateMobileMoneyPayment(phoneNumber: String, amount: Double): Result<String>
    suspend fun verifyPayment(transactionId: String): Result<Boolean>
}

class MobileMoneyPaymentServiceImpl : PaymentService {
    override suspend fun initiateMobileMoneyPayment(phoneNumber: String, amount: Double): Result<String> {
        // Simulate API call to M-Pesa/Flutterwave etc.
        delay(2000)
        return if (phoneNumber.length >= 10) {
            Result.success("TRX-${System.currentTimeMillis()}")
        } else {
            Result.failure(Exception("Invalid phone number"))
        }
    }

    override suspend fun verifyPayment(transactionId: String): Result<Boolean> {
        delay(1000)
        return Result.success(true)
    }
}
