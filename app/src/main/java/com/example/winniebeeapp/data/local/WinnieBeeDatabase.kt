package com.example.winniebeeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class, ProductEntity::class, ReviewEntity::class], version = 5, exportSchema = false)
abstract class WinnieBeeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: WinnieBeeDatabase? = null

        fun getDatabase(context: Context): WinnieBeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WinnieBeeDatabase::class.java,
                    "winniebee_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedAdmin(context)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        seedAdmin(context)
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private fun seedAdmin(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = getDatabase(context).userDao()
                    val adminEmail = "happyemma98@gmail.com"
                    val existingAdmin = dao.getUserByEmail(adminEmail)
                    
                    if (existingAdmin == null) {
                        val adminUser = UserEntity(
                            id = "admin_id",
                            name = "Administrator",
                            email = adminEmail,
                            pass = "Admin@123",
                            role = "ADMIN",
                            phoneNumber = "0772021325",
                            region = "Headquarters",
                            age = 30,
                            sex = "Male",
                            isVerified = true
                        )
                        dao.insertUser(adminUser)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
