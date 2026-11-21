package dev.yumi.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import dev.yumi.tokens.YumiDepth

/**
 * Raised surface: *you press it.*
 *
 * Draws the seat ring, drop shadow, fill and top highlight of a pebble. As [sink] goes from 0
 * (resting) to 1 (fully pressed) the pebble moves down by [sinkOffset], its drop shadow and
 * highlight fade out and a pressed-in inner shadow fades in.
 *
 * [sink] is a lambda and is only read inside `graphicsLayer` / shadow draw blocks, so an
 * animating press invalidates the draw phase only: no recomposition, no relayout.
 *
 * This modifier only draws; pair it with [animateSinkAsState] and a clickable for interaction.
 * Typical values come from the theme:
 * ```
 * val sink by animateSinkAsState(interactionSource, enabled)
 * Modifier.pebble(
 *     color = YumiTheme.colors.raised,
 *     shape = YumiTheme.shapes.button,
 *     seatColor = YumiTheme.colors.seat,
 *     depth = YumiTheme.depth,
 *     sinkOffset = YumiTheme.motion.sinkOffset,
 *     sink = { sink },
 * )
 * ```
 *
 * @param color pebble fill.
 * @param shape pebble outline; used for the ring, shadows and fill.
 * @param seatColor colour of the thin ring the pebble sits in.
 * @param depth shadow and highlight values.
 * @param sinkOffset distance travelled at `sink == 1`.
 * @param sink press progress, 0 = raised, 1 = sunk. Disabled pebbles rest at 1.
 */
public fun Modifier.pebble(
    color: Color,
    shape: Shape,
    seatColor: Color,
    depth: YumiDepth,
    sinkOffset: Dp,
    sink: () -> Float = { 0f },
): Modifier = this
    .graphicsLayer { translationY = sinkOffset.toPx() * sink() }
    // Seat ring: drawn after its content, so it sits on top of the fill and inner shadows.
    .border(depth.seatWidth, seatColor, shape)
    .dropShadow(shape) {
        this.radius = depth.raiseRadius.toPx()
        this.offset = depth.raiseOffset.toOffset(this)
        this.color = Color.Black
        this.alpha = depth.shadowAlpha * (1f - sink())
    }
    .background(color, shape)
    // Top highlight while raised...
    .innerShadow(shape) {
        this.radius = depth.highlightRadius.toPx()
        this.offset = depth.highlightOffset.toOffset(this)
        this.color = Color.White
        this.alpha = depth.highlightAlpha * (1f - sink())
    }
    // ...swaps for a pressed-in shadow as it sinks.
    .innerShadow(shape) {
        this.radius = depth.pressedInsetRadius.toPx()
        this.offset = depth.pressedInsetOffset.toOffset(this)
        this.color = Color.Black
        this.alpha = depth.pressedInsetAlpha * sink()
    }

/**
 * Recessed surface: *you read it.*
 *
 * A filled well with a top inner shadow. Static by design: trays never react to touch.
 *
 * @param color tray fill, normally `YumiTheme.colors.tray`.
 * @param shape tray outline, normally `YumiTheme.shapes.tray`.
 * @param depth inset values, normally `YumiTheme.depth`.
 */
public fun Modifier.tray(
    color: Color,
    shape: Shape,
    depth: YumiDepth,
): Modifier = this
    .background(color, shape)
    .innerShadow(
        shape = shape,
        shadow = Shadow(
            radius = depth.trayInsetRadius,
            color = Color.Black,
            offset = depth.trayInsetOffset,
            alpha = depth.trayInsetAlpha,
        ),
    )

private fun DpOffset.toOffset(density: Density): Offset =
    with(density) { Offset(x.toPx(), y.toPx()) }
