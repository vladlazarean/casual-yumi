package dev.yumi.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme

/** Defaults for [YumiLed]. */
public object YumiLedDefaults {
    /** Width of the LED pill. */
    public val Width: Dp = 14.dp

    /** Height of the LED pill. */
    public val Height: Dp = 5.dp
}

/**
 * The tiny status pill from the remote: lit in the accent when [on], dark otherwise.
 * It is the only thing in Casual Yumi that "glows", so use it for on / armed states only.
 *
 * @param on whether the LED is lit.
 * @param modifier modifier for the LED.
 * @param contentDescription what the LED reports, e.g. "Alarm armed". `null` makes it decorative
 *   (use when adjacent text already says the same).
 * @param onColor colour when lit; defaults to the accent.
 * @param offColor colour when dark; defaults to the seat grey.
 */
@Composable
public fun YumiLed(
    on: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onColor: Color = YumiTheme.colors.accent,
    offColor: Color = YumiTheme.colors.seat,
) {
    val color by animateColorAsState(
        targetValue = if (on) onColor else offColor,
        animationSpec = if (YumiTheme.motion.reduceMotion) snap() else tween(durationMillis = 160),
        label = "yumiLed",
    )
    Box(
        modifier
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            )
            .size(width = YumiLedDefaults.Width, height = YumiLedDefaults.Height)
            .background(color, YumiTheme.shapes.pill),
    )
}
