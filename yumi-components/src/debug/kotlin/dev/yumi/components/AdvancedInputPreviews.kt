package dev.yumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme

// Debug source set: never part of the published release AAR.

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    YumiTheme {
        Column(
            Modifier.background(YumiTheme.colors.body).padding(20.dp).width(360.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}

@PreviewLightDark
@Composable
private fun TimeDialPreview() = PreviewSurface {
    YumiTimeDial(hour = 7, minute = 30, onTimeChange = { _, _ -> }, onConfirm = {})
}

@PreviewLightDark
@Composable
private fun SlidersPreview() = PreviewSurface {
    YumiRangeSlider(value = 0.2f..0.7f, onValueChange = {})
    YumiCircularSlider(value = 0.6f, onValueChange = {}) { Text("60 %") }
}

@PreviewLightDark
@Composable
private fun FieldsPreview() = PreviewSurface {
    YumiTextField(value = "Morning run", onValueChange = {}, label = "Alarm name", supportingText = "Shown on the lock screen")
    YumiTextField(value = "25:99", onValueChange = {}, label = "Time", isError = true, supportingText = "Use 24-hour time")
    YumiSearchField(query = "rain", onQueryChange = {}, onSearch = {})
    YumiStepper(value = 9, onValueChange = {}, range = 1..30, label = "Snooze minutes")
}

@PreviewLightDark
@Composable
private fun ChipsPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        YumiFilterChip(selected = true, onClick = {}, label = { Text("Rain") })
        YumiFilterChip(selected = false, onClick = {}, label = { Text("Wind") })
        YumiInputChip(label = { Text("Gym") }, onRemove = {})
    }
    YumiSegmentedButtonRow(options = listOf("Day", "Week", "Month"), selected = "Week", onSelect = {}) { Text(it) }
}
