package dev.yumi.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import dev.yumi.foundation.tray

/** Sizes for [YumiCheckbox] and [YumiTriStateCheckbox]. */
public object YumiCheckboxDefaults {
    /** Size of the recessed well. */
    public val WellSize: Dp = 24.dp

    /** Size of the raised mark inside the well. */
    public val MarkSize: Dp = 18.dp
}

/**
 * A checkbox: a recessed well that holds a raised mark with an accent-ink check when [checked].
 *
 * Pass `onCheckedChange = null` when the whole row is toggleable (the row then owns semantics and
 * the 48 dp target), as with Material's `Checkbox`.
 *
 * @param checked whether the box is checked.
 * @param onCheckedChange called with the new state; `null` makes the box display-only.
 * @param modifier modifier for the control.
 * @param enabled whether the control responds to input.
 * @param colors colours of the well, mark and glyph.
 * @param interactionSource source of press interactions; `null` creates one internally.
 */
@Composable
public fun YumiCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: YumiSelectionColors = YumiSelectionDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    YumiTriStateCheckbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = onCheckedChange?.let { callback -> { callback(!checked) } },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}

/**
 * A checkbox with an indeterminate state (a dash), for "select all" parents.
 *
 * @param state current state.
 * @param onClick called on click; `null` makes the box display-only.
 * @param modifier modifier for the control.
 * @param enabled whether the control responds to input.
 * @param colors colours of the well, mark and glyph.
 * @param interactionSource source of press interactions; `null` creates one internally.
 */
@Composable
public fun YumiTriStateCheckbox(
    state: ToggleableState,
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

    val markProgress by animateFloatAsState(
        targetValue = if (state == ToggleableState.Off) 0f else 1f,
        animationSpec = if (motion.reduceMotion) snap() else spring(motion.pressDampingRatio, motion.pressStiffness),
        label = "yumiCheckMark",
    )
    val sink by animateSinkAsState(source, enabled = enabled)
    val description = stringResource(
        when (state) {
            ToggleableState.On -> R.string.yumi_state_checked
            ToggleableState.Off -> R.string.yumi_state_unchecked
            ToggleableState.Indeterminate -> R.string.yumi_state_indeterminate
        },
    )

    val toggleModifier = if (onClick != null) {
        Modifier
            .triStateToggleable(
                state = state,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onClick = {
                    haptics.toggle(state != ToggleableState.On)
                    onClick()
                },
            )
            .semantics { stateDescription = description }
            .minimumInteractiveComponentSize()
    } else {
        Modifier
    }

    val glyphColor = colors.glyphColor
    val indeterminate = state == ToggleableState.Indeterminate
    Box(
        modifier = modifier
            .then(toggleModifier)
            .alpha(if (enabled) 1f else DisabledControlAlpha),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(YumiCheckboxDefaults.WellSize)
                .tray(colors.wellColor, WellShape, depth.copy(trayInsetRadius = 4.dp, trayInsetOffset = DpOffset(0.dp, 1.5.dp))),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(YumiCheckboxDefaults.MarkSize)
                    .graphicsLayer {
                        alpha = markProgress
                        scaleX = 0.6f + 0.4f * markProgress
                        scaleY = 0.6f + 0.4f * markProgress
                    }
                    .pebble(
                        color = colors.markColor,
                        shape = MarkShape,
                        seatColor = YumiTheme.colors.seat,
                        depth = depth,
                        sinkOffset = motion.sinkOffset,
                        sink = { sink },
                    )
                    .drawWithContent {
                        drawContent()
                        val w = size.width
                        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        if (indeterminate) {
                            drawLine(glyphColor, Offset(w * 0.28f, w * 0.5f), Offset(w * 0.72f, w * 0.5f), stroke.width, StrokeCap.Round)
                        } else {
                            val path = Path().apply {
                                moveTo(w * 0.26f, w * 0.52f)
                                lineTo(w * 0.43f, w * 0.68f)
                                lineTo(w * 0.75f, w * 0.34f)
                            }
                            drawPath(path, glyphColor, style = stroke)
                        }
                    },
            )
        }
    }
}

private val WellShape = RoundedCornerShape(7.dp)
private val MarkShape = RoundedCornerShape(5.dp)
