package com.crsmthw.phase10tracker.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.data.model.GameResult
import com.crsmthw.phase10tracker.ui.GameResultsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameResultsScreen(
    vm: GameResultsViewModel,
    onHome: () -> Unit
) {
    val results by vm.results.collectAsState()

    // Pulse animation for the trophy
    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Over") },
                navigationIcon = {
                    IconButton(onClick = onHome) {
                        Icon(Icons.Filled.Home, "Home")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 16.dp)
                        .navigationBarsPadding()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Filled.Home, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Back to Home", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { padding ->
        val winners = results.filter { it.isWinner }
        val isTie = winners.size > 1

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Winner hero block
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .scale(trophyScale),
                        tint = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (isTie) "It's a Tie!" else "Champion!",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        if (isTie) winners.joinToString(" & ") { it.playerName }
                        else winners.firstOrNull()?.playerName ?: "",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Final score: ${winners.firstOrNull()?.finalScore ?: "-"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text(
                    "Final Standings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            itemsIndexed(results, key = { i, _ -> i }) { index, result ->
                ResultCard(result = result, rank = index + 1)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ResultCard(result: GameResult, rank: Int) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank / trophy icon
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                if (result.isWinner) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$rank", style = MaterialTheme.typography.titleSmall)
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

            Text(
                "${result.finalScore} pts",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
