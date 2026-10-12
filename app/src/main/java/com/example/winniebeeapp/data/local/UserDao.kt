package com.example.winniebeeapp.data.local

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email AND pass = :pass LIMIT 1")
    suspend fun login(email: String, pass: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE isVerified = 0 AND role = 'SELLER'")
    suspend fun getUnverifiedSellers(): List<UserEntity>

    @Query("UPDATE users SET isVerified = 1 WHERE id = :userId")
    suspend fun verifyUser(userId: String): Int
}
