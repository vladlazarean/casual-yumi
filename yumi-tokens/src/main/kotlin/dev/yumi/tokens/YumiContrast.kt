package dev.yumi.tokens

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/** WCAG 2.x contrast helpers, used to validate accents and palettes. */
public object YumiContrast {

    /** Minimum ratio for normal-size text (WCAG AA). */
    public const val AA_NORMAL_TEXT: Float = 4.5f

    /** Minimum ratio for large text and UI component boundaries (WCAG AA). */
    public const val AA_LARGE_TEXT: Float = 3f

    /**
     * Contrast ratio between two opaque sRGB colours, from 1.0 to 21.0.
     * Alpha is ignored: composite translucent colours over their background first.
     */
    public fun ratio(a: Color, b: Color): Float {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        val lighter = maxOf(la, lb)
        val darker = minOf(la, lb)
        return ((lighter + 0.05) / (darker + 0.05)).toFloat()
    }

    // Implemented by hand rather than via Color.luminance() so the formula is exactly WCAG's
    // and runs in plain JVM unit tests.
    private fun relativeLuminance(color: Color): Double {
        fun channel(c: Float): Double {
            val v = c.toDouble()
            return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
    }
}
