package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "ADMIN" or "USER"
    val avatarColor: Long,
    val phoneNumber: String = "",
    val isPhoneBound: Boolean = false,
    val isBlocked: Boolean = false,
    val blockedTimestamp: Long? = null
) {
    val isAdmin: Boolean
        get() = role == "ADMIN"
}
