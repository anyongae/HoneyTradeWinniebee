package com.example.winniebeeapp.data.local

import androidx.room.TypeConverter
import com.example.winniebeeapp.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }

    @TypeConverter
    fun toUserRole(role: String): UserRole {
        return UserRole.valueOf(role)
    }
}
