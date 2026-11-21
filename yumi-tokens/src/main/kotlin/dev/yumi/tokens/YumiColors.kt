package dev.yumi.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The complete Casual Yumi colour set for one theme.
 *
 * `YumiTheme` maps these onto a Material 3 `ColorScheme`, so stock M3 components still look
 * right. The roles M3 doesn't have ([seat], [tray], [accentInk]) are read from here.
 *
 * @property body the matte app body (M3 `background` / `surface`).
 * @property raised fill of a neutral pebble (M3 `surfaceContainerHigh`).
 * @property tray fill of a recessed well (M3 `surfaceDim`).
 * @property seat the thin ring a pebble sits in, unselected marks (M3 `outlineVariant`).
 * @property ink primary text and icons (M3 `onSurface`).
 * @property muted secondary text (M3 `onSurfaceVariant`). 4.5:1 on body, raised and tray.
 * @property accent fill of the single accent pebble, LEDs and active arcs (M3 `primary`).
 * @property onAccent content on [accent] (M3 `onPrimary`).
 * @property accentInk accent-coloured text on the greys (M3 `tertiary`).
 * @property charcoal the rare inverse pebble, toasts (M3 `inverseSurface`).
 * @property onCharcoal content on [charcoal] (M3 `inverseOnSurface`).
 * @property error destructive / error fill.
 * @property onError content on [error].
 * @property isDark whether this is the Graphite palette. Drives depth and M3 defaults.
 */
@Immutable
public class YumiColors(
    public val body: Color,
    public val raised: Color,
    public val tray: Color,
    public val seat: Color,
    public val ink: Color,
    public val muted: Color,
    public val accent: Color,
    public val onAccent: Color,
    public val accentInk: Color,
    public val charcoal: Color,
    public val onCharcoal: Color,
    public val error: Color,
    public val onError: Color,
    public val isDark: Boolean,
) {

    /** Returns a copy with the given values replaced. */
    public fun copy(
        body: Color = this.body,
        raised: Color = this.raised,
        tray: Color = this.tray,
        seat: Color = this.seat,
        ink: Color = this.ink,
        muted: Color = this.muted,
        accent: Color = this.accent,
        onAccent: Color = this.onAccent,
        accentInk: Color = this.accentInk,
        charcoal: Color = this.charcoal,
        onCharcoal: Color = this.onCharcoal,
        error: Color = this.error,
        onError: Color = this.onError,
        isDark: Boolean = this.isDark,
    ): YumiColors = YumiColors(
        body, raised, tray, seat, ink, muted, accent, onAccent, accentInk,
        charcoal, onCharcoal, error, onError, isDark,
    )

    /** Returns a copy using the tones of [accent] for this palette. */
    public fun withAccent(accent: YumiAccent): YumiColors {
        val tones = accent.tones(isDark)
        return copy(accent = tones.accent, onAccent = tones.onAccent, accentInk = tones.accentInk)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is YumiColors) return false
        return body == other.body && raised == other.raised && tray == other.tray &&
            seat == other.seat && ink == other.ink && muted == other.muted &&
            accent == other.accent && onAccent == other.onAccent && accentInk == other.accentInk &&
            charcoal == other.charcoal && onCharcoal == other.onCharcoal &&
            error == other.error && onError == other.onError && isDark == other.isDark
    }

    override fun hashCode(): Int {
        var result = body.hashCode()
        for (c in listOf(raised, tray, seat, ink, muted, accent, onAccent, accentInk, charcoal, onCharcoal, error, onError)) {
            result = 31 * result + c.hashCode()
        }
        return 31 * result + isDark.hashCode()
    }

    override fun toString(): String =
        "YumiColors(isDark=$isDark, body=$body, raised=$raised, tray=$tray, accent=$accent)"
}

/** The Putty (light) palette with the given [accent]. */
public fun lightYumiColors(accent: YumiAccent = YumiAccent.Orange): YumiColors = YumiColors(
    body = YumiTokens.Putty.Body,
    raised = YumiTokens.Putty.Raised,
    tray = YumiTokens.Putty.Tray,
    seat = YumiTokens.Putty.Seat,
    ink = YumiTokens.Putty.Ink,
    muted = YumiTokens.Putty.Muted,
    accent = accent.light.accent,
    onAccent = accent.light.onAccent,
    accentInk = accent.light.accentInk,
    charcoal = YumiTokens.Putty.Charcoal,
    onCharcoal = YumiTokens.Putty.OnCharcoal,
    error = YumiTokens.Putty.Error,
    onError = YumiTokens.Putty.OnError,
    isDark = false,
)

/** The Graphite (dark) palette with the given [accent]. */
public fun darkYumiColors(accent: YumiAccent = YumiAccent.Orange): YumiColors = YumiColors(
    body = YumiTokens.Graphite.Body,
    raised = YumiTokens.Graphite.Raised,
    tray = YumiTokens.Graphite.Tray,
    seat = YumiTokens.Graphite.Seat,
    ink = YumiTokens.Graphite.Ink,
    muted = YumiTokens.Graphite.Muted,
    accent = accent.dark.accent,
    onAccent = accent.dark.onAccent,
    accentInk = accent.dark.accentInk,
    charcoal = YumiTokens.Graphite.Charcoal,
    onCharcoal = YumiTokens.Graphite.OnCharcoal,
    error = YumiTokens.Graphite.Error,
    onError = YumiTokens.Graphite.OnError,
    isDark = true,
)
