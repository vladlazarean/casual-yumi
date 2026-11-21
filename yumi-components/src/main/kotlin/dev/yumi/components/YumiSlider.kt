package dev.yumi.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import dev.yumi.foundation.tray
import kotlin.math.roundToInt

/**
 * Colours of [YumiSlider].
 *
 * @property trackColor recessed track.
 * @property activeTrackColor the lit part of the track, from the start to the thumb.
 * @property thumbColor raised thumb.
 * @property tickColor step marks on the track.
 * @property disabledActiveTrackColor the lit part when disabled (no accent on a dead control).
 */
@Immutable
public class YumiSliderColors(
    public val trackColor: Color,
    public val activeTrackColor: Color,
    public val thumbColor: Color,
    public val tickColor: Color,
    public val disabledActiveTrackColor: Color,
) {
    /** Returns a copy with the given values replaced. */
    public fun copy(
        trackColor: Color = this.trackColor,
        activeTrackColor: Color = this.activeTrackColor,
        thumbColor: Color = this.thumbColor,
        tickColor: Color = this.tickColor,
        disabledActiveTrackColor: Color = this.disabledActiveTrackColor,
    ): YumiSliderColors = YumiSliderColors(trackColor, activeTrackColor, thumbColor, tickColor, disabledActiveTrackColor)

    override fun equals(other: Any?): Boolean =
        other is YumiSliderColors &&
            trackColor == other.trackColor &&
            activeTrackColor == other.activeTrackColor &&
            thumbColor == other.thumbColor &&
            tickColor == other.tickColor &&
            disabledActiveTrackColor == other.disabledActiveTrackColor

    override fun hashCode(): Int {
        var result = trackColor.hashCode()
        result = 31 * result + activeTrackColor.hashCode()
        result = 31 * result + thumbColor.hashCode()
        result = 31 * result + tickColor.hashCode()
        result = 31 * result + disabledActiveTrackColor.hashCode()
        return result
    }
}

/** Defaults for [YumiSlider]. */
public object YumiSliderDefaults {
    /** Height of the recessed track. */
    public val TrackHeight: Dp = 10.dp

    /** Diameter of the raised thumb. */
    public val ThumbSize: Dp = 28.dp

    /** Colours under the current theme. */
    @Composable
    @ReadOnlyComposable
    public fun colors(): YumiSliderColors {
        val c = YumiTheme.colors
        return YumiSliderColors(
            trackColor = c.tray,
            activeTrackColor = c.accent,
            thumbColor = c.raised,
            tickColor = c.muted,
            disabledActiveTrackColor = c.seat,
        )
    }
}

/**
 * A linear slider: recessed track, raised thumb, accent active section.
 *
 * Built on Material 3's `Slider` with custom thumb and track slots, so dragging, tapping the
 * track, arrow / D-pad keys, RTL, `progressBarRangeInfo` and the `setProgress` accessibility
 * action all come from M3 unchanged. Yumi adds the look, a sinking thumb and a haptic tick on
 * every step.
 *
 * @param value current value, within [valueRange].
 * @param onValueChange called while the value changes.
 * @param modifier modifier for the slider.
 * @param enabled whether the slider responds to input.
 * @param valueRange range of values.
 * @param steps number of discrete steps between the ends; 0 = continuous.
 * @param onValueChangeFinished called when the user finishes a change.
 * @param colors colours of the slider.
 * @param interactionSource source of press and drag interactions; `null` creates one internally.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: YumiSliderColors = YumiSliderDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val haptics = YumiTheme.haptics
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    // Plain holder, not state: the last step is bookkeeping and must not trigger recomposition.
    val lastStep = remember(steps, valueRange) { StepHolder(stepIndex(value, valueRange, steps)) }

    Slider(
        value = value,
        onValueChange = { newValue ->
            if (steps > 0) {
                val step = stepIndex(newValue, valueRange, steps)
                if (step != lastStep.index) {
                    lastStep.index = step
                    haptics.tick()
                }
            }
            currentOnValueChange(newValue)
        },
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = source,
        steps = steps,
        thumb = { YumiSliderThumb(source, enabled, colors) },
        track = { state -> YumiSliderTrack(state, enabled, colors) },
        valueRange = valueRange,
    )
}

@Composable
internal fun YumiSliderThumb(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    colors: YumiSliderColors,
) {
    val dragged by interactionSource.collectIsDraggedAsState()
    val sink by animateSinkAsState(interactionSource, enabled = enabled, selected = dragged)
    Box(
        Modifier
            .size(YumiSliderDefaults.ThumbSize)
            .pebble(
                color = colors.thumbColor,
                shape = CircleShape,
                seatColor = YumiTheme.colors.seat,
                depth = YumiTheme.depth,
                sinkOffset = YumiTheme.motion.sinkOffset,
                sink = { sink },
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YumiSliderTrack(state: SliderState, enabled: Boolean, colors: YumiSliderColors) {
    val active = if (enabled) colors.activeTrackColor else colors.disabledActiveTrackColor
    val tick = colors.tickColor
    Box(
        Modifier
            .fillMaxWidth()
            .height(YumiSliderDefaults.TrackHeight)
            .tray(
                color = colors.trackColor,
                shape = YumiTheme.shapes.pill,
                depth = YumiTheme.depth.copy(trayInsetRadius = 3.dp, trayInsetOffset = DpOffset(0.dp, 1.dp)),
            )
            // Draw-phase reads of state.value: dragging only redraws the track.
            .drawWithContent {
                drawContent()
                val range = state.valueRange
                val span = range.endInclusive - range.start
                val fraction = if (span == 0f) 0f else ((state.value - range.start) / span).coerceIn(0f, 1f)
                val activeWidth = size.width * fraction
                val rtl = layoutDirection == LayoutDirection.Rtl
                val radius = CornerRadius(size.height / 2f)
                drawRoundRect(
                    color = active,
                    topLeft = Offset(if (rtl) size.width - activeWidth else 0f, 0f),
                    size = Size(activeWidth, size.height),
                    cornerRadius = radius,
                )
                if (state.steps > 0) {
                    val dot = 1.5.dp.toPx()
                    for (i in 0..state.steps + 1) {
                        val x = size.width * i / (state.steps + 1)
                        val inset = x.coerceIn(size.height / 2f, size.width - size.height / 2f)
                        drawCircle(tick, radius = dot, center = Offset(inset, size.height / 2f))
                    }
                }
            },
    )
}

internal class StepHolder(var index: Int)

internal fun stepIndex(value: Float, range: ClosedFloatingPointRange<Float>, steps: Int): Int {
    if (steps <= 0) return 0
    val span = range.endInclusive - range.start
    if (span == 0f) return 0
    val fraction = ((value - range.start) / span).coerceIn(0f, 1f)
    return (fraction * (steps + 1)).roundToInt()
}
