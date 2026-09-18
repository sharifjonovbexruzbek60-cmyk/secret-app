package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.CalculatorVaultScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DecoyNewsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enterprise Anti-Screenshot & Screen Recording Protection:
        // FLAG_SECURE blocks screenshots, screen recorders, mirroring, and recent task switcher snapshots.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: ChatViewModel = viewModel()) {
    val isCalculatorUnlocked by viewModel.isCalculatorUnlocked.collectAsState()
    val isDecoyMode by viewModel.isDecoyMode.collectAsState()

    Crossfade(
        targetState = when {
            isDecoyMode -> "DECOY"
            !isCalculatorUnlocked -> "CALCULATOR"
            else -> "VAULT_APP"
        },
        label = "root_navigation_crossfade"
    ) { state ->
        when (state) {
            "DECOY" -> {
                DecoyNewsScreen(
                    onExitDecoy = { viewModel.exitDecoyMode() }
                )
            }
            "CALCULATOR" -> {
                CalculatorVaultScreen(
                    onUnlockVault = { viewModel.unlockCalculatorVault() },
                    onOpenDecoy = { viewModel.triggerDecoyMode() }
                )
            }
            else -> {
                TrioMessengerApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TrioMessengerApp(viewModel: ChatViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    var currentScreen by remember { mutableStateOf("CHAT") }

    Crossfade(
        targetState = currentUser,
        label = "auth_screen_crossfade"
    ) { user ->
        if (user == null) {
            LoginScreen(viewModel = viewModel)
        } else {
            Crossfade(
                targetState = currentScreen,
                label = "main_screen_crossfade"
            ) { screen ->
                if (screen == "ADMIN_PANEL" && user.isAdmin) {
                    AdminPanelScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = "CHAT" }
                    )
                } else {
                    ChatScreen(
                        viewModel = viewModel,
                        currentUser = user,
                        onNavigateToAdminPanel = { currentScreen = "ADMIN_PANEL" }
                    )
                }
            }
        }
    }
}


