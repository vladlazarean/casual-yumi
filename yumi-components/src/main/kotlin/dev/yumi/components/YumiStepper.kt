package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.tray
import dev.yumi.tokens.YumiTokens
import kotlin.math.roundToInt

/** Defaults for [YumiStepper]. */
public object YumiStepperDefaults {
    /** Minimum width of the recessed readout. */
    public val ReadoutMinWidth: Dp = 72.dp

    /** How many steps a long press jumps. */
    public const val LongPressMultiplier: Int = 10
}

/**
 * A stepper: − key, recessed readout, + key. The readout is recessed because you read it; the
 * keys are raised because you press them. Long-press a key to jump [YumiStepperDefaults.LongPressMultiplier]
 * steps. Keys disable at the range ends.
 *
 * The readout is an adjustable accessibility node (`progressBarRangeInfo` + `setProgress`), so
 * TalkBack users can swipe the value instead of hunting for the keys.
 *
 * @param value current value, within [range].
 * @param onValueChange called with the new value.
 * @param modifier modifier for the stepper.
 * @param range allowed values.
 * @param step amount per press.
 * @param enabled whether the stepper responds to input.
 * @param label accessibility label of the readout, e.g. "Snooze minutes".
 * @param valueText formats the readout.
 */
@Composable
public fun YumiStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..99,
    step: Int = 1,
    enabled: Boolean = true,
    label: String? = null,
    valueText: (Int) -> String = { it.toString() },
) {
    require(step > 0) { "step must be positive" }
    val haptics = YumiTheme.haptics
    val currentValue by rememberUpdatedState(value)
    val currentOnChange by rememberUpdatedState(onValueChange)

    fun change(delta: Int) {
        val next = (currentValue + delta).coerceIn(range)
        if (next != currentValue) {
            haptics.tick()
            currentOnChange(next)
        }
    }

    val decrease = stringResource(R.string.yumi_decrease)
    val increase = stringResource(R.string.yumi_increase)
    val text = valueText(value)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        YumiIconButton(
            onClick = { change(-step) },
            onLongClick = { change(-step * YumiStepperDefaults.LongPressMultiplier) },
            enabled = enabled && value > range.first,
            modifier = Modifier.semantics { contentDescription = decrease },
        ) { GlyphIcon(Glyph.Minus) }

        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = YumiStepperDefaults.ReadoutMinWidth)
                .height(YumiTokens.IconPebbleSize)
                .tray(YumiTheme.colors.tray, YumiTheme.shapes.small, YumiTheme.depth.copy(trayInsetRadius = 6.dp))
                .clearAndSetSemantics {
                    if (label != null) contentDescription = label
                    stateDescription = text
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        value.toFloat(),
                        range.first.toFloat()..range.last.toFloat(),
                        steps = ((range.last - range.first) / step - 1).coerceAtLeast(0),
                    )
                    if (enabled) {
                        setProgress { target ->
                            val snapped = range.first + ((target - range.first) / step).roundToInt() * step
                            change(snapped - currentValue)
                            true
                        }
                    } else {
                        disabled()
                    }
                }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text,
                style = YumiTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                maxLines = 1,
                color = YumiTheme.colors.ink.copy(alpha = if (enabled) 1f else YumiTokens.DisabledContentAlpha),
            )
        }

        YumiIconButton(
            onClick = { change(step) },
            onLongClick = { change(step * YumiStepperDefaults.LongPressMultiplier) },
            enabled = enabled && value < range.last,
            modifier = Modifier.semantics { contentDescription = increase },
        ) { GlyphIcon(Glyph.Plus) }
    }
}
