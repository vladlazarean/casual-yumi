package dev.yumi.foundation

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import dev.yumi.tokens.YumiColors
import dev.yumi.tokens.YumiShapes
import dev.yumi.tokens.YumiTokens

/**
 * Maps Yumi colours onto every Material 3 role, so stock M3 components inside [YumiTheme]
 * follow the same rules:
 * - Selected / container states use a grey fill + [YumiColors.accentInk] text, never an accent
 *   fill ("selected = sunk + accentInk", orange stays scarce).
 * - `surfaceTint` is the body colour, so tonal elevation never washes surfaces with the accent.
 */
public fun YumiColors.toColorScheme(): ColorScheme {
    val containerLow = lerp(body, raised, 0.33f)
    val container = lerp(body, raised, 0.66f)
    val containerHighest = lerp(raised, seat, 0.35f)
    return if (isDark) {
        darkColorScheme(
            primary = accent, onPrimary = onAccent,
            primaryContainer = raised, onPrimaryContainer = accentInk,
            inversePrimary = accent,
            secondary = muted, onSecondary = body,
            secondaryContainer = tray, onSecondaryContainer = accentInk,
            tertiary = accentInk, onTertiary = body,
            tertiaryContainer = tray, onTertiaryContainer = accentInk,
            background = body, onBackground = ink,
            surface = body, onSurface = ink,
            surfaceVariant = raised, onSurfaceVariant = muted,
            surfaceTint = body,
            inverseSurface = charcoal, inverseOnSurface = onCharcoal,
            error = error, onError = onError,
            errorContainer = error, onErrorContainer = onError,
            outline = muted, outlineVariant = seat,
            scrim = Color.Black,
            surfaceBright = raised, surfaceDim = tray,
            surfaceContainerLowest = tray, surfaceContainerLow = containerLow,
            surfaceContainer = container, surfaceContainerHigh = raised,
            surfaceContainerHighest = containerHighest,
        )
    } else {
        lightColorScheme(
            primary = accent, onPrimary = onAccent,
            primaryContainer = raised, onPrimaryContainer = accentInk,
            inversePrimary = accent,
            secondary = muted, onSecondary = body,
            secondaryContainer = tray, onSecondaryContainer = accentInk,
            tertiary = accentInk, onTertiary = body,
            tertiaryContainer = tray, onTertiaryContainer = accentInk,
            background = body, onBackground = ink,
            surface = body, onSurface = ink,
            surfaceVariant = raised, onSurfaceVariant = muted,
            surfaceTint = body,
            inverseSurface = charcoal, inverseOnSurface = onCharcoal,
            error = error, onError = onError,
            errorContainer = error, onErrorContainer = onError,
            outline = muted, outlineVariant = seat,
            scrim = Color.Black,
            surfaceBright = body, surfaceDim = tray,
            surfaceContainerLowest = body, surfaceContainerLow = containerLow,
            surfaceContainer = container, surfaceContainerHigh = raised,
            surfaceContainerHighest = containerHighest,
        )
    }
}

/**
 * Maps the Yumi shapes onto the M3 shape scale. M3 needs `CornerBasedShape`s; a custom
 * non-corner shape falls back to the default Yumi radius for that slot.
 */
public fun YumiShapes.toMaterialShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = small as? CornerBasedShape ?: RoundedCornerShape(YumiTokens.SmallCornerRadius),
    medium = RoundedCornerShape(16.dp),
    large = button as? CornerBasedShape ?: RoundedCornerShape(YumiTokens.ButtonCornerRadius),
    extraLarge = tray as? CornerBasedShape ?: RoundedCornerShape(YumiTokens.TrayCornerRadius),
)
