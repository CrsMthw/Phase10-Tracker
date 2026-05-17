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

    private val _gameFinished = MutableStateFlow(false)
    val gameFinished: StateFlow<Boolean> = _gameFinished

    private val _gameCancelled = MutableStateFlow(false)
    val gameCancelled: StateFlow<Boolean> = _gameCancelled

    init {
        viewModelScope.launch {
            gameState.collect { game ->
                if (game?.isComplete == true && !_gameCancelled.value) {
                    _gameFinished.value = true
                }
            }
        }
    }

    fun endGameEarly() {
        viewModelScope.launch {
            val players = boardState.value
            val anyScoreAboveZero = players.any { it.totalScore > 0 }
            if (!anyScoreAboveZero) {
                _gameCancelled.value = true
                repo.cancelGame(gameId)
            } else {
                repo.endGame(gameId)
                _gameFinished.value = true
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
            val autoComplete = scoreInt != null && (
                scoreInt == 0 || (score.trim().length >= 2 && scoreInt < PHASE_COMPLETE_THRESHOLD)
            )
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

    fun submitRound() {
        viewModelScope.launch {
            repo.submitRound(gameId, _entries.value)
            _submitted.value = true
        }
    }

    fun isValid(): Boolean = _entries.value.all {
        it.scoreInput.trim().toIntOrNull() != null
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

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePrefs.setThemeMode(mode) }
    }

    fun setAmoledBlack(enabled: Boolean) {
        viewModelScope.launch { themePrefs.setAmoledBlack(enabled) }
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
        modelClass.isAssignableFrom(LeaderboardViewModel::class.java)   -> LeaderboardViewModel(repo) as T
        modelClass.isAssignableFrom(CustomPhasesViewModel::class.java)  -> CustomPhasesViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
