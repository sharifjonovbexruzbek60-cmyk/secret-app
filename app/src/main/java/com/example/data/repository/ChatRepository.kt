package com.example.data.repository

import com.example.data.local.MessageDao
import com.example.data.local.UserDao
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.User
import com.example.util.CryptoUtils
import com.example.util.MediaUtils
import com.example.util.SMSNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class ChatRepository(
    private val userDao: UserDao,
    private val messageDao: MessageDao
) {
    companion object {
        val DEFAULT_USERS = listOf(
            User(
                id = "admin",
                name = "Admin",
                email = "admin@trio.local",
                passwordHash = "admin123",
                role = "ADMIN",
                avatarColor = 0xFF4F46E5, // Indigo
                phoneNumber = "+10000000000",
                isPhoneBound = true,
                isBlocked = false
            ),
            User(
                id = "friend",
                name = "Aziz",
                email = "aziz@trio.local",
                passwordHash = "friend123",
                role = "USER",
                avatarColor = 0xFF059669, // Emerald
                phoneNumber = "+998901234567",
                isPhoneBound = false,
                isBlocked = false
            ),
            User(
                id = "girlfriend",
                name = "Girl",
                email = "girl@trio.local",
                passwordHash = "love123",
                role = "USER",
                avatarColor = 0xFFDB2777, // Rose
                phoneNumber = "+998907654321",
                isPhoneBound = false,
                isBlocked = false
            )
        )
    }

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val count = userDao.getUserCount()
        if (count == 0) {
            userDao.insertOrUpdateUsers(DEFAULT_USERS)
            // Seed welcome messages with E2EE
            sendMessage(
                Message(
                    senderId = "admin",
                    senderName = "Admin",
                    senderAvatarColor = 0xFF4F46E5,
                    type = MessageType.TEXT,
                    textContent = "Welcome to WizChat! This is our private, lightweight space for the three of us.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30
                )
            )
            sendMessage(
                Message(
                    senderId = "friend",
                    senderName = "Aziz",
                    senderAvatarColor = 0xFF059669,
                    type = MessageType.TEXT,
                    textContent = "Hey Admin! WizChat is running super fast and smooth.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 20
                )
            )
            sendMessage(
                Message(
                    senderId = "girlfriend",
                    senderName = "Girl",
                    senderAvatarColor = 0xFFDB2777,
                    type = MessageType.TEXT,
                    textContent = "Love the clean design! Photos and voice notes are working great ❤️",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 10
                )
            )
        } else {
            // Keep users synced
            val existing = userDao.getUserByIdDirect("admin")
            if (existing == null) {
                userDao.insertOrUpdateUsers(DEFAULT_USERS)
            }
        }
    }

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return@withContext Result.failure(Exception("Account not found. Only the 3 registered members can log in."))

        if (user.passwordHash != trimmedPassword) {
            return@withContext Result.failure(Exception("Incorrect password. Please try again."))
        }

        if (user.isBlocked) {
            return@withContext Result.failure(Exception("This account is currently blocked by Admin. Access denied."))
        }

        Result.success(user)
    }

    suspend fun updatePhoneBinding(userId: String, phone: String, isBound: Boolean) = withContext(Dispatchers.IO) {
        userDao.updatePhoneBinding(userId, phone, isBound)
    }

    suspend fun reassignGirlfriendSlot(newName: String, newPhone: String) = withContext(Dispatchers.IO) {
        val oldSlot = userDao.getUserByIdDirect("girlfriend")
        val oldPhone = oldSlot?.phoneNumber ?: "+998907654321"

        // Wipe old message cache from Room database
        messageDao.deleteMessagesBySender("girlfriend")

        // Reassign the user slot in Room
        userDao.reassignUserSlot(
            userId = "girlfriend",
            name = newName.trim(),
            phone = newPhone.trim()
        )

        // Dispatch Twilio SMS notification
        SMSNotificationService.dispatchReassignSMS(
            oldPhone = oldPhone,
            newPhone = newPhone.trim(),
            newName = newName.trim()
        )
    }

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    fun getUser(userId: String): Flow<User?> = userDao.getUserById(userId)

    suspend fun getUserDirect(userId: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByIdDirect(userId)
    }

    suspend fun setBlockedStatus(userId: String, isBlocked: Boolean) = withContext(Dispatchers.IO) {
        userDao.setBlockedStatus(
            userId = userId,
            isBlocked = isBlocked,
            timestamp = if (isBlocked) System.currentTimeMillis() else null
        )
    }

    // Room Database Entries are strictly encrypted with AES-256 (E2EE)
    fun getAllMessages(): Flow<List<Message>> = messageDao.getAllMessages().map { messages ->
        messages.map { msg ->
            msg.copy(
                textContent = CryptoUtils.decrypt(msg.textContent) ?: "",
                mediaUri = CryptoUtils.decrypt(msg.mediaUri),
                thumbnailUri = CryptoUtils.decrypt(msg.thumbnailUri)
            )
        }
    }

    suspend fun sendMessage(message: Message): Long = withContext(Dispatchers.IO) {
        val encryptedMessage = message.copy(
            textContent = CryptoUtils.encrypt(message.textContent) ?: "",
            mediaUri = CryptoUtils.encrypt(message.mediaUri),
            thumbnailUri = CryptoUtils.encrypt(message.thumbnailUri)
        )
        messageDao.insertMessage(encryptedMessage)
    }

    suspend fun destroyViewOnceMedia(messageId: Long) = withContext(Dispatchers.IO) {
        val rawMessage = messageDao.getMessageById(messageId)
        if (rawMessage != null) {
            val decryptedMediaUri = CryptoUtils.decrypt(rawMessage.mediaUri)
            if (!decryptedMediaUri.isNullOrEmpty()) {
                try {
                    val file = File(decryptedMediaUri)
                    MediaUtils.zeroFillAndShred(file)
                } catch (_: Exception) {}
            }
            val decryptedThumb = CryptoUtils.decrypt(rawMessage.thumbnailUri)
            if (!decryptedThumb.isNullOrEmpty()) {
                try {
                    val thumbFile = File(decryptedThumb)
                    MediaUtils.zeroFillAndShred(thumbFile)
                } catch (_: Exception) {}
            }
            messageDao.markViewOnceAsDestroyed(messageId)
        }
    }

    suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        messageDao.deleteMessage(id)
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        messageDao.clearAllMessages()
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.deleteUserById(userId)
    }
}
