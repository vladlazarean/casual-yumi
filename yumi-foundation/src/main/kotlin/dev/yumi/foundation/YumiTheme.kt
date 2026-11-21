package dev.yumi.foundation

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import dev.yumi.tokens.YumiAccent
import dev.yumi.tokens.YumiColors
import dev.yumi.tokens.YumiDepth
import dev.yumi.tokens.YumiMotion
import dev.yumi.tokens.YumiShapes
import dev.yumi.tokens.darkYumiColors
import dev.yumi.tokens.darkYumiDepth
import dev.yumi.tokens.lightYumiColors
import dev.yumi.tokens.lightYumiDepth

/** Current [YumiColors]. Prefer [YumiTheme.colors]. */
public val LocalYumiColors: ProvidableCompositionLocal<YumiColors> =
    staticCompositionLocalOf { lightYumiColors() }

/** Current [YumiDepth]. Prefer [YumiTheme.depth]. */
public val LocalYumiDepth: ProvidableCompositionLocal<YumiDepth> =
    staticCompositionLocalOf { lightYumiDepth() }

/** Current [YumiShapes]. Prefer [YumiTheme.shapes]. */
public val LocalYumiShapes: ProvidableCompositionLocal<YumiShapes> =
    staticCompositionLocalOf { YumiShapes() }

/** Current [YumiMotion]. Prefer [YumiTheme.motion]. */
public val LocalYumiMotion: ProvidableCompositionLocal<YumiMotion> =
    staticCompositionLocalOf { YumiMotion() }

/**
 * Casual Yumi: matte grey, soft-touch "remote control" components with a single accent.
 *
 * Wraps [MaterialTheme] with a `ColorScheme`, `Typography` and `Shapes` derived from the Yumi
 * tokens, so stock Material 3 components inside it still look right. Ripples are switched off
 * for the whole subtree: press feedback in Casual Yumi is a physical sink, never a ripple.
 *
 * @param darkTheme `true` for Graphite, `false` for Putty.
 * @param accent the single accent colour. Defaults to [YumiAccent.Orange].
 * @param colors full colour override; defaults to the palette for [darkTheme] with [accent].
 * @param depth shadow / highlight values; defaults to the ones tuned for [darkTheme].
 * @param shapes shape families.
 * @param motion press motion. Reduced motion is also enabled automatically when the system
 *   animator duration scale is 0.
 * @param fontFamily the one type family, mapped to the whole M3 type scale. Defaults to Manrope.
 * @param haptics haptic feedback used by sliders, switches and long-press.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: YumiAccent = YumiAccent.Orange,
    colors: YumiColors = if (darkTheme) darkYumiColors(accent) else lightYumiColors(accent),
    depth: YumiDepth = if (darkTheme) darkYumiDepth() else lightYumiDepth(),
    shapes: YumiShapes = YumiShapes(),
    motion: YumiMotion = YumiMotion(),
    fontFamily: FontFamily = YumiFonts.Manrope,
    haptics: YumiHaptics = rememberYumiHaptics(),
    content: @Composable () -> Unit,
) {
    val systemReducedMotion = isSystemReducedMotion()
    val resolvedMotion = remember(motion, systemReducedMotion) {
        if (systemReducedMotion && !motion.reduceMotion) motion.copy(reduceMotion = true) else motion
    }
    val colorScheme = remember(colors) { colors.toColorScheme() }
    val typography = remember(fontFamily) { yumiTypography(fontFamily) }
    val materialShapes = remember(shapes) { shapes.toMaterialShapes() }

    CompositionLocalProvider(
        LocalYumiColors provides colors,
        LocalYumiDepth provides depth,
        LocalYumiShapes provides shapes,
        LocalYumiMotion provides resolvedMotion,
        LocalYumiHaptics provides haptics,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = materialShapes,
        ) {
            // MaterialTheme installs a ripple; replace it for foundation clickables and turn it
            // off for M3 components, so nothing in a Yumi subtree ripples.
            CompositionLocalProvider(
                LocalIndication provides NoIndication,
                LocalRippleConfiguration provides null,
                content = content,
            )
        }
    }
}

/** Accessors for the current Casual Yumi theme values. */
public object YumiTheme {
    /** Current colours. */
    public val colors: YumiColors
        @Composable @ReadOnlyComposable get() = LocalYumiColors.current

    /** Current depth (shadows, highlights). */
    public val depth: YumiDepth
        @Composable @ReadOnlyComposable get() = LocalYumiDepth.current

    /** Current shapes. */
    public val shapes: YumiShapes
        @Composable @ReadOnlyComposable get() = LocalYumiShapes.current

    /** Current motion, with system reduced motion already applied. */
    public val motion: YumiMotion
        @Composable @ReadOnlyComposable get() = LocalYumiMotion.current

    /** Current haptics. */
    public val haptics: YumiHaptics
        @Composable @ReadOnlyComposable get() = LocalYumiHaptics.current

    /** Current type scale (the Material 3 one, set in the Yumi family). */
    public val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography
}
