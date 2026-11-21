package dev.yumi.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.RangeSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.tray

/**
 * A range slider: one recessed track, two raised thumbs, the accent between them.
 *
 * Built on Material 3's `RangeSlider` with custom slots, so each thumb is its own accessibility
 * node with `progressBarRangeInfo` / `setProgress`, arrow keys move the focused thumb, and RTL is
 * handled. Yumi adds the look, sinking thumbs and a haptic tick when either end crosses a step.
 *
 * @param value current range, within [valueRange].
 * @param onValueChange called while either end moves.
 * @param modifier modifier for the slider.
 * @param enabled whether the slider responds to input.
 * @param valueRange range of values.
 * @param steps number of discrete steps between the ends; 0 = continuous.
 * @param onValueChangeFinished called when the user finishes a change.
 * @param colors colours, shared with [YumiSlider].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: YumiSliderColors = YumiSliderDefaults.colors(),
) {
    val startSource = remember { MutableInteractionSource() }
    val endSource = remember { MutableInteractionSource() }
    val haptics = YumiTheme.haptics
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val lastStart = remember(steps, valueRange) { StepHolder(stepIndex(value.start, valueRange, steps)) }
    val lastEnd = remember(steps, valueRange) { StepHolder(stepIndex(value.endInclusive, valueRange, steps)) }

    RangeSlider(
        value = value,
        onValueChange = { range ->
            if (steps > 0) {
                val s = stepIndex(range.start, valueRange, steps)
                val e = stepIndex(range.endInclusive, valueRange, steps)
                if (s != lastStart.index || e != lastEnd.index) {
                    lastStart.index = s
                    lastEnd.index = e
                    haptics.tick()
                }
            }
            currentOnValueChange(range)
        },
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        onValueChangeFinished = onValueChangeFinished,
        startInteractionSource = startSource,
        endInteractionSource = endSource,
        startThumb = { YumiSliderThumb(startSource, enabled, colors) },
        endThumb = { YumiSliderThumb(endSource, enabled, colors) },
        track = { state -> RangeTrack(state, enabled, colors) },
        steps = steps,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeTrack(state: RangeSliderState, enabled: Boolean, colors: YumiSliderColors) {
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
            .drawWithContent {
                drawContent()
                val range = state.valueRange
                val span = range.endInclusive - range.start
                fun fraction(v: Float) = if (span == 0f) 0f else ((v - range.start) / span).coerceIn(0f, 1f)
                val from = fraction(state.activeRangeStart)
                val to = fraction(state.activeRangeEnd)
                val rtl = layoutDirection == LayoutDirection.Rtl
                val left = size.width * (if (rtl) 1f - to else from)
                drawRoundRect(
                    color = active,
                    topLeft = Offset(left, 0f),
                    size = Size(size.width * (to - from), size.height),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
                if (state.steps > 0) {
                    for (i in 0..state.steps + 1) {
                        val x = (size.width * i / (state.steps + 1)).coerceIn(size.height / 2f, size.width - size.height / 2f)
                        drawCircle(tick, radius = 1.5.dp.toPx(), center = Offset(x, size.height / 2f))
                    }
                }
            },
    )
}
