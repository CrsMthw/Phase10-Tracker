package com.crsmthw.phase10tracker.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.crsmthw.phase10tracker.data.db.Phase10Database
import com.crsmthw.phase10tracker.data.repository.GameRepository
import com.crsmthw.phase10tracker.ui.screens.*

object Routes {
    const val HOME           = "home"
    const val PLAYER_ROSTER  = "players"
    const val GAME_SETUP     = "setup"
    const val ACTIVE_GAME    = "game/{gameId}"
    const val ROUND_ENTRY    = "round/{gameId}"
    const val GAME_RESULTS   = "results/{gameId}"
    const val LEADERBOARD    = "leaderboard"
    const val CUSTOM_RULES   = "custom_rules"
    const val ABOUT          = "about"

    fun activeGame(id: Long)  = "game/$id"
    fun roundEntry(id: Long)  = "round/$id"
    fun gameResults(id: Long) = "results/$id"
}

// ── Safe navigation helpers ───────────────────────────────────────────────────
// Guards every nav action behind a RESUMED check so rapid double-taps during
// transition animations can't pop extra destinations and cause a blank screen.

private fun NavController.navigateSafe(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(route, builder)
    }
}

private fun NavController.popSafe() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack()
    }
}

@Composable
fun Phase10NavHost(
    navController: NavHostController,
    themeVm: ThemeViewModel
) {
    val context = LocalContext.current
    val db = remember { Phase10Database.getInstance(context) }
    val repo = remember {
        GameRepository(
            db.playerDao(),
            db.gameDao(),
            db.gamePlayerDao(),
            db.roundDao(),
            db.customPhaseSetDao()
        )
    }
    val factory = remember { ViewModelFactory(repo) }

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = factory)
            val themeMode   by themeVm.themeMode.collectAsState()
            val amoledBlack by themeVm.amoledBlack.collectAsState()
            HomeScreen(
                vm                  = vm,
                themeMode           = themeMode,
                amoledBlack         = amoledBlack,
                onThemeModeChange   = themeVm::setThemeMode,
                onAmoledBlackChange = themeVm::setAmoledBlack,
                onContinueGame      = { gameId -> navController.navigateSafe(Routes.activeGame(gameId)) },
                onStartNew          = { navController.navigateSafe(Routes.GAME_SETUP) },
                onLeaderboard       = { navController.navigateSafe(Routes.LEADERBOARD) },
                onManagePlayers     = { navController.navigateSafe(Routes.PLAYER_ROSTER) },
                onCustomRules       = { navController.navigateSafe(Routes.CUSTOM_RULES) },
                onAbout             = { navController.navigateSafe(Routes.ABOUT) }
            )
        }

        composable(Routes.PLAYER_ROSTER) {
            val vm: PlayerRosterViewModel = viewModel(factory = factory)
            PlayerRosterScreen(
                vm     = vm,
                onBack = { navController.popSafe() }
            )
        }

        composable(Routes.GAME_SETUP) {
            val vm: GameSetupViewModel = viewModel(factory = factory)
            GameSetupScreen(
                vm            = vm,
                onGameStarted = { gameId ->
                    navController.navigateSafe(Routes.activeGame(gameId)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onBack = { navController.popSafe() }
            )
        }

        composable(
            route     = Routes.ACTIVE_GAME,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId      = backStackEntry.arguments!!.getLong("gameId")
            val gameFactory = remember { ViewModelFactory(repo, gameId) }
            val vm: ActiveGameViewModel = viewModel(factory = gameFactory)
            ActiveGameScreen(
                vm              = vm,
                onEnterRound    = { navController.navigateSafe(Routes.roundEntry(gameId)) },
                onGameEnd       = {
                    navController.navigateSafe(Routes.gameResults(gameId)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onGameCancelled = {
                    navController.navigateSafe(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onBack = { navController.popSafe() }
            )
        }

        composable(
            route     = Routes.ROUND_ENTRY,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId      = backStackEntry.arguments!!.getLong("gameId")
            val gameFactory = remember { ViewModelFactory(repo, gameId) }
            val vm: RoundEntryViewModel = viewModel(factory = gameFactory)
            RoundEntryScreen(
                vm               = vm,
                onRoundSubmitted = { navController.popSafe() },
                onBack           = { navController.popSafe() }
            )
        }

        composable(
            route     = Routes.GAME_RESULTS,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId      = backStackEntry.arguments!!.getLong("gameId")
            val gameFactory = remember { ViewModelFactory(repo, gameId) }
            val vm: GameResultsViewModel = viewModel(factory = gameFactory)
            GameResultsScreen(
                vm     = vm,
                onHome = {
                    navController.navigateSafe(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LEADERBOARD) {
            val vm: LeaderboardViewModel = viewModel(factory = factory)
            LeaderboardScreen(
                vm     = vm,
                onBack = { navController.popSafe() }
            )
        }

        composable(Routes.CUSTOM_RULES) {
            val vm: CustomPhasesViewModel = viewModel(factory = factory)
            CustomPhasesScreen(
                vm     = vm,
                onBack = { navController.popSafe() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popSafe() })
        }
    }
}
