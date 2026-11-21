package dev.yumi.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.PebbleKey
import dev.yumi.components.internal.flat
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.pebbleClickable
import dev.yumi.foundation.tray
import dev.yumi.tokens.YumiTokens
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** Defaults for [YumiListItem] and [YumiSwipeToDismiss]. */
public object YumiListDefaults {
    /** Minimum height of a one-line item. */
    public val OneLineHeight: Dp = 56.dp

    /** Minimum height of an item with supporting or overline text. */
    public val TwoLineHeight: Dp = 72.dp

    /** Fraction of the width a swipe must pass to dismiss. Same on every list. */
    public const val DismissThreshold: Float = 0.4f

    /** Fling speed (px/s) that dismisses regardless of distance. Same on every list. */
    public const val DismissVelocity: Float = 1_600f
}

/**
 * A list row. Read-only rows sit flat on the body. With [onClick] the whole row is a flat key
 * that dips in when pressed and supports long press (the "long-press to edit" rule).
 *
 * @param headlineContent the main line.
 * @param modifier modifier for the row.
 * @param overlineContent optional small line above the headline.
 * @param supportingContent optional muted line below the headline.
 * @param leadingContent optional content before the text (icon, avatar, checkbox).
 * @param trailingContent optional content after the text (switch, value, chevron).
 * @param onClick makes the row a key; `null` keeps it read-only.
 * @param onLongClickLabel accessibility label for the long-press action.
 * @param onLongClick called on long press.
 * @param enabled whether the row responds to input.
 */
@Composable
public fun YumiListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val minHeight = if (supportingContent != null || overlineContent != null) {
        YumiListDefaults.TwoLineHeight
    } else {
        YumiListDefaults.OneLineHeight
    }
    val colors = YumiTheme.colors
    val alpha = if (enabled) 1f else YumiTokens.DisabledContentAlpha

    val body: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                CompositionLocalProvider(LocalContentColor provides colors.muted.copy(alpha = alpha)) { leadingContent() }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (overlineContent != null) {
                    CompositionLocalProvider(
                        LocalContentColor provides colors.muted.copy(alpha = alpha),
                        LocalTextStyle provides YumiTheme.typography.labelSmall,
                    ) { overlineContent() }
                }
                CompositionLocalProvider(
                    LocalContentColor provides colors.ink.copy(alpha = alpha),
                    LocalTextStyle provides YumiTheme.typography.bodyLarge,
                ) { headlineContent() }
                if (supportingContent != null) {
                    CompositionLocalProvider(
                        LocalContentColor provides colors.muted.copy(alpha = alpha),
                        LocalTextStyle provides YumiTheme.typography.bodyMedium,
                    ) { supportingContent() }
                }
            }
            if (trailingContent != null) {
                CompositionLocalProvider(LocalContentColor provides colors.ink.copy(alpha = alpha)) { trailingContent() }
            }
        }
    }

    if (onClick == null) {
        Box(modifier.fillMaxWidth().heightIn(min = minHeight).semantics(mergeDescendants = true) {}, contentAlignment = Alignment.CenterStart) {
            body()
        }
    } else {
        val source = remember { MutableInteractionSource() }
        PebbleKey(
            input = Modifier.pebbleClickable(
                interactionSource = source,
                enabled = enabled,
                role = Role.Button,
                haptics = YumiTheme.haptics,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick,
            ),
            interactionSource = source,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            shape = YumiTheme.shapes.small,
            containerColor = Color.Transparent,
            contentColor = colors.ink,
            seatColor = Color.Transparent,
            depth = YumiTheme.depth.flat(),
            minHeight = minHeight,
        ) { body() }
    }
}

/**
 * Swipe a row toward the start edge to dismiss it.
 *
 * The behaviour is deliberately not configurable (coherence: swipe works the same on every
 * list): one direction, [YumiListDefaults.DismissThreshold] of the width or a fast fling, a
 * recessed tray revealed underneath with [dismissLabel] and an LED that lights once the swipe is
 * armed (with a haptic tick). TalkBack gets a custom action with the same label, so dismissing
 * never depends on the gesture.
 *
 * Remove the item from your list in [onDismiss]. If you keep it (e.g. to offer Undo), it springs
 * back to rest.
 *
 * @param onDismiss called once the row has animated out.
 * @param modifier modifier for the row.
 * @param dismissLabel what dismissing does, e.g. "Delete" or "Archive".
 * @param enabled whether swiping is allowed.
 * @param content the row, usually a [YumiListItem].
 */
@Composable
public fun YumiSwipeToDismiss(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(R.string.yumi_delete),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptics = YumiTheme.haptics
    val motion = YumiTheme.motion
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    // Logical offset: 0 at rest, negative toward the start edge. Modifier.offset mirrors it in RTL.
    val offset = remember { Animatable(0f) }
    var width by remember { mutableFloatStateOf(0f) }
    var armed by remember { mutableStateOf(false) }
    // Synchronous running total of the drag: offset.snapTo is launched, so offset.value lags.
    val dragTotal = remember { DragTotal() }

    fun settle(velocity: Float) {
        scope.launch {
            val logicalVelocity = if (rtl) -velocity else velocity
            val dismiss = width > 0f && (
                abs(offset.value) > width * YumiListDefaults.DismissThreshold ||
                    logicalVelocity < -YumiListDefaults.DismissVelocity
                )
            if (dismiss) {
                offset.animateTo(-width, if (motion.reduceMotion) snap() else spring(stiffness = 600f))
                currentOnDismiss()
                offset.snapTo(0f)
            } else {
                offset.animateTo(0f, if (motion.reduceMotion) snap() else spring(motion.pressDampingRatio, motion.pressStiffness))
            }
            armed = false
        }
    }

    val dragState = rememberDraggableState { delta ->
        val logical = if (rtl) -delta else delta
        val next = (dragTotal.value + logical).coerceIn(-width, 0f)
        dragTotal.value = next
        scope.launch { offset.snapTo(next) }
        val nowArmed = abs(next) > width * YumiListDefaults.DismissThreshold
        if (nowArmed != armed) {
            armed = nowArmed
            if (nowArmed) haptics.tick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { width = it.width.toFloat() }
            .semantics {
                customActions = listOf(CustomAccessibilityAction(dismissLabel) { currentOnDismiss(); true })
            },
    ) {
        // What's underneath: recessed, because it is information about the action.
        Row(
            modifier = Modifier
                .matchParentSize()
                .tray(YumiTheme.colors.tray, RectangleShape, YumiTheme.depth)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YumiLed(on = armed)
            Text(
                dismissLabel,
                style = YumiTheme.typography.labelLarge,
                // Ink, not error red: red reaches only 4.1:1 on the Putty tray. The LED carries "armed".
                color = if (armed) YumiTheme.colors.ink else YumiTheme.colors.muted,
            )
        }
        Box(
            Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .background(YumiTheme.colors.body)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = enabled,
                    onDragStarted = { dragTotal.value = offset.value },
                    onDragStopped = { velocity -> settle(velocity) },
                ),
        ) { content() }
    }
}

private class DragTotal {
    var value: Float = 0f
}
