package dev.yumi.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.activeArc
import dev.yumi.components.internal.clockAngle
import dev.yumi.components.internal.distance
import dev.yumi.components.internal.pointAt
import dev.yumi.components.internal.recessedArc
import dev.yumi.components.internal.rollHourOnMinuteWrap
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import dev.yumi.foundation.pebbleClickable
import dev.yumi.foundation.tray
import kotlin.math.abs
import kotlin.math.roundToInt

/** Defaults for [YumiTimeDial]. */
public object YumiTimeDialDefaults {
    /** Default diameter. */
    public val Size: Dp = 280.dp

    /** Width of each recessed ring. */
    public val RingWidth: Dp = 20.dp

    /** Gap between the minute ring and the hour ring. */
    public val RingGap: Dp = 14.dp

    /** Formats the centre readout. */
    public fun format(hour: Int, minute: Int): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

/**
 * The D-pad dial from the remote: two concentric rings.
 *
 * - Inner ring: hours, 0–23. Outer ring: minutes, 0–59. Both start at 12 o'clock.
 * - Dragging the minute thumb past 00 rolls the hour forward (or back), like a real clock.
 * - The centre key confirms ([onConfirm]); it is the one raised key allowed inside another
 *   control. Without [onConfirm] the centre is a recessed read-only readout.
 *
 * Setting a time is at most three actions: drag hours, drag minutes, press the centre.
 * Each ring is its own accessibility node ("Hours", "Minutes") with `setProgress`, and its own
 * focus stop for arrow keys. Haptic tick on every hour and minute.
 *
 * @param hour current hour, 0..23.
 * @param minute current minute, 0..59.
 * @param onTimeChange called with the new hour and minute.
 * @param modifier modifier for the dial; defaults to [YumiTimeDialDefaults.Size].
 * @param enabled whether the dial responds to input.
 * @param onConfirm called when the centre key is pressed; `null` makes the centre read-only.
 * @param colors ring and thumb colours, shared with [YumiSlider].
 */
@Composable
public fun YumiTimeDial(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onConfirm: (() -> Unit)? = null,
    colors: YumiSliderColors = YumiSliderDefaults.colors(),
) {
    val haptics = YumiTheme.haptics
    val seat = YumiTheme.colors.seat
    val focusColor = YumiTheme.colors.accentInk
    val currentHour by rememberUpdatedState(hour.coerceIn(0, 23))
    val currentMinute by rememberUpdatedState(minute.coerceIn(0, 59))
    val currentOnChange by rememberUpdatedState(onTimeChange)
    val hourFocus = remember { MutableInteractionSource() }
    val minuteFocus = remember { MutableInteractionSource() }
    val hourFocused by hourFocus.collectIsFocusedAsState()
    val minuteFocused by minuteFocus.collectIsFocusedAsState()
    var draggingRing by remember { mutableStateOf<Ring?>(null) }
    val hoursLabel = stringResource(R.string.yumi_hours)
    val minutesLabel = stringResource(R.string.yumi_minutes)

    fun setTime(newHour: Int, newMinute: Int) {
        val h = ((newHour % 24) + 24) % 24
        val m = ((newMinute % 60) + 60) % 60
        if (h == currentHour && m == currentMinute) return
        haptics.tick()
        currentOnChange(h, m)
    }

    fun setMinuteFromRing(newMinute: Int) {
        // Wrap rule lives in one place (rollHourOnMinuteWrap) so drag, keys and TalkBack agree.
        setTime(rollHourOnMinuteWrap(currentHour, currentMinute, newMinute), newMinute)
    }

    BoxWithConstraints(
        modifier = modifier
            .size(YumiTimeDialDefaults.Size)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val geometry = DialGeometry(size.width.toFloat(), this@pointerInput)
                    val d = distance(down.position, geometry.center)
                    // Pick the ring nearest the touch; the centre belongs to the centre key.
                    val ring = when {
                        abs(d - geometry.minuteRadius) <= geometry.ringWidth -> Ring.Minutes
                        abs(d - geometry.hourRadius) <= geometry.ringWidth -> Ring.Hours
                        else -> null
                    } ?: return@awaitEachGesture
                    draggingRing = ring
                    down.consume()
                    fun update(position: Offset) {
                        val angle = clockAngle(position, geometry.center)
                        when (ring) {
                            Ring.Hours -> setTime((angle / 15f).roundToInt() % 24, currentMinute)
                            Ring.Minutes -> setMinuteFromRing((angle / 6f).roundToInt() % 60)
                        }
                    }
                    update(down.position)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                update(change.position)
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    draggingRing = null
                }
            }
            .drawBehind {
                val g = DialGeometry(size.minDimension, this)
                val hourSweep = currentHour / 24f * 360f
                val minuteSweep = currentMinute / 60f * 360f
                if (minuteFocused) {
                    recessedArc(g.center, g.minuteRadius, g.ringWidth + 4.dp.toPx(), 0f, 360f, focusColor, focusColor)
                }
                if (hourFocused) {
                    recessedArc(g.center, g.hourRadius, g.ringWidth + 4.dp.toPx(), 0f, 360f, focusColor, focusColor)
                }
                recessedArc(g.center, g.minuteRadius, g.ringWidth, 0f, 360f, colors.trackColor, seat)
                recessedArc(g.center, g.hourRadius, g.ringWidth, 0f, 360f, colors.trackColor, seat)
                // Quarter marks on both rings.
                for (q in 0 until 4) {
                    drawCircle(colors.tickColor, 1.5.dp.toPx(), pointAt(g.center, g.minuteRadius, q * 90f))
                    drawCircle(colors.tickColor, 1.5.dp.toPx(), pointAt(g.center, g.hourRadius, q * 90f))
                }
                val active = if (enabled) colors.activeTrackColor else colors.disabledActiveTrackColor
                activeArc(g.center, g.minuteRadius, g.ringWidth, 0f, minuteSweep, active)
                activeArc(g.center, g.hourRadius, g.ringWidth, 0f, hourSweep, active)
            },
        contentAlignment = Alignment.Center,
    ) {
        val dialSize = minOf(maxWidth, maxHeight)
        val centerSize = dialSize - (YumiTimeDialDefaults.RingWidth * 2 + YumiTimeDialDefaults.RingGap) * 2 - 56.dp

        // Accessibility + keyboard layers, one per ring (no visuals of their own).
        RingNode(
            label = hoursLabel,
            value = currentHour,
            range = 0..23,
            enabled = enabled,
            focusSource = hourFocus,
            onSet = { setTime(it, currentMinute) },
        )
        RingNode(
            label = minutesLabel,
            value = currentMinute,
            range = 0..59,
            enabled = enabled,
            focusSource = minuteFocus,
            onSet = { setMinuteFromRing(it) },
        )

        DialThumb(Ring.Minutes, { currentMinute / 60f * 360f }, draggingRing == Ring.Minutes, enabled, colors)
        DialThumb(Ring.Hours, { currentHour / 24f * 360f }, draggingRing == Ring.Hours, enabled, colors)

        CenterKey(
            text = YumiTimeDialDefaults.format(currentHour, currentMinute),
            size = centerSize,
            enabled = enabled,
            onConfirm = onConfirm,
        )
    }
}

private enum class Ring { Hours, Minutes }

/** Ring radii for a dial of [size] px. Thumbs overhang the rings by 4 dp. */
private class DialGeometry(size: Float, density: Density) {
    val ringWidth: Float = with(density) { YumiTimeDialDefaults.RingWidth.toPx() }
    private val gap: Float = with(density) { YumiTimeDialDefaults.RingGap.toPx() }
    private val overhang: Float = with(density) { 4.dp.toPx() }
    val center: Offset = Offset(size / 2f, size / 2f)
    val minuteRadius: Float = size / 2f - overhang - ringWidth / 2f
    val hourRadius: Float = minuteRadius - ringWidth - gap
}

@Composable
private fun BoxScope.RingNode(
    label: String,
    value: Int,
    range: IntRange,
    enabled: Boolean,
    focusSource: MutableInteractionSource,
    onSet: (Int) -> Unit,
) {
    val currentValue by rememberUpdatedState(value)
    Box(
        Modifier
            .matchParentSize()
            .semantics {
                contentDescription = label
                stateDescription = value.toString().padStart(2, '0')
                progressBarRangeInfo = ProgressBarRangeInfo(
                    value.toFloat(),
                    range.first.toFloat()..range.last.toFloat(),
                    steps = range.last - range.first - 1,
                )
                if (enabled) setProgress { target -> onSet(target.roundToInt().coerceIn(range)); true }
            }
            .onKeyEvent { event ->
                if (!enabled || event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionRight, Key.DirectionUp -> { onSet(currentValue + 1); true }
                    Key.DirectionLeft, Key.DirectionDown -> { onSet(currentValue - 1); true }
                    else -> false
                }
            }
            .focusable(enabled, focusSource),
    )
}

@Composable
private fun BoxScope.DialThumb(
    ring: Ring,
    degrees: () -> Float,
    dragging: Boolean,
    enabled: Boolean,
    colors: YumiSliderColors,
) {
    val source = remember { MutableInteractionSource() }
    val sink by animateSinkAsState(source, enabled = enabled, selected = dragging)
    Box(
        Modifier
            .matchParentSize()
            .layout { measurable, constraints ->
                val thumbPx = YumiSliderDefaults.ThumbSize.roundToPx()
                val placeable = measurable.measure(Constraints.fixed(thumbPx, thumbPx))
                val w = constraints.maxWidth
                val h = constraints.maxHeight
                layout(w, h) {
                    val g = DialGeometry(minOf(w, h).toFloat(), this@layout)
                    val radius = if (ring == Ring.Minutes) g.minuteRadius else g.hourRadius
                    val p = pointAt(Offset(w / 2f, h / 2f), radius, degrees())
                    placeable.place((p.x - thumbPx / 2f).roundToInt(), (p.y - thumbPx / 2f).roundToInt())
                }
            }
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

@Composable
private fun CenterKey(text: String, size: Dp, enabled: Boolean, onConfirm: (() -> Unit)?) {
    val colors = YumiTheme.colors
    val base = if (size < 104.dp) YumiTheme.typography.titleMedium else YumiTheme.typography.headlineMedium
    val style = base.copy(fontFeatureSettings = "tnum")
    if (onConfirm == null) {
        // Read-only: recessed, because you read it.
        Box(
            Modifier.size(size).tray(colors.tray, CircleShape, YumiTheme.depth),
            contentAlignment = Alignment.Center,
        ) { Text(text, style = style, color = colors.ink, maxLines = 1) }
        return
    }
    val source = remember { MutableInteractionSource() }
    val haptics = YumiTheme.haptics
    val sink by animateSinkAsState(source, enabled = enabled)
    Box(
        Modifier
            .size(size)
            .pebble(
                color = colors.raised,
                shape = CircleShape,
                seatColor = colors.seat,
                depth = YumiTheme.depth,
                sinkOffset = YumiTheme.motion.sinkOffset,
                sink = { sink },
            )
            .pebbleClickable(
                interactionSource = source,
                enabled = enabled,
                role = Role.Button,
                haptics = haptics,
                onClick = {
                    haptics.confirm()
                    onConfirm()
                },
            ),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = style, color = colors.ink, maxLines = 1) }
}
