package dev.yumi.foundation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/**
 * Press progress for a pebble: 0 = raised, 1 = sunk.
 *
 * Sinks while pressed, rests sunk when disabled ("can't press" looks pushed in and dead) or when
 * [selected] (selected chips, segments and tabs stay down). Uses the theme's press spring, or
 * an instant change when reduced motion is on.
 *
 * Read the returned state only inside draw lambdas (e.g. [pebble]'s `sink`) so the animation
 * never recomposes. Only the press start / end recompose the caller.
 *
 * @param interactionSource the source the clickable reports presses to.
 * @param enabled `false` rests the pebble sunk.
 * @param selected `true` rests the pebble sunk.
 */
@Composable
public fun animateSinkAsState(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    selected: Boolean = false,
): State<Float> {
    val pressed by interactionSource.collectIsPressedAsState()
    val motion = YumiTheme.motion
    return animateFloatAsState(
        targetValue = if (!enabled || selected || pressed) 1f else 0f,
        animationSpec = if (motion.reduceMotion) {
            snap()
        } else {
            spring(dampingRatio = motion.pressDampingRatio, stiffness = motion.pressStiffness)
        },
        label = "yumiSink",
    )
}

/**
 * Click + long-click handling for pebbles, with no indication: the press feedback is the sink,
 * not a ripple. Long-press haptics follow [YumiHaptics.enabled].
 *
 * Pass the same [interactionSource] to [animateSinkAsState].
 *
 * @param interactionSource source that receives press interactions.
 * @param enabled whether the control responds to input.
 * @param role accessibility role; every Yumi control sets one.
 * @param haptics haptics to use; normally `YumiTheme.haptics`.
 * @param onClickLabel accessibility label for the click action.
 * @param onLongClickLabel accessibility label for the long-click action.
 * @param onLongClick called on long press; `null` disables long press.
 * @param onClick called on click.
 */
public fun Modifier.pebbleClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    role: Role,
    haptics: YumiHaptics,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = combinedClickable(
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    onClickLabel = onClickLabel,
    role = role,
    onLongClickLabel = onLongClickLabel,
    onLongClick = onLongClick,
    hapticFeedbackEnabled = haptics.enabled,
    onClick = onClick,
)
