package dev.yumi.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The colour of one accent in one theme.
 *
 * @property accent fill of the accent pebble, the LED and active arcs.
 * @property onAccent content (text, icons) drawn on [accent]. Must reach 4.5:1 against it.
 * @property accentInk accent-coloured *text* on the body / raised greys. Must reach 4.5:1 against both.
 */
@Immutable
public class YumiAccentTones(
    public val accent: Color,
    public val onAccent: Color,
    public val accentInk: Color,
) {
    override fun equals(other: Any?): Boolean =
        other is YumiAccentTones &&
            accent == other.accent &&
            onAccent == other.onAccent &&
            accentInk == other.accentInk

    override fun hashCode(): Int {
        var result = accent.hashCode()
        result = 31 * result + onAccent.hashCode()
        result = 31 * result + accentInk.hashCode()
        return result
    }

    override fun toString(): String = "YumiAccentTones(accent=$accent, onAccent=$onAccent, accentInk=$accentInk)"
}

/**
 * The single accent colour of a Casual Yumi theme, with tones for Putty (light) and Graphite (dark).
 *
 * The library is not wired to orange: pick a preset or build your own with [YumiAccent.custom].
 * Every preset is verified (unit-tested) to reach WCAG AA for content on the accent and for
 * accent ink on the body and raised greys. Use [meetsContrastAA] to check a custom accent.
 *
 * Orange is scarce by design: one accent action per screen, plus "on / armed" indicators.
 */
@Immutable
public class YumiAccent private constructor(
    /** Human-readable name, used by the catalog and for debugging. */
    public val name: String,
    /** Tones used under the Putty (light) palette. */
    public val light: YumiAccentTones,
    /** Tones used under the Graphite (dark) palette. */
    public val dark: YumiAccentTones,
) {

    /** Returns the tones for the requested theme. */
    public fun tones(darkTheme: Boolean): YumiAccentTones = if (darkTheme) dark else light

    /**
     * `true` if both themes reach 4.5:1 for [YumiAccentTones.onAccent] on the accent, and for
     * [YumiAccentTones.accentInk] on the body and raised surfaces.
     */
    public fun meetsContrastAA(): Boolean =
        tonesPass(light, YumiTokens.Putty.Body, YumiTokens.Putty.Raised) &&
            tonesPass(dark, YumiTokens.Graphite.Body, YumiTokens.Graphite.Raised)

    private fun tonesPass(tones: YumiAccentTones, body: Color, raised: Color): Boolean =
        YumiContrast.ratio(tones.onAccent, tones.accent) >= YumiContrast.AA_NORMAL_TEXT &&
            YumiContrast.ratio(tones.accentInk, body) >= YumiContrast.AA_NORMAL_TEXT &&
            YumiContrast.ratio(tones.accentInk, raised) >= YumiContrast.AA_NORMAL_TEXT

    override fun equals(other: Any?): Boolean =
        other is YumiAccent && name == other.name && light == other.light && dark == other.dark

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + light.hashCode()
        result = 31 * result + dark.hashCode()
        return result
    }

    override fun toString(): String = "YumiAccent($name)"

    public companion object {
        // Content on the accent is ink/body, never white: white on #E8772E is ~2.9:1 and fails AA.

        /** The default: the orange key from the remote. */
        public val Orange: YumiAccent = preset(
            name = "Orange",
            light = 0xFFE8772E, lightInk = 0xFF8A3B0E,
            dark = 0xFFF08A45, darkInk = 0xFFF08A45,
        )

        /** Warm amber, for a softer standby-light feel. */
        public val Amber: YumiAccent = preset(
            name = "Amber",
            light = 0xFFE0A100, lightInk = 0xFF6E4A00,
            dark = 0xFFF2B632, darkInk = 0xFFF2B632,
        )

        /** Muted green. */
        public val Sage: YumiAccent = preset(
            name = "Sage",
            light = 0xFF6FA287, lightInk = 0xFF2F5A44,
            dark = 0xFF8CC2A4, darkInk = 0xFF8CC2A4,
        )

        /** Calm blue. */
        public val Sky: YumiAccent = preset(
            name = "Sky",
            light = 0xFF4C9BD6, lightInk = 0xFF1D4F78,
            dark = 0xFF7DB8E8, darkInk = 0xFF7DB8E8,
        )

        /** Soft red-pink. */
        public val Coral: YumiAccent = preset(
            name = "Coral",
            light = 0xFFEC7B6D, lightInk = 0xFF8E2A20,
            dark = 0xFFF28A7E, darkInk = 0xFFF28A7E,
        )

        /** All built-in presets, in display order. */
        public val presets: List<YumiAccent> = listOf(Orange, Amber, Sage, Sky, Coral)

        /**
         * Builds a custom accent. Not validated at runtime (a theme shouldn't crash an app);
         * check it with [meetsContrastAA] in a unit test.
         */
        public fun custom(name: String, light: YumiAccentTones, dark: YumiAccentTones): YumiAccent =
            YumiAccent(name, light, dark)

        private fun preset(name: String, light: Long, lightInk: Long, dark: Long, darkInk: Long) =
            YumiAccent(
                name = name,
                light = YumiAccentTones(Color(light), YumiTokens.Putty.Ink, Color(lightInk)),
                dark = YumiAccentTones(Color(dark), YumiTokens.Graphite.Body, Color(darkInk)),
            )
    }
}
