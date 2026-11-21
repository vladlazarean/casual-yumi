package dev.yumi.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiAssistChip
import dev.yumi.components.YumiButton
import dev.yumi.components.YumiCircularSlider
import dev.yumi.components.YumiDatePickerDialog
import dev.yumi.components.YumiFilterChip
import dev.yumi.components.YumiInputChip
import dev.yumi.components.YumiRangeSlider
import dev.yumi.components.YumiSearchField
import dev.yumi.components.YumiSecureTextField
import dev.yumi.components.YumiSegmentedButtonRow
import dev.yumi.components.YumiStepper
import dev.yumi.components.YumiTextField
import dev.yumi.components.YumiTimeDial
import dev.yumi.components.YumiTimeDialDefaults
import dev.yumi.components.YumiTimePickerDialog
import dev.yumi.foundation.YumiTheme
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AdvancedInputSection() {
    val toasts = LocalCatalogToasts.current
    val scope = rememberCoroutineScope()
    var low by rememberSaveable { mutableFloatStateOf(0.2f) }
    var high by rememberSaveable { mutableFloatStateOf(0.7f) }
    var knob by rememberSaveable { mutableFloatStateOf(0.6f) }
    var hour by rememberSaveable { mutableIntStateOf(7) }
    var minute by rememberSaveable { mutableIntStateOf(30) }
    var name by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var snooze by rememberSaveable { mutableIntStateOf(9) }
    var rain by rememberSaveable { mutableStateOf(true) }
    var wind by rememberSaveable { mutableStateOf(false) }
    var tags by rememberSaveable { mutableStateOf(listOf("Weekdays", "Gym")) }
    var period by rememberSaveable { mutableStateOf("Week") }
    var timeDialog by rememberSaveable { mutableStateOf(false) }
    var dateDialog by rememberSaveable { mutableStateOf(false) }
    val password = rememberTextFieldState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Advanced input", style = YumiTheme.typography.titleLarge, color = YumiTheme.colors.ink)

        Label("YumiRangeSlider · ${(low * 100).toInt()}–${(high * 100).toInt()} %")
        YumiRangeSlider(value = low..high, onValueChange = { low = it.start; high = it.endInclusive })

        Label("YumiCircularSlider and YumiTimeDial")
        YumiCircularSlider(value = knob, onValueChange = { knob = it }, steps = 9, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("${(knob * 100).toInt()} %", style = YumiTheme.typography.titleLarge, color = YumiTheme.colors.ink)
        }
        YumiTimeDial(
            hour = hour,
            minute = minute,
            onTimeChange = { h, m -> hour = h; minute = m },
            onConfirm = { scope.launch { toasts.show("Saved ${YumiTimeDialDefaults.format(hour, minute)}") } },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            YumiButton(onClick = { timeDialog = true }) { Text("Time picker") }
            YumiButton(onClick = { dateDialog = true }) { Text("Date picker") }
        }

        Label("Fields")
        YumiTextField(
            value = name,
            onValueChange = { name = it },
            label = "Alarm name",
            placeholder = "Morning run",
            isError = name.length > 20,
            supportingText = if (name.length > 20) "Keep it under 20 characters" else "Shown on the lock screen",
        )
        YumiSecureTextField(state = password, label = "Password")
        YumiSearchField(query = query, onQueryChange = { query = it }, onSearch = { scope.launch { toasts.show("Searching “$it”") } })
        YumiStepper(value = snooze, onValueChange = { snooze = it }, range = 1..30, label = "Snooze minutes", valueText = { "$it min" })

        Label("Chips and segments")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            YumiFilterChip(selected = rain, onClick = { rain = !rain }, label = { Text("Rain") })
            YumiFilterChip(selected = wind, onClick = { wind = !wind }, label = { Text("Wind") })
            YumiAssistChip(onClick = { scope.launch { toasts.show("Added") } }, label = { Text("Add to calendar") })
            tags.forEach { tag ->
                YumiInputChip(label = { Text(tag) }, onRemove = { tags = tags - tag }, removeContentDescription = "Remove $tag")
            }
        }
        YumiSegmentedButtonRow(options = listOf("Day", "Week", "Month"), selected = period, onSelect = { period = it }) { Text(it) }
    }

    if (timeDialog) {
        YumiTimePickerDialog(
            onDismissRequest = { timeDialog = false },
            onConfirm = { h, m ->
                hour = h
                minute = m
                timeDialog = false
            },
            initialHour = hour,
            initialMinute = minute,
        )
    }
    if (dateDialog) {
        YumiDatePickerDialog(
            onDismissRequest = { dateDialog = false },
            onConfirm = { millis ->
                dateDialog = false
                val format = DateFormat.getDateInstance(DateFormat.MEDIUM).apply { timeZone = TimeZone.getTimeZone("UTC") }
                scope.launch { toasts.show("Picked ${format.format(Date(millis))}") }
            },
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = YumiTheme.typography.labelMedium, color = YumiTheme.colors.muted)
}
