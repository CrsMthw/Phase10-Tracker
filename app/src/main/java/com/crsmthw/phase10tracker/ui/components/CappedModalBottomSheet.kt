package com.crsmthw.phase10tracker.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Modal bottom sheet hardened against the M3 fill-the-screen fling bug (issuetracker 285847707):
 * Hidden↔Expanded only (skipPartiallyExpanded) + scrollable content capped a status-bar-height
 * below the top, so the Expanded anchor is never at offset 0 where the spring fling overshoots and
 * the sheet stops responding to touch. Content is scrollable and ime-padded for text fields.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CappedModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cap = maxHeight - sheetTopGap()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = cap)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                content = content,
            )
        }
    }
}

@Composable
private fun sheetTopGap(): Dp {
    val density = LocalDensity.current
    val top = WindowInsets.statusBars.getTop(density)
    return with(density) { top.toDp() }
}
