package dev.yumi.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import dev.yumi.foundation.tray
import dev.yumi.tokens.YumiTokens

/** Sizes for [YumiRadioButton]. */
public object YumiRadioButtonDefaults {
    /** Diameter of the recessed well. */
    public val WellSize: Dp = 24.dp

    /** Diameter of the raised knob. */
    public val KnobSize: Dp = 14.dp

    /** Diameter of the lit dot on the knob. */
    public val DotSize: Dp = 5.dp
}

/**
 * A radio button: a round recessed well that holds a raised knob with a lit dot when [selected].
 *
 * Pass `onClick = null` when the row is selectable (see [YumiRadioGroup]).
 *
 * @param selected whether this option is selected.
 * @param onClick called on click; `null` makes the button display-only.
 * @param modifier modifier for the control.
 * @param enabled whether the control responds to input.
 * @param colors colours of the well, knob and dot.
 * @param interactionSource source of press interactions; `null` creates one internally.
 */
@Composable
public fun YumiRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: YumiSelectionColors = YumiSelectionDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val haptics = YumiTheme.haptics
    val motion = YumiTheme.motion
    val depth = YumiTheme.depth
    val knobProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = if (motion.reduceMotion) snap() else spring(motion.pressDampingRatio, motion.pressStiffness),
        label = "yumiRadioKnob",
    )
    val sink by animateSinkAsState(source, enabled = enabled)
    val description = stringResource(if (selected) R.string.yumi_state_selected else R.string.yumi_state_not_selected)

    val selectModifier = if (onClick != null) {
        Modifier
            .selectable(
                selected = selected,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = {
                    if (!selected) haptics.toggle(true)
                    onClick()
                },
            )
            .semantics { stateDescription = description }
            .minimumInteractiveComponentSize()
    } else {
        Modifier
    }

    val dotColor = colors.indicatorOnColor
    Box(
        modifier = modifier
            .then(selectModifier)
            .alpha(if (enabled) 1f else DisabledControlAlpha),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(YumiRadioButtonDefaults.WellSize)
                .tray(colors.wellColor, CircleShape, depth.copy(trayInsetRadius = 4.dp, trayInsetOffset = DpOffset(0.dp, 1.5.dp))),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(YumiRadioButtonDefaults.KnobSize)
                    .graphicsLayer {
                        alpha = knobProgress
                        scaleX = 0.5f + 0.5f * knobProgress
                        scaleY = 0.5f + 0.5f * knobProgress
                    }
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
                        drawCircle(dotColor, radius = YumiRadioButtonDefaults.DotSize.toPx() / 2f)
                    },
            )
        }
    }
}

/**
 * A vertical group of radio options. Each whole row is the touch target and the accessibility
 * node (not just the 24 dp well), and the column is a `selectableGroup` so TalkBack announces
 * "1 of 3".
 *
 * @param options the options, in display order.
 * @param selected the selected option, or `null` for none.
 * @param onSelect called with the option the user picked.
 * @param modifier modifier for the group.
 * @param enabled whether the group responds to input.
 * @param colors colours of each radio button.
 * @param label content for one option, usually a `Text`.
 */
@Composable
public fun <T> YumiRadioGroup(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: YumiSelectionColors = YumiSelectionDefaults.colors(),
    label: @Composable (T) -> Unit,
) {
    val haptics = YumiTheme.haptics
    Column(modifier.selectableGroup()) {
        options.forEach { option ->
            val isSelected = option == selected
            val source = remember(option) { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = YumiTokens.SecondaryMinHeight)
                    .selectable(
                        selected = isSelected,
                        interactionSource = source,
                        indication = null,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = {
                            if (!isSelected) haptics.toggle(true)
                            onSelect(option)
                        },
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                YumiRadioButton(
                    selected = isSelected,
                    onClick = null,
                    enabled = enabled,
                    colors = colors,
                    interactionSource = source,
                )
                CompositionLocalProvider(
                    LocalContentColor provides YumiTheme.colors.ink.copy(alpha = if (enabled) 1f else DisabledControlAlpha),
                    LocalTextStyle provides YumiTheme.typography.bodyLarge,
                ) {
                    label(option)
                }
            }
        }
    }
}
