package dev.yumi.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiButton
import dev.yumi.components.YumiButtonVariant
import dev.yumi.components.YumiCard
import dev.yumi.components.YumiCheckbox
import dev.yumi.components.YumiFab
import dev.yumi.components.YumiIconButton
import dev.yumi.components.YumiLed
import dev.yumi.components.YumiRadioGroup
import dev.yumi.components.YumiSlider
import dev.yumi.components.YumiSwitch
import dev.yumi.components.YumiTextButton
import dev.yumi.components.YumiToggleButton
import dev.yumi.components.YumiTray
import dev.yumi.components.YumiTriStateCheckbox
import dev.yumi.foundation.YumiTheme

@Composable
internal fun ButtonsSection() {
    var presses by rememberSaveable { mutableIntStateOf(0) }
    var latched by rememberSaveable { mutableStateOf(true) }
    CatalogSection("Buttons") {
        CatalogFlow {
            YumiButton(onClick = { presses++ }, variant = YumiButtonVariant.Accent) { Text("Accent") }
            YumiButton(onClick = { presses++ }, onLongClick = { presses += 10 }) { Text("Neutral") }
            YumiButton(onClick = { presses++ }, variant = YumiButtonVariant.Charcoal) { Text("Charcoal") }
            YumiButton(onClick = {}, enabled = false) { Text("Disabled") }
        }
        CatalogFlow {
            YumiIconButton(onClick = { presses++ }) {
                Text("+", Modifier.clearAndSetSemantics { contentDescription = "Add" })
            }
            YumiFab(onClick = { presses++ }) { Text("Go") }
            YumiTextButton(onClick = { presses++ }) { Text("Text button") }
            YumiToggleButton(checked = latched, onCheckedChange = { latched = it }) { Text("Latch") }
        }
        CatalogNote("Presses: $presses (long-press Neutral adds 10)")
    }
}

@Composable
internal fun SelectionSection() {
    var checked by rememberSaveable { mutableStateOf(true) }
    var parent by rememberSaveable { mutableStateOf(ToggleableState.Indeterminate) }
    var switchOn by rememberSaveable { mutableStateOf(true) }
    var theme by rememberSaveable { mutableStateOf("Graphite") }
    CatalogSection("Selection") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            YumiCheckbox(checked = checked, onCheckedChange = { checked = it })
            YumiTriStateCheckbox(
                state = parent,
                onClick = {
                    parent = if (parent == ToggleableState.On) ToggleableState.Off else ToggleableState.On
                },
            )
            YumiCheckbox(checked = true, onCheckedChange = {}, enabled = false)
            YumiSwitch(checked = switchOn, onCheckedChange = { switchOn = it })
            YumiSwitch(checked = false, onCheckedChange = {}, enabled = false)
        }
        YumiRadioGroup(
            options = listOf("Putty", "Graphite", "Follow system"),
            selected = theme,
            onSelect = { theme = it },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(it) }
    }
}

@Composable
internal fun SliderSection() {
    var continuous by rememberSaveable { mutableFloatStateOf(0.4f) }
    var stepped by rememberSaveable { mutableFloatStateOf(0.5f) }
    CatalogSection("Slider") {
        YumiSlider(value = continuous, onValueChange = { continuous = it })
        YumiSlider(value = stepped, onValueChange = { stepped = it }, steps = 4)
        YumiSlider(value = 0.7f, onValueChange = {}, enabled = false)
        CatalogNote("Stepped: ${(stepped * 100).toInt()} %, haptic tick on every step")
    }
}

@Composable
internal fun SurfacesSection() {
    var armed by rememberSaveable { mutableStateOf(true) }
    CatalogSection("Tray, card, LED") {
        YumiTray(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                YumiLed(on = armed, contentDescription = if (armed) "Armed" else "Off")
                Text(if (armed) "Armed" else "Off", color = YumiTheme.colors.muted)
            }
            Text("07:30", style = YumiTheme.typography.displaySmall)
            YumiTextButton(onClick = { armed = !armed }) { Text(if (armed) "Disarm" else "Arm") }
        }
        YumiCard(Modifier.fillMaxWidth()) {
            Text("Raised card", style = YumiTheme.typography.titleMedium)
            Text("A pebble inside a card renders flat: one depth.", color = YumiTheme.colors.muted)
            YumiButton(onClick = {}) { Text("Flat key") }
        }
        YumiCard(onClick = {}, onLongClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Clickable card: the whole card sinks", style = YumiTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CatalogSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(title, style = YumiTheme.typography.titleLarge, color = YumiTheme.colors.ink)
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogFlow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

@Composable
private fun CatalogNote(text: String) {
    Text(text, style = YumiTheme.typography.bodySmall, color = YumiTheme.colors.muted)
}
