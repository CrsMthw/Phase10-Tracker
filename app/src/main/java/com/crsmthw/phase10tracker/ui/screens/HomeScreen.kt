package com.crsmthw.phase10tracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.R
import com.crsmthw.phase10tracker.data.ThemeMode
import com.crsmthw.phase10tracker.ui.HomeViewModel
import com.crsmthw.phase10tracker.ui.components.CappedModalBottomSheet
import com.crsmthw.phase10tracker.ui.components.ConnectedChoiceRow
import com.crsmthw.phase10tracker.util.confirm
import com.crsmthw.phase10tracker.util.press
import com.crsmthw.phase10tracker.util.toggle

// ── Home Screen ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    themeMode: ThemeMode,
    amoledBlack: Boolean,
    haptics: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAmoledBlackChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onContinueGame: (Long) -> Unit,
    onStartNew: () -> Unit,
    onLeaderboard: () -> Unit,
    onGameHistory: () -> Unit,
    onManagePlayers: () -> Unit,
    onCustomRules: () -> Unit,
    onAbout: () -> Unit
) {
    val hasActiveGame by vm.hasActiveGame.collectAsState()
    val activeGameId  by vm.activeGameId.collectAsState()
    val hf = LocalHapticFeedback.current

    var showResumeDialog by remember { mutableStateOf(false) }
    var showThemeSheet   by remember { mutableStateOf(false) }

    if (showResumeDialog && hasActiveGame && activeGameId != null) {
        ResumeGameDialog(
            onContinue = {
                hf.confirm(); showResumeDialog = false; onContinueGame(activeGameId!!)
            },
            onStartNew = {
                hf.confirm(); showResumeDialog = false; onStartNew()
            },
            onDismiss = { showResumeDialog = false }
        )
    }

    if (showThemeSheet) {
        AppearanceSheet(
            currentMode         = themeMode,
            amoledBlack         = amoledBlack,
            haptics             = haptics,
            onModeSelected      = onThemeModeChange,
            onAmoledBlackChange = onAmoledBlackChange,
            onHapticsChange     = onHapticsChange,
            onDismiss           = { showThemeSheet = false }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { hf.press(); showThemeSheet = true }) {
                        Icon(themeMode.icon(), contentDescription = "Display theme")
                    }
                    IconButton(onClick = { hf.press(); onAbout() }) {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .clip(MaterialShapes.Clover4Leaf.toShape())
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Phase 10\nScore Tracker",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ad-free. Open source. Always.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    hf.confirm()
                    if (hasActiveGame) showResumeDialog = true else onStartNew()
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(if (hasActiveGame) Icons.Filled.PlayArrow else Icons.Filled.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (hasActiveGame) "Game In Progress" else "Start New Game",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = { hf.press(); onLeaderboard() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Outlined.EmojiEvents, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Leaderboard", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                }
                FilledTonalButton(
                    onClick = { hf.press(); onGameHistory() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Filled.History, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("History", style = MaterialTheme.typography.titleSmall, maxLines = 1)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { hf.press(); onManagePlayers() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Outlined.Group, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Players")
                }
                OutlinedButton(
                    onClick = { hf.press(); onCustomRules() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Filled.Tune, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Phases")
                }
            }
        }
    }
}

// ── Theme icon helper ──────────────────────────────────────────────────────────

private fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.LIGHT  -> Icons.Outlined.LightMode
    ThemeMode.DARK   -> Icons.Outlined.DarkMode
    ThemeMode.SYSTEM -> Icons.Outlined.BrightnessMedium
}

// ── Appearance bottom sheet (theme mode + AMOLED + haptics) ─────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppearanceSheet(
    currentMode: ThemeMode,
    amoledBlack: Boolean,
    haptics: Boolean,
    onModeSelected: (ThemeMode) -> Unit,
    onAmoledBlackChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val hf = LocalHapticFeedback.current
    val themeOptions = listOf(
        ThemeMode.SYSTEM to "Auto",
        ThemeMode.LIGHT to "Light",
        ThemeMode.DARK to "Dark",
    )

    CappedModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.Palette, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Theme",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ConnectedChoiceRow(
                    options = themeOptions,
                    selected = currentMode,
                    onSelect = { hf.press(); onModeSelected(it) }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingSwitchRow(
                icon = Icons.Outlined.Contrast,
                title = "AMOLED Pure Black",
                subtitle = "True-black backgrounds — saves battery on OLED screens",
                checked = amoledBlack,
                onCheckedChange = { hf.toggle(it); onAmoledBlackChange(it) }
            )

            SettingSwitchRow(
                icon = Icons.Outlined.Vibration,
                title = "Haptic Feedback",
                subtitle = "Subtle vibrations on taps, toggles and scrolling",
                checked = haptics,
                onCheckedChange = { hf.toggle(it); onHapticsChange(it) }
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 34.dp)
        )
    }
}

// ── Resume Game Dialog ─────────────────────────────────────────────────────────

@Composable
private fun ResumeGameDialog(
    onContinue: () -> Unit,
    onStartNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "10",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        title = { Text("Game In Progress", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Text(
                "You have an unfinished game. Do you want to continue it or start a new one?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = { Button(onClick = onContinue) { Text("Continue") } },
        dismissButton = { OutlinedButton(onClick = onStartNew) { Text("New Game") } }
    )
}
