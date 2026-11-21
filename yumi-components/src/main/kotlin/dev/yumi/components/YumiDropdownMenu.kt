package dev.yumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.LocalYumiSurfaceLevel
import dev.yumi.components.internal.YumiSurfaceLevel
import dev.yumi.foundation.YumiTheme
import dev.yumi.tokens.YumiTokens

/** Defaults for [YumiDropdownMenu]. */
public object YumiMenuDefaults {
    /** Menu panel shape. */
    public val shape: Shape = RoundedCornerShape(20.dp)
}

/**
 * A dropdown menu on a body-coloured panel with a soft shadow, anchored to its parent.
 *
 * Built on Material 3's `DropdownMenu` (popup positioning, keyboard, dismiss on outside tap),
 * with the tonal tint removed and the panel recoloured. Use [YumiDropdownMenuItem] inside.
 *
 * @param expanded whether the menu is open.
 * @param onDismissRequest called on outside tap or back.
 * @param modifier modifier for the menu content.
 * @param offset offset from the anchor.
 * @param content the items.
 */
@Composable
public fun YumiDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 4.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.widthIn(min = 180.dp),
        offset = offset,
        shape = YumiMenuDefaults.shape,
        containerColor = YumiTheme.colors.body,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
    ) {
        CompositionLocalProvider(LocalYumiSurfaceLevel provides YumiSurfaceLevel.Body) { content() }
    }
}

/**
 * One row in a [YumiDropdownMenu]: a flat key that dips into a tray tint while pressed.
 *
 * @param text the item label.
 * @param onClick called on click.
 * @param modifier modifier for the item.
 * @param leadingIcon optional icon before the label.
 * @param trailingIcon optional icon or shortcut after the label.
 * @param enabled whether the item responds to input.
 */
@Composable
public fun YumiDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val colors = YumiTheme.colors
    val shape = YumiTheme.shapes.small
    val sinkOffset = YumiTheme.motion.sinkOffset
    val content = if (enabled) colors.ink else colors.ink.copy(alpha = YumiTokens.DisabledContentAlpha)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 6.dp)
            .clip(shape)
            .background(if (pressed) colors.tray else Color.Transparent, shape)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .graphicsLayer { translationY = if (pressed) sinkOffset.toPx() else 0f }
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides content,
            LocalTextStyle provides YumiTheme.typography.bodyLarge,
        ) {
            leadingIcon?.invoke()
            Box(Modifier.weight(1f)) { text() }
            if (trailingIcon != null) {
                CompositionLocalProvider(LocalContentColor provides colors.muted) { trailingIcon() }
            }
        }
    }
}
