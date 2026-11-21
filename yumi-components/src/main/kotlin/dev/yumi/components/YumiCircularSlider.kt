package dev.yumi.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.activeArc
import dev.yumi.components.internal.clockAngle
import dev.yumi.components.internal.distance
import dev.yumi.components.internal.fractionOnArc
import dev.yumi.components.internal.pointAt
import dev.yumi.components.internal.recessedArc
import dev.yumi.components.internal.snapToSteps
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import kotlin.math.roundToInt

/** Defaults for [YumiCircularSlider]. */
public object YumiCircularSliderDefaults {
    /** Default diameter. */
    public val Size: Dp = 200.dp

    /** Width of the recessed track. */
    public val TrackWidth: Dp = 14.dp

    /** Clock angle where the arc starts (225 = about 7:30, bottom-left). */
    public const val StartDegrees: Float = 225f

    /** Length of the arc in degrees; the gap sits at the bottom, like a volume knob. */
    public const val SweepDegrees: Float = 270f
}

/**
 * A circular slider: a 270° recessed arc with the gap at the bottom, a raised thumb and an accent
 * arc up to the value, like a volume knob.
 *
 * Drag anywhere on the ring or tap it to jump. Arrow keys / D-pad adjust when focused (focus draws
 * an accent-ink ring). Exposes `progressBarRangeInfo` and `setProgress` like a linear slider.
 * Not mirrored in RTL: a dial turns clockwise everywhere.
 *
 * @param value current value, within [valueRange].
 * @param onValueChange called while the value changes.
 * @param modifier modifier for the slider; defaults to [YumiCircularSliderDefaults.Size].
 * @param enabled whether the slider responds to input.
 * @param valueRange range of values.
 * @param steps number of discrete steps between the ends; 0 = continuous.
 * @param onValueChangeFinished called when a drag or key press ends.
 * @param colors colours, shared with [YumiSlider].
 * @param content optional centre content, e.g. the value as text.
 */
@Composable
public fun YumiCircularSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: YumiSliderColors = YumiSliderDefaults.colors(),
    content: @Composable BoxScope.() -> Unit = {},
) {
    val haptics = YumiTheme.haptics
    val seat = YumiTheme.colors.seat
    val focusRing = YumiTheme.colors.accentInk
    val currentValue by rememberUpdatedState(value)
    val currentOnChange by rememberUpdatedState(onValueChange)
    val currentOnFinished by rememberUpdatedState(onValueChangeFinished)
    val focusSource = remember { MutableInteractionSource() }
    val focused by focusSource.collectIsFocusedAsState()
    var dragging by remember { mutableStateOf(false) }
    val thumbSource = remember { MutableInteractionSource() }
    val sink by animateSinkAsState(thumbSource, enabled = enabled, selected = dragging)
    val lastStep = remember(steps, valueRange) { StepHolder(stepIndex(value, valueRange, steps)) }

    val span = valueRange.endInclusive - valueRange.start
    fun fractionOf(v: Float) = if (span == 0f) 0f else ((v - valueRange.start) / span).coerceIn(0f, 1f)

    fun emit(newValue: Float) {
        val snapped = snapToSteps(newValue, valueRange, steps)
        if (snapped == currentValue) return
        val step = stepIndex(snapped, valueRange, steps)
        if (steps > 0 && step != lastStep.index) {
            lastStep.index = step
            haptics.tick()
        }
        currentOnChange(snapped)
    }

    val keyStep = if (steps > 0) span / (steps + 1) else span / 100f

    Box(
        modifier = modifier
            .size(YumiCircularSliderDefaults.Size)
            .alpha(if (enabled) 1f else DisabledControlAlpha)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(value, valueRange, steps)
                if (enabled) {
                    setProgress { target -> emit(target); currentOnFinished?.invoke(); true }
                } else {
                    disabled()
                }
            }
            .onKeyEvent { event ->
                if (!enabled || event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val delta = when (event.key) {
                    Key.DirectionRight, Key.DirectionUp -> keyStep
                    Key.DirectionLeft, Key.DirectionDown -> -keyStep
                    else -> return@onKeyEvent false
                }
                emit(currentValue + delta)
                currentOnFinished?.invoke()
                true
            }
            .focusable(enabled, focusSource)
            .pointerInput(enabled, valueRange, steps) {
                if (!enabled) return@pointerInput
                val trackWidth = YumiCircularSliderDefaults.TrackWidth.toPx()
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - trackWidth
                    // Only the ring is the target; the centre stays free for content.
                    if (distance(down.position, center) < radius - trackWidth * 1.5f) return@awaitEachGesture
                    dragging = true
                    down.consume()
                    fun update(position: Offset) {
                        val fraction = fractionOnArc(
                            clockAngle(position, center),
                            YumiCircularSliderDefaults.StartDegrees,
                            YumiCircularSliderDefaults.SweepDegrees,
                        )
                        emit(valueRange.start + fraction * span)
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
                    dragging = false
                    currentOnFinished?.invoke()
                }
            }
            .drawBehind {
                val trackWidth = YumiCircularSliderDefaults.TrackWidth.toPx()
                val radius = size.minDimension / 2f - trackWidth
                val center = Offset(size.width / 2f, size.height / 2f)
                if (focused) {
                    recessedArc(
                        center, radius, trackWidth + 4.dp.toPx(),
                        YumiCircularSliderDefaults.StartDegrees, YumiCircularSliderDefaults.SweepDegrees,
                        focusRing, focusRing,
                    )
                }
                recessedArc(
                    center, radius, trackWidth,
                    YumiCircularSliderDefaults.StartDegrees, YumiCircularSliderDefaults.SweepDegrees,
                    colors.trackColor, seat,
                )
                activeArc(
                    center, radius, trackWidth,
                    YumiCircularSliderDefaults.StartDegrees,
                    YumiCircularSliderDefaults.SweepDegrees * fractionOf(currentValue),
                    if (enabled) colors.activeTrackColor else colors.disabledActiveTrackColor,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
        // Raised thumb, placed in the layout phase from the parent's size: moving it never recomposes.
        Box(
            Modifier
                .matchParentSize()
                .layout { measurable, constraints ->
                    val thumbPx = YumiSliderDefaults.ThumbSize.roundToPx()
                    val placeable = measurable.measure(Constraints.fixed(thumbPx, thumbPx))
                    val w = constraints.maxWidth
                    val h = constraints.maxHeight
                    layout(w, h) {
                        val trackWidth = YumiCircularSliderDefaults.TrackWidth.toPx()
                        val radius = minOf(w, h) / 2f - trackWidth
                        val angle = YumiCircularSliderDefaults.StartDegrees +
                            YumiCircularSliderDefaults.SweepDegrees * fractionOf(currentValue)
                        val p = pointAt(Offset(w / 2f, h / 2f), radius, angle)
                        placeable.place((p.x - thumbPx / 2f).roundToInt(), (p.y - thumbPx / 2f).roundToInt())
                    }
                }
                .pebble(
                    color = colors.thumbColor,
                    shape = CircleShape,
                    seatColor = seat,
                    depth = YumiTheme.depth,
                    sinkOffset = YumiTheme.motion.sinkOffset,
                    sink = { sink },
                ),
        )
    }
}
