package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Text alignment under stress: 200 % font scale and narrow keys, where labels wrap.
 * Every line of a key label must stay centred; readouts must stay on one line.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w420dp-h900dp-xhdpi")
class AlignmentScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun wrappedLabelsStayCentred() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                TestTheme {
                    Column(Modifier.width(388.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        YumiButton(onClick = {}, variant = YumiButtonVariant.Accent, modifier = Modifier.fillMaxWidth()) {
                            Text("Dismiss and open the morning briefing")
                        }
                        YumiSegmentedButtonRow(options = listOf("Day", "Fortnight", "Month"), selected = "Fortnight", onSelect = {}) { Text(it) }
                        YumiTabRow(selectedTabIndex = 0) {
                            YumiTab(selected = true, onClick = {}, text = { Text("Upcoming alarms") })
                            YumiTab(selected = false, onClick = {}, text = { Text("Past") })
                        }
                        YumiNavigationBar(windowInsets = WindowInsets(0)) {
                            YumiNavigationBarItem(selected = true, onClick = {}, icon = {}, label = { Text("Morning briefing") })
                            YumiNavigationBarItem(selected = false, onClick = {}, icon = {}, label = { Text("Settings") })
                        }
                        YumiStepper(value = 15, onValueChange = {}, range = 1..30, valueText = { "$it min" })
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage("src/test/screenshots/alignment_font200_putty.png")
    }
}
