package com.crsmthw.phase10tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.data.model.GameResult
import com.crsmthw.phase10tracker.data.model.RoundEntity
import com.crsmthw.phase10tracker.ui.GameDetailViewModel
import com.crsmthw.phase10tracker.ui.components.BottomFadeScrim
import com.crsmthw.phase10tracker.ui.components.ExpressiveShapes
import com.crsmthw.phase10tracker.util.BiometricAuth
import com.crsmthw.phase10tracker.util.findFragmentActivity
import com.crsmthw.phase10tracker.util.press
import com.crsmthw.phase10tracker.util.reject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GameDetailScreen(
    vm: GameDetailViewModel,
    onBack: () -> Unit
) {
    val results      by vm.results.collectAsState()
    val rounds       by vm.rounds.collectAsState()
    val players      by vm.players.collectAsState()
    val phaseSetName by vm.phaseSetName.collectAsState()
    val loading      by vm.loading.collectAsState()
    val deleted      by vm.deleted.collectAsState()
    val playedAt     by vm.playedAt.collectAsState()
    val hf = LocalHapticFeedback.current
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var showDelete by remember { mutableStateOf(false) }

    val nameById = remember(players) { players.associate { it.id to it.playerName } }

    LaunchedEffect(deleted) { if (deleted) onBack() }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            icon = { Icon(Icons.Filled.DeleteOutline, null) },
            title = { Text("Delete this game?") },
            text = {
                Text(
                    "It's removed from history and the leaderboard is adjusted (the winner loses " +
                        "this win). This can't be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        hf.reject()
                        showDelete = false
                        BiometricAuth.authenticate(
                            activity = context.findFragmentActivity(),
                            title = "Delete game",
                            subtitle = "Authenticate to delete this game",
                            onSuccess = { vm.deleteGame() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Game Details") },
                subtitle = { Text(phaseSetName.ifBlank { "Finished game" }) },
                navigationIcon = {
                    IconButton(onClick = { hf.press(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { hf.press(); showDelete = true }) {
                        Icon(Icons.Filled.DeleteOutline, "Delete game")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                ContainedLoadingIndicator()
            }
            return@Scaffold
        }

        val winners = results.filter { it.isWinner }
        val isTie = winners.size > 1

        Box(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = navBottom + 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { WinnerBanner(isTie = isTie, winners = winners, playedAt = playedAt) }

                item {
                    SectionHeader("Final Standings")
                }
                itemsIndexed(results, key = { i, _ -> "standing_$i" }) { index, result ->
                    StandingCard(result = result, rank = index + 1)
                }

                if (rounds.isNotEmpty()) {
                    item { SectionHeader("Round by Round") }
                    items(rounds.keys.sorted(), key = { "round_$it" }) { roundNo ->
                        RoundBreakdownCard(
                            roundNumber = roundNo,
                            rows = rounds[roundNo].orEmpty(),
                            nameOf = { id -> nameById[id] ?: "?" }
                        )
                    }
                }
            }
            BottomFadeScrim(color = MaterialTheme.colorScheme.background, height = navBottom + 48.dp)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WinnerBanner(isTie: Boolean, winners: List<GameResult>, playedAt: Long?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Surface(
            shape = ExpressiveShapes.winner,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            if (isTie) "It's a Tie!" else "Champion",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            if (isTie) winners.joinToString(" & ") { it.playerName }
            else winners.firstOrNull()?.playerName ?: "",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Winning score: ${winners.firstOrNull()?.finalScore ?: "-"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        playedAt?.let { ts ->
            Spacer(Modifier.height(2.dp))
            Text(
                formatDateTime(ts),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

private fun formatDateTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(dateTimeFormatter)

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun StandingCard(result: GameResult, rank: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (result.isWinner)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                if (result.isWinner) {
                    Icon(
                        Icons.Filled.EmojiEvents, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$rank", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(result.playerName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Finished on Phase ${result.finalPhase}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("${result.finalScore} pts", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun RoundBreakdownCard(
    roundNumber: Int,
    rows: List<RoundEntity>,
    nameOf: (Long) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                "Round $roundNumber",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            rows.forEach { r ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(nameOf(r.gamePlayerId), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    if (r.phaseCompleted) {
                        Icon(
                            Icons.Filled.CheckCircle, "Phase completed",
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
