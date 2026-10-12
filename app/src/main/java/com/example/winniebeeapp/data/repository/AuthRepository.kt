package com.example.winniebeeapp.data.repository

import android.content.Context
import com.example.winniebeeapp.data.local.UserDao
import com.example.winniebeeapp.data.local.UserEntity
import com.example.winniebeeapp.data.local.WinnieBeeDatabase
import com.example.winniebeeapp.data.model.User
import com.example.winniebeeapp.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository(context: Context) {
    private val userDao = WinnieBeeDatabase.getDatabase(context).userDao()

    private val auth: FirebaseAuth? by lazy {
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    }
    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    suspend fun registerUser(
        name: String, 
        email: String, 
        pass: String, 
        role: UserRole,
        phoneNumber: String,
        region: String,
        age: Int,
        sex: String,
        maaifCertificateUrl: String? = null,
        batchQrCodeUrl: String? = null
    ): Result<User> {
        val a = auth 
        val fs = firestore

        // 1. Create a User object
        val user = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            role = role,
            phoneNumber = phoneNumber,
            region = region,
            age = age,
            sex = sex,
            maaifCertificateUrl = maaifCertificateUrl,
            batchQrCodeUrl = batchQrCodeUrl,
            isVerified = false
        )

        // 2. Save Locally in SQLite
        try {
            userDao.insertUser(UserEntity(
                id = user.id,
                name = name,
                email = email,
                pass = pass,
                role = role.name, // Convert to string
                phoneNumber = phoneNumber,
                region = region,
                age = age,
                sex = sex,
                isVerified = false
            ))
        } catch (e: Exception) {
            return Result.failure(e)
        }

        // 3. Optional: Sync with Firebase if available
        if (a != null && fs != null) {
            try {
                val authResult = a.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userWithFbId = user.copy(id = firebaseUser.uid)
                    fs.collection("users").document(userWithFbId.id).set(userWithFbId).await()
                }
            } catch (e: Exception) {
                // Ignore Firebase failures for now as per local-first request
            }
        }

        return Result.success(user)
    }

    suspend fun login(email: String, pass: String): Result<User> {
        // 1. Check Local SQLite first
        val localUser = userDao.login(email, pass)
        if (localUser != null) {
            val user = User(
                id = localUser.id,
                name = localUser.name,
                email = localUser.email,
                role = UserRole.valueOf(localUser.role), // Convert back to Enum
                phoneNumber = localUser.phoneNumber,
                region = localUser.region,
                age = localUser.age,
                sex = localUser.sex,
                isVerified = localUser.isVerified
            )
            return Result.success(user)
        }

        // 2. Fallback to Firebase
        val a = auth
        val fs = firestore
        if (a != null && fs != null) {
            return try {
                val authResult = a.signInWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user ?: throw Exception("Login failed")
                
                val userDoc = fs.collection("users").document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found")
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(Exception("Wrong email/password entered"))
            }
        }

        return Result.failure(Exception("Wrong email/password entered"))
    }

    fun logout() {
        auth?.signOut()
    }
}
