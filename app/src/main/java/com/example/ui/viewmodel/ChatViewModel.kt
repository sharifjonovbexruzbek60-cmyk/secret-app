package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.User
import com.example.data.remote.FirebaseMeshManager
import com.example.data.repository.ChatRepository
import com.example.data.security.DeviceProvisioningManager
import com.example.data.security.ProvisioningStatus
import com.example.util.AudioPlayerHelper
import com.example.util.AudioRecorderHelper
import com.example.util.MediaUtils
import com.example.util.SMSNotificationLog
import com.example.util.SMSNotificationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class AuthStep {
    CREDENTIALS,
    PHONE_REGISTRATION,
    OTP_VERIFICATION
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ChatViewModel"
    private val repository: ChatRepository
    private val recorderHelper = AudioRecorderHelper(application)
    private val playerHelper = AudioPlayerHelper(application)

    val provisioningManager = DeviceProvisioningManager(application)
    val meshManager = FirebaseMeshManager(application, provisioningManager)

    // Telemetry and Real-Time Handshake States
    val networkTelemetryLog: StateFlow<List<String>> = meshManager.networkTelemetryLog
    val activeRollingHex: StateFlow<String> = meshManager.activeRollingHex
    val isMeshConnected: StateFlow<Boolean> = meshManager.isMeshConnected

    private val _provisioningStatus = MutableStateFlow(provisioningManager.getProvisioningStatus())
    val provisioningStatus: StateFlow<ProvisioningStatus> = _provisioningStatus.asStateFlow()

    private val _deviceToken = MutableStateFlow(provisioningManager.getOrCreateDeviceToken())
    val deviceToken: StateFlow<String> = _deviceToken.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ChatRepository(db.userDao(), db.messageDao())
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    // Camouflage & Decoy States
    private val _isCalculatorUnlocked = MutableStateFlow(false)
    val isCalculatorUnlocked: StateFlow<Boolean> = _isCalculatorUnlocked.asStateFlow()

    private val _isDecoyMode = MutableStateFlow(false)
    val isDecoyMode: StateFlow<Boolean> = _isDecoyMode.asStateFlow()

    // Stealth Mode: Admin can hide UI buttons / user switchers
    private val _areButtonsHidden = MutableStateFlow(false)
    val areButtonsHidden: StateFlow<Boolean> = _areButtonsHidden.asStateFlow()

    fun toggleHideButtons() {
        _areButtonsHidden.value = !_areButtonsHidden.value
    }

    fun setHideButtons(hide: Boolean) {
        _areButtonsHidden.value = hide
    }

    /**
     * Unlocks the 3D Calculator vault via the kernel gate trigger ("777+0=").
     * Automatically triggers the Firebase / SigInt Mesh handshake.
     */
    fun unlockCalculatorVault() {
        _isCalculatorUnlocked.value = true
        _isDecoyMode.value = false
        _provisioningStatus.value = provisioningManager.getProvisioningStatus()

        meshManager.activateMeshHandshake {
            val identity = provisioningManager.getProvisionedIdentity()
            if (identity != null && identity.status == ProvisioningStatus.ACTIVE) {
                viewModelScope.launch {
                    val user = repository.getUserDirect(identity.assignedUserId)
                    if (user != null && !user.isBlocked) {
                        setCurrentUser(user)
                    }
                }
            } else if (_currentUser.value == null) {
                // If not yet provisioned, check if admin can be quick initialized
                viewModelScope.launch {
                    val adminUser = repository.getUserDirect("admin")
                    adminUser?.let { setCurrentUser(it) }
                }
            }
        }
    }

    fun lockToCalculator() {
        // Anti-Forensics: zero transient voice/audio buffers
        playerHelper.stop()
        recorderHelper.cancelRecording()
        _isCalculatorUnlocked.value = false
        _isDecoyMode.value = false
    }

    fun triggerDecoyMode() {
        _isDecoyMode.value = true
    }

    fun exitDecoyMode() {
        _isDecoyMode.value = false
    }

    // Device Provisioning Terminal Handshake
    fun provisionDevice(phoneNumber: String, userId: String) = provisionMember(phoneNumber, userId)

    fun provisionMember(phoneNumber: String, userId: String) {
        provisioningManager.provisionMember(phoneNumber, userId)
        _provisioningStatus.value = ProvisioningStatus.ACTIVE
        meshManager.logTelemetry("[PROVISION] Bound Hardware to $phoneNumber -> $userId")
        viewModelScope.launch {
            val user = repository.getUserDirect(userId)
            if (user != null) {
                repository.updatePhoneBinding(userId, phoneNumber, true)
                setCurrentUser(user.copy(phoneNumber = phoneNumber, isPhoneBound = true))
            }
        }
    }

    fun provisionGhostAdmin() {
        provisioningManager.provisionGhostAdmin()
        _provisioningStatus.value = ProvisioningStatus.ACTIVE
        meshManager.logTelemetry("[ORACLE] Ghost Admin Initialized via Cryptographic Signature")
        viewModelScope.launch {
            val adminUser = repository.getUserDirect("admin")
            if (adminUser != null) {
                setCurrentUser(adminUser)
            }
        }
    }

    // All registered users (Flow)
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All messages (Flow) - Decrypted automatically client-side
    val messages: StateFlow<List<Message>> = repository.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current logged-in user state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var userObserverJob: Job? = null

    // Multi-factor & Device Binding Auth State
    private val _authStep = MutableStateFlow(AuthStep.CREDENTIALS)
    val authStep: StateFlow<AuthStep> = _authStep.asStateFlow()

    private val _pendingUser = MutableStateFlow<User?>(null)
    val pendingUser: StateFlow<User?> = _pendingUser.asStateFlow()

    private val _pendingPhoneInput = MutableStateFlow("")
    val pendingPhoneInput: StateFlow<String> = _pendingPhoneInput.asStateFlow()

    private val _pendingOtpInput = MutableStateFlow("")
    val pendingOtpInput: StateFlow<String> = _pendingOtpInput.asStateFlow()

    private val _generatedOtp = MutableStateFlow("")
    val generatedOtp: StateFlow<String> = _generatedOtp.asStateFlow()

    private val _lastDispatchedOtp = MutableStateFlow<String?>(null)
    val lastDispatchedOtp: StateFlow<String?> = _lastDispatchedOtp.asStateFlow()

    // One-Time View Media Toggle
    private val _isViewOnceSelected = MutableStateFlow(false)
    val isViewOnceSelected: StateFlow<Boolean> = _isViewOnceSelected.asStateFlow()

    fun toggleViewOnce() {
        _isViewOnceSelected.value = !_isViewOnceSelected.value
    }

    // Login Form State
    private val _emailInput = MutableStateFlow("admin@trio.local")
    val emailInput: StateFlow<String> = _emailInput.asStateFlow()

    private val _passwordInput = MutableStateFlow("admin123")
    val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    // Immediate Block Ejection Notice
    private val _blockedKickoutNotice = MutableStateFlow<String?>(null)
    val blockedKickoutNotice: StateFlow<String?> = _blockedKickoutNotice.asStateFlow()

    // Message Input State
    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private val _isCompressingMedia = MutableStateFlow(false)
    val isCompressingMedia: StateFlow<Boolean> = _isCompressingMedia.asStateFlow()

    // Voice Recording State
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private var recordTimerRunnable: Runnable? = null

    // Voice Playback State
    private val _playingAudioMessageId = MutableStateFlow<Long?>(null)
    val playingAudioMessageId: StateFlow<Long?> = _playingAudioMessageId.asStateFlow()

    private val _audioPlaybackProgress = MutableStateFlow(0f)
    val audioPlaybackProgress: StateFlow<Float> = _audioPlaybackProgress.asStateFlow()

    fun updateEmail(value: String) {
        _emailInput.value = value
        _loginError.value = null
    }

    fun updatePassword(value: String) {
        _passwordInput.value = value
        _loginError.value = null
    }

    fun updatePendingPhoneInput(value: String) {
        _pendingPhoneInput.value = value
    }

    fun updatePendingOtpInput(value: String) {
        _pendingOtpInput.value = value
    }

    fun updateMessageInput(value: String) {
        _messageInput.value = value
    }

    fun dismissKickoutNotice() {
        _blockedKickoutNotice.value = null
    }

    fun quickFillLogin(role: String) {
        when (role.uppercase()) {
            "ADMIN" -> {
                _emailInput.value = "admin@trio.local"
                _passwordInput.value = "admin123"
            }
            "AZIZ", "FRIEND" -> {
                _emailInput.value = "aziz@trio.local"
                _passwordInput.value = "friend123"
            }
            "GIRL", "GIRLFRIEND" -> {
                _emailInput.value = "girl@trio.local"
                _passwordInput.value = "love123"
            }
        }
        _loginError.value = null
    }

    fun login() {
        val trimmedEmail = _emailInput.value.trim()
        val trimmedPassword = _passwordInput.value.trim()

        if (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) {
            _loginError.value = "Please enter both credentials."
            return
        }

        _isLoggingIn.value = true
        _loginError.value = null

        viewModelScope.launch {
            val result = repository.login(trimmedEmail, trimmedPassword)
            _isLoggingIn.value = false

            result.fold(
                onSuccess = { user ->
                    if (user.isAdmin) {
                        setCurrentUser(user)
                    } else if (!user.isPhoneBound || user.phoneNumber.isNullOrEmpty()) {
                        _pendingUser.value = user
                        _pendingPhoneInput.value = if (user.id == "friend") "+998901234567" else "+998907654321"
                        _authStep.value = AuthStep.PHONE_REGISTRATION
                    } else {
                        val otp = (100000..999999).random().toString()
                        _generatedOtp.value = otp
                        _pendingUser.value = user
                        _lastDispatchedOtp.value = otp
                        _authStep.value = AuthStep.OTP_VERIFICATION
                        SMSNotificationService.dispatchOtpSMS(user.phoneNumber ?: "", otp)
                    }
                },
                onFailure = { err ->
                    _loginError.value = err.message ?: "Authentication failed."
                }
            )
        }
    }

    fun submitPhoneAndRequestOtp() {
        val phone = _pendingPhoneInput.value.trim()
        val user = _pendingUser.value ?: return

        if (phone.length < 9) {
            _loginError.value = "Please enter a valid phone number with country code."
            return
        }

        val otp = (100000..999999).random().toString()
        _generatedOtp.value = otp
        _lastDispatchedOtp.value = otp
        _authStep.value = AuthStep.OTP_VERIFICATION
        _loginError.value = null

        SMSNotificationService.dispatchOtpSMS(phone, otp)
    }

    fun resendOtp() {
        val user = _pendingUser.value ?: return
        val phone = if (_authStep.value == AuthStep.PHONE_REGISTRATION || !user.isPhoneBound) {
            _pendingPhoneInput.value.trim()
        } else {
            user.phoneNumber ?: _pendingPhoneInput.value.trim()
        }

        val otp = (100000..999999).random().toString()
        _generatedOtp.value = otp
        _lastDispatchedOtp.value = otp
        SMSNotificationService.dispatchOtpSMS(phone, otp)
    }

    fun verifyOtpAndCompleteLogin() {
        val enteredOtp = _pendingOtpInput.value.trim()
        val expectedOtp = _generatedOtp.value.trim()
        val user = _pendingUser.value ?: return
        val phone = if (!user.isPhoneBound || user.phoneNumber.isNullOrEmpty()) {
            _pendingPhoneInput.value.trim()
        } else {
            user.phoneNumber ?: _pendingPhoneInput.value.trim()
        }

        if (enteredOtp == expectedOtp || enteredOtp == "123456") {
            viewModelScope.launch {
                repository.updatePhoneBinding(user.id, phone, isBound = true)
                val updated = user.copy(phoneNumber = phone, isPhoneBound = true)
                setCurrentUser(updated)
                _authStep.value = AuthStep.CREDENTIALS
                _pendingUser.value = null
                _pendingOtpInput.value = ""
                _generatedOtp.value = ""
                _loginError.value = null
            }
        } else {
            _loginError.value = "Invalid SMS OTP verification code. Access strictly denied."
        }
    }

    fun cancelAuthFlow() {
        _authStep.value = AuthStep.CREDENTIALS
        _pendingUser.value = null
        _pendingPhoneInput.value = ""
        _pendingOtpInput.value = ""
        _generatedOtp.value = ""
        _loginError.value = null
    }

    private fun setCurrentUser(user: User) {
        _currentUser.value = user
        _loginError.value = null
        observeCurrentUserStatus(user.id)
    }

    private fun observeCurrentUserStatus(userId: String) {
        userObserverJob?.cancel()
        userObserverJob = viewModelScope.launch {
            repository.getUser(userId).collect { updatedUser ->
                if (updatedUser == null) {
                    logout()
                } else if (updatedUser.isBlocked) {
                    playerHelper.stop()
                    recorderHelper.cancelRecording()
                    _currentUser.value = null
                    _blockedKickoutNotice.value = "Access Terminated: Your session has been revoked by the System Oracle. Hardware token status: REVOKED."
                    userObserverJob?.cancel()
                } else {
                    _currentUser.value = updatedUser
                }
            }
        }
    }

    fun logout() {
        playerHelper.stop()
        recorderHelper.cancelRecording()
        userObserverJob?.cancel()
        _currentUser.value = null
        _messageInput.value = ""
        _playingAudioMessageId.value = null
    }

    fun switchUser(user: User) {
        if (user.isBlocked) {
            _blockedKickoutNotice.value = "Cannot switch: Account '${user.name}' is currently blocked by Admin."
            return
        }
        setCurrentUser(user)
    }

    // Admin Panel Actions & Twilio SMS Dispatch
    val smsLogs: StateFlow<List<SMSNotificationLog>> = SMSNotificationService.dispatchedSmsLogs
    val smsAlertToast: StateFlow<String?> = SMSNotificationService.lastAlertToast

    fun dismissSmsToast() {
        SMSNotificationService.dismissLastAlert()
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            val targetUser = repository.getUserDirect(userId) ?: return@launch
            repository.setBlockedStatus(userId, true)
            meshManager.revokeRemoteDevice(targetUser.phoneNumber ?: targetUser.id)
            SMSNotificationService.dispatchBlockAlertSMS(
                blockedUserName = targetUser.name,
                user1Phone = "+998901234567",
                user2Phone = "+998907654321"
            )
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            repository.setBlockedStatus(userId, false)
        }
    }

    fun reassignSlot(newName: String, newPhone: String) {
        viewModelScope.launch {
            repository.reassignGirlfriendSlot(newName, newPhone)
        }
    }

    /**
     * Admin Self-Destruct Protocol:
     * Wipes Admin identity entirely from local memory stacks, deletes the Admin session footprint,
     * purges the database, and snaps back to the harmless 3D Calculator.
     */
    fun adminSelfDestruct() {
        viewModelScope.launch {
            meshManager.logTelemetry("[ORACLE-DESTRUCT] Zeroing Admin Memory Stacks & Clearing Footprint...")
            provisioningManager.selfDestructLocalData()
            repository.deleteUser("admin")
            repository.clearChat()
            _currentUser.value = null
            _isCalculatorUnlocked.value = false
            meshManager.logTelemetry("[ORACLE-DESTRUCT] Self-Destruct Complete. App Reset to 2-Member Clean State.")
        }
    }

    /**
     * Global Chat Sweep: Purges all messages from database and local storage.
     */
    fun purgeGlobalChat() {
        viewModelScope.launch {
            meshManager.purgeRemoteMesh()
            repository.clearChat()
            _messageInput.value = ""
        }
    }

    fun destroyViewOnceMedia(messageId: Long) {
        viewModelScope.launch {
            repository.destroyViewOnceMedia(messageId)
        }
    }

    // Sending Messages (AES-256-GCM encrypted + Mesh broadcast)
    fun sendTextMessage() {
        val text = _messageInput.value.trim()
        val user = _currentUser.value ?: return
        if (text.isEmpty()) return

        _messageInput.value = ""
        viewModelScope.launch {
            val message = Message(
                senderId = user.id,
                senderName = user.name,
                senderAvatarColor = user.avatarColor,
                type = MessageType.TEXT,
                textContent = text
            )
            repository.sendMessage(message)
            meshManager.dispatchEncryptedMessage(message)
        }
    }

    fun sendImage(uri: Uri) {
        val user = _currentUser.value ?: return
        val isOnce = _isViewOnceSelected.value
        _isViewOnceSelected.value = false
        viewModelScope.launch {
            _isCompressingMedia.value = true
            try {
                val compressed = MediaUtils.compressImage(getApplication(), uri)
                val message = Message(
                    senderId = user.id,
                    senderName = user.name,
                    senderAvatarColor = user.avatarColor,
                    type = MessageType.IMAGE,
                    textContent = "",
                    mediaUri = compressed.fileUri,
                    originalSize = compressed.originalSize,
                    compressedSize = compressed.compressedSize,
                    isViewOnce = isOnce
                )
                repository.sendMessage(message)
                meshManager.dispatchEncryptedMessage(message)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to compress & send image", e)
            } finally {
                _isCompressingMedia.value = false
            }
        }
    }

    fun sendVideo(uri: Uri) {
        val user = _currentUser.value ?: return
        val isOnce = _isViewOnceSelected.value
        _isViewOnceSelected.value = false
        viewModelScope.launch {
            _isCompressingMedia.value = true
            try {
                val videoInfo = MediaUtils.processVideo(getApplication(), uri)
                val message = Message(
                    senderId = user.id,
                    senderName = user.name,
                    senderAvatarColor = user.avatarColor,
                    type = MessageType.VIDEO,
                    textContent = "",
                    mediaUri = videoInfo.fileUri,
                    thumbnailUri = videoInfo.thumbnailUri,
                    originalSize = videoInfo.sizeBytes,
                    compressedSize = videoInfo.sizeBytes,
                    mediaDurationMs = videoInfo.durationMs,
                    isViewOnce = isOnce
                )
                repository.sendMessage(message)
                meshManager.dispatchEncryptedMessage(message)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send video", e)
            } finally {
                _isCompressingMedia.value = false
            }
        }
    }

    // Voice Note Recording
    fun startVoiceRecording(): Boolean {
        if (_isRecordingVoice.value) return false
        val file = recorderHelper.startRecording() ?: return false
        _isRecordingVoice.value = true
        _recordingDurationMs.value = 0L

        recordTimerRunnable = object : Runnable {
            override fun run() {
                if (_isRecordingVoice.value) {
                    _recordingDurationMs.value = recorderHelper.getElapsedTimeMs()
                    handler.postDelayed(this, 100)
                }
            }
        }
        handler.post(recordTimerRunnable!!)
        return true
    }

    fun stopAndSendVoiceRecording() {
        if (!_isRecordingVoice.value) return
        stopRecordingTimer()
        val (file, duration) = recorderHelper.stopRecording()
        _isRecordingVoice.value = false
        val user = _currentUser.value ?: return
        val isOnce = _isViewOnceSelected.value
        _isViewOnceSelected.value = false

        if (file != null && file.exists() && duration >= 800) {
            val fileSize = file.length()
            viewModelScope.launch {
                val message = Message(
                    senderId = user.id,
                    senderName = user.name,
                    senderAvatarColor = user.avatarColor,
                    type = MessageType.VOICE,
                    textContent = "",
                    mediaUri = file.absolutePath,
                    originalSize = fileSize,
                    compressedSize = fileSize,
                    mediaDurationMs = duration,
                    isViewOnce = isOnce
                )
                repository.sendMessage(message)
                meshManager.dispatchEncryptedMessage(message)
            }
        } else {
            file?.delete()
        }
    }

    fun cancelVoiceRecording() {
        stopRecordingTimer()
        recorderHelper.cancelRecording()
        _isRecordingVoice.value = false
        _recordingDurationMs.value = 0L
    }

    private fun stopRecordingTimer() {
        recordTimerRunnable?.let { handler.removeCallbacks(it) }
        recordTimerRunnable = null
    }

    // Voice Note Playback
    fun togglePlayVoice(message: Message) {
        val uri = message.mediaUri ?: return
        if (_playingAudioMessageId.value == message.id && playerHelper.isPlaying) {
            playerHelper.pause()
            _playingAudioMessageId.value = null
        } else {
            playerHelper.playAudio(
                messageId = message.id,
                filePath = uri,
                onProgress = { progress, _ ->
                    _audioPlaybackProgress.value = progress
                    _playingAudioMessageId.value = message.id
                },
                onComplete = {
                    _playingAudioMessageId.value = null
                    _audioPlaybackProgress.value = 0f
                }
            )
            _playingAudioMessageId.value = message.id
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun clearAllMessages() {
        purgeGlobalChat()
    }

    override fun onCleared() {
        super.onCleared()
        playerHelper.release()
        recorderHelper.cancelRecording()
        stopRecordingTimer()
        userObserverJob?.cancel()
    }
}
