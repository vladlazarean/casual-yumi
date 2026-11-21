package dev.yumi.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import dev.yumi.foundation.YumiTheme

/**
 * Colours shared by every selection control ([YumiCheckbox], [YumiRadioButton], [YumiSwitch]):
 * one material, so they read as one family. A recessed well holds a raised mark or knob; "on"
 * lights an LED-style indicator.
 *
 * @property wellColor recessed well / track fill.
 * @property markColor raised mark or knob fill.
 * @property glyphColor check / dash glyph. Accent *ink*, so it reaches 3:1 on the raised mark.
 * @property indicatorOnColor lit indicator (radio dot, switch LED).
 * @property indicatorOffColor dark indicator.
 */
@Immutable
public class YumiSelectionColors(
    public val wellColor: Color,
    public val markColor: Color,
    public val glyphColor: Color,
    public val indicatorOnColor: Color,
    public val indicatorOffColor: Color,
) {
    /** Returns a copy with the given values replaced. */
    public fun copy(
        wellColor: Color = this.wellColor,
        markColor: Color = this.markColor,
        glyphColor: Color = this.glyphColor,
        indicatorOnColor: Color = this.indicatorOnColor,
        indicatorOffColor: Color = this.indicatorOffColor,
    ): YumiSelectionColors = YumiSelectionColors(wellColor, markColor, glyphColor, indicatorOnColor, indicatorOffColor)

    override fun equals(other: Any?): Boolean =
        other is YumiSelectionColors &&
            wellColor == other.wellColor &&
            markColor == other.markColor &&
            glyphColor == other.glyphColor &&
            indicatorOnColor == other.indicatorOnColor &&
            indicatorOffColor == other.indicatorOffColor

    override fun hashCode(): Int {
        var result = wellColor.hashCode()
        result = 31 * result + markColor.hashCode()
        result = 31 * result + glyphColor.hashCode()
        result = 31 * result + indicatorOnColor.hashCode()
        result = 31 * result + indicatorOffColor.hashCode()
        return result
    }
}

/** Default [YumiSelectionColors] for the current theme. */
public object YumiSelectionDefaults {
    /** Colours for checkboxes, radio buttons and switches. */
    @Composable
    @ReadOnlyComposable
    public fun colors(): YumiSelectionColors {
        val c = YumiTheme.colors
        return YumiSelectionColors(
            wellColor = c.tray,
            markColor = c.raised,
            glyphColor = c.accentInk,
            indicatorOnColor = c.accent,
            indicatorOffColor = c.seat,
        )
    }
}

/** Alpha for a whole selection control when disabled. */
internal const val DisabledControlAlpha: Float = 0.38f
