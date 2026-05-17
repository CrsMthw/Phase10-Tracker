package com.crsmthw.phase10tracker

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.crsmthw.phase10tracker.data.repository.ThemePreferenceRepository
import com.crsmthw.phase10tracker.ui.Phase10NavHost
import com.crsmthw.phase10tracker.ui.ThemeViewModel
import com.crsmthw.phase10tracker.ui.ThemeViewModelFactory
import com.crsmthw.phase10tracker.ui.theme.Phase10Theme
import com.crsmthw.phase10tracker.ui.theme.isDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeRepo = remember { ThemePreferenceRepository(context.applicationContext) }
            val themeVm: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(themeRepo))
            val themePreference by themeVm.themePreference.collectAsState()
            val darkTheme = themePreference.isDark()

            // Keep status-bar / nav-bar icon contrast in sync with the user's chosen
            // in-app theme — otherwise forcing dark while the system is light (or vice
            // versa) leaves the icons invisible against the app background.
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    val insets = WindowCompat.getInsetsController(window, view)
                    insets.isAppearanceLightStatusBars = !darkTheme
                    insets.isAppearanceLightNavigationBars = !darkTheme
                }
            }

            Phase10Theme(themePreference = themePreference) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Phase10NavHost(
                        navController = navController,
                        currentTheme = themePreference,
                        onThemeChange = themeVm::setTheme
                    )
                }
            }
        }
    }
}
