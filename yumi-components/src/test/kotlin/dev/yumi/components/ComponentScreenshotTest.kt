package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.yumi.foundation.YumiHaptics
import dev.yumi.foundation.YumiTheme
import dev.yumi.tokens.YumiAccent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Every component, every state, Putty and Graphite.
 *
 * Record: `./gradlew :yumi-components:recordRoborazziDebug`
 * Verify (CI): `./gradlew :yumi-components:verifyRoborazziDebug`
 * Images live in `src/test/screenshots/` and are committed.
 */
@OptIn(ExperimentalLayoutApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w420dp-h900dp-xhdpi")
class ComponentScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun buttons() = snapshot("buttons") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                YumiButton(onClick = {}, variant = YumiButtonVariant.Accent) { Text("Accent") }
                YumiButton(onClick = {}) { Text("Neutral") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                YumiButton(onClick = {}, variant = YumiButtonVariant.Charcoal) { Text("Charcoal") }
                YumiButton(onClick = {}, enabled = false) { Text("Disabled") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                YumiButton(onClick = {}, variant = YumiButtonVariant.Accent, enabled = false) { Text("Accent off") }
                YumiIconButton(onClick = {}) { Text("+") }
                YumiFab(onClick = {}) { Text("Go") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                YumiTextButton(onClick = {}) { Text("Text") }
                YumiTextButton(onClick = {}, enabled = false) { Text("Text off") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                YumiToggleButton(checked = true, onCheckedChange = {}) { Text("Latched") }
                YumiToggleButton(checked = false, onCheckedChange = {}) { Text("Released") }
            }
        }
    }

    @Test
    fun buttonPressed() {
        compose.setContent {
            TestTheme { YumiButton(onClick = {}) { Text("Pressed") } }
        }
        compose.onNodeWithText("Pressed").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(500)
        compose.onRoot().captureRoboImage("src/test/screenshots/button_pressed_putty.png")
    }

    @Test
    fun selection() = snapshot("selection") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                YumiCheckbox(checked = false, onCheckedChange = {})
                YumiCheckbox(checked = true, onCheckedChange = {})
                YumiTriStateCheckbox(state = ToggleableState.Indeterminate, onClick = {})
                YumiCheckbox(checked = true, onCheckedChange = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                YumiRadioButton(selected = false, onClick = {})
                YumiRadioButton(selected = true, onClick = {})
                YumiRadioButton(selected = true, onClick = {}, enabled = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                YumiSwitch(checked = false, onCheckedChange = {})
                YumiSwitch(checked = true, onCheckedChange = {})
                YumiSwitch(checked = true, onCheckedChange = {}, enabled = false)
            }
            YumiRadioGroup(
                options = listOf("Putty", "Graphite", "System"),
                selected = "Graphite",
                onSelect = {},
                modifier = Modifier.width(280.dp),
            ) { Text(it) }
        }
    }

    @Test
    fun sliders() = snapshot("sliders") {
        Column(Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            YumiSlider(value = 0.4f, onValueChange = {})
            YumiSlider(value = 0.5f, onValueChange = {}, steps = 4)
            YumiSlider(value = 0.7f, onValueChange = {}, enabled = false)
        }
    }

    @Test
    fun surfaces() = snapshot("surfaces") {
        Column(Modifier.width(380.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            YumiTray(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    YumiLed(on = true)
                    Text("Armed")
                    YumiLed(on = false)
                    Text("Off")
                }
                Text("07:30")
            }
            YumiCard(Modifier.fillMaxWidth()) {
                Text("Card")
                // One-depth rule: this button renders flat inside the card.
                YumiButton(onClick = {}) { Text("Flat inside card") }
            }
            YumiCard(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Clickable card") }
        }
    }

    @Test
    fun accentPresets() = snapshot("accents") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            YumiAccent.presets.forEach { accent ->
                YumiTheme(
                    darkTheme = YumiTheme.colors.isDark,
                    accent = accent,
                    fontFamily = FontFamily.SansSerif,
                    haptics = YumiHaptics.None,
                ) {
                    YumiButton(onClick = {}, variant = YumiButtonVariant.Accent) { Text(accent.name) }
                }
            }
        }
    }

    /** Captures [content] under Putty, then the same tree under Graphite. */
    private fun snapshot(name: String, content: @Composable () -> Unit) {
        var dark by mutableStateOf(false)
        compose.setContent { TestTheme(dark = dark) { content() } }
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_putty.png")
        dark = true
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_graphite.png")
    }
}
