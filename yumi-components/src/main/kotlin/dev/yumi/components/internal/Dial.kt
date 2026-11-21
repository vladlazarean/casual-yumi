package dev.yumi.components.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

// Dial geometry. All angles are "clock degrees": 0 = 12 o'clock, increasing clockwise.

/** Clock angle of [position] around [center], in 0 until 360. */
internal fun clockAngle(position: Offset, center: Offset): Float {
    val dx = position.x - center.x
    val dy = position.y - center.y
    val degrees = (atan2(dx, -dy) * 180.0 / PI).toFloat()
    return (degrees + 360f) % 360f
}

internal fun distance(position: Offset, center: Offset): Float = hypot(position.x - center.x, position.y - center.y)

/** Point on a circle of [radius] around [center] at clock angle [degrees]. */
internal fun pointAt(center: Offset, radius: Float, degrees: Float): Offset {
    val radians = (degrees - 90f) * PI.toFloat() / 180f
    return Offset(center.x + radius * cos(radians), center.y + radius * sin(radians))
}

/**
 * Fraction 0..1 along an arc that starts at [startDegrees] and sweeps [sweepDegrees] clockwise.
 * Angles in the gap snap to the nearer end.
 */
internal fun fractionOnArc(angle: Float, startDegrees: Float, sweepDegrees: Float): Float {
    val relative = (angle - startDegrees + 360f) % 360f
    if (relative <= sweepDegrees) return relative / sweepDegrees
    val gap = 360f - sweepDegrees
    return if (relative - sweepDegrees < gap / 2f) 1f else 0f
}

/** Snaps [value] to the nearest of [steps] + 2 evenly spaced stops; continuous when steps == 0. */
internal fun snapToSteps(value: Float, range: ClosedFloatingPointRange<Float>, steps: Int): Float {
    val clamped = value.coerceIn(range.start, range.endInclusive)
    if (steps <= 0) return clamped
    val span = range.endInclusive - range.start
    if (span == 0f) return range.start
    val segments = steps + 1
    val index = (((clamped - range.start) / span) * segments).roundToInt()
    return range.start + span * index / segments
}

/**
 * Minute ring wrap: dragging the minute thumb past 00 rolls the hour, like a real clock.
 * Returns the new hour (0..23).
 */
internal fun rollHourOnMinuteWrap(hour: Int, previousMinute: Int, newMinute: Int): Int = when {
    previousMinute >= 45 && newMinute < 15 -> (hour + 1) % 24
    previousMinute < 15 && newMinute >= 45 -> (hour + 23) % 24
    else -> hour
}

/**
 * A recessed ring track: the well colour, plus seat hairlines on both edges so it reads as cut
 * into the body. [sweepDegrees] of 360 draws a full ring.
 */
internal fun DrawScope.recessedArc(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    startDegrees: Float,
    sweepDegrees: Float,
    trackColor: Color,
    seatColor: Color,
) {
    val full = sweepDegrees >= 360f
    val cap = if (full) StrokeCap.Butt else StrokeCap.Round
    drawArcAt(trackColor, center, radius, startDegrees, sweepDegrees, Stroke(strokeWidth, cap = cap))
    val hairline = 1f * density
    if (full) {
        drawCircle(seatColor, radius = radius - strokeWidth / 2f, center = center, style = Stroke(hairline))
        drawCircle(seatColor.copy(alpha = seatColor.alpha * 0.5f), radius = radius + strokeWidth / 2f, center = center, style = Stroke(hairline))
    }
}

/** Accent arc from [startDegrees] sweeping [sweepDegrees]. */
internal fun DrawScope.activeArc(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    startDegrees: Float,
    sweepDegrees: Float,
    color: Color,
) {
    if (sweepDegrees <= 0f) return
    drawArcAt(color, center, radius, startDegrees, sweepDegrees, Stroke(strokeWidth, cap = StrokeCap.Round))
}

private fun DrawScope.drawArcAt(
    color: Color,
    center: Offset,
    radius: Float,
    startDegrees: Float,
    sweepDegrees: Float,
    stroke: Stroke,
) {
    drawArc(
        color = color,
        startAngle = startDegrees - 90f,
        sweepAngle = sweepDegrees,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = stroke,
    )
}
