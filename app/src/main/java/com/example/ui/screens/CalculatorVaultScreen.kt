package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.DecimalFormat

/**
 * High-Fidelity 3D-Styled Cyberpunk/Obsidian Calculator (Camouflage Layer).
 * Features Neumorphic metallic 3D buttons with tactile drop shadows, haptic vibration on touch,
 * flawless arithmetic math operations, and a classified kernel interceptor:
 * Typing `777+0` followed by `=` triggers a cyber-matrix dissolve transition into the Covert Terminal.
 */
@Composable
fun CalculatorVaultScreen(
    onUnlockVault: () -> Unit,
    onOpenDecoy: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var displayValue by remember { mutableStateOf("0") }
    var expressionHistory by remember { mutableStateOf("") }
    var previousValue by remember { mutableStateOf<Double?>(null) }
    var pendingOperation by remember { mutableStateOf<String?>(null) }
    var isNewEntry by remember { mutableStateOf(true) }
    var typedSequence by remember { mutableStateOf("") }

    // Secret Matrix Shatter/Dissolve State
    var isMatrixTransitioning by remember { mutableStateOf(false) }

    LaunchedEffect(isMatrixTransitioning) {
        if (isMatrixTransitioning) {
            delay(1200)
            onUnlockVault()
        }
    }

    fun handleDigit(digit: String) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        typedSequence += digit
        if (isNewEntry || displayValue == "0") {
            displayValue = digit
            isNewEntry = false
        } else {
            if (displayValue.length < 11) {
                displayValue += digit
            }
        }
    }

    fun handleDecimal() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        typedSequence += "."
        if (isNewEntry) {
            displayValue = "0."
            isNewEntry = false
        } else if (!displayValue.contains(".")) {
            displayValue += "."
        }
    }

    fun handleClear() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        displayValue = "0"
        expressionHistory = ""
        previousValue = null
        pendingOperation = null
        isNewEntry = true
        typedSequence = ""
    }

    fun handleToggleSign() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val current = displayValue.toDoubleOrNull() ?: return
        displayValue = if (current == current.toLong().toDouble()) {
            (-current.toLong()).toString()
        } else {
            (-current).toString()
        }
    }

    fun handlePercent() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val current = displayValue.toDoubleOrNull() ?: return
        displayValue = (current / 100.0).toString()
    }

    fun handleOperation(op: String) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        typedSequence += op
        previousValue = displayValue.toDoubleOrNull()
        pendingOperation = op
        expressionHistory = "$displayValue $op"
        isNewEntry = true
    }

    fun calculateResult() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        // =========================================================================
        // SECRET GATE INTERCEPTOR:
        // Exclusive kernel trigger: "777+0=" or "7788=" or "777="
        // Bypasses standard calculation flow completely and triggers Matrix Dissolve.
        // =========================================================================
        val is777Plus0Trigger = typedSequence.contains("777+0") ||
                (previousValue == 777.0 && pendingOperation == "+" && displayValue == "0") ||
                typedSequence.endsWith("777+0")

        val isAlternateVaultTrigger = typedSequence.contains("7788") ||
                displayValue == "7788" ||
                typedSequence.endsWith("7788") ||
                displayValue == "777" ||
                typedSequence.endsWith("777")

        if (is777Plus0Trigger || isAlternateVaultTrigger) {
            isMatrixTransitioning = true
            return
        }

        // Decoy trigger
        if (typedSequence.endsWith("0000") || displayValue == "0000" || typedSequence.endsWith("9999") || displayValue == "9999") {
            onOpenDecoy()
            return
        }

        // Standard arithmetic calculation
        val current = displayValue.toDoubleOrNull() ?: 0.0
        val prev = previousValue

        if (prev != null && pendingOperation != null) {
            expressionHistory = "$expressionHistory $displayValue ="
            val result = when (pendingOperation) {
                "+" -> prev + current
                "-" -> prev - current
                "×" -> prev * current
                "÷" -> if (current != 0.0) prev / current else Double.NaN
                else -> current
            }

            displayValue = if (result.isNaN()) {
                "Error"
            } else {
                val format = DecimalFormat("#.########")
                format.format(result)
            }
            previousValue = null
            pendingOperation = null
            isNewEntry = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D14)) // Deep Obsidian Canvas
            .testTag("calculator_vault_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Telemetry Header (Discreet Camouflage)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MATH-CORE 64",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF4B5563),
                        letterSpacing = 1.5.sp
                    )
                }

                Text(
                    text = "RAD",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF374151),
                    fontWeight = FontWeight.Bold
                )
            }

            // Futuristic 3D Beveled LED Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F1522), Color(0xFF080C14))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF26324A), Color(0xFF121826))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Previous Formula History
                    Text(
                        text = expressionHistory,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6B7280),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Large Glowing Digits
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = if (displayValue.length > 8) 38.sp else 50.sp
                        ),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0),
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Neumorphic Keypad Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Row 1: AC, +/-, %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Calculator3DButton(
                        text = "AC",
                        textColor = Color(0xFFEF4444), // Amber/Red
                        baseColor = Color(0xFF181F2F),
                        modifier = Modifier.weight(1f),
                        onClick = { handleClear() }
                    )
                    Calculator3DButton(
                        text = "±",
                        textColor = Color(0xFF94A3B8),
                        baseColor = Color(0xFF181F2F),
                        modifier = Modifier.weight(1f),
                        onClick = { handleToggleSign() }
                    )
                    Calculator3DButton(
                        text = "%",
                        textColor = Color(0xFF94A3B8),
                        baseColor = Color(0xFF181F2F),
                        modifier = Modifier.weight(1f),
                        onClick = { handlePercent() }
                    )
                    Calculator3DButton(
                        text = "÷",
                        textColor = Color(0xFF00E5FF), // Cyan Operator
                        baseColor = Color(0xFF131F33),
                        modifier = Modifier.weight(1f),
                        onClick = { handleOperation("÷") }
                    )
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Calculator3DButton(
                        text = "7",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("7") }
                    )
                    Calculator3DButton(
                        text = "8",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("8") }
                    )
                    Calculator3DButton(
                        text = "9",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("9") }
                    )
                    Calculator3DButton(
                        text = "×",
                        textColor = Color(0xFF00E5FF),
                        baseColor = Color(0xFF131F33),
                        modifier = Modifier.weight(1f),
                        onClick = { handleOperation("×") }
                    )
                }

                // Row 3: 4, 5, 6, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Calculator3DButton(
                        text = "4",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("4") }
                    )
                    Calculator3DButton(
                        text = "5",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("5") }
                    )
                    Calculator3DButton(
                        text = "6",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("6") }
                    )
                    Calculator3DButton(
                        text = "-",
                        textColor = Color(0xFF00E5FF),
                        baseColor = Color(0xFF131F33),
                        modifier = Modifier.weight(1f),
                        onClick = { handleOperation("-") }
                    )
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Calculator3DButton(
                        text = "1",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("1") }
                    )
                    Calculator3DButton(
                        text = "2",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("2") }
                    )
                    Calculator3DButton(
                        text = "3",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDigit("3") }
                    )
                    Calculator3DButton(
                        text = "+",
                        textColor = Color(0xFF00E5FF),
                        baseColor = Color(0xFF131F33),
                        modifier = Modifier.weight(1f),
                        onClick = { handleOperation("+") }
                    )
                }

                // Row 5: 0 (span 2), ., =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Calculator3DButton(
                        text = "0",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(2.08f),
                        onClick = { handleDigit("0") }
                    )
                    Calculator3DButton(
                        text = ".",
                        textColor = Color(0xFFF1F5F9),
                        baseColor = Color(0xFF161D2B),
                        modifier = Modifier.weight(1f),
                        onClick = { handleDecimal() }
                    )
                    Calculator3DButton(
                        text = "=",
                        textColor = Color(0xFF00FF66), // Glowing Cyber Green
                        baseColor = Color(0xFF0F3826),
                        modifier = Modifier.weight(1f),
                        onClick = { calculateResult() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // =========================================================================
        // CYBER-MATRIX DISSOLVE / TRANSITION GRID ANIMATION
        // Triggered upon executing "777+0="
        // =========================================================================
        AnimatedVisibility(
            visible = isMatrixTransitioning,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(300))
        ) {
            CyberMatrixDissolveOverlay()
        }
    }
}

/**
 * 3D Tactile Neumorphic Button with metallic/obsidian bevel and dynamic touch compression.
 */
@Composable
fun Calculator3DButton(
    text: String,
    textColor: Color,
    baseColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topLightColor = if (isPressed) baseColor else baseColor.copy(alpha = 0.9f)
    val bottomBevelColor = if (isPressed) Color(0xFF0A0F17) else Color(0xFF070A0F)

    Box(
        modifier = modifier
            .aspectRatio(if (text == "0") 2.15f else 1f)
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        if (isPressed) Color(0xFF0D121B) else topLightColor,
                        if (isPressed) Color(0xFF182232) else bottomBevelColor
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF334155).copy(alpha = 0.6f),
                        Color(0xFF0F172A).copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("calc_btn_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Cyber-Matrix Dissolve Overlay animation.
 * Features cascading digital telemetry rain, green/cyan rolling hash streams,
 * and military telecom handshake indicators.
 */
@Composable
fun CyberMatrixDissolveOverlay() {
    val transition = rememberInfiniteTransition(label = "matrix_pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03070A).copy(alpha = 0.96f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00FF66).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFF00FF66).copy(alpha = alphaAnim), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = Color(0xFF00FF66),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "KERNEL BYPASS VERIFIED",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF66),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "0x777+0 // DISSOLVING CAMOUFLAGE LAYER...",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF00E5FF).copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Telemetry Stream Hex Lines
            Text(
                text = "[CRYPT] AES-256-GCM READY\n[SIGINT] HARDWARE TOKEN DERIVED\n[MESH] MOUNTING DXX TELECOM HUB...",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
