package com.crsmthw.phase10tracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.crsmthw.phase10tracker.data.model.*
import com.crsmthw.phase10tracker.data.repository.GameRepository
import com.crsmthw.phase10tracker.data.repository.ThemePreferenceRepository
import com.crsmthw.phase10tracker.ui.theme.ThemePreference
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Home ViewModel ────────────────────────────────────────────────────────────

class HomeViewModel(private val repo: GameRepository) : ViewModel() {

    // Observes the DB directly — updates instantly when game state changes
    // (new game started, game ended, navigating back from active game etc.)
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

    val customRuleSets: StateFlow<List<CustomRuleSet>> = repo.getAllCustomRuleSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlayers = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val selectedPlayers: StateFlow<List<PlayerEntity>> = _selectedPlayers

    private val _selectedRuleSet = MutableStateFlow<CustomRuleSet?>(null)
    val selectedRuleSet: StateFlow<CustomRuleSet?> = _selectedRuleSet

    fun selectRuleSet(ruleSet: CustomRuleSet?) {
        _selectedRuleSet.value = ruleSet
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
            // The allPlayers flow will update; we'll add it to selected via a new query
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
        viewModelScope.launch {
            val id = repo.startNewGame(players)
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

    private val _gameFinished = MutableStateFlow(false)
    val gameFinished: StateFlow<Boolean> = _gameFinished

    private val _gameCancelled = MutableStateFlow(false)
    val gameCancelled: StateFlow<Boolean> = _gameCancelled

    init {
        viewModelScope.launch {
            gameState.collect { game ->
                // Only trigger gameFinished if we haven't already cancelled
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
                // Set cancelled flag FIRST before DB write, so the gameState
                // collector above won't fire gameFinished when isComplete flips
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
            // Auto-complete only when score is non-empty AND definitively < 50.
            // If the user typed "6", that could become "60", "65" etc — don't auto-complete yet.
            // We consider a score "definitive" only if it's 0, OR if adding another digit
            // cannot bring it under 50 (i.e. it already has 2+ digits and is < 50,
            // or it is exactly 0).
            // Simplest safe rule: auto-complete only if score has >= 2 digits and < 50,
            // OR score is exactly 0 (went out).
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
            // Only allow manual toggle if not auto-completed
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

// ── Custom Rules ViewModel ────────────────────────────────────────────────────

class CustomRulesViewModel(private val repo: GameRepository) : ViewModel() {

    val customRuleSets: StateFlow<List<CustomRuleSet>> = repo.getAllCustomRuleSets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveRuleSet(name: String, phases: List<PhaseRule>) {
        if (name.isBlank() || phases.isEmpty()) return
        viewModelScope.launch { repo.saveCustomRuleSet(name, phases) }
    }

    fun deleteRuleSet(ruleSet: CustomRuleSet) {
        viewModelScope.launch { repo.deleteCustomRuleSet(ruleSet) }
    }
}

// ── ViewModel Factory ─────────────────────────────────────────────────────────

class ViewModelFactory(
    private val repo: GameRepository,
    private val gameId: Long = -1L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java)         -> HomeViewModel(repo) as T
        modelClass.isAssignableFrom(PlayerRosterViewModel::class.java) -> PlayerRosterViewModel(repo) as T
        modelClass.isAssignableFrom(GameSetupViewModel::class.java)    -> GameSetupViewModel(repo) as T
        modelClass.isAssignableFrom(ActiveGameViewModel::class.java)   -> ActiveGameViewModel(repo, gameId) as T
        modelClass.isAssignableFrom(RoundEntryViewModel::class.java)   -> RoundEntryViewModel(repo, gameId) as T
        modelClass.isAssignableFrom(GameResultsViewModel::class.java)  -> GameResultsViewModel(repo, gameId) as T
        modelClass.isAssignableFrom(LeaderboardViewModel::class.java)  -> LeaderboardViewModel(repo) as T
        modelClass.isAssignableFrom(CustomRulesViewModel::class.java)  -> CustomRulesViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ── Theme ViewModel ───────────────────────────────────────────────────────────

class ThemeViewModel(private val repo: ThemePreferenceRepository) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = repo.themePreference
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreference.SYSTEM)

    fun setTheme(preference: ThemePreference) {
        viewModelScope.launch { repo.setThemePreference(preference) }
    }
}

class ThemeViewModelFactory(
    private val repo: ThemePreferenceRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(ThemeViewModel::class.java) -> ThemeViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
