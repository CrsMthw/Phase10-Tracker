package com.crsmthw.phase10tracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.crsmthw.phase10tracker.data.model.PhaseRule
import com.crsmthw.phase10tracker.data.model.PlayerGameState
import com.crsmthw.phase10tracker.ui.ActiveGameViewModel

private enum class ViewMode { SCORES, PHASES }

// ── Helper: resolve the right PhaseRule from the game's list ─────────────────

private fun List<PhaseRule>.forPhase(phase: Int): PhaseRule =
    getOrElse(phase - 1) { last() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    vm: ActiveGameViewModel,
    onEnterRound: () -> Unit,
    onGameEnd: () -> Unit,
    onGameCancelled: () -> Unit,
    onBack: () -> Unit
) {
    val boardState    by vm.boardState.collectAsState()
    val gameState     by vm.gameState.collectAsState()
    val gameFinished  by vm.gameFinished.collectAsState()
    val gameCancelled by vm.gameCancelled.collectAsState()
    val phaseRules    by vm.phaseRules.collectAsState()

    var showEndGameDialog by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.SCORES) }

    LaunchedEffect(gameFinished)  { if (gameFinished)  onGameEnd() }
    LaunchedEffect(gameCancelled) { if (gameCancelled) onGameCancelled() }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isTwoPane = adaptiveInfo.windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

    if (showEndGameDialog) {
        val allZero = boardState.all { it.totalScore == 0 }
        AlertDialog(
            onDismissRequest = { showEndGameDialog = false },
            icon = { Icon(Icons.Filled.Flag, null) },
            title = { Text("End Game Early?") },
            text = {
                Text(
                    if (allZero)
                        "No rounds have been played yet. The game will be cancelled with no winner recorded."
                    else
                        "The current leader (highest phase, lowest score) will be declared the winner. This cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = { vm.endGameEarly(); showEndGameDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(if (boardState.all { it.totalScore == 0 }) "Cancel Game" else "End Game") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEndGameDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Round ${gameState?.currentRound ?: 1}")
                        Text(
                            "${boardState.size} players",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEndGameDialog = true }) {
                        Icon(Icons.Filled.Flag, "End Game")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onEnterRound,
                icon = { Icon(Icons.Filled.Edit, null) },
                text = { Text("Enter Round ${gameState?.currentRound ?: 1}") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        if (isTwoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    PaneSectionHeader("Scores")
                    ScoresView(boardState = boardState, phaseRules = phaseRules, fabClearance = false)
                }
                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.fillMaxHeight()
                )
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    PaneSectionHeader("By Phase")
                    PhasesView(boardState = boardState, phaseRules = phaseRules, fabClearance = false)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                PrimaryTabRow(selectedTabIndex = viewMode.ordinal) {
                    Tab(
                        selected = viewMode == ViewMode.SCORES,
                        onClick = { viewMode = ViewMode.SCORES },
                        text = { Text("Scores") },
                        icon = { Icon(Icons.Filled.Leaderboard, null, Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = viewMode == ViewMode.PHASES,
                        onClick = { viewMode = ViewMode.PHASES },
                        text = { Text("By Phase") },
                        icon = { Icon(Icons.Filled.GridView, null, Modifier.size(18.dp)) }
                    )
                }
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "viewMode"
                ) { mode ->
                    when (mode) {
                        ViewMode.SCORES -> ScoresView(boardState, phaseRules, fabClearance = true)
                        ViewMode.PHASES -> PhasesView(boardState, phaseRules, fabClearance = true)
                    }
                }
            }
        }
    }
}

// ── Pane header (expanded layout only) ───────────────────────────────────────

@Composable
private fun PaneSectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

// ── Scores pane ───────────────────────────────────────────────────────────────

@Composable
private fun ScoresView(
    boardState: List<PlayerGameState>,
    phaseRules: List<PhaseRule>,
    fabClearance: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(boardState, key = { _, p -> p.gamePlayerId }) { index, player ->
            PlayerScoreCard(player = player, rank = index + 1, phaseRules = phaseRules)
        }
        if (fabClearance) item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Phases pane ───────────────────────────────────────────────────────────────

@Composable
private fun PhasesView(
    boardState: List<PlayerGameState>,
    phaseRules: List<PhaseRule>,
    fabClearance: Boolean
) {
    val grouped = boardState
        .groupBy { minOf(it.currentPhase, 10) }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        grouped.forEach { (phase, players) ->
            val rule = phaseRules.forPhase(phase)
            item(key = "header_$phase") {
                PhaseGroupHeader(phase = phase, rule = rule.title)
            }
            items(players, key = { it.gamePlayerId }) { player ->
                PhaseGroupPlayerCard(player = player)
            }
        }
        if (fabClearance) item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Phase group header ────────────────────────────────────────────────────────

@Composable
private fun PhaseGroupHeader(phase: Int, rule: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "P$phase",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = rule,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// ── Phase group player card ───────────────────────────────────────────────────

@Composable
private fun PhaseGroupPlayerCard(player: PlayerGameState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        player.playerName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(player.playerName, style = MaterialTheme.typography.titleMedium)
                    if (player.isDealer) {
                        Spacer(Modifier.width(6.dp))
                        DealerBadge()
                    }
                }
            }
            Text(
                "${player.totalScore} pts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Score card ────────────────────────────────────────────────────────────────

@Composable
private fun PlayerScoreCard(
    player: PlayerGameState,
    rank: Int,
    phaseRules: List<PhaseRule>
) {
    var expanded by remember { mutableStateOf(false) }
    val phase = minOf(player.currentPhase, 10)
    val phaseRule = phaseRules.forPhase(phase)

    val cardColor = when (rank) {
        1    -> MaterialTheme.colorScheme.primaryContainer
        2    -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val badgeColor = when (rank) {
        1    -> MaterialTheme.colorScheme.primary
        2    -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val badgeTextColor = when (rank) {
        1    -> MaterialTheme.colorScheme.onPrimary
        2    -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (rank == 1) 4.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = badgeColor,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$rank",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            player.playerName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.isDealer) {
                            Spacer(Modifier.width(6.dp))
                            DealerBadge()
                        }
                    }
                }

                Text(
                    "${player.totalScore}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(12.dp))

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        "P$phase",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Phase $phase: ${phaseRule.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        phaseRule.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Dealer badge ──────────────────────────────────────────────────────────────

@Composable
private fun DealerBadge() {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.tertiary
    ) {
        Text(
            " DEALER ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
