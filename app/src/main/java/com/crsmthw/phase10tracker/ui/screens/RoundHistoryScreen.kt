package com.crsmthw.phase10tracker.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.data.model.PhaseRule
import com.crsmthw.phase10tracker.data.model.RoundEditEntry
import com.crsmthw.phase10tracker.data.model.RoundEntity
import com.crsmthw.phase10tracker.ui.RoundHistoryViewModel
import com.crsmthw.phase10tracker.ui.isScoreShapeInvalid
import com.crsmthw.phase10tracker.ui.components.BottomFadeScrim
import com.crsmthw.phase10tracker.ui.components.EmptyState
import com.crsmthw.phase10tracker.ui.components.PlayerAvatar
import com.crsmthw.phase10tracker.util.confirm
import com.crsmthw.phase10tracker.util.press

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RoundHistoryScreen(
    vm: RoundHistoryViewModel,
    onBack: () -> Unit
) {
    val editing    by vm.editing.collectAsState()
    val saved      by vm.saved.collectAsState()
    val rounds     by vm.rounds.collectAsState()
    val phaseRules by vm.phaseRules.collectAsState()
    val canSave    by vm.canSave.collectAsState()
    val players    by vm.players.collectAsState()
    val nameById = remember(players) { players.associate { it.id to it.playerName } }
    val hf = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showConfirm by remember { mutableStateOf(false) }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(saved) { if (saved) onBack() }
    BackHandler(enabled = editing != null) { vm.cancelEdit() }

    if (showConfirm) {
        val draft = editing
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            icon = { Icon(Icons.Filled.EditNote, null) },
            title = { Text("Apply changes to Round ${draft?.roundNumber}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Every player's total and phase will be recalculated.")
                    Spacer(Modifier.height(4.dp))
                    draft?.entries?.forEach { e ->
                        Text(
                            "${nameById[e.gamePlayerId] ?: e.playerName}: ${e.scoreInput.ifBlank { "0" }} pts" +
                                if (e.phaseCompleted) " ✓" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { hf.confirm(); vm.saveRound(); showConfirm = false }) { Text("Apply") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(editing?.let { "Edit Round ${it.roundNumber}" } ?: "Round History") },
                subtitle = { Text(if (editing != null) "Fix a mistake, then apply" else "Tap a round to edit") },
                navigationIcon = {
                    IconButton(onClick = {
                        hf.press()
                        if (editing != null) vm.cancelEdit() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior            )
        },
        bottomBar = {
            if (editing != null) {
                Surface(tonalElevation = 3.dp) {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        Button(
                            onClick = { hf.confirm(); focusManager.clearFocus(); showConfirm = true },
                            enabled = canSave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(vertical = 16.dp)
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Filled.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Round ${editing?.roundNumber}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    ) { padding ->
        val draft = editing
        when {
            draft != null -> {
                // padding + consumeWindowInsets + imePadding (same as Round Entry) so the keyboard
                // adds only the EXTRA space beyond the Save bar — no tall white gap.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .imePadding(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(draft.entries, key = { it.roundId }) { entry ->
                        EditEntryCard(
                            entry = entry,
                            playerName = nameById[entry.gamePlayerId] ?: entry.playerName,
                            phaseRules = phaseRules,
                            isLast = entry == draft.entries.last(),
                            onScoreChange = { vm.updateScore(entry.roundId, it) },
                            onTogglePhase = { hf.press(); vm.togglePhaseCompleted(entry.roundId) },
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = { focusManager.clearFocus() }
                        )
                    }
                }
            }
            rounds.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "No rounds yet",
                    subtitle = "Play a round, then come back to edit it",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 12.dp, bottom = navBottom + 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(rounds.keys.sortedDescending(), key = { it }) { roundNo ->
                            RoundSummaryCard(
                                roundNumber = roundNo,
                                rows = rounds[roundNo].orEmpty(),
                                playerName = { id -> nameById[id] ?: "?" },
                                onEdit = { hf.press(); vm.startEdit(roundNo) }
                            )
                        }
                    }
                    BottomFadeScrim(color = MaterialTheme.colorScheme.background, height = navBottom + 48.dp)
                }
            }
        }
    }
}

// ── Read-only round summary card (tap to edit) ─────────────────────────────────

@Composable
private fun RoundSummaryCard(
    roundNumber: Int,
    rows: List<RoundEntity>,
    playerName: (Long) -> String,
    onEdit: () -> Unit
) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Round $roundNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            rows.forEach { r ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        playerName(r.gamePlayerId),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (r.phaseCompleted) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Phase completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        "${r.score} pts",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Editable per-player card (mirrors Round Entry) ─────────────────────────────

@Composable
private fun EditEntryCard(
    entry: RoundEditEntry,
    playerName: String,
    phaseRules: List<PhaseRule>,
    isLast: Boolean,
    onScoreChange: (String) -> Unit,
    onTogglePhase: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit
) {
    val rule = remember(entry.phaseAtRoundStart, phaseRules) {
        phaseRules.getOrElse(entry.phaseAtRoundStart - 1) { phaseRules.last() }
    }
    val scoreInt = entry.scoreInput.trim().toIntOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(name = playerName, size = 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(playerName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Phase ${entry.phaseAtRoundStart}: ${rule.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = entry.scoreInput,
                onValueChange = { input -> if (input.all { it.isDigit() }) onScoreChange(input) },
                label = { Text("Score") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (isLast) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onDone() }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                supportingText = {
                    when {
                        isScoreShapeInvalid(entry.scoreInput) ->
                            Text("Must be a multiple of 5", color = MaterialTheme.colorScheme.error)
                        scoreInt == 0 -> Text("🏆 Went out!", color = MaterialTheme.colorScheme.primary)
                        entry.autoCompleted -> Text("✓ Phase auto-completed", color = MaterialTheme.colorScheme.tertiary)
                        else -> {}
                    }
                },
                isError = (entry.scoreInput.isNotEmpty() && scoreInt == null) ||
                    isScoreShapeInvalid(entry.scoreInput)
            )

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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = entry.phaseCompleted,
                        onCheckedChange = { if (!entry.autoCompleted) onTogglePhase() },
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
