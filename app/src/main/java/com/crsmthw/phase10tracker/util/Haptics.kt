package com.crsmthw.phase10tracker.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.flow.drop

/**
 * Material 3 Expressive haptics, mapped to semantic intents so call sites read clearly and the
 * whole app's haptic vocabulary lives in one place. Always fired from a user gesture (never from
 * reactive state changes).
 *
 * Every helper is gated on [HapticsConfig.enabled], which mirrors the Settings → Haptic feedback
 * toggle (kept in sync from the app root). When off, all haptics no-op.
 */
object HapticsConfig {
    @Volatile var enabled: Boolean = true
}

/** Distinct on/off feedback for a switch-like control (segmented toggle, settings switches…). */
fun HapticFeedback.toggle(enabled: Boolean) = perform(
    if (enabled) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
)

/** Affirmative: submit a round, start a game, confirm a dialog, declare the winner. */
fun HapticFeedback.confirm() = perform(HapticFeedbackType.Confirm)

/** Failed/blocked or destructive action (end game / delete). */
fun HapticFeedback.reject() = perform(HapticFeedbackType.Reject)

/** Long-press to reveal a menu or start a drag. */
fun HapticFeedback.longPress() = perform(HapticFeedbackType.LongPress)

/** Light click: tapping a card, opening a sheet, secondary buttons. */
fun HapticFeedback.press() = perform(HapticFeedbackType.ContextClick)

/** A slider/stepper crossing a notch. */
fun HapticFeedback.tick() = perform(HapticFeedbackType.SegmentTick)

/** A list crossing an item boundary while scrolling (fired per item). */
fun HapticFeedback.scrollTick() = perform(HapticFeedbackType.SegmentFrequentTick)

/** A drag crossing an activation threshold. */
fun HapticFeedback.threshold() = perform(HapticFeedbackType.GestureThresholdActivate)

private fun HapticFeedback.perform(type: HapticFeedbackType) {
    if (HapticsConfig.enabled) performHapticFeedback(type)
}

/**
 * Fires [scrollTick] as [listState] scrolls — once each time a new item is revealed (or hidden)
 * at the **bottom** edge of the viewport, i.e. whenever the last visible item's index changes.
 * Anchoring on the bottom edge means a tall item at the top never gates the feedback. Gated on an
 * in-progress scroll so appending items stays silent; the initial value is dropped so opening a
 * screen doesn't buzz.
 */
@Composable
fun ListScrollHaptics(listState: LazyListState) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .drop(1)
            .collect { if (listState.isScrollInProgress) haptics.scrollTick() }
    }
}
