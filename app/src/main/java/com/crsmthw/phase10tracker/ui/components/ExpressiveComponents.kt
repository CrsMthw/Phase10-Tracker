@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.crsmthw.phase10tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── M3 Expressive shapes used across the app (graphics-shapes / MaterialShapes) ──

object ExpressiveShapes {
    val avatar: Shape @Composable get() = MaterialShapes.Cookie9Sided.toShape()
    val badge: Shape @Composable get() = MaterialShapes.Cookie6Sided.toShape()
    val winner: Shape @Composable get() = MaterialShapes.Clover4Leaf.toShape()
    val accent: Shape @Composable get() = MaterialShapes.Cookie4Sided.toShape()
}

/** A player's initial in an expressive cookie-shaped chip. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    shape: Shape = ExpressiveShapes.avatar,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Surface(shape = shape, color = container, contentColor = content, modifier = modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.trim().take(1).uppercase().ifBlank { "?" },
                style = textStyle,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** Small label/number in an expressive shape (rank, phase, etc.). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveBadge(
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    shape: Shape = ExpressiveShapes.badge,
    container: Color = MaterialTheme.colorScheme.secondary,
    content: Color = MaterialTheme.colorScheme.onSecondary,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    Surface(shape = shape, color = container, contentColor = content, modifier = modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = textStyle, fontWeight = FontWeight.Bold)
        }
    }
}

/** Centered empty-state with an icon + title + subtitle, used by list screens with no items. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                icon, null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/** A bottom gradient that fades a scrolling list out into the pane background (M3 Expressive long-list cue). */
@Composable
fun BoxScope.BottomFadeScrim(
    color: Color = MaterialTheme.colorScheme.surface,
    height: Dp = 56.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(listOf(Color.Transparent, color)))
    )
}
