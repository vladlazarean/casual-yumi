package dev.yumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme

// Debug source set: never part of the published release AAR.

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    YumiTheme {
        Column(
            Modifier.background(YumiTheme.colors.body).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}

@PreviewLightDark
@Composable
private fun ButtonsPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        YumiButton(onClick = {}, variant = YumiButtonVariant.Accent) { Text("Accent") }
        YumiButton(onClick = {}) { Text("Neutral") }
        YumiButton(onClick = {}, enabled = false) { Text("Disabled") }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        YumiButton(onClick = {}, variant = YumiButtonVariant.Charcoal) { Text("Charcoal") }
        YumiIconButton(onClick = {}) { Text("+") }
        YumiTextButton(onClick = {}) { Text("Text") }
        YumiToggleButton(checked = true, onCheckedChange = {}) { Text("Latch") }
    }
}

@PreviewLightDark
@Composable
private fun SelectionPreview() = PreviewSurface {
    Row(verticalAlignment = Alignment.CenterVertically) {
        YumiCheckbox(checked = false, onCheckedChange = {})
        YumiCheckbox(checked = true, onCheckedChange = {})
        YumiTriStateCheckbox(state = ToggleableState.Indeterminate, onClick = {})
        YumiRadioButton(selected = true, onClick = {})
        YumiSwitch(checked = true, onCheckedChange = {})
        YumiSwitch(checked = false, onCheckedChange = {})
    }
}

@PreviewLightDark
@Composable
private fun SliderPreview() = PreviewSurface {
    Column(Modifier.width(320.dp)) {
        YumiSlider(value = 0.4f, onValueChange = {})
        YumiSlider(value = 0.5f, onValueChange = {}, steps = 4)
    }
}

@PreviewLightDark
@Composable
private fun SurfacesPreview() = PreviewSurface {
    YumiTray(Modifier.width(320.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            YumiLed(on = true)
            Text("Armed")
        }
    }
    YumiCard(Modifier.width(320.dp)) {
        Text("Card")
        YumiButton(onClick = {}) { Text("Flat inside card") }
    }
}
