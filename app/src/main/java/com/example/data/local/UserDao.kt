package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY role ASC, name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdDirect(id: String): User?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsersIfAbsent(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsers(users: List<User>)

    @Query("UPDATE users SET isBlocked = :isBlocked, blockedTimestamp = :timestamp WHERE id = :userId")
    suspend fun setBlockedStatus(userId: String, isBlocked: Boolean, timestamp: Long?)

    @Query("UPDATE users SET phoneNumber = :phone, isPhoneBound = :isBound WHERE id = :userId")
    suspend fun updatePhoneBinding(userId: String, phone: String, isBound: Boolean)

    @Query("UPDATE users SET name = :name, phoneNumber = :phone, isBlocked = 0, isPhoneBound = 0, blockedTimestamp = NULL WHERE id = :userId")
    suspend fun reassignUserSlot(userId: String, name: String, phone: String)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
