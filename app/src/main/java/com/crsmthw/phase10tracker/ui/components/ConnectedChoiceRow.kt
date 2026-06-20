package com.crsmthw.phase10tracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Single-choice connected button group (M3 Expressive). Each segment gets the connected
 * leading/middle/trailing shape morph plus the inter-button press-squeeze. A real overflow
 * indicator collapses a too-narrow row into a menu instead of throwing a negative-width measure
 * exception. Used for the Scores / By Phase toggle and the phase-set picker.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ConnectedChoiceRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    ButtonGroup(
        overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
        modifier = modifier.fillMaxWidth(),
    ) {
        options.forEach { (value, label) ->
            toggleableItem(
                checked = selected == value,
                onCheckedChange = { if (it && selected != value) onSelect(value) },
                label = label,
                weight = 1f,
            )
        }
    }
}
