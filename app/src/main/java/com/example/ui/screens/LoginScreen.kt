package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.ProvisioningStatus
import com.example.ui.viewmodel.ChatViewModel

/**
 * Classified DXX Telecom Military Authentication & Device Provisioning Terminal.
 * Monochromatic tactical black (#05080C), glowing cyber green (#00FF66), cyan telemetry accents (#00E5FF).
 * Displays live cascading network logs, rotating hexadecimal keys, hardware token status,
 * and handles first-time phone provisioning vs subsequent silent handshakes.
 */
@Composable
fun LoginScreen(viewModel: ChatViewModel) {
    val provisioningStatus by viewModel.provisioningStatus.collectAsState()
    val deviceToken by viewModel.deviceToken.collectAsState()
    val activeRollingHex by viewModel.activeRollingHex.collectAsState()
    val telemetryLogs by viewModel.networkTelemetryLog.collectAsState()
    val isMeshConnected by viewModel.isMeshConnected.collectAsState()
    val blockedNotice by viewModel.blockedKickoutNotice.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    var phoneInput by remember { mutableStateOf("+998901234567") }
    var selectedIdentity by remember { mutableStateOf("friend") } // "friend" (Aziz) or "girlfriend" (Girl)
    var showOracleBypassDialog by remember { mutableStateOf(false) }
    var oracleSignatureInput by remember { mutableStateOf("") }
    var oracleError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dxx_telecom_terminal_screen"),
        color = Color(0xFF05080C) // Tactical Deep Obsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: System ID & Emergency Lock to Calculator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isMeshConnected) Color(0xFF00FF66) else Color(0xFFFFB300))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DXX // SIGINT-HUB v2.6",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00FF66),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                // Emergency Camouflage Return
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.clickable { viewModel.lockToCalculator() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Lock",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CALC LOCK",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Central Telemetry Holographic Widget
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00FF66).copy(alpha = 0.1f))
                    .border(1.5.dp, Color(0xFF00FF66), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = Color(0xFF00FF66),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "OPERATION WIZCHAT MESH",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF1F5F9),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CLASSIFIED TELECOM ENCLAVE • END-TO-END AES-256-GCM",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Live Encryption Key & Hardware Fingerprint Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111A)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "HW-FINGERPRINT:",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "0x" + deviceToken.take(16).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ROTATING HEX KEY:",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = activeRollingHex,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00FF66),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PROVISION STATUS:",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = provisioningStatus.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = when (provisioningStatus) {
                                ProvisioningStatus.ACTIVE -> Color(0xFF00FF66)
                                ProvisioningStatus.REVOKED -> Color(0xFFEF4444)
                                ProvisioningStatus.UNPROVISIONED -> Color(0xFFFFB300)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Action Console: Depending on Provisioning Status
            if (provisioningStatus == ProvisioningStatus.REVOKED) {
                // Session Revoked Warning
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF200B0B)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "HARDWARE TOKEN REVOKED",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your device signature has been permanently revoked by the System Oracle. Access to the covert mesh network is terminated.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFCA5A5),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.lockToCalculator() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("FACTORY RESET TO CALCULATOR", fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            } else {
                // Device Provisioning Terminal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111A)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TERMINAL PROVISIONING & MOUNT",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF66)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Bind this physical hardware token to your registered network profile:",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Target Identity Selector (Aziz or Girl)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IdentityPill(
                                label = "Aziz (Node 1)",
                                isSelected = selectedIdentity == "friend",
                                color = Color(0xFF10B981),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedIdentity = "friend"
                                    phoneInput = "+998901234567"
                                }
                            )
                            IdentityPill(
                                label = "Girl (Node 2)",
                                isSelected = selectedIdentity == "girlfriend",
                                color = Color(0xFFDB2777),
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedIdentity = "girlfriend"
                                    phoneInput = "+998907654321"
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Phone Number Input
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Registered Phone Number", fontFamily = FontFamily.Monospace) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF00FF66))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FF66),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Provision Button
                        Button(
                            onClick = {
                                viewModel.provisionDevice(phoneInput, selectedIdentity)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("provision_device_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3826)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF66)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF00FF66))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INITIALIZE MESH HANDSHAKE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FF66)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ghost Admin Oracle Secret Gate Trigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = { showOracleBypassDialog = true }
                            ) {
                                Text(
                                    text = "CREATOR ORACLE BYPASS [GHOST ADMIN]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Real-Time Cascading Telemetry Log Stream
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF06090E)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE MESH TELEMETRY STREAM",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "PORT 443 // TLS-GCM",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00FF66)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFF020406))
                            .border(1.dp, Color(0xFF111827))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (telemetryLogs.isEmpty()) {
                            Text(
                                text = "[SIGINT] Awaiting kernel trigger handshake...",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF475569)
                            )
                        } else {
                            telemetryLogs.forEach { logLine ->
                                Text(
                                    text = logLine,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = if (logLine.contains("DISPATCH") || logLine.contains("ORACLE")) Color(0xFF00FF66)
                                    else Color(0xFF00E5FF).copy(alpha = 0.9f),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Ghost Admin Oracle Signature Bypass Dialog
        if (showOracleBypassDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    showOracleBypassDialog = false
                    oracleSignatureInput = ""
                    oracleError = null
                },
                containerColor = Color(0xFF0D121B),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00FF66))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GHOST ORACLE SIGNATURE",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF1F5F9),
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Enter the Creator Master Passphrase to mount the invisible Ghost Admin node:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = oracleSignatureInput,
                            onValueChange = {
                                oracleSignatureInput = it
                                oracleError = null
                            },
                            label = { Text("Passphrase", fontFamily = FontFamily.Monospace) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FF66),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        if (oracleError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = oracleError!!,
                                color = Color(0xFFEF4444),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (oracleSignatureInput.trim().equals("hello", ignoreCase = true) ||
                                oracleSignatureInput.trim() == "admin123" ||
                                oracleSignatureInput.trim() == "7788" ||
                                oracleSignatureInput.trim() == "777"
                            ) {
                                showOracleBypassDialog = false
                                viewModel.provisionGhostAdmin()
                            } else {
                                oracleError = "Invalid cryptographic signature. Access denied."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3826)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF66))
                    ) {
                        Text("VERIFY & MOUNT", fontFamily = FontFamily.Monospace, color = Color(0xFF00FF66))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOracleBypassDialog = false }) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}

@Composable
fun IdentityPill(
    label: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF131A26))
            .border(
                width = 1.dp,
                color = if (isSelected) color else Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else Color(0xFF94A3B8)
        )
    }
}
