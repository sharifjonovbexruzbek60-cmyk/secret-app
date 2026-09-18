package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.viewmodel.ChatViewModel

/**
 * Secret Admin Control Panel (Ghost Admin Command Center).
 * Styled with DXX Military Telecom aesthetics (Tactical Obsidian, Glowing Neon Green #00FF66, Telemetry Cyan).
 * Controls:
 * 1. Stealth Mode Toggle (Hides user switchers and action affordances)
 * 2. Session Terminate / Kick (Sets hardware token to REVOKED, kicking user to basic calculator)
 * 3. Admin Self-Destruct (Wipes Admin identity entirely from local memory and remote footprint)
 * 4. Global Chat Sweep (Purges all encrypted collection nodes with zero-fill)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    val users by viewModel.allUsers.collectAsState()
    val otherUsers = users.filter { it.role != "ADMIN" }
    val messages by viewModel.messages.collectAsState()
    val smsLogs by viewModel.smsLogs.collectAsState()
    val areButtonsHidden by viewModel.areButtonsHidden.collectAsState()
    val telemetryLogs by viewModel.networkTelemetryLog.collectAsState()
    val activeRollingHex by viewModel.activeRollingHex.collectAsState()
    val deviceToken by viewModel.deviceToken.collectAsState()

    var userToBlock by remember { mutableStateOf<User?>(null) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var showSelfDestructDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Admin Security",
                            tint = Color(0xFF00FF66),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DXX // GHOST ORACLE PANEL",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF00FF66)
                            )
                            Text(
                                text = "ROOT ACCESS // LEVEL 5 CLASSIFIED",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.lockToCalculator() }) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Lock",
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF05080C)
                )
            )
        },
        containerColor = Color(0xFF05080C)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111A)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "EPHEMERAL SESSION KEY:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = activeRollingHex,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF66)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ORACLE FINGERPRINT:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "0x" + deviceToken.take(12).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            // 1. STEALTH MODE TOGGLE (Hide Buttons)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (areButtonsHidden) Color(0xFF0D2418) else Color(0xFF0B111A)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (areButtonsHidden) Color(0xFF00FF66) else Color(0xFF1E293B)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (areButtonsHidden) Color(0xFF00FF66) else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "STEALTH MODE (HIDE BUTTONS)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (areButtonsHidden) Color(0xFF00FF66) else Color(0xFFF1F5F9)
                            )
                            Text(
                                text = if (areButtonsHidden) "ACTIVE: User switchers & admin affordances are hidden"
                                else "INACTIVE: Standard controls visible in conversation view",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Switch(
                        checked = areButtonsHidden,
                        onCheckedChange = { viewModel.toggleHideButtons() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00FF66),
                            checkedTrackColor = Color(0xFF0F3826),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }

            // 2. SESSION TERMINATE / REMOTE KICK
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111A)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SESSION TERMINATE / HARDWARE REVOCATION",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "Revoking a member forces an immediate disconnect, marks their hardware token REVOKED, and kicks them back to the basic calculator.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    otherUsers.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = user.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = user.phoneNumber ?: "No phone registered",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            if (user.isBlocked) {
                                OutlinedButton(
                                    onClick = { viewModel.unblockUser(user.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00FF66)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF66)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("RESTORE", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { userToBlock = user },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("REVOKE & KICK", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFFCA5A5))
                                }
                            }
                        }
                    }
                }
            }

            // 3. GLOBAL CHAT SWEEP & ADMIN SELF-DESTRUCT
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111A)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FORENSIC PURGE & ORACLE LIFECYCLE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFFFB300)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Global Chat Sweep
                    Button(
                        onClick = { showClearChatDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFFFB300))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GLOBAL CHAT SWEEP (${messages.size} msgs)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Admin Self-Destruct
                    Button(
                        onClick = { showSelfDestructDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF450A0A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Dangerous, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADMIN SELF-DESTRUCT (WIPE ORACLE)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 4. LIVE TELEMETRY AUDIT LOGS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030508)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE AUDIT & TELEMETRY STREAM",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color(0xFF020406))
                            .border(1.dp, Color(0xFF111827))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        telemetryLogs.forEach { logLine ->
                            Text(
                                text = logLine,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = if (logLine.contains("ORACLE") || logLine.contains("ADMIN")) Color(0xFF00FF66)
                                else Color(0xFF00E5FF).copy(alpha = 0.8f),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Revoke Confirmation Dialog
        userToBlock?.let { target ->
            AlertDialog(
                onDismissRequest = { userToBlock = null },
                containerColor = Color(0xFF0D121B),
                title = {
                    Text(
                        text = "CONFIRM HARDWARE REVOCATION",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Revoking '${target.name}' will immediately eject them, flag their provisioning token as REVOKED, and reset their client back to the basic calculator.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.blockUser(target.id)
                            userToBlock = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D))
                    ) {
                        Text("EXECUTE KICK", fontFamily = FontFamily.Monospace, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToBlock = null }) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        // Clear Chat Confirmation Dialog
        if (showClearChatDialog) {
            AlertDialog(
                onDismissRequest = { showClearChatDialog = false },
                containerColor = Color(0xFF0D121B),
                title = {
                    Text("PURGE ALL CHAT DATA", fontFamily = FontFamily.Monospace, color = Color(0xFFFFB300))
                },
                text = {
                    Text(
                        "Permanently wipes all encrypted messages and zero-fills local file systems across all members.",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllMessages()
                            showClearChatDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF78350F))
                    ) {
                        Text("PURGE ALL", fontFamily = FontFamily.Monospace, color = Color(0xFFFFB300))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearChatDialog = false }) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        // Admin Self-Destruct Dialog
        if (showSelfDestructDialog) {
            AlertDialog(
                onDismissRequest = { showSelfDestructDialog = false },
                containerColor = Color(0xFF1C0A0A),
                title = {
                    Text("ADMIN SELF-DESTRUCT PROTOCOL", fontFamily = FontFamily.Monospace, color = Color(0xFFEF4444))
                },
                text = {
                    Text(
                        "This will completely erase the Admin identity, zero all memory stacks, and purge the Admin footprint from the database. The app will permanently appear as if only 2 members ever existed. Snap back to Calculator is immediate.",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFCA5A5)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSelfDestructDialog = false
                            viewModel.adminSelfDestruct()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B))
                    ) {
                        Text("EXECUTE SELF-DESTRUCT", fontFamily = FontFamily.Monospace, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSelfDestructDialog = false }) {
                        Text("ABORT", fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}
