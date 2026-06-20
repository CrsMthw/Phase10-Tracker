package com.crsmthw.phase10tracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.crsmthw.phase10tracker.data.model.PhaseRule
import com.crsmthw.phase10tracker.data.model.PlayerGameState
import com.crsmthw.phase10tracker.ui.ActiveGameViewModel
import com.crsmthw.phase10tracker.ui.GameOutcome
import com.crsmthw.phase10tracker.ui.components.BottomFadeScrim
import com.crsmthw.phase10tracker.ui.components.ConnectedChoiceRow
import com.crsmthw.phase10tracker.ui.components.ExpressiveBadge
import com.crsmthw.phase10tracker.ui.components.PlayerAvatar
import com.crsmthw.phase10tracker.util.ListScrollHaptics
import com.crsmthw.phase10tracker.util.confirm
import com.crsmthw.phase10tracker.util.press
import com.crsmthw.phase10tracker.util.reject
import com.crsmthw.phase10tracker.util.toggle

private enum class ViewMode { SCORES, PHASES }

private fun List<PhaseRule>.forPhase(phase: Int): PhaseRule =
    getOrElse(phase - 1) { last() }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActiveGameScreen(
    vm: ActiveGameViewModel,
    onEnterRound: () -> Unit,
    onHistory: () -> Unit,
    onGameEnd: () -> Unit,
    onGameCancelled: () -> Unit,
    onBack: () -> Unit
) {
    val boardState by vm.boardState.collectAsState()
    val gameState  by vm.gameState.collectAsState()
    val outcome    by vm.outcome.collectAsState()
    val phaseRules by vm.phaseRules.collectAsState()
    val hf = LocalHapticFeedback.current

    var showEndGameDialog by remember { mutableStateOf(false) }

    // State-driven, idempotent: routes to results / home off the persisted game outcome,
    // so it survives a return from Round Entry or a warm restore (fixes the lost winner screen).
    LaunchedEffect(outcome) {
        when (outcome) {
            GameOutcome.RESULTS   -> onGameEnd()
            GameOutcome.CANCELLED -> onGameCancelled()
            GameOutcome.NONE      -> {}
        }
    }

    val isTwoPane = currentWindowAdaptiveInfo().windowSizeClass
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
                    onClick = { hf.reject(); vm.endGameEarly(); showEndGameDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(if (allZero) "Cancel Game" else "End Game") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEndGameDialog = false }) { Text("Cancel") }
            }
        )
    }

    val round = gameState?.currentRound ?: 1
    val fab: @Composable () -> Unit = {
        ExtendedFloatingActionButton(
            onClick = { hf.confirm(); onEnterRound() },
            icon = { Icon(Icons.Filled.Edit, null) },
            text = { Text("Enter Round $round") },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.navigationBarsPadding()   // sit above the transparent nav bar
        )
    }

    if (isTwoPane) {
        TwoPaneBoard(
            round = round,
            playerCount = boardState.size,
            boardState = boardState,
            phaseRules = phaseRules,
            onBack = onBack,
            onHistory = onHistory,
            onEndGame = { showEndGameDialog = true },
            fab = fab
        )
    } else {
        SinglePaneBoard(
            round = round,
            playerCount = boardState.size,
            boardState = boardState,
            phaseRules = phaseRules,
            onBack = onBack,
            onHistory = onHistory,
            onEndGame = { showEndGameDialog = true },
            fab = fab
        )
    }
}

// ── Single pane (phone) ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SinglePaneBoard(
    round: Int,
    playerCount: Int,
    boardState: List<PlayerGameState>,
    phaseRules: List<PhaseRule>,
    onBack: () -> Unit,
    onHistory: () -> Unit,
    onEndGame: () -> Unit,
    fab: @Composable () -> Unit
) {
    val hf = LocalHapticFeedback.current
    var viewMode by rememberSaveable { mutableStateOf(ViewMode.SCORES) }  // survives Round Entry (Bug 3)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val slideSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Round $round") },
                subtitle = { Text("$playerCount players") },
                navigationIcon = {
                    IconButton(onClick = { hf.press(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { hf.press(); onHistory() }) {
                        Icon(Icons.Filled.History, "Round history")
                    }
                    IconButton(onClick = { hf.press(); onEndGame() }) {
                        Icon(Icons.Filled.Flag, "End Game")
                    }
                },
                scrollBehavior = scrollBehavior            )
        },
        floatingActionButton = fab
    ) { padding ->
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                ConnectedChoiceRow(
                    options = listOf(ViewMode.SCORES to "Scores", ViewMode.PHASES to "By Phase"),
                    selected = viewMode,
                    onSelect = { hf.toggle(true); viewMode = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (slideInHorizontally(slideSpec) { w -> dir * w / 6 } + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(slideSpec) { w -> -dir * w / 6 } + fadeOut(tween(140)))
                    },
                    label = "viewMode"
                ) { mode ->
                    when (mode) {
                        ViewMode.SCORES -> ScoresList(boardState, phaseRules, fabClearance = true)
                        ViewMode.PHASES -> PhasesList(boardState, phaseRules, fabClearance = true)
                    }
                }
            }
            BottomFadeScrim(color = MaterialTheme.colorScheme.background, height = navBottom + 48.dp)
        }
    }
}

// ── Two pane (tablet / unfolded) ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoPaneBoard(
    round: Int,
    playerCount: Int,
    boardState: List<PlayerGameState>,
    phaseRules: List<PhaseRule>,
    onBack: () -> Unit,
    onHistory: () -> Unit,
    onEndGame: () -> Unit,
    fab: @Composable () -> Unit
) {
    val hf = LocalHapticFeedback.current
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Round $round")
                        Text(
                            "$playerCount players",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { hf.press(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { hf.press(); onHistory() }) {
                        Icon(Icons.Filled.History, "Round history")
                    }
                    IconButton(onClick = { hf.press(); onEndGame() }) {
                        Icon(Icons.Filled.Flag, "End Game")
                    }
                }            )
        },
        floatingActionButton = fab
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BoardPaneCard(title = "Scores", modifier = Modifier.weight(1f)) {
                ScoresList(boardState, phaseRules, fabClearance = true)
            }
            BoardPaneCard(title = "By Phase", modifier = Modifier.weight(1f)) {
                PhasesList(boardState, phaseRules, fabClearance = true)
            }
        }
    }
}

@Composable
private fun BoardPaneCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val container = MaterialTheme.colorScheme.surfaceContainer
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
                content()
            }
            BottomFadeScrim(color = container)
        }
    }
}

// ── Scores list ──────────────────────────────────────────────────────────────────

@Composable
private fun ScoresList(
    boardState: List<PlayerGameState>,
    phaseRules: List<PhaseRule>,
    fabClearance: Boolean
) {
    val listState = rememberLazyListState()
    ListScrollHaptics(listState)
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(boardState, key = { _, p -> p.gamePlayerId }) { index, player ->
            PlayerScoreCard(player = player, rank = index + 1, phaseRules = phaseRules)
        }
        if (fabClearance) item { Spacer(Modifier.height(96.dp)) }
    }
}

// ── Phases list ────────────────────────────────────────────────────────────────

@Composable
private fun PhasesList(
    boardState: List<PlayerGameState>,
    phaseRules: List<PhaseRule>,
    fabClearance: Boolean
) {
    val listState = rememberLazyListState()
    ListScrollHaptics(listState)
    val grouped = boardState.groupBy { minOf(it.currentPhase, 10) }.toSortedMap()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        grouped.forEach { (phase, players) ->
            val rule = phaseRules.forPhase(phase)
            item(key = "header_$phase") { PhaseGroupHeader(phase = phase, rule = rule.title) }
            items(players, key = { it.gamePlayerId }) { player -> PhaseGroupPlayerCard(player = player) }
        }
        if (fabClearance) item { Spacer(Modifier.height(96.dp)) }
    }
}

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
            ExpressiveBadge(
                label = "P$phase",
                container = MaterialTheme.colorScheme.secondary,
                content = MaterialTheme.colorScheme.onSecondary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                rule,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun PhaseGroupPlayerCard(player: PlayerGameState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(name = player.playerName, size = 36.dp)
            Spacer(Modifier.width(12.dp))
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(player.playerName, style = MaterialTheme.typography.titleMedium)
                if (player.isDealer) { Spacer(Modifier.width(6.dp)); DealerBadge() }
            }
            Text(
                "${player.totalScore} pts",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Score card (with wavy phase progress) ────────────────────────────────────────

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerScoreCard(
    player: PlayerGameState,
    rank: Int,
    phaseRules: List<PhaseRule>
) {
    val hf = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    val phase = minOf(player.currentPhase, 10)
    val phaseRule = phaseRules.forPhase(phase)
    val phasesDone = (player.currentPhase - 1).coerceIn(0, 10)

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
        onClick = { hf.press(); expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (rank == 1) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ExpressiveBadge(label = "$rank", container = badgeColor, content = badgeTextColor)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            player.playerName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.isDealer) { Spacer(Modifier.width(6.dp)); DealerBadge() }
                    }
                }
                Text("${player.totalScore}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        "P$phase",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Expressive wavy progress: phases completed out of 10.
            LinearWavyProgressIndicator(
                progress = { phasesDone / 10f },
                modifier = Modifier.fillMaxWidth()
            )

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

@Composable
private fun DealerBadge() {
    Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.tertiary) {
        Text(
            " DEALER ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
