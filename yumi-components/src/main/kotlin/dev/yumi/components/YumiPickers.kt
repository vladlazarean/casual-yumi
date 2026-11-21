package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme

/**
 * Time picker dialog built on [YumiTimeDial]. The dial's centre key confirms, exactly like the
 * remote's select key; the accent "OK" key does the same for users who don't know that.
 *
 * @param onDismissRequest called on back, outside tap or the dismiss key.
 * @param onConfirm called with the chosen hour (0..23) and minute.
 * @param modifier modifier for the dialog panel.
 * @param initialHour starting hour.
 * @param initialMinute starting minute.
 * @param title dialog title.
 * @param confirmLabel label of the accent confirm key.
 * @param dismissLabel label of the flat dismiss key.
 */
@Composable
public fun YumiTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    initialHour: Int = 7,
    initialMinute: Int = 0,
    title: String = stringResource(R.string.yumi_set_time),
    confirmLabel: String = stringResource(R.string.yumi_ok),
    dismissLabel: String = stringResource(R.string.yumi_cancel),
) {
    var hour by rememberSaveable { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var minute by rememberSaveable { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }
    YumiDialog(onDismissRequest = onDismissRequest, modifier = modifier) {
        Text(title, style = YumiTheme.typography.headlineSmall, color = YumiTheme.colors.ink)
        YumiTimeDial(
            hour = hour,
            minute = minute,
            onTimeChange = { h, m -> hour = h; minute = m },
            onConfirm = { onConfirm(hour, minute) },
            modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            YumiTextButton(onClick = onDismissRequest) { Text(dismissLabel) }
            YumiButton(onClick = { onConfirm(hour, minute) }, variant = YumiButtonVariant.Accent) { Text(confirmLabel) }
        }
    }
}

/**
 * Date picker dialog: Material 3's calendar (locale, first weekday, input mode, accessibility)
 * dressed in Yumi colours. The selected day is a sunk tray with accent-ink text, not an orange
 * fill: the only accent key is "OK".
 *
 * @param onDismissRequest called on back, outside tap or the dismiss key.
 * @param onConfirm called with the selected date as UTC midnight epoch millis.
 * @param modifier modifier for the dialog.
 * @param initialSelectedDateMillis initially selected date (UTC midnight millis), or `null`.
 * @param title dialog title.
 * @param confirmLabel label of the accent confirm key; disabled until a date is selected.
 * @param dismissLabel label of the flat dismiss key.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiDatePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (selectedDateMillis: Long) -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedDateMillis: Long? = null,
    title: String = stringResource(R.string.yumi_select_date),
    confirmLabel: String = stringResource(R.string.yumi_ok),
    dismissLabel: String = stringResource(R.string.yumi_cancel),
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)
    val colors = yumiDatePickerColors()
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val selected = state.selectedDateMillis
            YumiButton(
                onClick = { if (selected != null) onConfirm(selected) },
                variant = YumiButtonVariant.Accent,
                enabled = selected != null,
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
        DatePicker(
            state = state,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun yumiDatePickerColors(): DatePickerColors {
    val c = YumiTheme.colors
    return DatePickerDefaults.colors(
        containerColor = c.body,
        titleContentColor = c.muted,
        headlineContentColor = c.ink,
        weekdayContentColor = c.muted,
        subheadContentColor = c.muted,
        navigationContentColor = c.ink,
        yearContentColor = c.ink,
        currentYearContentColor = c.accentInk,
        selectedYearContentColor = c.accentInk,
        selectedYearContainerColor = c.tray,
        dayContentColor = c.ink,
        selectedDayContentColor = c.accentInk,
        selectedDayContainerColor = c.tray,
        todayContentColor = c.accentInk,
        todayDateBorderColor = c.seat,
        dayInSelectionRangeContainerColor = c.tray,
        dayInSelectionRangeContentColor = c.ink,
        dividerColor = c.seat,
    )
}
