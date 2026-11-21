package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Milestone 4: sliders, dial, pickers, fields, chips and segments, in Putty and Graphite. */
@OptIn(ExperimentalRoborazziApi::class, ExperimentalLayoutApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w420dp-h900dp-xhdpi")
class AdvancedInputScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sliders() = snapshot("advanced_sliders") {
        Column(Modifier.width(380.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            YumiRangeSlider(value = 0.2f..0.7f, onValueChange = {})
            YumiRangeSlider(value = 0.25f..0.75f, onValueChange = {}, steps = 3)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                YumiCircularSlider(value = 0.6f, onValueChange = {}, modifier = Modifier.size(180.dp)) {
                    Text("60 %")
                }
                YumiCircularSlider(value = 0.3f, onValueChange = {}, enabled = false, modifier = Modifier.size(140.dp))
            }
        }
    }

    @Test
    fun timeDial() = snapshot("time_dial") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            YumiTimeDial(hour = 7, minute = 30, onTimeChange = { _, _ -> }, onConfirm = {}, modifier = Modifier.size(260.dp))
        }
    }

    @Test
    fun timeDialReadOnly() = snapshot("time_dial_readonly") {
        YumiTimeDial(hour = 18, minute = 45, onTimeChange = { _, _ -> }, modifier = Modifier.size(220.dp))
    }

    @Test
    fun textFields() = snapshot("text_fields") {
        Column(Modifier.width(380.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            YumiTextField(value = "", onValueChange = {}, label = "Alarm name", placeholder = "Morning run")
            YumiTextField(value = "Morning run", onValueChange = {}, label = "Alarm name", supportingText = "Shown on the lock screen")
            YumiTextField(value = "25:99", onValueChange = {}, label = "Time", isError = true, supportingText = "Use 24-hour time, e.g. 07:30")
            YumiTextField(value = "Locked", onValueChange = {}, label = "Disabled", enabled = false)
            YumiSecureTextField(state = rememberTextFieldState("hunter2"), label = "Password")
            YumiSearchField(query = "rain", onQueryChange = {}, onSearch = {})
            YumiStepper(value = 9, onValueChange = {}, range = 1..30, label = "Snooze minutes")
        }
    }

    @Test
    fun chipsAndSegments() = snapshot("chips_segments") {
        Column(Modifier.width(380.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                YumiFilterChip(selected = true, onClick = {}, label = { Text("Rain") })
                YumiFilterChip(selected = false, onClick = {}, label = { Text("Wind") })
                YumiFilterChip(selected = false, onClick = {}, label = { Text("Off") }, enabled = false)
                YumiAssistChip(onClick = {}, label = { Text("Add to calendar") })
                YumiInputChip(label = { Text("Weekdays") }, onRemove = {}, removeContentDescription = "Remove Weekdays")
            }
            YumiSegmentedButtonRow(options = listOf("Day", "Week", "Month"), selected = "Week", onSelect = {}) { Text(it) }
        }
    }

    @Test
    fun timePickerDialog() = screenSnapshot("time_picker") {
        YumiTimePickerDialog(onDismissRequest = {}, onConfirm = { _, _ -> }, initialHour = 7, initialMinute = 30)
    }

    @Test
    fun datePickerDialog() = screenSnapshot("date_picker") {
        // 2000-03-15 00:00 UTC. A month in the past on purpose: Material rings "today" in the
        // current month, which would make the screenshot depend on the date and time zone.
        YumiDatePickerDialog(onDismissRequest = {}, onConfirm = {}, initialSelectedDateMillis = 953_078_400_000L)
    }

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        var dark by mutableStateOf(false)
        compose.setContent { TestTheme(dark = dark) { content() } }
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_putty.png")
        dark = true
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_graphite.png")
    }

    private fun screenSnapshot(name: String, content: @Composable () -> Unit) {
        var dark by mutableStateOf(false)
        compose.setContent { TestTheme(dark = dark) { content() } }
        compose.waitForIdle()
        captureScreenRoboImage("src/test/screenshots/${name}_putty.png")
        dark = true
        compose.waitForIdle()
        captureScreenRoboImage("src/test/screenshots/${name}_graphite.png")
    }
}
