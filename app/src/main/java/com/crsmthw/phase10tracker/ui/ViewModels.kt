package com.crsmthw.phase10tracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crsmthw.phase10tracker.data.ThemeMode
import com.crsmthw.phase10tracker.data.ThemePreferenceManager
import com.crsmthw.phase10tracker.data.model.*
import com.crsmthw.phase10tracker.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Home ViewModel ────────────────────────────────────────────────────────────

class HomeViewModel(private val repo: GameRepository) : ViewModel() {

    private val _activeGame: StateFlow<GameEntity?> = repo.observeActiveGame()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val hasActiveGame: StateFlow<Boolean> = _activeGame
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activeGameId: StateFlow<Long?> = _activeGame
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** A finished game whose winner screen hasn't been shown yet — used to route straight to
     *  the results screen on launch so a completed game can never "lose" its winner reveal. */
    val pendingResultsGameId: StateFlow<Long?> = repo.observePendingResults()
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

// ── Player Roster ViewModel ───────────────────────────────────────────────────

class PlayerRosterViewModel(private val repo: GameRepository) : ViewModel() {

    val players: StateFlow<List<PlayerEntity>> = repo.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPlayer(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.addPlayer(name) }
    }

    fun deletePlayer(player: PlayerEntity) {
        viewModelScope.launch { repo.deletePlayer(player) }
    }
}

// ── Game Setup ViewModel ──────────────────────────────────────────────────────

class GameSetupViewModel(private val repo: GameRepository) : ViewModel() {

    val allPlayers: StateFlow<List<PlayerEntity>> = repo.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customPhaseSets: StateFlow<List<CustomPhaseSet>> = repo.getAllCustomPhaseSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Built-in preset sets — negative IDs so they never clash with DB rows. */
    val presetPhaseSets: List<CustomPhaseSet> = PRESET_PHASE_SETS.mapIndexed { i, p ->
        CustomPhaseSet(id = -(i + 2).toLong(), name = p.name, phases = p.phases)
    }

    private val _selectedPlayers = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val selectedPlayers: StateFlow<List<PlayerEntity>> = _selectedPlayers

    private val _selectedPhaseSet = MutableStateFlow<CustomPhaseSet?>(null)
    val selectedPhaseSet: StateFlow<CustomPhaseSet?> = _selectedPhaseSet

    fun selectPhaseSet(phaseSet: CustomPhaseSet?) {
        _selectedPhaseSet.value = phaseSet
    }

    /** Picks a random phase set from official + all 14 presets + any user-created sets. */
    fun selectRandomPhaseSet() {
        val all: List<CustomPhaseSet?> = listOf(null) + presetPhaseSets + customPhaseSets.value
        _selectedPhaseSet.value = all.random()
    }

    fun togglePlayer(player: PlayerEntity) {
        val current = _selectedPlayers.value.toMutableList()
        if (current.any { it.id == player.id }) {
            current.removeAll { it.id == player.id }
        } else {
            current.add(player)
        }
        _selectedPlayers.value = current
    }

    fun movePlayer(fromIndex: Int, toIndex: Int) {
        _selectedPlayers.value = _selectedPlayers.value.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
    }

    fun addAndSelectNewPlayer(name: String) {
        viewModelScope.launch {
            val id = repo.addPlayer(name)
            val updatedPlayers = repo.getAllPlayers().first()
            val newPlayer = updatedPlayers.find { it.id == id } ?: return@launch
            _selectedPlayers.value = _selectedPlayers.value + newPlayer
        }
    }

    private val _newGameId = MutableStateFlow<Long?>(null)
    val newGameId: StateFlow<Long?> = _newGameId

    fun startGame() {
        val players = _selectedPlayers.value
        if (players.size < 2) return
        val phaseSetId = _selectedPhaseSet.value?.id ?: -1L
        viewModelScope.launch {
            val id = repo.startNewGame(players, phaseSetId)
            _newGameId.value = id
        }
    }
}

// ── Active Game ViewModel ─────────────────────────────────────────────────────

/** What the board should do next, derived purely from the persisted game row. */
enum class GameOutcome { NONE, RESULTS, CANCELLED }

class ActiveGameViewModel(
    private val repo: GameRepository,
    private val gameId: Long
) : ViewModel() {

    val boardState: StateFlow<List<PlayerGameState>> =
        repo.getActiveBoardState(gameId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameState: StateFlow<GameEntity?> =
        repo.getGameById(gameId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Resolves the correct phase rules for this game from its stored phaseSetId. */
    val phaseRules: StateFlow<List<PhaseRule>> = gameState
        .map { game -> repo.resolvePhaseRules(game?.phaseSetId ?: -1L) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OFFICIAL_PHASE_RULES)

    /**
     * The next action for the board, computed from the persisted game state rather than a one-shot
     * event. Because it's derived state it re-emits on every (re)subscription — including a warm
     * restore or a return from Round Entry — so the results screen can never be missed by a
     * navigation that happened to fire before the destination was RESUMED.
     */
    val outcome: StateFlow<GameOutcome> = gameState
        .map { g ->
            when {
                g == null || !g.isComplete -> GameOutcome.NONE
                g.winnerId == -1L          -> GameOutcome.CANCELLED   // ended with no winner
                else                       -> GameOutcome.RESULTS
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameOutcome.NONE)

    fun endGameEarly() {
        viewModelScope.launch {
            val anyScoreAboveZero = boardState.value.any { it.totalScore > 0 }
            if (!anyScoreAboveZero) {
                repo.cancelGame(gameId)   // winnerId = -1 → CANCELLED → Home
            } else {
                repo.endGame(gameId)      // current leader wins → RESULTS
            }
        }
    }
}

// ── Round Entry ViewModel ─────────────────────────────────────────────────────

class RoundEntryViewModel(
    private val repo: GameRepository,
    private val gameId: Long
) : ViewModel() {

    private val _entries = MutableStateFlow<List<RoundEntry>>(emptyList())
    val entries: StateFlow<List<RoundEntry>> = _entries

    val gameState: StateFlow<GameEntity?> =
        repo.getGameById(gameId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Resolves the correct phase rules for this game from its stored phaseSetId. */
    val phaseRules: StateFlow<List<PhaseRule>> = gameState
        .map { game -> repo.resolvePhaseRules(game?.phaseSetId ?: -1L) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OFFICIAL_PHASE_RULES)

    init {
        viewModelScope.launch {
            repo.getGamePlayers(gameId).collect { gamePlayers ->
                if (_entries.value.isEmpty()) {
                    _entries.value = gamePlayers.map { gp ->
                        RoundEntry(
                            gamePlayerId = gp.id,
                            playerName = gp.playerName,
                            currentPhase = gp.currentPhase
                        )
                    }
                }
            }
        }
    }

    fun updateScore(gamePlayerId: Long, score: String) {
        _entries.value = _entries.value.map { entry ->
            if (entry.gamePlayerId != gamePlayerId) return@map entry
            val scoreInt = score.trim().toIntOrNull()
            // Any score below the threshold (0..49, including a single-digit 5) means the player
            // completed their phase. >= threshold means they didn't. The old code gated on string
            // length >= 2, which wrongly excluded a one-digit score of 5.
            val autoComplete = scoreInt != null && scoreInt < PHASE_COMPLETE_THRESHOLD
            entry.copy(
                scoreInput = score,
                phaseCompleted = if (autoComplete) true else if (scoreInt != null && scoreInt >= PHASE_COMPLETE_THRESHOLD) false else entry.phaseCompleted,
                autoCompleted = autoComplete
            )
        }
    }

    fun togglePhaseCompleted(gamePlayerId: Long) {
        _entries.value = _entries.value.map { entry ->
            if (entry.gamePlayerId != gamePlayerId) return@map entry
            if (entry.autoCompleted) return@map entry
            entry.copy(phaseCompleted = !entry.phaseCompleted)
        }
    }

    private val _submitted = MutableStateFlow(false)
    val submitted: StateFlow<Boolean> = _submitted

    /**
     * Whether the round can be submitted. Derived (observed) state so the Submit button's enabled
     * value recomposes as scores are typed — the old `isValid()` was read as a plain StateFlow.value
     * inside the Scaffold bottomBar slot and didn't reliably update.
     */
    val canSubmit: StateFlow<Boolean> = _entries
        .map { entries -> isRoundValid(entries.map { it.scoreInput }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun submitRound() {
        viewModelScope.launch {
            repo.submitRound(gameId, _entries.value)
            _submitted.value = true
        }
    }
}

/**
 * A round is submittable when every player's score is filled, every score is a non-negative
 * multiple of 5, and exactly one player went out (scored 0). Shared by round entry and the round
 * editor.
 */
internal fun isRoundValid(scoreInputs: List<String>): Boolean {
    if (scoreInputs.isEmpty()) return false
    val ints = scoreInputs.map { it.trim().toIntOrNull() }
    if (ints.any { it == null }) return false
    val vals = ints.filterNotNull()
    if (vals.any { it < 0 || it % 5 != 0 }) return false
    return vals.count { it == 0 } == 1
}

/** True for a score that's entered but not a valid round value (negative or not a multiple of 5). */
internal fun isScoreShapeInvalid(scoreInput: String): Boolean {
    val n = scoreInput.trim().toIntOrNull() ?: return false
    return n < 0 || n % 5 != 0
}

// ── Round History (edit past rounds, active game) ViewModel ─────────────────────

class RoundHistoryViewModel(
    private val repo: GameRepository,
    private val gameId: Long
) : ViewModel() {

    val gameState: StateFlow<GameEntity?> =
        repo.getGameById(gameId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val phaseRules: StateFlow<List<PhaseRule>> = gameState
        .map { repo.resolvePhaseRules(it?.phaseSetId ?: -1L) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OFFICIAL_PHASE_RULES)

    // Public so the screen collects it — otherwise the WhileSubscribed upstream never starts and
    // name lookups always hit the empty initial value (the "?" bug).
    val players: StateFlow<List<GamePlayerEntity>> =
        repo.getGamePlayers(gameId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Completed rounds grouped by round number. */
    val rounds: StateFlow<Map<Int, List<RoundEntity>>> =
        repo.getRoundsForGame(gameId)
            .map { list -> list.groupBy { it.roundNumber } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _editing = MutableStateFlow<RoundDraft?>(null)
    val editing: StateFlow<RoundDraft?> = _editing

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    val canSave: StateFlow<Boolean> = _editing
        .map { draft -> draft != null && isRoundValid(draft.entries.map { it.scoreInput }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun playerName(gamePlayerId: Long): String =
        players.value.firstOrNull { it.id == gamePlayerId }?.playerName ?: "?"

    fun startEdit(roundNumber: Int) {
        val rows = rounds.value[roundNumber] ?: return
        val byId = players.value.associateBy { it.id }
        _editing.value = RoundDraft(
            roundNumber = roundNumber,
            entries = rows.sortedBy { byId[it.gamePlayerId]?.turnOrder ?: 0 }.map { r ->
                RoundEditEntry(
                    roundId = r.id,
                    gamePlayerId = r.gamePlayerId,
                    playerName = byId[r.gamePlayerId]?.playerName ?: "?",
                    phaseAtRoundStart = r.phaseAtRoundStart,
                    scoreInput = r.score.toString(),
                    phaseCompleted = r.phaseCompleted,
                    autoCompleted = r.score < PHASE_COMPLETE_THRESHOLD
                )
            }
        )
    }

    fun cancelEdit() { _editing.value = null }

    fun updateScore(roundId: Long, score: String) {
        _editing.value = _editing.value?.let { draft ->
            draft.copy(entries = draft.entries.map { e ->
                if (e.roundId != roundId) return@map e
                val scoreInt = score.trim().toIntOrNull()
                val autoComplete = scoreInt != null && scoreInt < PHASE_COMPLETE_THRESHOLD
                e.copy(
                    scoreInput = score,
                    phaseCompleted = when {
                        autoComplete -> true
                        scoreInt != null && scoreInt >= PHASE_COMPLETE_THRESHOLD -> false
                        else -> e.phaseCompleted
                    },
                    autoCompleted = autoComplete
                )
            })
        }
    }

    fun togglePhaseCompleted(roundId: Long) {
        _editing.value = _editing.value?.let { draft ->
            draft.copy(entries = draft.entries.map { e ->
                if (e.roundId != roundId || e.autoCompleted) e
                else e.copy(phaseCompleted = !e.phaseCompleted)
            })
        }
    }

    fun saveRound() {
        val draft = _editing.value ?: return
        viewModelScope.launch {
            val original = rounds.value[draft.roundNumber]?.associateBy { it.id } ?: return@launch
            val updated = draft.entries.mapNotNull { e ->
                val orig = original[e.roundId] ?: return@mapNotNull null
                orig.copy(
                    score = e.scoreInput.trim().toIntOrNull() ?: 0,
                    phaseCompleted = e.phaseCompleted
                )
            }
            repo.editRound(gameId, updated)
            _editing.value = null
            _saved.value = true
        }
    }
}

// ── Game History (browse finished games) ViewModel ──────────────────────────────

class GameHistoryViewModel(private val repo: GameRepository) : ViewModel() {
    val games: StateFlow<List<GameSummary>> = repo.getFinishedGameSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// ── Game Detail (read-only finished game) ViewModel ─────────────────────────────

class GameDetailViewModel(
    private val repo: GameRepository,
    private val gameId: Long
) : ViewModel() {

    private val _results = MutableStateFlow<List<GameResult>>(emptyList())
    val results: StateFlow<List<GameResult>> = _results

    private val _rounds = MutableStateFlow<Map<Int, List<RoundEntity>>>(emptyMap())
    val rounds: StateFlow<Map<Int, List<RoundEntity>>> = _rounds

    private val _players = MutableStateFlow<List<GamePlayerEntity>>(emptyList())
    val players: StateFlow<List<GamePlayerEntity>> = _players

    private val _phaseRules = MutableStateFlow(OFFICIAL_PHASE_RULES)
    val phaseRules: StateFlow<List<PhaseRule>> = _phaseRules

    private val _phaseSetName = MutableStateFlow("")
    val phaseSetName: StateFlow<String> = _phaseSetName

    // When the game finished (falls back to start time for older rows that never stored finishedAt).
    private val _playedAt = MutableStateFlow<Long?>(null)
    val playedAt: StateFlow<Long?> = _playedAt

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted

    init {
        viewModelScope.launch {
            val game = repo.getGameById(gameId).first()
            _players.value = repo.getGamePlayers(gameId).first()
            _results.value = repo.getGameResults(gameId)
            _rounds.value = repo.getRoundsForGameList(gameId).groupBy { it.roundNumber }
            _phaseRules.value = repo.resolvePhaseRules(game?.phaseSetId ?: -1L)
            _phaseSetName.value = repo.resolvePhaseSetName(game?.phaseSetId ?: -1L)
            _playedAt.value = game?.finishedAt ?: game?.startedAt
            _loading.value = false
        }
    }

    /** Deletes this game and reverses its leaderboard stats (call after biometric auth). */
    fun deleteGame() {
        viewModelScope.launch {
            repo.deleteGame(gameId)
            _deleted.value = true
        }
    }
}

// ── About ViewModel ─────────────────────────────────────────────────────────────

class AboutViewModel(private val repo: GameRepository) : ViewModel() {
    /** Wipes all finished games + resets every player's stats (call after biometric auth). */
    fun deleteAllHistoryAndStats() {
        viewModelScope.launch { repo.deleteAllHistoryAndStats() }
    }
}

// ── Game Results ViewModel ────────────────────────────────────────────────────

class GameResultsViewModel(
    private val repo: GameRepository,
    private val gameId: Long
) : ViewModel() {

    private val _results = MutableStateFlow<List<GameResult>>(emptyList())
    val results: StateFlow<List<GameResult>> = _results

    init {
        viewModelScope.launch {
            // Acknowledge the win the moment the results screen is shown so it won't pop again.
            repo.markResultsSeen(gameId)
            _results.value = repo.getGameResults(gameId)
        }
    }
}

// ── Leaderboard ViewModel ─────────────────────────────────────────────────────

class LeaderboardViewModel(private val repo: GameRepository) : ViewModel() {

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repo.getLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// ── Custom Phases ViewModel ───────────────────────────────────────────────────

class CustomPhasesViewModel(private val repo: GameRepository) : ViewModel() {

    val customPhaseSets: StateFlow<List<CustomPhaseSet>> = repo.getAllCustomPhaseSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun savePhaseSet(name: String, phases: List<PhaseRule>) {
        if (name.isBlank() || phases.isEmpty()) return
        viewModelScope.launch { repo.saveCustomPhaseSet(name, phases) }
    }

    fun deletePhaseSet(phaseSet: CustomPhaseSet) {
        viewModelScope.launch { repo.deleteCustomPhaseSet(phaseSet) }
    }
}

// ── Theme ViewModel ───────────────────────────────────────────────────────────

class ThemeViewModel(private val themePrefs: ThemePreferenceManager) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePrefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val amoledBlack: StateFlow<Boolean> = themePrefs.amoledBlack
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val haptics: StateFlow<Boolean> = themePrefs.haptics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePrefs.setThemeMode(mode) }
    }

    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch { themePrefs.setAmoledBlack(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { themePrefs.setHaptics(enabled) }
    }
}

class ThemeViewModelFactory(
    private val themePrefs: ThemePreferenceManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            return ThemeViewModel(themePrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ── ViewModel Factory ─────────────────────────────────────────────────────────

class ViewModelFactory(
    private val repo: GameRepository,
    private val gameId: Long = -1L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java)          -> HomeViewModel(repo) as T
        modelClass.isAssignableFrom(PlayerRosterViewModel::class.java)  -> PlayerRosterViewModel(repo) as T
        modelClass.isAssignableFrom(GameSetupViewModel::class.java)     -> GameSetupViewModel(repo) as T
        modelClass.isAssignableFrom(ActiveGameViewModel::class.java)    -> ActiveGameViewModel(repo, gameId) as T
        modelClass.isAssignableFrom(RoundEntryViewModel::class.java)    -> RoundEntryViewModel(repo, gameId) as T
        modelClass.isAssignableFrom(GameResultsViewModel::class.java)   -> GameResultsViewModel(repo, gameId) as T
        modelClass.isAssignableFrom(RoundHistoryViewModel::class.java)  -> RoundHistoryViewModel(repo, gameId) as T
        modelClass.isAssignableFrom(GameHistoryViewModel::class.java)   -> GameHistoryViewModel(repo) as T
        modelClass.isAssignableFrom(GameDetailViewModel::class.java)    -> GameDetailViewModel(repo, gameId) as T
        modelClass.isAssignableFrom(LeaderboardViewModel::class.java)   -> LeaderboardViewModel(repo) as T
        modelClass.isAssignableFrom(CustomPhasesViewModel::class.java)  -> CustomPhasesViewModel(repo) as T
        modelClass.isAssignableFrom(AboutViewModel::class.java)         -> AboutViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
