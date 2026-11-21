package dev.yumi.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.tray

/** Defaults for [YumiLinearProgress] and [YumiCircularProgress]. */
public object YumiProgressDefaults {
    /** Height of the linear track. */
    public val LinearTrackHeight: Dp = 8.dp

    /** Diameter of the circular indicator. */
    public val CircularSize: Dp = 40.dp

    /** Ring width of the circular indicator. */
    public val CircularStrokeWidth: Dp = 6.dp
}

/**
 * Determinate linear progress: a recessed track that fills with the accent.
 *
 * [progress] is a lambda read in the draw phase, so animating progress only redraws.
 *
 * @param progress current progress, 0..1.
 * @param modifier modifier for the indicator; defaults to full width.
 * @param color fill colour.
 * @param trackColor recessed track colour.
 */
@Composable
public fun YumiLinearProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = YumiTheme.colors.accent,
    trackColor: Color = YumiTheme.colors.tray,
) {
    LinearTrack(
        modifier = modifier.semantics(mergeDescendants = true) {
            progressBarRangeInfo = ProgressBarRangeInfo(progress().coerceIn(0f, 1f), 0f..1f)
        },
        color = color,
        trackColor = trackColor,
        segment = { 0f to progress().coerceIn(0f, 1f) },
    )
}

/**
 * Indeterminate linear progress: an accent segment that travels along the recessed track.
 * With reduced motion it holds still at a fixed segment instead of animating.
 *
 * @param modifier modifier for the indicator; defaults to full width.
 * @param color segment colour.
 * @param trackColor recessed track colour.
 */
@Composable
public fun YumiLinearProgress(
    modifier: Modifier = Modifier,
    color: Color = YumiTheme.colors.accent,
    trackColor: Color = YumiTheme.colors.tray,
) {
    val phase = indeterminatePhase(durationMillis = 1_400)
    LinearTrack(
        modifier = modifier.semantics(mergeDescendants = true) {
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        },
        color = color,
        trackColor = trackColor,
        segment = {
            val p = phase?.value ?: 0.35f
            val head = (p * 1.4f).coerceIn(0f, 1f)
            val tail = (p * 1.4f - 0.4f).coerceIn(0f, 1f)
            tail to head
        },
    )
}

/**
 * Determinate circular progress: a recessed ring with an accent arc, starting at 12 o'clock.
 *
 * @param progress current progress, 0..1, read in the draw phase.
 * @param modifier modifier for the indicator.
 * @param color arc colour.
 * @param trackColor recessed ring colour.
 */
@Composable
public fun YumiCircularProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = YumiTheme.colors.accent,
    trackColor: Color = YumiTheme.colors.tray,
) {
    Ring(
        modifier = modifier.semantics(mergeDescendants = true) {
            progressBarRangeInfo = ProgressBarRangeInfo(progress().coerceIn(0f, 1f), 0f..1f)
        },
        color = color,
        trackColor = trackColor,
        arc = { 0f to 360f * progress().coerceIn(0f, 1f) },
    )
}

/**
 * Indeterminate circular progress: a quarter arc that orbits the recessed ring.
 * Holds still with reduced motion.
 *
 * @param modifier modifier for the indicator.
 * @param color arc colour.
 * @param trackColor recessed ring colour.
 */
@Composable
public fun YumiCircularProgress(
    modifier: Modifier = Modifier,
    color: Color = YumiTheme.colors.accent,
    trackColor: Color = YumiTheme.colors.tray,
) {
    val phase = indeterminatePhase(durationMillis = 1_100)
    Ring(
        modifier = modifier.semantics(mergeDescendants = true) {
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        },
        color = color,
        trackColor = trackColor,
        arc = { 360f * (phase?.value ?: 0f) to 90f },
    )
}

/** 0..1 looping phase, or `null` when reduced motion is on. */
@Composable
private fun indeterminatePhase(durationMillis: Int): State<Float>? {
    if (YumiTheme.motion.reduceMotion) return null
    val transition = rememberInfiniteTransition(label = "yumiProgress")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "yumiProgressPhase",
    )
}

/** Recessed track; [segment] returns (start, end) as 0..1 fractions, read in the draw phase. */
@Composable
private fun LinearTrack(
    modifier: Modifier,
    color: Color,
    trackColor: Color,
    segment: () -> Pair<Float, Float>,
) {
    val depth = YumiTheme.depth
    val inset = remember(depth) { depth.copy(trayInsetRadius = 3.dp, trayInsetOffset = DpOffset(0.dp, 1.dp)) }
    Box(
        modifier
            .fillMaxWidth()
            .height(YumiProgressDefaults.LinearTrackHeight)
            .tray(trackColor, YumiTheme.shapes.pill, inset)
            .drawWithContent {
                drawContent()
                val (start, end) = segment()
                if (end <= start) return@drawWithContent
                val rtl = layoutDirection == LayoutDirection.Rtl
                val left = if (rtl) size.width * (1f - end) else size.width * start
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left, 0f),
                    size = Size(size.width * (end - start), size.height),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            },
    )
}

/** Recessed ring; [arc] returns (startDegrees from 12 o'clock, sweepDegrees), read in the draw phase. */
@Composable
private fun Ring(
    modifier: Modifier,
    color: Color,
    trackColor: Color,
    arc: () -> Pair<Float, Float>,
) {
    val seat = YumiTheme.colors.seat
    Canvas(modifier.size(YumiProgressDefaults.CircularSize)) {
        val stroke = YumiProgressDefaults.CircularStrokeWidth.toPx()
        val radius = (size.minDimension - stroke) / 2f
        // The well: track ring with a hairline seat on its inner edge so it reads recessed.
        drawCircle(trackColor, radius = radius, style = Stroke(stroke))
        drawCircle(seat, radius = radius - stroke / 2f, style = Stroke(1.dp.toPx()))
        val (start, sweep) = arc()
        if (sweep <= 0f) return@Canvas
        val rtl = layoutDirection == LayoutDirection.Rtl
        val topLeft = Offset(center.x - radius, center.y - radius)
        drawArc(
            color = color,
            startAngle = -90f + if (rtl) -start - sweep else start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}
