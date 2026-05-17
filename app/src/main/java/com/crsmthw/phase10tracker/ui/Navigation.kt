package com.crsmthw.phase10tracker.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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
                onContinueGame      = { gameId -> navController.navigate(Routes.activeGame(gameId)) },
                onStartNew          = { navController.navigate(Routes.GAME_SETUP) },
                onLeaderboard       = { navController.navigate(Routes.LEADERBOARD) },
                onManagePlayers     = { navController.navigate(Routes.PLAYER_ROSTER) },
                onCustomRules       = { navController.navigate(Routes.CUSTOM_RULES) },
                onAbout             = { navController.navigate(Routes.ABOUT) }
            )
        }

        composable(Routes.PLAYER_ROSTER) {
            val vm: PlayerRosterViewModel = viewModel(factory = factory)
            PlayerRosterScreen(
                vm     = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GAME_SETUP) {
            val vm: GameSetupViewModel = viewModel(factory = factory)
            GameSetupScreen(
                vm            = vm,
                onGameStarted = { gameId ->
                    navController.navigate(Routes.activeGame(gameId)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onBack = { navController.popBackStack() }
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
                onEnterRound    = { navController.navigate(Routes.roundEntry(gameId)) },
                onGameEnd       = {
                    navController.navigate(Routes.gameResults(gameId)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onGameCancelled = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
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
                onRoundSubmitted = { navController.popBackStack() },
                onBack           = { navController.popBackStack() }
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
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LEADERBOARD) {
            val vm: LeaderboardViewModel = viewModel(factory = factory)
            LeaderboardScreen(
                vm     = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CUSTOM_RULES) {
            val vm: CustomPhasesViewModel = viewModel(factory = factory)
            CustomPhasesScreen(
                vm     = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
