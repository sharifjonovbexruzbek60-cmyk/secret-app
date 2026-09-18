package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SMSNotificationLog(
    val id: String = UUID.randomUUID().toString(),
    val recipientName: String,
    val recipientPhone: String,
    val messageBody: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED",
    val gatewayProvider: String = "Twilio Programmable SMS"
) {
    val message: String
        get() = messageBody

    val phoneNumber: String
        get() = recipientPhone

    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

/**
 * Simulates enterprise Twilio SMS Gateway integration for WizChat.
 * Dispatches automated security SMS messages to phone numbers upon Admin access events.
 */
object SMSNotificationService {
    private const val TAG = "SMSNotificationService"

    private val _dispatchedSmsLogs = MutableStateFlow<List<SMSNotificationLog>>(emptyList())
    val dispatchedSmsLogs: StateFlow<List<SMSNotificationLog>> = _dispatchedSmsLogs.asStateFlow()

    private val _lastAlertToast = MutableStateFlow<String?>(null)
    val lastAlertToast: StateFlow<String?> = _lastAlertToast.asStateFlow()

    fun dismissLastAlert() {
        _lastAlertToast.value = null
    }

    /**
     * Triggers critical SMS security alerts when an Admin blocks a member.
     * Dispatches custom security texts to both Aziz and Girl according to specifications.
     */
    fun dispatchSecurityAlertsOnBlock(
        blockedUser: User,
        otherUser: User
    ): List<SMSNotificationLog> {
        val blockedUserMessage = "WizChat Alert: Access Terminated. Your session has been revoked by the system administrator."
        val otherUserMessage = "WizChat Alert: Chat room configuration changed. Secure connection updated."

        val logBlocked = SMSNotificationLog(
            recipientName = blockedUser.name,
            recipientPhone = blockedUser.phoneNumber,
            messageBody = blockedUserMessage
        )

        val logOther = SMSNotificationLog(
            recipientName = otherUser.name,
            recipientPhone = otherUser.phoneNumber,
            messageBody = otherUserMessage
        )

        Log.i(TAG, "[TWILIO SMS DISPATCH] To: ${blockedUser.phoneNumber} (${blockedUser.name}) -> $blockedUserMessage")
        Log.i(TAG, "[TWILIO SMS DISPATCH] To: ${otherUser.phoneNumber} (${otherUser.name}) -> $otherUserMessage")

        val currentLogs = _dispatchedSmsLogs.value.toMutableList()
        currentLogs.add(0, logBlocked)
        currentLogs.add(0, logOther)
        _dispatchedSmsLogs.value = currentLogs

        _lastAlertToast.value = "Twilio SMS Sent: Automated alerts dispatched to ${blockedUser.phoneNumber} and ${otherUser.phoneNumber}"

        return listOf(logBlocked, logOther)
    }

    /**
     * Dispatches simulated 6-digit SMS OTP for device binding and multi-factor login.
     */
    fun dispatchOtpSMS(recipientName: String, recipientPhone: String, otpCode: String): SMSNotificationLog {
        val message = "WizChat Security: Your 6-digit device verification code is $otpCode. Valid for 5 minutes. Do not share this code."
        val log = SMSNotificationLog(
            recipientName = recipientName,
            recipientPhone = recipientPhone,
            messageBody = message
        )
        Log.i(TAG, "[TWILIO OTP SMS] To: $recipientPhone -> $message")
        val currentLogs = _dispatchedSmsLogs.value.toMutableList()
        currentLogs.add(0, log)
        _dispatchedSmsLogs.value = currentLogs

        _lastAlertToast.value = "Twilio SMS OTP sent to $recipientPhone: $otpCode"
        return log
    }

    fun dispatchOtpSMS(recipientPhone: String, otpCode: String): SMSNotificationLog {
        return dispatchOtpSMS("User", recipientPhone, otpCode)
    }

    fun dispatchBlockAlertSMS(blockedUserName: String, user1Phone: String, user2Phone: String) {
        val msgBlocked = "WizChat Security Alert: Terminal session for $blockedUserName has been terminated and revoked by System Oracle."
        val log1 = SMSNotificationLog(
            recipientName = blockedUserName,
            recipientPhone = user1Phone,
            messageBody = msgBlocked
        )
        val log2 = SMSNotificationLog(
            recipientName = "Mesh Member",
            recipientPhone = user2Phone,
            messageBody = "WizChat Security Alert: Mesh node configuration updated. Connection secured."
        )
        val currentLogs = _dispatchedSmsLogs.value.toMutableList()
        currentLogs.add(0, log1)
        currentLogs.add(0, log2)
        _dispatchedSmsLogs.value = currentLogs
        _lastAlertToast.value = "Twilio SMS Alert: Revocation notifications dispatched to mesh terminals."
    }

    /**
     * Dispatches SMS alert on Admin partner slot re-assignment.
     */
    fun dispatchReassignSMS(oldPhone: String, newPhone: String, newName: String) {
        val msg1 = "WizChat Alert: Partner slot session has been permanently reset and wiped by the Administrator."
        val log1 = SMSNotificationLog(
            recipientName = "Former Slot",
            recipientPhone = oldPhone,
            messageBody = msg1
        )
        val msg2 = "WizChat Alert: You have been assigned as $newName in the private chat space by the Administrator."
        val log2 = SMSNotificationLog(
            recipientName = newName,
            recipientPhone = newPhone,
            messageBody = msg2
        )
        val currentLogs = _dispatchedSmsLogs.value.toMutableList()
        currentLogs.add(0, log1)
        currentLogs.add(0, log2)
        _dispatchedSmsLogs.value = currentLogs
        _lastAlertToast.value = "Slot Reassigned: Twilio SMS alerts dispatched."
    }
}
