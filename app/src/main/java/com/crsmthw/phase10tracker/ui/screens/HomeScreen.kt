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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.R
import com.crsmthw.phase10tracker.data.ThemeMode
import com.crsmthw.phase10tracker.ui.HomeViewModel

// ── Home Screen ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    themeMode: ThemeMode,
    amoledBlack: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAmoledBlackChange: (Boolean) -> Unit,
    onContinueGame: (Long) -> Unit,
    onStartNew: () -> Unit,
    onLeaderboard: () -> Unit,
    onManagePlayers: () -> Unit,
    onCustomRules: () -> Unit,
    onAbout: () -> Unit
) {
    val hasActiveGame by vm.hasActiveGame.collectAsState()
    val activeGameId  by vm.activeGameId.collectAsState()

    var showResumeDialog by remember { mutableStateOf(false) }
    var showThemeSheet   by remember { mutableStateOf(false) }

    // ── Resume dialog ──────────────────────────────────────────────────────────
    if (showResumeDialog && hasActiveGame && activeGameId != null) {
        ResumeGameDialog(
            onContinue = {
                showResumeDialog = false
                onContinueGame(activeGameId!!)
            },
            onStartNew = {
                showResumeDialog = false
                onStartNew()
            },
            onDismiss = { showResumeDialog = false }
        )
    }

    // ── Theme bottom sheet ─────────────────────────────────────────────────────
    if (showThemeSheet) {
        ThemePickerSheet(
            currentMode         = themeMode,
            amoledBlack         = amoledBlack,
            onModeSelected      = onThemeModeChange,
            onAmoledBlackChange = onAmoledBlackChange,
            onDismiss           = { showThemeSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Phase 10",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    // Theme picker button — icon reflects current mode
                    IconButton(onClick = { showThemeSheet = true }) {
                        Icon(
                            imageVector      = themeMode.icon(),
                            contentDescription = "Display theme"
                        )
                    }
                    IconButton(onClick = onAbout) {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor   = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier              = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Image(
                painter           = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier          = Modifier
                    .size(120.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text      = "Phase 10\nScore Tracker",
                style     = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text  = "Ad-free. Open source. Always.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(56.dp))

            Button(
                onClick  = {
                    if (hasActiveGame) showResumeDialog = true
                    else onStartNew()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = if (hasActiveGame) "Game In Progress" else "Start New Game",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick  = onLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Outlined.EmojiEvents, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Leaderboard", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick  = onManagePlayers,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Group, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Manage Saved Players", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick  = onCustomRules,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Tune, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Custom Phases", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ── Theme icon helper ──────────────────────────────────────────────────────────
// The TopAppBar icon reflects the currently-active mode.

private fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.LIGHT  -> Icons.Outlined.LightMode
    ThemeMode.DARK   -> Icons.Outlined.DarkMode
    ThemeMode.SYSTEM -> Icons.Outlined.BrightnessMedium
}

// ── Theme Picker BottomSheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePickerSheet(
    currentMode: ThemeMode,
    amoledBlack: Boolean,
    onModeSelected: (ThemeMode) -> Unit,
    onAmoledBlackChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        tonalElevation   = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(24.dp)
                )
                Text(
                    text  = "Display Theme",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Mode selector (segmented buttons) ─────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeModeOption.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = currentMode == option.mode,
                            onClick  = { onModeSelected(option.mode) },
                            shape    = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ThemeModeOption.entries.size
                            ),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = currentMode == option.mode) {
                                    Icon(
                                        imageVector        = option.icon,
                                        contentDescription = null,
                                        modifier           = Modifier.size(SegmentedButtonDefaults.IconSize)
                                    )
                                }
                            }
                        ) {
                            Text(option.label)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── AMOLED toggle ─────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(12.dp),
                        modifier               = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.Contrast,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text  = "AMOLED Pure Black",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Switch(
                        checked         = amoledBlack,
                        onCheckedChange = onAmoledBlackChange
                    )
                }
                Text(
                    text     = "Replaces dark backgrounds with true black — saves battery on OLED screens",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 34.dp)
                )
            }
        }
    }
}

// ── Segmented button data ──────────────────────────────────────────────────────

private enum class ThemeModeOption(
    val mode: ThemeMode,
    val label: String,
    val icon: ImageVector
) {
    SYSTEM(ThemeMode.SYSTEM, "Auto",  Icons.Outlined.BrightnessMedium),
    LIGHT (ThemeMode.LIGHT,  "Light", Icons.Outlined.LightMode),
    DARK  (ThemeMode.DARK,   "Dark",  Icons.Outlined.DarkMode),
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
                shape    = MaterialTheme.shapes.large,
                color    = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "10",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        title = {
            Text("Game In Progress", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Text(
                "You have an unfinished game. Do you want to continue it or start a new one?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(onClick = onContinue) { Text("Continue") }
        },
        dismissButton = {
            OutlinedButton(onClick = onStartNew) { Text("New Game") }
        }
    )
}
