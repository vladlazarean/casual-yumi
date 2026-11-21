package dev.yumi.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import dev.yumi.foundation.tray
import kotlin.math.roundToInt

/** Sizes for [YumiSwitch]. */
public object YumiSwitchDefaults {
    /** Width of the recessed track. */
    public val TrackWidth: Dp = 52.dp

    /** Height of the recessed track. */
    public val TrackHeight: Dp = 32.dp

    /** Diameter of the raised knob. */
    public val KnobSize: Dp = 24.dp

    /** Gap between knob and track edge. */
    public val KnobInset: Dp = 4.dp
}

/**
 * A switch: a recessed track with a raised knob that slides across. The knob carries a small
 * LED that lights in the accent when [checked], like a lit key on the remote.
 *
 * The knob position is read in the layout phase and the LED colour in the draw phase, so the
 * slide never recomposes. RTL-aware: "on" is at the end.
 *
 * @param checked whether the switch is on.
 * @param onCheckedChange called with the new state; `null` makes the switch display-only.
 * @param modifier modifier for the control.
 * @param enabled whether the control responds to input.
 * @param colors colours of the track, knob and LED.
 * @param interactionSource source of press interactions; `null` creates one internally.
 */
@Composable
public fun YumiSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: YumiSelectionColors = YumiSelectionDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val haptics = YumiTheme.haptics
    val motion = YumiTheme.motion
    val depth = YumiTheme.depth
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = if (motion.reduceMotion) snap() else spring(motion.pressDampingRatio, motion.pressStiffness),
        label = "yumiSwitchKnob",
    )
    val sink by animateSinkAsState(source, enabled = enabled)
    val description = stringResource(if (checked) R.string.yumi_state_on else R.string.yumi_state_off)

    val toggleModifier = if (onCheckedChange != null) {
        Modifier
            .toggleable(
                value = checked,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = { on ->
                    haptics.toggle(on)
                    onCheckedChange(on)
                },
            )
            .semantics { stateDescription = description }
            .minimumInteractiveComponentSize()
    } else {
        Modifier
    }

    val ledOn = colors.indicatorOnColor
    val ledOff = colors.indicatorOffColor
    val travel = YumiSwitchDefaults.TrackWidth - YumiSwitchDefaults.KnobSize - YumiSwitchDefaults.KnobInset * 2

    Box(
        modifier = modifier
            .then(toggleModifier)
            .alpha(if (enabled) 1f else DisabledControlAlpha),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(YumiSwitchDefaults.TrackWidth, YumiSwitchDefaults.TrackHeight)
                .tray(colors.wellColor, YumiTheme.shapes.pill, depth.copy(trayInsetRadius = 6.dp, trayInsetOffset = DpOffset(0.dp, 2.dp)))
                .padding(YumiSwitchDefaults.KnobInset),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    // Layout-phase read: RTL-aware, no recomposition while sliding.
                    .offset { IntOffset((travel.toPx() * progress).roundToInt(), 0) }
                    .size(YumiSwitchDefaults.KnobSize)
                    .pebble(
                        color = colors.markColor,
                        shape = CircleShape,
                        seatColor = YumiTheme.colors.seat,
                        depth = depth,
                        sinkOffset = motion.sinkOffset,
                        sink = { sink },
                    )
                    .drawWithContent {
                        drawContent()
                        val w = 8.dp.toPx()
                        val h = 3.dp.toPx()
                        drawRoundRect(
                            color = lerp(ledOff, ledOn, progress),
                            topLeft = Offset((size.width - w) / 2f, (size.height - h) / 2f),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(h / 2f),
                        )
                    },
            )
        }
    }
}
