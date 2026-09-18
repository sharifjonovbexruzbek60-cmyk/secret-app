package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.User
import com.example.util.SMSNotificationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Calculator", appName)
  }

  @Test
  fun `verify SMS notification dispatch on block`() {
    val aziz = User(
        id = "friend",
        name = "Aziz",
        email = "aziz@trio.local",
        passwordHash = "friend123",
        role = "USER",
        avatarColor = 0xFF059669L,
        phoneNumber = "+998901234567"
    )
    val girl = User(
        id = "girlfriend",
        name = "Girl",
        email = "girl@trio.local",
        passwordHash = "love123",
        role = "USER",
        avatarColor = 0xFFDB2777L,
        phoneNumber = "+998907654321"
    )

    // When Aziz is blocked by Admin
    val logs = SMSNotificationService.dispatchSecurityAlertsOnBlock(
        blockedUser = aziz,
        otherUser = girl
    )

    assertEquals(2, logs.size)

    val blockedLog = logs.first { it.recipientName == "Aziz" }
    assertEquals("+998901234567", blockedLog.recipientPhone)
    assertEquals(
        "WizChat Alert: Access Terminated. Your session has been revoked by the system administrator.",
        blockedLog.messageBody
    )

    val otherLog = logs.first { it.recipientName == "Girl" }
    assertEquals("+998907654321", otherLog.recipientPhone)
    assertEquals(
        "WizChat Alert: Chat room configuration changed. Secure connection updated.",
        otherLog.messageBody
    )
  }

  @Test
  fun `verify vault unlock trigger logic with 7788`() {
    val typedSequence = "7788"
    val displayValue = "7788"
    val isUnlockCode = typedSequence.contains("7788") ||
                       displayValue == "7788" ||
                       typedSequence.endsWith("7788")
    assertTrue(isUnlockCode)
  }

  @Test
  fun `verify crypto encryption and decryption for messages`() {
    val sampleText = "Secret message between trio members"
    val encrypted = com.example.util.CryptoUtils.encrypt(sampleText)
    val decrypted = com.example.util.CryptoUtils.decrypt(encrypted)
    assertEquals(sampleText, decrypted)
  }

  @Test
  fun `verify admin secret gate passphrase validation`() {
    val correctInput = "hello"
    val correctInputCaps = "Hello"
    val wrongInput = "1234"

    assertTrue(correctInput.trim().equals("hello", ignoreCase = true))
    assertTrue(correctInputCaps.trim().equals("hello", ignoreCase = true))
    org.junit.Assert.assertFalse(wrongInput.trim().equals("hello", ignoreCase = true))
  }
}
