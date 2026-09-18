package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.model.User
import com.example.ui.viewmodel.ChatViewModel
import com.example.util.MediaUtils
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    currentUser: User,
    onNavigateToAdminPanel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val messages by viewModel.messages.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val messageText by viewModel.messageInput.collectAsState()
    val isCompressingMedia by viewModel.isCompressingMedia.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val recordDurationMs by viewModel.recordingDurationMs.collectAsState()
    val playingMessageId by viewModel.playingAudioMessageId.collectAsState()
    val audioProgress by viewModel.audioPlaybackProgress.collectAsState()
    val smsAlertToast by viewModel.smsAlertToast.collectAsState()
    val isViewOnceSelected by viewModel.isViewOnceSelected.collectAsState()
    val areButtonsHidden by viewModel.areButtonsHidden.collectAsState()

    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHoldingTitle by remember { mutableStateOf(false) }
    var showAdminSecretDialog by remember { mutableStateOf(false) }
    var adminPassphraseInput by remember { mutableStateOf("") }
    var adminPassphraseError by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var selectedImageForView by remember { mutableStateOf<Message?>(null) }
    var selectedVideoForView by remember { mutableStateOf<Message?>(null) }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Photo picker for image
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendImage(uri)
        }
    }

    // Video picker for video
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendVideo(uri)
        }
    }

    // Audio recording permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .testTag("app_bar_title_area")
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    isHoldingTitle = true
                                    holdProgress = 0f
                                    var elapsed = 0L
                                    val holdJob = coroutineScope.launch {
                                        val interval = 50L
                                        while (elapsed < 5000L) {
                                            delay(interval)
                                            elapsed += interval
                                            holdProgress = (elapsed.toFloat() / 5000f).coerceIn(0f, 1f)
                                        }
                                        holdProgress = 1f
                                        isHoldingTitle = false
                                        adminPassphraseInput = ""
                                        adminPassphraseError = null
                                        showAdminSecretDialog = true
                                    }
                                    waitForUpOrCancellation()
                                    holdJob.cancel()
                                    isHoldingTitle = false
                                    holdProgress = 0f
                                }
                            }
                    ) {
                        // WizChat Logo Badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "WizChat Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "WizChat",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isHoldingTitle) MaterialTheme.colorScheme.primary else Color(0xFF10B981))
                                )
                                if (isHoldingTitle) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    LinearProgressIndicator(
                                        progress = { holdProgress },
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = if (isHoldingTitle) "Holding for Admin (${(holdProgress * 5).toInt()}s/5s)..."
                                else if (currentUser.isAdmin) "Oracle Node • Aziz • Girl (Covert Mesh)"
                                else "Mesh Node // 2 Verified Terminals",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isHoldingTitle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Quick Camouflage Lock to 3D Calculator
                    IconButton(
                        onClick = { viewModel.lockToCalculator() },
                        modifier = Modifier.testTag("chat_calc_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Lock to Calculator",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Admin Panel direct button (strictly visible ONLY to Admin and when buttons are not hidden)
                    if (currentUser.isAdmin && !areButtonsHidden) {
                        IconButton(
                            onClick = onNavigateToAdminPanel,
                            modifier = Modifier.testTag("admin_panel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Control Panel",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Identity Pill (hidden if Admin chose to hide buttons and user is not Admin)
                    if (!areButtonsHidden || currentUser.isAdmin) {
                        Surface(
                            color = Color(currentUser.avatarColor).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentUser.avatarColor))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentUser.name.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(currentUser.avatarColor)
                                )
                            }
                        }
                    }

                    // Options Menu
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("chat_options_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Admin exclusive controls: Toggle hiding buttons / Stealth Mode
                        if (currentUser.isAdmin) {
                            DropdownMenuItem(
                                text = {
                                    Text(if (areButtonsHidden) "Show Action Buttons" else "Hide Buttons (Stealth)")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (areButtonsHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleHideButtons()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Admin Panel") },
                                leadingIcon = {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToAdminPanel()
                                }
                            )
                        }

                        // Switch identity helper (hidden if Admin chose to hide buttons)
                        if (!areButtonsHidden || currentUser.isAdmin) {
                            Text(
                                text = "SWITCH CURRENT USER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )

                            val switchableUsers = if (currentUser.isAdmin) allUsers else allUsers.filter { !it.isAdmin }
                            switchableUsers.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(user.name)
                                            if (user.id == currentUser.id) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("(You)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            } else if (user.isBlocked) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("(Blocked)", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(user.avatarColor))
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.switchUser(user)
                                    }
                                )
                            }
                        }

                        DropdownMenuItem(
                            text = { Text("Lock to Calculator") },
                            leadingIcon = {
                                Icon(Icons.Default.Calculate, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                viewModel.lockToCalculator()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            leadingIcon = {
                                Icon(Icons.Default.SwitchAccount, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                viewModel.logout()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Simulated Twilio SMS Dispatch Notification Banner
            AnimatedVisibility(
                visible = smsAlertToast != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "SMS Alert",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = smsAlertToast ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissSmsToast() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            // Media compression banner
            AnimatedVisibility(
                visible = isCompressingMedia,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Auto-compressing media for low-end device...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isMe = message.senderId == currentUser.id
                    MessageBubbleItem(
                        message = message,
                        isMe = isMe,
                        isViewerAdmin = currentUser.isAdmin,
                        isPlayingVoice = playingMessageId == message.id,
                        audioProgress = if (playingMessageId == message.id) audioProgress else 0f,
                        onPlayVoiceClick = { viewModel.togglePlayVoice(message) },
                        onImageClick = { selectedImageForView = message },
                        onVideoClick = { selectedVideoForView = message }
                    )
                }
            }

            // Bottom Input Bar
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                if (isRecordingVoice) {
                    // Voice Recording Active Bar
                    ActiveVoiceRecordingBar(
                        durationMs = recordDurationMs,
                        onCancel = { viewModel.cancelVoiceRecording() },
                        onSend = { viewModel.stopAndSendVoiceRecording() }
                    )
                } else {
                    // Standard Message Input Row
                    ChatInputRow(
                        messageText = messageText,
                        isViewOnceSelected = isViewOnceSelected,
                        onToggleViewOnce = { viewModel.toggleViewOnce() },
                        onMessageChange = { viewModel.updateMessageInput(it) },
                        onSendText = { viewModel.sendTextMessage() },
                        onAttachmentClick = { showAttachmentSheet = true },
                        onMicClick = {
                            val permission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            )
                            if (permission == PackageManager.PERMISSION_GRANTED) {
                                viewModel.startVoiceRecording()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )
                }
            }
        }
    }

    // Attachment Modal Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Share Media (Auto-Compressed)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // 1-Time View (Self-Destruct) Toggle Switch in Attachment Sheet
                Surface(
                    color = if (isViewOnceSelected) Color(0xFFEF4444).copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleViewOnce() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (isViewOnceSelected) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1-Time View (Self-Destruct)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isViewOnceSelected) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Permanently deleted from storage immediately after viewing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isViewOnceSelected,
                            onCheckedChange = { viewModel.toggleViewOnce() }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Photo Upload
                    AttachmentOption(
                        icon = Icons.Default.Image,
                        label = "Photo\n(Compressed)",
                        color = Color(0xFF6366F1),
                        onClick = {
                            showAttachmentSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    // Video Upload
                    AttachmentOption(
                        icon = Icons.Default.Videocam,
                        label = "Video\n(Optimized)",
                        color = Color(0xFFEC4899),
                        onClick = {
                            showAttachmentSheet = false
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                    )

                    // Quick Demo Photo (helpful for emulator testing when gallery is empty)
                    AttachmentOption(
                        icon = Icons.Default.Add,
                        label = "Demo Photo\n(Instant)",
                        color = Color(0xFF10B981),
                        onClick = {
                            showAttachmentSheet = false
                            coroutineScope.launch {
                                val demoFile = createQuickDemoImage(context)
                                viewModel.sendImage(Uri.fromFile(demoFile))
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Fullscreen Viewers with View-Once Self-Destruct Trigger
    selectedImageForView?.let { imgMsg ->
        ImageViewerDialog(
            message = imgMsg,
            onDismiss = {
                if (imgMsg.isViewOnce) {
                    viewModel.destroyViewOnceMedia(imgMsg.id)
                }
                selectedImageForView = null
            }
        )
    }

        selectedVideoForView?.let { vidMsg ->
        VideoPlayerDialog(
            message = vidMsg,
            onDismiss = {
                if (vidMsg.isViewOnce) {
                    viewModel.destroyViewOnceMedia(vidMsg.id)
                }
                selectedVideoForView = null
            }
        )
    }

    // Secret Admin Gate Dialog (Triggered by holding WizChat logo for 5 seconds)
    if (showAdminSecretDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminSecretDialog = false
                adminPassphraseInput = ""
                adminPassphraseError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Admin Security Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter secret passphrase to access the confidential Admin Control Panel:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = adminPassphraseInput,
                        onValueChange = {
                            adminPassphraseInput = it
                            adminPassphraseError = null
                        },
                        label = { Text("Passphrase") },
                        placeholder = { Text("Type hello") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_passphrase_input"),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (adminPassphraseInput.trim().equals("hello", ignoreCase = true)) {
                                    showAdminSecretDialog = false
                                    adminPassphraseInput = ""
                                    adminPassphraseError = null
                                    val adminUser = allUsers.firstOrNull { it.isAdmin }
                                    if (adminUser != null && currentUser.id != adminUser.id) {
                                        viewModel.switchUser(adminUser)
                                    }
                                    onNavigateToAdminPanel()
                                } else {
                                    adminPassphraseError = "Incorrect passphrase. Access denied."
                                }
                            }
                        ),
                        isError = adminPassphraseError != null
                    )
                    if (adminPassphraseError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = adminPassphraseError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminPassphraseInput.trim().equals("hello", ignoreCase = true)) {
                            showAdminSecretDialog = false
                            adminPassphraseInput = ""
                            adminPassphraseError = null
                            val adminUser = allUsers.firstOrNull { it.isAdmin }
                            if (adminUser != null && currentUser.id != adminUser.id) {
                                viewModel.switchUser(adminUser)
                            }
                            onNavigateToAdminPanel()
                        } else {
                            adminPassphraseError = "Incorrect passphrase. Access denied."
                        }
                    },
                    modifier = Modifier.testTag("admin_passphrase_submit_button")
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminSecretDialog = false
                        adminPassphraseInput = ""
                        adminPassphraseError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun ChatInputRow(
    messageText: String,
    isViewOnceSelected: Boolean,
    onToggleViewOnce: () -> Unit,
    onMessageChange: (String) -> Unit,
    onSendText: () -> Unit,
    onAttachmentClick: () -> Unit,
    onMicClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onAttachmentClick,
            modifier = Modifier.testTag("chat_attachment_button")
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "Attach Media",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1-Time View Quick Toggle
        IconButton(
            onClick = onToggleViewOnce,
            modifier = Modifier.testTag("chat_view_once_toggle_button")
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isViewOnceSelected) Color(0xFFEF4444)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "1",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isViewOnceSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageChange,
            placeholder = { Text("Type a message...") },
            modifier = Modifier
                .weight(1f)
                .testTag("chat_message_input"),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendText() })
        )

        Spacer(modifier = Modifier.width(6.dp))

        if (messageText.isNotBlank()) {
            FilledIconButton(
                onClick = onSendText,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("chat_send_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            FilledIconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("chat_record_voice_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Record Voice Note",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun ActiveVoiceRecordingBar(
    durationMs: Long,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red recording pulse indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Color.Red)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = MediaUtils.formatDuration(durationMs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Recording (AMR-NB 12.2 kbps low-bitrate)...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Cancel Button
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Cancel Recording",
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Send Voice Note Button
        FilledIconButton(
            onClick = onSend,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Send Voice Note",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun MessageBubbleItem(
    message: Message,
    isMe: Boolean,
    isViewerAdmin: Boolean = false,
    isPlayingVoice: Boolean,
    audioProgress: Float,
    onPlayVoiceClick: () -> Unit,
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    val isGhostAdminMsg = message.senderId == "admin"
    val displayName = if (isGhostAdminMsg && !isViewerAdmin) "[SYSTEM RELAY]" else message.senderName
    val displayColor = if (isGhostAdminMsg && !isViewerAdmin) Color(0xFF00FF66) else Color(message.senderAvatarColor)
    val displayInitial = if (isGhostAdminMsg && !isViewerAdmin) "Ω" else message.senderName.take(1)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Sender Name if from another member
        if (!isMe) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(displayColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayInitial,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = displayColor
                )
            }
        }

        // Message Content Bubble
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (message.isViewOnce && (message.isViewed || message.mediaUri == null)) {
                    // Destroyed View-Once Media Placeholder
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                    else Color(0xFFEF4444).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = if (isMe) MaterialTheme.colorScheme.onPrimary else Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Opened • Self-Destructed",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "1-Time media permanently purged",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.textContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    MessageType.IMAGE -> {
                        val file = message.mediaUri?.let { File(it) }
                        val imageModel = if (file != null && file.exists()) file else message.mediaUri

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onImageClick)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Shared Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // 1-Time View Badge
                            if (message.isViewOnce) {
                                Surface(
                                    color = Color(0xFFEF4444).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "1-TIME VIEW",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Compression Tag Badge
                            if (message.compressedSize > 0) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "📉 ${MediaUtils.formatFileSize(message.compressedSize)}",
                                        color = Color(0xFF34D399),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    MessageType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .clickable(onClick = onVideoClick),
                            contentAlignment = Alignment.Center
                        ) {
                            if (message.thumbnailUri != null) {
                                AsyncImage(
                                    model = File(message.thumbnailUri),
                                    contentDescription = "Video Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Center Play Icon Circle
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // 1-Time View Badge
                            if (message.isViewOnce) {
                                Surface(
                                    color = Color(0xFFEF4444).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "1-TIME VIEW",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Duration badge
                            Surface(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "${MediaUtils.formatDuration(message.mediaDurationMs)} • ${MediaUtils.formatFileSize(message.compressedSize)}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    MessageType.VOICE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = onPlayVoiceClick,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isPlayingVoice) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlayingVoice) "Pause" else "Play",
                                    tint = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                if (isPlayingVoice) {
                                    LinearProgressIndicator(
                                        progress = { audioProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    )
                                } else {
                                    // Static faux waveform indicator
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val barHeights = listOf(8, 14, 18, 10, 16, 22, 12, 18, 14, 20, 10, 15, 12)
                                        barHeights.forEach { heightDp ->
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(heightDp.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(
                                                        if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                    )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = MediaUtils.formatDuration(message.mediaDurationMs),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = (if (message.isViewOnce) "🔥 1-TIME • " else "") + "AMR-NB • ${MediaUtils.formatFileSize(message.compressedSize)}",
                                        fontSize = 9.sp,
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

                // Timestamp
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp, end = 2.dp)
                )
            }
        }
    }
}

/**
 * Creates a quick lightweight demo image locally to test compressed image sharing on emulator.
 */
private fun createQuickDemoImage(context: android.content.Context): File {
    val dir = File(context.filesDir, "demo_images").apply { mkdirs() }
    val file = File(dir, "demo_${System.currentTimeMillis()}.jpg")
    val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.RGB_565)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        color = android.graphics.Color.rgb(79, 70, 229)
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 800f, 600f, paint)

    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 42f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Trio Messenger", 400f, 280f, textPaint)

    textPaint.textSize = 28f
    canvas.drawText("Low-End Optimized Photo", 400f, 340f, textPaint)

    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
    }
    bitmap.recycle()
    return file
}
