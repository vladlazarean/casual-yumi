package dev.yumi.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Haptic feedback used by Yumi controls. On by default for sliders, switches and long-press.
 * Provide [YumiHaptics.None] (or `rememberYumiHaptics(enabled = false)`) to turn it off.
 */
@Stable
public interface YumiHaptics {
    /** Whether any feedback is produced. Also forwarded to long-press haptics. */
    public val enabled: Boolean

    /** A slider or dial moved one step. */
    public fun tick()

    /** A switch, checkbox or toggle changed to [on]. */
    public fun toggle(on: Boolean)

    /** A long press was recognised. */
    public fun longPress()

    /** A confirming action completed (e.g. the dial centre key). */
    public fun confirm()

    /** Ready-made [YumiHaptics] instances. */
    public companion object {
        /** No haptics at all. */
        public val None: YumiHaptics = object : YumiHaptics {
            override val enabled: Boolean = false
            override fun tick() = Unit
            override fun toggle(on: Boolean) = Unit
            override fun longPress() = Unit
            override fun confirm() = Unit
        }
    }
}

/** Current [YumiHaptics]. Prefer [YumiTheme.haptics]. */
public val LocalYumiHaptics: ProvidableCompositionLocal<YumiHaptics> =
    staticCompositionLocalOf { YumiHaptics.None }

/** Haptics backed by the platform [HapticFeedback]. */
@Composable
public fun rememberYumiHaptics(enabled: Boolean = true): YumiHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback, enabled) {
        if (enabled) PlatformYumiHaptics(feedback) else YumiHaptics.None
    }
}

private class PlatformYumiHaptics(private val feedback: HapticFeedback) : YumiHaptics {
    override val enabled: Boolean = true
    override fun tick() = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
    override fun toggle(on: Boolean) = feedback.performHapticFeedback(
        if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
    )
    override fun longPress() = feedback.performHapticFeedback(HapticFeedbackType.LongPress)
    override fun confirm() = feedback.performHapticFeedback(HapticFeedbackType.Confirm)
}
