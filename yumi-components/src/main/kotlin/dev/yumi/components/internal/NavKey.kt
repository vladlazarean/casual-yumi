package dev.yumi.components.internal

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiLed
import dev.yumi.foundation.YumiTheme
import dev.yumi.tokens.YumiTokens

/**
 * One navigation key, shared by the navigation bar, rail and tabs so they behave identically:
 * a flat key in its strip that rests sunk when [selected], with accent-ink content and an LED
 * underline. Unselected keys are flat (they live in a recessed strip, where raised pebbles would
 * break the one-depth rule).
 */
@Composable
internal fun NavKey(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    icon: (@Composable () -> Unit)?,
    label: (@Composable () -> Unit)?,
    showLabel: Boolean,
    minHeight: Dp,
    role: Role,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
) {
    val source = remember { MutableInteractionSource() }
    val colors = YumiTheme.colors
    val content = when {
        !enabled -> colors.ink.copy(alpha = YumiTokens.DisabledContentAlpha)
        selected -> colors.accentInk
        else -> colors.ink
    }
    PebbleKey(
        input = Modifier.selectable(
            selected = selected,
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick,
        ),
        interactionSource = source,
        modifier = modifier,
        enabled = enabled,
        shape = YumiTheme.shapes.button,
        containerColor = Color.Transparent,
        contentColor = content,
        seatColor = Color.Transparent,
        depth = YumiTheme.depth.flat(),
        selected = selected,
        minHeight = minHeight,
        contentPadding = contentPadding,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                Box(Modifier.defaultMinSize(minWidth = 24.dp, minHeight = 24.dp), contentAlignment = Alignment.Center) { icon() }
            }
            if (label != null && (showLabel || selected)) {
                CompositionLocalProvider(
                    LocalContentColor provides content,
                    LocalTextStyle provides YumiTheme.typography.labelMedium.copy(textAlign = TextAlign.Center),
                ) { label() }
            }
            YumiLed(on = selected && enabled)
        }
    }
}
