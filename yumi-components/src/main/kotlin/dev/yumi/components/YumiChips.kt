package dev.yumi.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import dev.yumi.components.internal.PebbleKey
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.pebbleClickable
import dev.yumi.tokens.YumiTokens

/** Defaults for chips and segmented buttons. */
public object YumiChipDefaults {
    /** Visual height of a chip (touch target is still 48 dp). */
    public val Height: Dp = 40.dp

    /** Chip shape. */
    public val shape: Shape
        @Composable get() = YumiTheme.shapes.small

    /** Padding inside a chip. */
    public val ContentPadding: PaddingValues = PaddingValues(horizontal = 14.dp)
}

/**
 * A filter chip. Selected = sunk + accent-ink text, never a colour fill, with a check glyph
 * unless you pass your own [leadingIcon].
 *
 * @param selected whether the filter is on.
 * @param onClick called on click.
 * @param label chip label, usually a `Text`.
 * @param modifier modifier for the chip.
 * @param enabled whether the chip responds to input.
 * @param leadingIcon optional icon; replaces the default check when selected.
 * @param interactionSource source of press interactions; `null` creates one internally.
 */
@Composable
public fun YumiFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val haptics = YumiTheme.haptics
    val state = stringResource(if (selected) R.string.yumi_state_selected else R.string.yumi_state_not_selected)
    ChipKey(
        input = Modifier
            .toggleable(
                value = selected,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = {
                    haptics.toggle(it)
                    onClick()
                },
            )
            .semantics { stateDescription = state },
        source = source,
        modifier = modifier,
        enabled = enabled,
        selected = selected,
    ) {
        when {
            leadingIcon != null -> leadingIcon()
            selected -> GlyphIcon(Glyph.Check, size = 18.dp)
        }
        label()
    }
}

/**
 * An assist chip: a small neutral key for a contextual action ("Add to calendar").
 *
 * @param onClick called on click.
 * @param label chip label.
 * @param modifier modifier for the chip.
 * @param enabled whether the chip responds to input.
 * @param leadingIcon optional icon.
 * @param interactionSource source of press interactions; `null` creates one internally.
 */
@Composable
public fun YumiAssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    ChipKey(
        input = Modifier.pebbleClickable(source, enabled, Role.Button, YumiTheme.haptics, onClick = onClick),
        source = source,
        modifier = modifier,
        enabled = enabled,
        selected = false,
    ) {
        leadingIcon?.invoke()
        label()
    }
}

/**
 * An input chip: a value the user entered (a tag, a recipient) with a remove key.
 *
 * @param label chip label.
 * @param onRemove called when the remove key is pressed.
 * @param modifier modifier for the chip.
 * @param onClick optional click on the chip body, e.g. to edit.
 * @param enabled whether the chip responds to input.
 * @param leadingIcon optional icon.
 * @param removeContentDescription accessibility label of the remove key; include the value,
 *   e.g. "Remove Rain".
 */
@Composable
public fun YumiInputChip(
    label: @Composable () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    removeContentDescription: String = stringResource(R.string.yumi_remove),
) {
    val source = remember { MutableInteractionSource() }
    ChipKey(
        input = if (onClick != null) {
            Modifier.pebbleClickable(source, enabled, Role.Button, YumiTheme.haptics, onClick = onClick)
        } else {
            Modifier
        },
        source = source,
        modifier = modifier,
        enabled = enabled,
        selected = false,
        endPadding = 0.dp,
    ) {
        leadingIcon?.invoke()
        label()
        Box(
            Modifier
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    interactionSource = null,
                    indication = null,
                    onClick = onRemove,
                )
                .semantics { contentDescription = removeContentDescription },
            contentAlignment = Alignment.Center,
        ) { GlyphIcon(Glyph.Close, size = 16.dp) }
    }
}

/**
 * A single-choice segmented row: a strip of keys sitting side by side in one seat. The selected
 * key rests sunk with accent-ink text (no colour fill); the others stay raised.
 *
 * @param options the options, in display order.
 * @param selected the selected option.
 * @param onSelect called with the option the user picked.
 * @param modifier modifier for the row.
 * @param enabled whether the row responds to input.
 * @param label content for one segment, usually a `Text`.
 */
@Composable
public fun <T> YumiSegmentedButtonRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (T) -> Unit,
) {
    val haptics = YumiTheme.haptics
    val colors = YumiTheme.colors
    Row(modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            val source = remember(option) { MutableInteractionSource() }
            PebbleKey(
                input = Modifier.selectable(
                    selected = isSelected,
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = {
                        if (!isSelected) haptics.toggle(true)
                        onSelect(option)
                    },
                ),
                interactionSource = source,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                shape = segmentShape(index, options.size),
                containerColor = colors.raised,
                contentColor = when {
                    !enabled -> colors.ink.copy(alpha = YumiTokens.DisabledContentAlpha)
                    isSelected -> colors.accentInk
                    else -> colors.ink
                },
                selected = isSelected,
                minHeight = YumiTokens.SecondaryMinHeight,
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) { label(option) }
        }
    }
}

@Composable
private fun ChipKey(
    input: Modifier,
    source: MutableInteractionSource,
    modifier: Modifier,
    enabled: Boolean,
    selected: Boolean,
    endPadding: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val colors = YumiTheme.colors
    PebbleKey(
        input = input,
        interactionSource = source,
        modifier = modifier,
        enabled = enabled,
        shape = YumiChipDefaults.shape,
        containerColor = colors.raised,
        contentColor = when {
            !enabled -> colors.ink.copy(alpha = YumiTokens.DisabledContentAlpha)
            selected -> colors.accentInk
            else -> colors.ink
        },
        selected = selected,
        minHeight = YumiChipDefaults.Height,
        contentPadding = PaddingValues(start = 14.dp, end = endPadding),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            content()
        }
    }
}

internal fun segmentShape(index: Int, count: Int): Shape {
    val outer = 24.dp
    val inner = 6.dp
    return when {
        count == 1 -> RoundedCornerShape(outer)
        index == 0 -> RoundedCornerShape(topStart = outer, bottomStart = outer, topEnd = inner, bottomEnd = inner)
        index == count - 1 -> RoundedCornerShape(topStart = inner, bottomStart = inner, topEnd = outer, bottomEnd = outer)
        else -> RoundedCornerShape(inner)
    }
}
