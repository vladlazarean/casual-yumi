package dev.yumi.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Raw Casual Yumi design values (formerly `PebbleTokens`).
 *
 * Components never read these directly: they go through [YumiColors], [YumiDepth], [YumiShapes]
 * and [YumiMotion] as provided by `YumiTheme`, so a consumer override reaches every component.
 * This object exists so the source of truth for each value lives in one place.
 */
public object YumiTokens {

    /** Light palette ("Putty"): matte putty-grey remote body. */
    public object Putty {
        /** Matte app body (M3 `background` / `surface`). */
        public val Body: Color = Color(0xFFE4E1DC)
        /** Fill of a neutral raised pebble (M3 `surfaceContainerHigh`). */
        public val Raised: Color = Color(0xFFD6D2CB)
        /** Fill of a recessed tray or well (M3 `surfaceDim`). */
        public val Tray: Color = Color(0xFFCFCBC4)
        /** Thin ring a pebble sits in; unselected marks (M3 `outlineVariant`). */
        public val Seat: Color = Color(0xFFB9B4AC)
        /** Primary text and icons (M3 `onSurface`). */
        public val Ink: Color = Color(0xFF2B2A28)
        /** Secondary text; at least 4.5:1 on body, raised and tray (M3 `onSurfaceVariant`). */
        public val Muted: Color = Color(0xFF55514B)
        /** The rare inverse pebble and toasts (M3 `inverseSurface`). */
        public val Charcoal: Color = Color(0xFF2E3033)
        /** Content on [Charcoal] (M3 `inverseOnSurface`). */
        public val OnCharcoal: Color = Color(0xFFECE9E4)
        /** Error and destructive colour. */
        public val Error: Color = Color(0xFFA8322A)
        /** Content on [Error]. */
        public val OnError: Color = Color(0xFFFFFFFF)
    }

    /** Dark palette ("Graphite"). */
    public object Graphite {
        /** Matte app body (M3 `background` / `surface`). */
        public val Body: Color = Color(0xFF1E1F21)
        /** Fill of a neutral raised pebble (M3 `surfaceContainerHigh`). */
        public val Raised: Color = Color(0xFF2C2D30)
        /** Fill of a recessed tray or well (M3 `surfaceDim`). */
        public val Tray: Color = Color(0xFF18191A)
        /** Thin ring a pebble sits in; unselected marks (M3 `outlineVariant`). */
        public val Seat: Color = Color(0xFF3A3B3F)
        /** Primary text and icons (M3 `onSurface`). */
        public val Ink: Color = Color(0xFFECE9E4)
        /** Secondary text; at least 4.5:1 on body, raised and tray (M3 `onSurfaceVariant`). */
        public val Muted: Color = Color(0xFFA29E97)
        /** The "inverse" pebble: on Graphite the charcoal key flips to a light key. */
        public val Charcoal: Color = Color(0xFFECE9E4)
        /** Content on [Charcoal] (M3 `inverseOnSurface`). */
        public val OnCharcoal: Color = Color(0xFF1E1F21)
        /** Error and destructive colour. */
        public val Error: Color = Color(0xFFF2A49C)
        /** Content on [Error]. */
        public val OnError: Color = Color(0xFF1E1F21)
    }

    // ── Geometry ────────────────────────────────────────────────────────────

    /** Pill-rect corner radius for text pebbles. */
    public val ButtonCornerRadius: Dp = 24.dp
    /** Corner radius for recessed trays. */
    public val TrayCornerRadius: Dp = 32.dp
    /** Corner radius for raised cards. */
    public val CardCornerRadius: Dp = 28.dp
    /** Corner radius for small parts: fields, chips, checkbox wells. */
    public val SmallCornerRadius: Dp = 12.dp

    /** Diameter of a round icon pebble. */
    public val IconPebbleSize: Dp = 56.dp
    /** Minimum height of the accent (primary) pebble. */
    public val PrimaryMinHeight: Dp = 56.dp
    /** Minimum height of every other pebble. Also the minimum touch target. */
    public val SecondaryMinHeight: Dp = 48.dp
    /** Width of the seat ring the key sits in. */
    public val SeatWidth: Dp = 1.dp

    // ── Depth ───────────────────────────────────────────────────────────────

    /** Drop shadow under a raised pebble (key light from the top). */
    public val RaiseRadius: Dp = 8.dp
    /** Offset of the drop shadow under a raised pebble. */
    public val RaiseOffset: DpOffset = DpOffset(0.dp, 3.dp)

    /** Inner top highlight on a raised pebble. */
    public val HighlightRadius: Dp = 2.dp
    /** Offset of the inner top highlight on a raised pebble. */
    public val HighlightOffset: DpOffset = DpOffset(0.dp, (-1).dp)

    /** Inner shadow that appears as a pebble sinks. */
    public val PressedInsetRadius: Dp = 6.dp
    /** Offset of the inner shadow on a fully sunk pebble. */
    public val PressedInsetOffset: DpOffset = DpOffset(0.dp, 2.dp)
    /** Alpha of the inner shadow on a fully sunk pebble. */
    public const val PressedInsetAlpha: Float = 0.22f

    /** Inner shadow that makes a tray read as recessed. */
    public val TrayInsetRadius: Dp = 10.dp
    /** Offset of the inner shadow that recesses a tray. */
    public val TrayInsetOffset: DpOffset = DpOffset(0.dp, 3.dp)
    /** Alpha of the inner shadow that recesses a tray. */
    public const val TrayInsetAlpha: Float = 0.14f

    /** Drop-shadow alpha under a resting pebble on Putty. */
    public const val ShadowAlphaLight: Float = 0.18f
    /** Drop-shadow alpha under a resting pebble on Graphite; higher so it stays visible. */
    public const val ShadowAlphaDark: Float = 0.45f
    /** Top-highlight alpha on a resting pebble on Putty. */
    public const val HighlightAlphaLight: Float = 0.60f
    /** Top-highlight alpha on a resting pebble on Graphite. */
    public const val HighlightAlphaDark: Float = 0.06f

    // ── Motion ──────────────────────────────────────────────────────────────

    /** How far a pebble travels when pressed. */
    public val SinkOffset: Dp = 1.5.dp
    /** Press spring: quick, firm, no wobble. */
    public const val PressDampingRatio: Float = 0.7f
    /** Stiffness of the press spring. */
    public const val PressStiffness: Float = 900f

    // ── Content ─────────────────────────────────────────────────────────────

    /** Alpha applied to content of a disabled control. */
    public const val DisabledContentAlpha: Float = 0.38f
}
