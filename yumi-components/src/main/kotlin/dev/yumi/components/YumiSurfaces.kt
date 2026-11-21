package dev.yumi.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.LocalYumiSurfaceLevel
import dev.yumi.components.internal.YumiSurfaceLevel
import dev.yumi.components.internal.pebbleDepth
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import dev.yumi.foundation.pebbleClickable
import dev.yumi.foundation.tray
import dev.yumi.tokens.YumiTokens

/** Defaults for [YumiTray] and [YumiCard]. */
public object YumiSurfaceDefaults {
    /** Padding inside a tray. */
    public val TrayContentPadding: PaddingValues = PaddingValues(16.dp)

    /** Padding inside a card. */
    public val CardContentPadding: PaddingValues = PaddingValues(20.dp)

    /** Tray shape. */
    public val trayShape: Shape
        @Composable @ReadOnlyComposable get() = YumiTheme.shapes.tray

    /** Card shape. */
    public val cardShape: Shape
        @Composable @ReadOnlyComposable get() = YumiTheme.shapes.card
}

/**
 * A recessed tray: *you read it.* Holds read-only information and, at most, flat text buttons
 * or icon pebbles.
 *
 * @param modifier modifier for the tray.
 * @param shape tray outline.
 * @param color tray fill.
 * @param contentPadding padding around [content].
 * @param content tray content.
 */
@Composable
public fun YumiTray(
    modifier: Modifier = Modifier,
    shape: Shape = YumiSurfaceDefaults.trayShape,
    color: Color = YumiTheme.colors.tray,
    contentPadding: PaddingValues = YumiSurfaceDefaults.TrayContentPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .tray(color, shape, YumiTheme.depth)
            .padding(contentPadding),
    ) {
        CompositionLocalProvider(
            LocalYumiSurfaceLevel provides YumiSurfaceLevel.Recessed,
            LocalContentColor provides YumiTheme.colors.ink,
        ) {
            content()
        }
    }
}

/**
 * A raised card for grouped content.
 *
 * One depth: pebbles placed inside a card render flat (no drop shadow, no highlight) but still
 * sink, so you never get a pebble on a pebble.
 *
 * @param modifier modifier for the card.
 * @param shape card outline.
 * @param color card fill.
 * @param contentPadding padding around [content].
 * @param content card content.
 */
@Composable
public fun YumiCard(
    modifier: Modifier = Modifier,
    shape: Shape = YumiSurfaceDefaults.cardShape,
    color: Color = YumiTheme.colors.raised,
    contentPadding: PaddingValues = YumiSurfaceDefaults.CardContentPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .pebble(
                color = color,
                shape = shape,
                seatColor = YumiTheme.colors.seat,
                depth = pebbleDepth(),
                sinkOffset = YumiTheme.motion.sinkOffset,
            )
            .clip(shape)
            .padding(contentPadding),
    ) {
        CardContent(content)
    }
}

/**
 * A raised card that is itself a key: the whole card sinks on press and supports long press.
 *
 * @param onClick called on click.
 * @param modifier modifier for the card.
 * @param enabled whether the card responds to input.
 * @param shape card outline.
 * @param color card fill.
 * @param contentPadding padding around [content].
 * @param interactionSource source of press interactions; `null` creates one internally.
 * @param onLongClickLabel accessibility label for the long-press action.
 * @param onLongClick called on long press; `null` disables long press.
 * @param content card content.
 */
@Composable
public fun YumiCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = YumiSurfaceDefaults.cardShape,
    color: Color = YumiTheme.colors.raised,
    contentPadding: PaddingValues = YumiSurfaceDefaults.CardContentPadding,
    interactionSource: MutableInteractionSource? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val sink by animateSinkAsState(source, enabled = enabled)
    Column(
        modifier = modifier
            .pebble(
                color = color,
                shape = shape,
                seatColor = YumiTheme.colors.seat,
                depth = pebbleDepth(),
                sinkOffset = YumiTheme.motion.sinkOffset,
                sink = { sink },
            )
            .clip(shape)
            .pebbleClickable(
                interactionSource = source,
                enabled = enabled,
                role = Role.Button,
                haptics = YumiTheme.haptics,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick,
            )
            .padding(contentPadding),
    ) {
        CardContent(content, alpha = if (enabled) 1f else YumiTokens.DisabledContentAlpha)
    }
}

@Composable
private fun ColumnScope.CardContent(content: @Composable ColumnScope.() -> Unit, alpha: Float = 1f) {
    CompositionLocalProvider(
        LocalYumiSurfaceLevel provides YumiSurfaceLevel.Raised,
        LocalContentColor provides YumiTheme.colors.ink.copy(alpha = alpha),
    ) {
        content()
    }
}
