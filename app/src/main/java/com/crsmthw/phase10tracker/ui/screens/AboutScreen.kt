package com.crsmthw.phase10tracker.ui.screens

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.crsmthw.phase10tracker.BuildConfig
import com.crsmthw.phase10tracker.R
import com.crsmthw.phase10tracker.ui.components.BottomFadeScrim
import com.crsmthw.phase10tracker.util.BiometricAuth
import com.crsmthw.phase10tracker.util.findFragmentActivity
import com.crsmthw.phase10tracker.util.press
import com.crsmthw.phase10tracker.util.reject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    onDeleteAll: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hf = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showConfirm1 by remember { mutableStateOf(false) }
    var showConfirm2 by remember { mutableStateOf(false) }

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    if (showConfirm1) {
        AlertDialog(
            onDismissRequest = { showConfirm1 = false },
            icon = { Icon(Icons.Filled.DeleteForever, null) },
            title = { Text("Delete all history & leaderboard?") },
            text = {
                Text(
                    "This clears every finished game and resets all win/games stats to zero. Your " +
                        "saved players and any game in progress are kept."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showConfirm1 = false; showConfirm2 = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Continue") }
            },
            dismissButton = { OutlinedButton(onClick = { showConfirm1 = false }) { Text("Cancel") } }
        )
    }

    if (showConfirm2) {
        AlertDialog(
            onDismissRequest = { showConfirm2 = false },
            icon = { Icon(Icons.Filled.Warning, null) },
            title = { Text("Are you absolutely sure?") },
            text = { Text("This can't be undone. All game history and the leaderboard will be permanently erased.") },
            confirmButton = {
                Button(
                    onClick = {
                        hf.reject()
                        showConfirm2 = false
                        BiometricAuth.authenticate(
                            activity = context.findFragmentActivity(),
                            title = "Delete all history",
                            subtitle = "Authenticate to erase history & leaderboard",
                            onSuccess = onDeleteAll
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete everything") }
            },
            dismissButton = { OutlinedButton(onClick = { showConfirm2 = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("About") },
                subtitle = { Text("App info & credits") },
                navigationIcon = {
                    IconButton(onClick = { hf.press(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(Modifier.fillMaxSize().padding(padding)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = navBottom + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(108.dp)
                    .clip(MaterialShapes.Clover4Leaf.toShape())
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Phase 10 Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ad-free. Open source. Always.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Credits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("❤️", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Made by CrsMthw and Claude", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("App design & development", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("❤️", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Icon by Shubbu", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("App icon design", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hf.press(); openUrl("https://github.com/CrsMthw") },
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Code, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GitHub", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("github.com/CrsMthw", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            Image(
                painter = painterResource(id = R.drawable.bmc_button),
                contentDescription = "Buy me a coffee",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable { hf.press(); openUrl("https://buymeacoffee.com/crsmthw") }
            )

            HorizontalDivider()

            Button(
                onClick = { hf.reject(); showConfirm1 = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Filled.DeleteForever, null)
                Spacer(Modifier.width(8.dp))
                Text("Delete all history & leaderboard")
            }

            Text(
                "Because the people didn't deserve\nanother ad-infested score tracker.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
        }
            BottomFadeScrim(color = MaterialTheme.colorScheme.background, height = navBottom + 48.dp)
        }
    }
}
