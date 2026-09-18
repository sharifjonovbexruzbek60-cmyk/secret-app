package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    VOICE
}

class Converters {
    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = runCatching {
        MessageType.valueOf(value)
    }.getOrDefault(MessageType.TEXT)
}

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val senderName: String,
    val senderAvatarColor: Long,
    val type: MessageType,
    val textContent: String = "",
    val mediaUri: String? = null,
    val originalSize: Long = 0L,
    val compressedSize: Long = 0L,
    val mediaDurationMs: Long = 0L,
    val thumbnailUri: String? = null,
    val isViewOnce: Boolean = false,
    val isViewed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
