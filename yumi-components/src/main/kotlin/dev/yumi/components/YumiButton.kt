package dev.yumi.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.PebbleKey
import dev.yumi.components.internal.flat
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.pebbleClickable
import dev.yumi.tokens.YumiTokens

/** The three pebble finishes. */
public enum class YumiButtonVariant {
    /** The orange key: "the thing you came here to do". Max one per screen. */
    Accent,

    /** The default raised grey key. */
    Neutral,

    /** The rare inverse key. Use only on screens with no accent key, so it never competes with orange. */
    Charcoal,
}

/**
 * Colours of a Yumi button in its enabled and disabled states.
 *
 * Disabled keys keep their fill but rest sunk with faded content: "can't press" looks pushed in
 * and dead, not just greyed.
 *
 * @property containerColor fill when enabled.
 * @property contentColor label and icon colour when enabled.
 * @property disabledContainerColor fill when disabled.
 * @property disabledContentColor label and icon colour when disabled.
 */
@Immutable
public class YumiButtonColors(
    public val containerColor: Color,
    public val contentColor: Color,
    public val disabledContainerColor: Color,
    public val disabledContentColor: Color,
) {
    /** Container colour for the given state. */
    public fun containerColor(enabled: Boolean): Color = if (enabled) containerColor else disabledContainerColor

    /** Content colour for the given state. */
    public fun contentColor(enabled: Boolean): Color = if (enabled) contentColor else disabledContentColor

    /** Returns a copy with the given values replaced; [Color.Unspecified] keeps the current value. */
    public fun copy(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = Color.Unspecified,
        disabledContainerColor: Color = Color.Unspecified,
        disabledContentColor: Color = Color.Unspecified,
    ): YumiButtonColors = YumiButtonColors(
        containerColor.orIfUnspecified(this.containerColor),
        contentColor.orIfUnspecified(this.contentColor),
        disabledContainerColor.orIfUnspecified(this.disabledContainerColor),
        disabledContentColor.orIfUnspecified(this.disabledContentColor),
    )

    override fun equals(other: Any?): Boolean =
        other is YumiButtonColors &&
            containerColor == other.containerColor &&
            contentColor == other.contentColor &&
            disabledContainerColor == other.disabledContainerColor &&
            disabledContentColor == other.disabledContentColor

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + disabledContainerColor.hashCode()
        result = 31 * result + disabledContentColor.hashCode()
        return result
    }
}

/** Defaults for [YumiButton], [YumiIconButton], [YumiTextButton], [YumiToggleButton] and [YumiFab]. */
public object YumiButtonDefaults {
    /** Minimum height of neutral and charcoal keys. */
    public val MinHeight: Dp = YumiTokens.SecondaryMinHeight

    /** Minimum height of the accent key: the primary action reads larger. */
    public val AccentMinHeight: Dp = YumiTokens.PrimaryMinHeight

    /** Diameter of [YumiIconButton]. */
    public val IconButtonSize: Dp = YumiTokens.IconPebbleSize

    /** Size of [YumiFab]. */
    public val FabSize: Dp = 64.dp

    /** Padding inside a text key. */
    public val ContentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp)

    /** Padding inside [YumiTextButton]. */
    public val TextButtonContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

    /** Pill-rect shape of text keys. */
    public val shape: Shape
        @Composable @ReadOnlyComposable get() = YumiTheme.shapes.button

    /** Minimum height for [variant]. */
    public fun minHeight(variant: YumiButtonVariant): Dp =
        if (variant == YumiButtonVariant.Accent) AccentMinHeight else MinHeight

    /** Colours for [variant] under the current theme. */
    @Composable
    @ReadOnlyComposable
    public fun colors(variant: YumiButtonVariant = YumiButtonVariant.Neutral): YumiButtonColors {
        val c = YumiTheme.colors
        val (container, content) = when (variant) {
            YumiButtonVariant.Accent -> c.accent to c.onAccent
            YumiButtonVariant.Neutral -> c.raised to c.ink
            YumiButtonVariant.Charcoal -> c.charcoal to c.onCharcoal
        }
        return YumiButtonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = if (variant == YumiButtonVariant.Accent) c.raised else container,
            disabledContentColor = (if (variant == YumiButtonVariant.Accent) c.ink else content)
                .copy(alpha = YumiTokens.DisabledContentAlpha),
        )
    }

    /** Colours for [YumiTextButton]: no fill, ink label. */
    @Composable
    @ReadOnlyComposable
    public fun textButtonColors(): YumiButtonColors {
        val c = YumiTheme.colors
        return YumiButtonColors(
            containerColor = Color.Transparent,
            contentColor = c.ink,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = c.ink.copy(alpha = YumiTokens.DisabledContentAlpha),
        )
    }
}

/**
 * A raised pebble key.
 *
 * Sinks 1.5 dp and swaps its shadow on press; no ripple. A disabled key rests sunk. Long press is
 * supported on every key, so "long-press to edit" works the same everywhere.
 *
 * Keep it to one [YumiButtonVariant.Accent] key per screen.
 *
 * @param onClick called on click.
 * @param modifier modifier for the key.
 * @param variant finish of the key; sets default [colors] and minimum height.
 * @param enabled whether the key responds to input.
 * @param shape outline of the key.
 * @param colors colours for each state.
 * @param contentPadding padding around [content].
 * @param interactionSource source of press interactions; `null` creates one internally.
 * @param onLongClickLabel accessibility label for the long-press action.
 * @param onLongClick called on long press; `null` disables long press.
 * @param content label, usually a `Text` and optionally an `Icon`.
 */
@Composable
public fun YumiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: YumiButtonVariant = YumiButtonVariant.Neutral,
    enabled: Boolean = true,
    shape: Shape = YumiButtonDefaults.shape,
    colors: YumiButtonColors = YumiButtonDefaults.colors(variant),
    contentPadding: PaddingValues = YumiButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    PebbleKey(
        input = Modifier.pebbleClickable(
            interactionSource = source,
            enabled = enabled,
            role = Role.Button,
            haptics = YumiTheme.haptics,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onClick = onClick,
        ),
        interactionSource = source,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        containerColor = colors.containerColor(enabled),
        contentColor = colors.contentColor(enabled),
        minHeight = YumiButtonDefaults.minHeight(variant),
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * A round icon pebble, 56 dp, mirroring the remote's round keys.
 *
 * The icon inside must carry its own `contentDescription`.
 *
 * @param onClick called on click.
 * @param modifier modifier for the key.
 * @param enabled whether the key responds to input.
 * @param colors colours for each state.
 * @param interactionSource source of press interactions; `null` creates one internally.
 * @param onLongClickLabel accessibility label for the long-press action.
 * @param onLongClick called on long press; `null` disables long press.
 * @param content the icon.
 */
@Composable
public fun YumiIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: YumiButtonColors = YumiButtonDefaults.colors(YumiButtonVariant.Neutral),
    interactionSource: MutableInteractionSource? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    PebbleKey(
        input = Modifier.pebbleClickable(
            interactionSource = source,
            enabled = enabled,
            role = Role.Button,
            haptics = YumiTheme.haptics,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onClick = onClick,
        ),
        interactionSource = source,
        modifier = modifier.size(YumiButtonDefaults.IconButtonSize),
        enabled = enabled,
        shape = YumiTheme.shapes.icon,
        containerColor = colors.containerColor(enabled),
        contentColor = colors.contentColor(enabled),
    ) { content() }
}

/**
 * A flat label key with no fill or shadow, for use inside trays and cards where a raised pebble
 * would break the one-depth rule. Still physical: it dips in when pressed.
 *
 * @param onClick called on click.
 * @param modifier modifier for the key.
 * @param enabled whether the key responds to input.
 * @param colors colours for each state; the container is normally transparent.
 * @param contentPadding padding around [content].
 * @param interactionSource source of press interactions; `null` creates one internally.
 * @param onLongClickLabel accessibility label for the long-press action.
 * @param onLongClick called on long press; `null` disables long press.
 * @param content the label.
 */
@Composable
public fun YumiTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: YumiButtonColors = YumiButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = YumiButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    PebbleKey(
        input = Modifier.pebbleClickable(
            interactionSource = source,
            enabled = enabled,
            role = Role.Button,
            haptics = YumiTheme.haptics,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onClick = onClick,
        ),
        interactionSource = source,
        modifier = modifier,
        enabled = enabled,
        shape = YumiButtonDefaults.shape,
        containerColor = colors.containerColor(enabled),
        contentColor = colors.contentColor(enabled),
        seatColor = Color.Transparent,
        depth = YumiTheme.depth.flat(),
        minHeight = YumiButtonDefaults.MinHeight,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * The floating accent key for a screen's primary action. It *is* the screen's one accent key,
 * so don't combine it with a [YumiButtonVariant.Accent] button.
 *
 * @param onClick called on click.
 * @param modifier modifier for the key.
 * @param enabled whether the key responds to input.
 * @param shape outline of the key.
 * @param colors colours for each state.
 * @param interactionSource source of press interactions; `null` creates one internally.
 * @param onLongClickLabel accessibility label for the long-press action.
 * @param onLongClick called on long press; `null` disables long press.
 * @param content the icon, with its own `contentDescription`.
 */
@Composable
public fun YumiFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = YumiButtonDefaults.shape,
    colors: YumiButtonColors = YumiButtonDefaults.colors(YumiButtonVariant.Accent),
    interactionSource: MutableInteractionSource? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    PebbleKey(
        input = Modifier.pebbleClickable(
            interactionSource = source,
            enabled = enabled,
            role = Role.Button,
            haptics = YumiTheme.haptics,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onClick = onClick,
        ),
        interactionSource = source,
        modifier = modifier.size(YumiButtonDefaults.FabSize),
        enabled = enabled,
        shape = shape,
        containerColor = colors.containerColor(enabled),
        contentColor = colors.contentColor(enabled),
    ) { content() }
}

private fun Color.orIfUnspecified(fallback: Color): Color = if (this == Color.Unspecified) fallback else this
