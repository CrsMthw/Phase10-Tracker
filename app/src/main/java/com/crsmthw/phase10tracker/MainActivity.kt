package com.crsmthw.phase10tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.crsmthw.phase10tracker.data.ThemePreferenceManager
import com.crsmthw.phase10tracker.ui.Phase10NavHost
import com.crsmthw.phase10tracker.ui.ThemeViewModel
import com.crsmthw.phase10tracker.ui.ThemeViewModelFactory
import com.crsmthw.phase10tracker.ui.theme.Phase10Theme

class MainActivity : ComponentActivity() {

    private val themeVm: ThemeViewModel by viewModels {
        ThemeViewModelFactory(ThemePreferenceManager(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode   by themeVm.themeMode.collectAsState()
            val amoledBlack by themeVm.amoledBlack.collectAsState()

            Phase10Theme(
                themeMode   = themeMode,
                amoledBlack = amoledBlack,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Phase10NavHost(
                        navController = navController,
                        themeVm       = themeVm
                    )
                }
            }
        }
    }
}
