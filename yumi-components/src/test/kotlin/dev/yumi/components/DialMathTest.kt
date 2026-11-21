package dev.yumi.components

import androidx.compose.ui.geometry.Offset
import dev.yumi.components.internal.clockAngle
import dev.yumi.components.internal.fractionOnArc
import dev.yumi.components.internal.rollHourOnMinuteWrap
import dev.yumi.components.internal.snapToSteps
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pure geometry and wrap rules behind the circular slider and time dial. */
class DialMathTest {

    private val center = Offset(100f, 100f)

    @Test
    fun clockAngleStartsAtTwelveAndRunsClockwise() {
        assertEquals(0f, clockAngle(Offset(100f, 0f), center), 0.01f)
        assertEquals(90f, clockAngle(Offset(200f, 100f), center), 0.01f)
        assertEquals(180f, clockAngle(Offset(100f, 200f), center), 0.01f)
        assertEquals(270f, clockAngle(Offset(0f, 100f), center), 0.01f)
    }

    @Test
    fun arcFractionSnapsGapToNearestEnd() {
        // 270° knob starting at 225° (7:30).
        assertEquals(0f, fractionOnArc(225f, 225f, 270f), 0.001f)
        assertEquals(0.5f, fractionOnArc(0f, 225f, 270f), 0.001f)
        assertEquals(1f, fractionOnArc(135f, 225f, 270f), 0.001f)
        assertEquals(1f, fractionOnArc(170f, 225f, 270f), 0.001f) // gap, nearer the end
        assertEquals(0f, fractionOnArc(200f, 225f, 270f), 0.001f) // gap, nearer the start
    }

    @Test
    fun snapToStepsRoundsToNearestStop() {
        assertEquals(0.5f, snapToSteps(0.46f, 0f..1f, steps = 3), 0.001f)
        assertEquals(0.75f, snapToSteps(0.7f, 0f..1f, steps = 3), 0.001f)
        assertEquals(0.37f, snapToSteps(0.37f, 0f..1f, steps = 0), 0.001f)
    }

    @Test
    fun minuteWrapRollsTheHour() {
        assertEquals(8, rollHourOnMinuteWrap(hour = 7, previousMinute = 58, newMinute = 1))
        assertEquals(6, rollHourOnMinuteWrap(hour = 7, previousMinute = 2, newMinute = 57))
        assertEquals(0, rollHourOnMinuteWrap(hour = 23, previousMinute = 59, newMinute = 0))
        assertEquals(23, rollHourOnMinuteWrap(hour = 0, previousMinute = 0, newMinute = 59))
        assertEquals(7, rollHourOnMinuteWrap(hour = 7, previousMinute = 20, newMinute = 40))
    }
}
