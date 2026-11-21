package dev.yumi.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import dev.yumi.components.internal.PebbleKey
import dev.yumi.foundation.YumiTheme
import dev.yumi.tokens.YumiTokens

// ── Select field ───────────────────────────────────────────────────────────

/**
 * A select field (exposed dropdown): the recessed text-field well, read-only, that opens a
 * [YumiDropdownMenu] of [options] as wide as the field. The selected option shows a check.
 *
 * @param options the choices.
 * @param selected the current choice, or `null` for none.
 * @param onSelect called with the option the user picked.
 * @param modifier modifier for the field.
 * @param label label above the well.
 * @param placeholder shown while nothing is selected.
 * @param supportingText help or error text below the well.
 * @param isError whether the selection is invalid.
 * @param enabled whether the field opens.
 * @param optionLabel how to display an option.
 */
@Composable
public fun <T> YumiSelectField(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    optionLabel: (T) -> String = { it.toString() },
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val selectedText = selected?.let(optionLabel)
    val chevronDegrees = if (expanded) 180f else 0f
    Box(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .onSizeChanged { fieldWidth = it.width }
                .alpha(if (enabled) 1f else YumiTokens.DisabledContentAlpha)
                .clickable(
                    enabled = enabled,
                    role = Role.DropdownList,
                    interactionSource = null,
                    indication = null,
                    onClick = { expanded = true },
                ),
        ) {
            FieldFrame(
                label = label,
                placeholder = placeholder,
                showPlaceholder = selectedText == null,
                supportingText = supportingText,
                isError = isError,
                focused = expanded,
                shape = YumiTextFieldDefaults.shape,
                leadingIcon = null,
                trailingIcon = {
                    Box(Modifier.padding(end = 10.dp)) { GlyphIcon(Glyph.ChevronDown, Modifier.rotate(chevronDegrees)) }
                },
                innerTextField = {
                    if (selectedText != null) {
                        Text(selectedText, style = YumiTheme.typography.bodyLarge, color = YumiTheme.colors.ink, maxLines = 1)
                    }
                },
            )
        }
        YumiDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(density) { fieldWidth.toDp() }),
        ) {
            options.forEach { option ->
                YumiDropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    trailingIcon = if (option == selected) {
                        { GlyphIcon(Glyph.Check, color = YumiTheme.colors.accentInk) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

// ── Date range picker ──────────────────────────────────────────────────────

/**
 * Date range picker dialog: Material 3's range calendar in Yumi colours. The range is a tray
 * band with accent-ink ends; "OK" (the accent key) enables once both ends are picked.
 *
 * @param onDismissRequest called on back, outside tap or the dismiss key.
 * @param onConfirm called with the start and end dates as UTC midnight epoch millis.
 * @param modifier modifier for the dialog.
 * @param initialSelectedStartDateMillis initial start (UTC millis), or `null`.
 * @param initialSelectedEndDateMillis initial end (UTC millis), or `null`.
 * @param title dialog title.
 * @param confirmLabel label of the accent confirm key.
 * @param dismissLabel label of the flat dismiss key.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiDateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedStartDateMillis: Long? = null,
    initialSelectedEndDateMillis: Long? = null,
    title: String = stringResource(R.string.yumi_select_dates),
    confirmLabel: String = stringResource(R.string.yumi_ok),
    dismissLabel: String = stringResource(R.string.yumi_cancel),
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialSelectedStartDateMillis,
        initialSelectedEndDateMillis = initialSelectedEndDateMillis,
    )
    // The range band is the recessed tray, so tray-coloured end caps would vanish into it.
    // Ends use the strong neutral (charcoal) instead: still no orange on a day.
    val yc = YumiTheme.colors
    val colors = yumiDatePickerColors().copy(
        selectedDayContainerColor = yc.charcoal,
        selectedDayContentColor = yc.onCharcoal,
    )
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val start = state.selectedStartDateMillis
            val end = state.selectedEndDateMillis
            YumiButton(
                onClick = { if (start != null && end != null) onConfirm(start, end) },
                variant = YumiButtonVariant.Accent,
                enabled = start != null && end != null,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
            ) { Text(confirmLabel) }
        },
        modifier = modifier,
        dismissButton = {
            YumiTextButton(onClick = onDismissRequest, modifier = Modifier.padding(bottom = 8.dp)) { Text(dismissLabel) }
        },
        shape = YumiTheme.shapes.card,
        tonalElevation = 0.dp,
        colors = colors,
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.weight(1f),
            colors = colors,
            title = {
                Text(
                    title,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    style = YumiTheme.typography.labelLarge,
                    color = YumiTheme.colors.muted,
                )
            },
        )
    }
}

// ── Multi-choice segments ──────────────────────────────────────────────────

/**
 * A multi-choice segmented row, e.g. weekdays for a repeating alarm. Each selected key rests sunk
 * with accent-ink text; any number can be on.
 *
 * @param options the options, in display order.
 * @param selected the options that are on.
 * @param onSelectionChange called with the new set.
 * @param modifier modifier for the row.
 * @param enabled whether the row responds to input.
 * @param label content for one segment.
 */
@Composable
public fun <T> YumiMultiChoiceSegmentedButtonRow(
    options: List<T>,
    selected: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (T) -> Unit,
) {
    val haptics = YumiTheme.haptics
    val colors = YumiTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEachIndexed { index, option ->
            val on = option in selected
            val source = remember(option) { MutableInteractionSource() }
            PebbleKey(
                input = Modifier.toggleable(
                    value = on,
                    interactionSource = source,
                    indication = null,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = { now ->
                        haptics.toggle(now)
                        onSelectionChange(if (now) selected + option else selected - option)
                    },
                ),
                interactionSource = source,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                shape = segmentShape(index, options.size),
                containerColor = colors.raised,
                contentColor = when {
                    !enabled -> colors.ink.copy(alpha = YumiTokens.DisabledContentAlpha)
                    on -> colors.accentInk
                    else -> colors.ink
                },
                selected = on,
                minHeight = YumiTokens.SecondaryMinHeight,
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) { label(option) }
        }
    }
}

// ── Icon toggle ────────────────────────────────────────────────────────────

/**
 * A round latching icon key (mute, favourite, flashlight): stays sunk while [checked], with a
 * small LED under the icon.
 *
 * @param checked whether it is on.
 * @param onCheckedChange called with the new state.
 * @param modifier modifier for the key.
 * @param enabled whether the key responds to input.
 * @param colors colours for each state.
 * @param interactionSource source of press interactions; `null` creates one internally.
 * @param content the icon, with its own `contentDescription`.
 */
@Composable
public fun YumiIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: YumiButtonColors = YumiButtonDefaults.colors(YumiButtonVariant.Neutral),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val haptics = YumiTheme.haptics
    val state = stringResource(if (checked) R.string.yumi_state_on else R.string.yumi_state_off)
    PebbleKey(
        input = Modifier
            .toggleable(
                value = checked,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { on ->
                    haptics.toggle(on)
                    onCheckedChange(on)
                },
            )
            .semantics { stateDescription = state },
        interactionSource = source,
        modifier = modifier.size(YumiButtonDefaults.IconButtonSize),
        enabled = enabled,
        shape = YumiTheme.shapes.icon,
        containerColor = colors.containerColor(enabled),
        contentColor = if (checked && enabled) YumiTheme.colors.accentInk else colors.contentColor(enabled),
        selected = checked,
    ) {
        Box(Modifier.size(YumiButtonDefaults.IconButtonSize), contentAlignment = Alignment.Center) {
            content()
            YumiLed(on = checked && enabled, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 9.dp))
        }
    }
}

// ── Split button ───────────────────────────────────────────────────────────

/**
 * A split button: a main key and a narrow chevron key in one seat. The main key does the common
 * action; the chevron opens a menu of related ones ("Snooze" / "Snooze 5, 10, 15 min").
 *
 * @param onClick the main action.
 * @param menuContent menu items; call the given `close` when an item is chosen.
 * @param modifier modifier for the pair.
 * @param variant finish of both keys.
 * @param enabled whether the keys respond to input.
 * @param menuContentDescription accessibility label of the chevron key.
 * @param content label of the main key.
 */
@Composable
public fun YumiSplitButton(
    onClick: () -> Unit,
    menuContent: @Composable ColumnScope.(close: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    variant: YumiButtonVariant = YumiButtonVariant.Neutral,
    enabled: Boolean = true,
    menuContentDescription: String = stringResource(R.string.yumi_more_options),
    content: @Composable RowScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = YumiButtonDefaults.colors(variant)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        YumiButton(
            onClick = onClick,
            variant = variant,
            enabled = enabled,
            shape = segmentShape(0, 2),
            colors = colors,
            content = content,
        )
        Box {
            YumiButton(
                onClick = { expanded = !expanded },
                variant = variant,
                enabled = enabled,
                shape = segmentShape(1, 2),
                colors = colors,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.semantics { contentDescription = menuContentDescription },
            ) { GlyphIcon(Glyph.ChevronDown, Modifier.rotate(if (expanded) 180f else 0f)) }
            YumiDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                menuContent(this) { expanded = false }
            }
        }
    }
}

// ── Rating ─────────────────────────────────────────────────────────────────

/** Defaults for [YumiRatingBar]. */
public object YumiRatingDefaults {
    /** Visual size of one star. */
    public val StarSize: Dp = 28.dp
}

/**
 * A star rating. Filled stars use the accent (an "on" indicator), empty stars the seat grey.
 * Interactive when [onRatingChange] is set: each star is a 48 dp radio target ("3 stars").
 * Display-only otherwise, read as "4 of 5".
 *
 * @param rating current rating, 0..[max].
 * @param onRatingChange called with the tapped rating; `null` makes it display-only.
 * @param modifier modifier for the row.
 * @param max number of stars.
 * @param enabled whether the stars respond to input.
 */
@Composable
public fun YumiRatingBar(
    rating: Int,
    onRatingChange: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    max: Int = 5,
    enabled: Boolean = true,
) {
    val colors = YumiTheme.colors
    val haptics = YumiTheme.haptics
    val summary = stringResource(R.string.yumi_rating_of, rating, max)
    val rowModifier = if (onRatingChange == null) {
        modifier.clearAndSetSemantics { contentDescription = summary }
    } else {
        modifier.selectableGroup()
    }
    Row(rowModifier.alpha(if (enabled) 1f else YumiTokens.DisabledContentAlpha), verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..max) {
            val filled = i <= rating
            val starLabel = stringResource(R.string.yumi_star_n, i)
            val star: @Composable () -> Unit = {
                GlyphIcon(
                    if (filled) Glyph.StarFilled else Glyph.Star,
                    size = YumiRatingDefaults.StarSize,
                    color = if (filled) colors.accent else colors.seat,
                )
            }
            if (onRatingChange == null) {
                Box(Modifier.padding(2.dp)) { star() }
            } else {
                Box(
                    Modifier
                        .minimumInteractiveComponentSize()
                        .selectable(
                            selected = rating == i,
                            enabled = enabled,
                            role = Role.RadioButton,
                            interactionSource = null,
                            indication = null,
                            onClick = {
                                haptics.tick()
                                onRatingChange(i)
                            },
                        )
                        .semantics { contentDescription = starLabel },
                    contentAlignment = Alignment.Center,
                ) { star() }
            }
        }
    }
}
