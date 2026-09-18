package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.UUID

enum class ProvisioningStatus {
    UNPROVISIONED,
    ACTIVE,
    REVOKED
}

data class ProvisionedIdentity(
    val deviceToken: String,
    val phoneNumber: String,
    val assignedUserId: String,
    val status: ProvisioningStatus,
    val isGhostAdmin: Boolean
)

/**
 * Covert Device Provisioning Manager for Operation "WizChat Covert Mesh".
 * Generates an obfuscated hardware signature token and binds physical devices
 * to identities with persistent status checks.
 */
class DeviceProvisioningManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("covert_mesh_opsec_store", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DEVICE_TOKEN = "sec_device_token"
        private const val KEY_PHONE_NUMBER = "sec_phone_number"
        private const val KEY_USER_ID = "sec_user_id"
        private const val KEY_STATUS = "sec_provision_status"
        private const val KEY_IS_GHOST_ADMIN = "sec_is_ghost_admin"

        // Cryptographic signature hash for Creator/Ghost Admin
        const val GHOST_ADMIN_SIGNATURE_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        const val GHOST_ADMIN_DEFAULT_ID = "admin"
    }

    /**
     * Retrieves or generates an obfuscated Hardware Token for this physical device.
     */
    fun getOrCreateDeviceToken(): String {
        var token = prefs.getString(KEY_DEVICE_TOKEN, null)
        if (token == null) {
            val raw = "${UUID.randomUUID()}-${System.currentTimeMillis()}-DXX-SIGINT"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
            token = hashBytes.joinToString("") { "%02x".format(it) }
            prefs.edit().putString(KEY_DEVICE_TOKEN, token).apply()
        }
        return token
    }

    /**
     * Checks if this device has an active verified provisioning.
     */
    fun getProvisioningStatus(): ProvisioningStatus {
        val statusStr = prefs.getString(KEY_STATUS, null) ?: return ProvisioningStatus.UNPROVISIONED
        return try {
            ProvisioningStatus.valueOf(statusStr)
        } catch (e: Exception) {
            ProvisioningStatus.UNPROVISIONED
        }
    }

    fun getProvisionedIdentity(): ProvisionedIdentity? {
        val status = getProvisioningStatus()
        if (status == ProvisioningStatus.UNPROVISIONED) return null

        val token = getOrCreateDeviceToken()
        val phone = prefs.getString(KEY_PHONE_NUMBER, "") ?: ""
        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        val isGhost = prefs.getBoolean(KEY_IS_GHOST_ADMIN, false)

        return ProvisionedIdentity(
            deviceToken = token,
            phoneNumber = phone,
            assignedUserId = userId,
            status = status,
            isGhostAdmin = isGhost
        )
    }

    /**
     * Provision a standard member (Aziz or Girl) with phone mapping.
     */
    fun provisionMember(phoneNumber: String, userId: String) {
        val token = getOrCreateDeviceToken()
        prefs.edit()
            .putString(KEY_DEVICE_TOKEN, token)
            .putString(KEY_PHONE_NUMBER, phoneNumber)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_STATUS, ProvisioningStatus.ACTIVE.name)
            .putBoolean(KEY_IS_GHOST_ADMIN, false)
            .apply()
    }

    /**
     * Provision the Ghost Admin bypassing phone requirement.
     */
    fun provisionGhostAdmin() {
        val token = getOrCreateDeviceToken()
        prefs.edit()
            .putString(KEY_DEVICE_TOKEN, token)
            .putString(KEY_PHONE_NUMBER, "ORACLE-ENCRYPTED")
            .putString(KEY_USER_ID, GHOST_ADMIN_DEFAULT_ID)
            .putString(KEY_STATUS, ProvisioningStatus.ACTIVE.name)
            .putBoolean(KEY_IS_GHOST_ADMIN, true)
            .apply()
    }

    /**
     * Revoke provisioning (Executed on Remote Kick).
     */
    fun revokeProvisioning() {
        prefs.edit()
            .putString(KEY_STATUS, ProvisioningStatus.REVOKED.name)
            .apply()
    }

    /**
     * Complete wipe of all security credentials (Self-Destruct or Global Reset).
     */
    fun selfDestructLocalData() {
        prefs.edit().clear().apply()
    }
}
