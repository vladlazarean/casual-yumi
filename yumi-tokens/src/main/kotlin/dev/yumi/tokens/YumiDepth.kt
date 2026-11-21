package dev.yumi.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset

/**
 * Shadow and highlight values that make a surface read as raised (you press it) or recessed
 * (you read it).
 *
 * Provided by the theme rather than derived from `isSystemInDarkTheme()` in each component, so a
 * forced-dark preview or an in-app theme toggle stays consistent.
 *
 * @property shadowAlpha drop-shadow alpha under a resting pebble. Higher on Graphite so it stays visible.
 * @property highlightAlpha inner top-highlight alpha on a resting pebble.
 * @property pressedInsetAlpha inner-shadow alpha on a fully sunk pebble.
 * @property trayInsetAlpha inner-shadow alpha that recesses a tray.
 * @property seatWidth stroke width of the seat ring.
 * @property raiseRadius blur radius of the drop shadow.
 * @property raiseOffset offset of the drop shadow.
 * @property highlightRadius blur radius of the top highlight.
 * @property highlightOffset offset of the top highlight.
 * @property pressedInsetRadius blur radius of the pressed inner shadow.
 * @property pressedInsetOffset offset of the pressed inner shadow.
 * @property trayInsetRadius blur radius of the tray inner shadow.
 * @property trayInsetOffset offset of the tray inner shadow.
 */
@Immutable
public class YumiDepth(
    public val shadowAlpha: Float,
    public val highlightAlpha: Float,
    public val pressedInsetAlpha: Float = YumiTokens.PressedInsetAlpha,
    public val trayInsetAlpha: Float = YumiTokens.TrayInsetAlpha,
    public val seatWidth: Dp = YumiTokens.SeatWidth,
    public val raiseRadius: Dp = YumiTokens.RaiseRadius,
    public val raiseOffset: DpOffset = YumiTokens.RaiseOffset,
    public val highlightRadius: Dp = YumiTokens.HighlightRadius,
    public val highlightOffset: DpOffset = YumiTokens.HighlightOffset,
    public val pressedInsetRadius: Dp = YumiTokens.PressedInsetRadius,
    public val pressedInsetOffset: DpOffset = YumiTokens.PressedInsetOffset,
    public val trayInsetRadius: Dp = YumiTokens.TrayInsetRadius,
    public val trayInsetOffset: DpOffset = YumiTokens.TrayInsetOffset,
) {

    /** Returns a copy with the given values replaced. */
    public fun copy(
        shadowAlpha: Float = this.shadowAlpha,
        highlightAlpha: Float = this.highlightAlpha,
        pressedInsetAlpha: Float = this.pressedInsetAlpha,
        trayInsetAlpha: Float = this.trayInsetAlpha,
        seatWidth: Dp = this.seatWidth,
        raiseRadius: Dp = this.raiseRadius,
        raiseOffset: DpOffset = this.raiseOffset,
        highlightRadius: Dp = this.highlightRadius,
        highlightOffset: DpOffset = this.highlightOffset,
        pressedInsetRadius: Dp = this.pressedInsetRadius,
        pressedInsetOffset: DpOffset = this.pressedInsetOffset,
        trayInsetRadius: Dp = this.trayInsetRadius,
        trayInsetOffset: DpOffset = this.trayInsetOffset,
    ): YumiDepth = YumiDepth(
        shadowAlpha, highlightAlpha, pressedInsetAlpha, trayInsetAlpha, seatWidth,
        raiseRadius, raiseOffset, highlightRadius, highlightOffset,
        pressedInsetRadius, pressedInsetOffset, trayInsetRadius, trayInsetOffset,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is YumiDepth) return false
        return shadowAlpha == other.shadowAlpha && highlightAlpha == other.highlightAlpha &&
            pressedInsetAlpha == other.pressedInsetAlpha && trayInsetAlpha == other.trayInsetAlpha &&
            seatWidth == other.seatWidth && raiseRadius == other.raiseRadius &&
            raiseOffset == other.raiseOffset && highlightRadius == other.highlightRadius &&
            highlightOffset == other.highlightOffset && pressedInsetRadius == other.pressedInsetRadius &&
            pressedInsetOffset == other.pressedInsetOffset && trayInsetRadius == other.trayInsetRadius &&
            trayInsetOffset == other.trayInsetOffset
    }

    override fun hashCode(): Int {
        var result = shadowAlpha.hashCode()
        result = 31 * result + highlightAlpha.hashCode()
        result = 31 * result + pressedInsetAlpha.hashCode()
        result = 31 * result + trayInsetAlpha.hashCode()
        result = 31 * result + seatWidth.hashCode()
        result = 31 * result + raiseRadius.hashCode()
        result = 31 * result + raiseOffset.hashCode()
        result = 31 * result + highlightRadius.hashCode()
        result = 31 * result + highlightOffset.hashCode()
        result = 31 * result + pressedInsetRadius.hashCode()
        result = 31 * result + pressedInsetOffset.hashCode()
        result = 31 * result + trayInsetRadius.hashCode()
        result = 31 * result + trayInsetOffset.hashCode()
        return result
    }

    override fun toString(): String = "YumiDepth(shadowAlpha=$shadowAlpha, highlightAlpha=$highlightAlpha)"
}

/** Depth for the Putty palette: soft shadow, strong white highlight. */
public fun lightYumiDepth(): YumiDepth = YumiDepth(
    shadowAlpha = YumiTokens.ShadowAlphaLight,
    highlightAlpha = YumiTokens.HighlightAlphaLight,
)

/** Depth for the Graphite palette: heavier shadow so it still reads, faint highlight. */
public fun darkYumiDepth(): YumiDepth = YumiDepth(
    shadowAlpha = YumiTokens.ShadowAlphaDark,
    highlightAlpha = YumiTokens.HighlightAlphaDark,
)
