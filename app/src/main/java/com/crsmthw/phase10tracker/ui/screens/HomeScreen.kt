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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.R
import com.crsmthw.phase10tracker.ui.HomeViewModel
import com.crsmthw.phase10tracker.ui.theme.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    currentTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit,
    onContinueGame: (Long) -> Unit,
    onStartNew: () -> Unit,
    onLeaderboard: () -> Unit,
    onManagePlayers: () -> Unit,
    onCustomRules: () -> Unit,
    onAbout: () -> Unit
) {
    val hasActiveGame by vm.hasActiveGame.collectAsState()
    val activeGameId  by vm.activeGameId.collectAsState()

    // Dialog only shown when user explicitly taps "Game In Progress" button
    var showResumeDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Phase 10",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    ThemeToggleButton(
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange
                    )
                    IconButton(onClick = onAbout) {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon artwork used as hero image
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.primary)
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

            Spacer(Modifier.height(56.dp))

            Button(
                onClick = {
                    if (hasActiveGame) showResumeDialog = true
                    else onStartNew()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (hasActiveGame) "Game In Progress" else "Start New Game",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Outlined.EmojiEvents, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Leaderboard", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onManagePlayers,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Group, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Manage Saved Players", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick = onCustomRules,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Tune, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Custom Rules", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

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

// ── Theme toggle ─────────────────────────────────────────────────────────────
// IconButton in the TopAppBar that reflects the current theme and opens a
// dropdown letting the user pick Light / Dark / System.

@Composable
private fun ThemeToggleButton(
    currentTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = when (currentTheme) {
                    ThemePreference.LIGHT  -> Icons.Filled.LightMode
                    ThemePreference.DARK   -> Icons.Filled.DarkMode
                    ThemePreference.AMOLED -> Icons.Filled.Contrast
                    ThemePreference.SYSTEM -> Icons.Filled.BrightnessAuto
                },
                contentDescription = "Theme"
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            ThemeMenuItem(
                label = "Light",
                icon = Icons.Filled.LightMode,
                selected = currentTheme == ThemePreference.LIGHT,
                onClick = {
                    onThemeChange(ThemePreference.LIGHT)
                    menuExpanded = false
                }
            )
            ThemeMenuItem(
                label = "Dark",
                icon = Icons.Filled.DarkMode,
                selected = currentTheme == ThemePreference.DARK,
                onClick = {
                    onThemeChange(ThemePreference.DARK)
                    menuExpanded = false
                }
            )
            ThemeMenuItem(
                label = "AMOLED Black",
                icon = Icons.Filled.Contrast,
                selected = currentTheme == ThemePreference.AMOLED,
                onClick = {
                    onThemeChange(ThemePreference.AMOLED)
                    menuExpanded = false
                }
            )
            ThemeMenuItem(
                label = "System default",
                icon = Icons.Filled.BrightnessAuto,
                selected = currentTheme == ThemePreference.SYSTEM,
                onClick = {
                    onThemeChange(ThemePreference.SYSTEM)
                    menuExpanded = false
                }
            )
        }
    }
}

@Composable
private fun ThemeMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        } else null
    )
}