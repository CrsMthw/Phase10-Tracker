package com.crsmthw.phase10tracker.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.data.model.PlayerEntity
import com.crsmthw.phase10tracker.ui.GameSetupViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    vm: GameSetupViewModel,
    onGameStarted: (Long) -> Unit,
    onBack: () -> Unit
) {
    val allPlayers      by vm.allPlayers.collectAsState()
    val selectedPlayers by vm.selectedPlayers.collectAsState()
    val newGameId       by vm.newGameId.collectAsState()
    val customRuleSets  by vm.customRuleSets.collectAsState()
    val selectedRuleSet by vm.selectedRuleSet.collectAsState()
    var showAddDialog    by remember { mutableStateOf(false) }
    var showRuleDropdown by remember { mutableStateOf(false) }
    val hapticFeedback  = LocalHapticFeedback.current

    LaunchedEffect(newGameId) {
        newGameId?.let { onGameStarted(it) }
    }

    if (showAddDialog) {
        AddPlayerDialog(
            onAdd = { name ->
                vm.addAndSelectNewPlayer(name)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    val lazyListState = rememberLazyListState()

    // The LazyColumn has several non-reorderable header items before the player list.
    // Count them so we can subtract the offset when calling movePlayer.
    // Headers: rules_header, players_header, player_chips, order_header = 4 items
    // But order_header only appears when selectedPlayers is not empty.
    // So offset = 3 (rules, select header, chips) + 1 (order header) = 4
    val headerCount = if (selectedPlayers.isNotEmpty()) 4 else 3

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Subtract header offset to get actual player list indices
        val fromPlayerIndex = from.index - headerCount
        val toPlayerIndex   = to.index   - headerCount
        if (fromPlayerIndex >= 0 && toPlayerIndex >= 0 &&
            fromPlayerIndex < selectedPlayers.size && toPlayerIndex < selectedPlayers.size) {
            vm.movePlayer(fromPlayerIndex, toPlayerIndex)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Game") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Button(
                    onClick = { vm.startGame() },
                    enabled = selectedPlayers.size >= 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 16.dp)
                        .navigationBarsPadding()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Filled.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Start Game (${selectedPlayers.size} players)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Rules selector ───────────────────────────────────────────────
            item(key = "rules_header") {
                Text(
                    "Rules",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = showRuleDropdown,
                    onExpandedChange = { showRuleDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedRuleSet?.name ?: "Official Rules",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Phase rules") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRuleDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = showRuleDropdown,
                        onDismissRequest = { showRuleDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Official Rules") },
                            onClick = { vm.selectRuleSet(null); showRuleDropdown = false },
                            leadingIcon = { if (selectedRuleSet == null) Icon(Icons.Filled.Check, null) }
                        )
                        customRuleSets.forEach { ruleSet ->
                            DropdownMenuItem(
                                text = { Text(ruleSet.name) },
                                onClick = { vm.selectRuleSet(ruleSet); showRuleDropdown = false },
                                leadingIcon = {
                                    if (selectedRuleSet?.id == ruleSet.id) Icon(Icons.Filled.Check, null)
                                }
                            )
                        }
                    }
                }
            }

            // ── Select Players ───────────────────────────────────────────────
            item(key = "players_header") {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Select Players",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Tap to add players to this game.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item(key = "player_chips") {
                if (allPlayers.isEmpty()) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No saved players yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { showAddDialog = true }) {
                                Icon(Icons.Outlined.PersonAdd, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Add your first player")
                            }
                        }
                    }
                } else {
                    PlayerSelectionGrid(
                        allPlayers = allPlayers,
                        selectedPlayers = selectedPlayers,
                        onToggle = { vm.togglePlayer(it) },
                        onAddNew = { showAddDialog = true }
                    )
                }
            }

            // ── Reorder section ──────────────────────────────────────────────
            if (selectedPlayers.isNotEmpty()) {
                item(key = "order_header") {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Player Order & Dealer Rotation",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        "Hold the ≡ handle and drag to reorder. First player is the initial dealer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }

                itemsIndexed(
                    items = selectedPlayers,
                    key = { _, p -> "player_${p.id}" }
                ) { index, player ->
                    ReorderableItem(
                        state = reorderableState,
                        key = "player_${player.id}"
                    ) { isDragging ->
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "drag_elevation"
                        )
                        PlayerOrderCard(
                            player = player,
                            position = index + 1,
                            elevation = elevation,
                            onRemove = { vm.togglePlayer(player) }
                        )
                    }
                }
            }

            item(key = "bottom_space") { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun sh.calvin.reorderable.ReorderableCollectionItemScope.PlayerOrderCard(
    player: PlayerEntity,
    position: Int,
    elevation: androidx.compose.ui.unit.Dp,
    onRemove: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (position == 1)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle — draggableHandle MUST be on IconButton, not Icon
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .draggableHandle(
                        onDragStarted = {
                            hapticFeedback.performHapticFeedback(
                                HapticFeedbackType.GestureThresholdActivate
                            )
                        },
                        onDragStopped = {
                            hapticFeedback.performHapticFeedback(
                                HapticFeedbackType.GestureEnd
                            )
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(4.dp))

            // Position badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (position == 1)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "$position",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (position == 1)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, style = MaterialTheme.typography.titleMedium)
                if (position == 1) {
                    Text(
                        "First dealer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerSelectionGrid(
    allPlayers: List<PlayerEntity>,
    selectedPlayers: List<PlayerEntity>,
    onToggle: (PlayerEntity) -> Unit,
    onAddNew: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allPlayers.forEach { player ->
            val selected = selectedPlayers.any { it.id == player.id }
            FilterChip(
                selected = selected,
                onClick = { onToggle(player) },
                label = { Text(player.name) },
                leadingIcon = if (selected) {
                    { Icon(Icons.Filled.Check, null, Modifier.size(18.dp)) }
                } else null,
                shape = MaterialTheme.shapes.medium
            )
        }
        AssistChip(
            onClick = onAddNew,
            label = { Text("New player") },
            leadingIcon = { Icon(Icons.Outlined.PersonAdd, null, Modifier.size(18.dp)) },
            shape = MaterialTheme.shapes.medium
        )
    }
}
