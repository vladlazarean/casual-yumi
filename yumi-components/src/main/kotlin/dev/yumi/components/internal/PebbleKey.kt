package dev.yumi.components.internal

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import dev.yumi.tokens.YumiDepth

/** Which material a composable sits on. Drives the one-depth rule. */
internal enum class YumiSurfaceLevel { Body, Raised, Recessed }

internal val LocalYumiSurfaceLevel = staticCompositionLocalOf { YumiSurfaceLevel.Body }

/**
 * Depth for a pebble at the current surface level.
 *
 * One depth: no pebble on a pebble. On a raised surface (a card) pebbles lose their drop shadow
 * and highlight and become flat keys that still sink. Enforced by rendering rather than by
 * throwing, so a nested layout degrades instead of crashing.
 */
@Composable
@ReadOnlyComposable
internal fun pebbleDepth(): YumiDepth {
    val depth = YumiTheme.depth
    return if (LocalYumiSurfaceLevel.current == YumiSurfaceLevel.Raised) depth.flat() else depth
}

/** Depth with no drop shadow and no highlight: keeps only the pressed-in inner shadow. */
internal fun YumiDepth.flat(): YumiDepth = copy(shadowAlpha = 0f, highlightAlpha = 0f)

/**
 * The shared body of every pressable Yumi key: seat ring, raise/sink, no ripple, content colour
 * and label style. [input] is the clickable / toggleable / selectable modifier, applied after
 * the drawing so the whole key is the touch target.
 */
@Composable
internal fun PebbleKey(
    input: Modifier,
    interactionSource: MutableInteractionSource,
    modifier: Modifier,
    enabled: Boolean,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    seatColor: Color = YumiTheme.colors.seat,
    depth: YumiDepth = pebbleDepth(),
    selected: Boolean = false,
    minWidth: Dp = Dp.Unspecified,
    minHeight: Dp = Dp.Unspecified,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val sink by animateSinkAsState(interactionSource, enabled = enabled, selected = selected)
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .pebble(
                color = containerColor,
                shape = shape,
                seatColor = seatColor,
                depth = depth,
                sinkOffset = YumiTheme.motion.sinkOffset,
                sink = { sink },
            )
            .clip(shape)
            .then(input)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            // Centre wrapped lines too: at 200 % font scale or in narrow segments a label can wrap.
            LocalTextStyle provides YumiTheme.typography.labelLarge.copy(textAlign = TextAlign.Center),
        ) {
            content()
        }
    }
}
