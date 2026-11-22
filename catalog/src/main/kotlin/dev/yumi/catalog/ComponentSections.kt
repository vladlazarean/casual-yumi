package dev.yumi.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiButton
import dev.yumi.components.YumiButtonVariant
import dev.yumi.components.YumiCard
import dev.yumi.components.YumiCheckbox
import dev.yumi.components.YumiFab
import dev.yumi.components.YumiIconButton
import dev.yumi.components.YumiLed
import dev.yumi.components.YumiRadioButton
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
        CatalogGroup("YumiButton: Accent, Neutral, Charcoal") {
            CatalogFlow {
                YumiButton(onClick = { presses++ }, variant = YumiButtonVariant.Accent) { Text("Accent") }
                YumiButton(onClick = { presses++ }, onLongClick = { presses += 10 }) { Text("Neutral") }
                YumiButton(onClick = { presses++ }, variant = YumiButtonVariant.Charcoal) { Text("Charcoal") }
                YumiButton(onClick = {}, enabled = false) { Text("Disabled") }
            }
            CatalogNote("Presses: $presses (long-press Neutral adds 10)")
        }
        CatalogGroup("Icon button, FAB, text button, latch") {
            CatalogFlow {
                YumiIconButton(onClick = { presses++ }) { CatalogIcon(CatalogIcons.Add, "Add") }
                YumiFab(onClick = { presses++ }) { CatalogIcon(CatalogIcons.Alarm, "New alarm") }
                YumiTextButton(onClick = { presses++ }) { Text("Text button") }
                YumiToggleButton(checked = latched, onCheckedChange = { latched = it }) { Text("Latch") }
            }
        }
    }
}

@Composable
internal fun SelectionSection() {
    var rain by rememberSaveable { mutableStateOf(true) }
    var parent by rememberSaveable { mutableStateOf(ToggleableState.Indeterminate) }
    var switchOn by rememberSaveable { mutableStateOf(true) }
    var sound by rememberSaveable { mutableStateOf("Chime") }
    var volume by rememberSaveable { mutableStateOf("Soft") }
    CatalogSection("Selection") {
        CatalogGroup("Checkboxes") {
            CatalogFlow {
                Labeled("Rain") { YumiCheckbox(checked = rain, onCheckedChange = { rain = it }) }
                Labeled("All days") {
                    YumiTriStateCheckbox(
                        state = parent,
                        onClick = { parent = if (parent == ToggleableState.On) ToggleableState.Off else ToggleableState.On },
                    )
                }
                Labeled("Locked") { YumiCheckbox(checked = true, onCheckedChange = {}, enabled = false) }
            }
        }
        CatalogGroup("Switches") {
            CatalogFlow {
                Labeled("Alarm armed") { YumiSwitch(checked = switchOn, onCheckedChange = { switchOn = it }) }
                Labeled("Disabled") { YumiSwitch(checked = false, onCheckedChange = {}, enabled = false) }
            }
        }
        CatalogGroup("YumiRadioGroup") {
            YumiRadioGroup(
                options = listOf("Chime", "Bell", "Birds"),
                selected = sound,
                onSelect = { sound = it },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(it) }
        }
        CatalogGroup("YumiRadioButton on its own") {
            CatalogFlow {
                listOf("Soft", "Loud").forEach { option ->
                    Labeled(option) { YumiRadioButton(selected = volume == option, onClick = { volume = option }) }
                }
                Labeled("Disabled") { YumiRadioButton(selected = false, onClick = {}, enabled = false) }
            }
        }
    }
}

@Composable
internal fun SliderSection() {
    var continuous by rememberSaveable { mutableFloatStateOf(0.4f) }
    var stepped by rememberSaveable { mutableFloatStateOf(0.5f) }
    CatalogSection("Slider") {
        CatalogGroup("Continuous") {
            YumiSlider(value = continuous, onValueChange = { continuous = it })
        }
        CatalogGroup("Stepped: ${(stepped * 100).toInt()} %, a haptic tick on every step") {
            YumiSlider(value = stepped, onValueChange = { stepped = it }, steps = 4)
        }
        CatalogGroup("Disabled") {
            YumiSlider(value = 0.7f, onValueChange = {}, enabled = false)
        }
    }
}

@Composable
internal fun SurfacesSection() {
    var armed by rememberSaveable { mutableStateOf(true) }
    CatalogSection("Tray, card, LED") {
        CatalogGroup("YumiTray and YumiLed: recessed, read only") {
            YumiTray(Modifier.fillMaxWidth()) {
                CatalogRow {
                    YumiLed(on = armed, contentDescription = if (armed) "Armed" else "Off")
                    Text(if (armed) "Armed" else "Off", color = YumiTheme.colors.muted)
                }
                Text("07:30", style = YumiTheme.typography.displaySmall)
                YumiTextButton(onClick = { armed = !armed }) { Text(if (armed) "Disarm" else "Arm") }
            }
        }
        CatalogGroup("YumiCard") {
            YumiCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Raised card", style = YumiTheme.typography.titleMedium)
                    Text("A key inside a card draws flat: one depth.", color = YumiTheme.colors.muted)
                }
                YumiButton(onClick = {}, modifier = Modifier.padding(top = 16.dp)) { Text("Flat key") }
            }
            YumiCard(onClick = {}, onLongClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Clickable card: the whole card sinks", style = YumiTheme.typography.titleMedium)
            }
        }
    }
}
