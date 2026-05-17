package com.crsmthw.phase10tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.data.model.PhaseRule
import com.crsmthw.phase10tracker.data.model.RoundEntry
import com.crsmthw.phase10tracker.ui.RoundEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundEntryScreen(
    vm: RoundEntryViewModel,
    onRoundSubmitted: () -> Unit,
    onBack: () -> Unit
) {
    val entries    by vm.entries.collectAsState()
    val gameState  by vm.gameState.collectAsState()
    val submitted  by vm.submitted.collectAsState()
    val phaseRules by vm.phaseRules.collectAsState()
    val focusManager = LocalFocusManager.current
    var showCardValues by remember { mutableStateOf(false) }

    LaunchedEffect(submitted) {
        if (submitted) onRoundSubmitted()
    }

    if (showCardValues) {
        CardValuesDialog(onDismiss = { showCardValues = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Round ${gameState?.currentRound ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCardValues = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Card point values")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            vm.submitRound()
                        },
                        enabled = vm.isValid(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 16.dp)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Filled.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Submit Round", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // Tell Compose the Scaffold padding already consumed those insets,
                // so imePadding() only adds the *extra* keyboard space — not the
                // full keyboard height on top of the already-padded bottomBar height.
                // Without this, the gap = bottomBar height (the double-count).
                .consumeWindowInsets(padding)
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Enter the value of cards left in each player's hand.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }

            items(entries, key = { it.gamePlayerId }) { entry ->
                RoundEntryCard(
                    entry = entry,
                    phaseRules = phaseRules,
                    isLast = entry == entries.last(),
                    onScoreChange = { vm.updateScore(entry.gamePlayerId, it) },
                    onTogglePhase = { vm.togglePhaseCompleted(entry.gamePlayerId) },
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { focusManager.clearFocus() }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun RoundEntryCard(
    entry: RoundEntry,
    phaseRules: List<PhaseRule>,
    isLast: Boolean,
    onScoreChange: (String) -> Unit,
    onTogglePhase: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit
) {
    val phaseRule = remember(entry.currentPhase, phaseRules) {
        phaseRules.getOrElse(entry.currentPhase - 1) { phaseRules.last() }
    }
    val scoreInt = entry.scoreInput.trim().toIntOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            entry.playerName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.playerName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Phase ${entry.currentPhase}: ${phaseRule.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Score field — full width now
            OutlinedTextField(
                value = entry.scoreInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) onScoreChange(input)
                },
                label = { Text("Score") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (isLast) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onNext() },
                    onDone = { onDone() }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                supportingText = {
                    when {
                        scoreInt == 0 ->
                            Text("🏆 Went out!", color = MaterialTheme.colorScheme.primary)
                        entry.autoCompleted ->
                            Text("✓ Phase auto-completed", color = MaterialTheme.colorScheme.tertiary)
                        else -> {}
                    }
                },
                isError = entry.scoreInput.isNotEmpty() && scoreInt == null
            )

            // Phase Done — full-width tappable row below the score field
            Surface(
                onClick = { if (!entry.autoCompleted) onTogglePhase() },
                shape = MaterialTheme.shapes.large,
                color = if (entry.phaseCompleted)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = entry.phaseCompleted,
                        onCheckedChange = {
                            if (!entry.autoCompleted) onTogglePhase()
                        },
                        enabled = !entry.autoCompleted
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = when {
                            scoreInt == 0 -> "Phase completed ✓"
                            entry.autoCompleted -> "Phase completed (auto)"
                            entry.phaseCompleted -> "Phase completed"
                            else -> "Phase not completed"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (entry.phaseCompleted)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CardValuesDialog(onDismiss: () -> Unit) {
    val cardValues = listOf(
        Triple("1 – 9",     "Single digit cards", "5 pts each"),
        Triple("10, 11, 12","Double digit cards",  "10 pts each"),
        Triple("Skip",      "Skip card",           "15 pts each"),
        Triple("Wild",      "Wild card",            "25 pts each"),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
        title = { Text("Card Point Values") },
        text = {
            Column {
                Text(
                    "Points are based on the cards left in your hand at the end of each round.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                cardValues.forEachIndexed { index, (cards, label, points) ->
                    if (index > 0) HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.width(72.dp)
                        ) {
                            Text(
                                text = cards,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = points,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it") }
        }
    )
}