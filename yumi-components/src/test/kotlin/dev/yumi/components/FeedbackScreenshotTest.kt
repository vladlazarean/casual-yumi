package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

/** Milestone 3: toast, snackbar, progress, dialogs and sheet, in Putty and Graphite. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w420dp-h900dp-xhdpi")
class FeedbackScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun toasts() = snapshot("toasts") {
        Column(Modifier.width(388.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            YumiToast(message = "Alarm saved")
            YumiToast(message = "Alarm deleted", actionLabel = "Undo")
            YumiSnackbar(message = "Couldn't reach the server. Showing the last saved data.", actionLabel = "Retry", withDismissAction = true)
        }
    }

    @Test
    fun progress() {
        // Paused clock: indeterminate indicators render one fixed frame.
        compose.mainClock.autoAdvance = false
        snapshot("progress") {
            Column(Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                YumiLinearProgress(progress = { 0f })
                YumiLinearProgress(progress = { 0.62f })
                YumiLinearProgress()
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    YumiCircularProgress(progress = { 0.25f })
                    YumiCircularProgress(progress = { 0.75f })
                    YumiCircularProgress()
                }
            }
        }
    }

    @Test
    fun alertDialog() = screenSnapshot("alert_dialog") {
        YumiAlertDialog(
            onDismissRequest = {},
            title = "Delete alarm?",
            text = "The 07:30 alarm and its settings will be removed.",
            confirmLabel = "Delete",
            onConfirm = {},
            dismissLabel = "Cancel",
        )
    }

    @Test
    fun customDialog() = screenSnapshot("dialog") {
        YumiDialog(onDismissRequest = {}) {
            Text("Custom content")
            YumiSlider(value = 0.5f, onValueChange = {}, modifier = Modifier.padding(vertical = 8.dp))
            YumiButton(onClick = {}, variant = YumiButtonVariant.Accent) { Text("Done") }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheet() = screenSnapshot("bottom_sheet") {
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        YumiModalBottomSheet(onDismissRequest = {}, sheetState = state) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Repeat")
                YumiRadioGroup(options = listOf("Once", "Weekdays", "Every day"), selected = "Weekdays", onSelect = {}) { Text(it) }
                YumiButton(onClick = {}, variant = YumiButtonVariant.Accent) { Text("Save") }
            }
        }
    }

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        var dark by mutableStateOf(false)
        compose.setContent { TestTheme(dark = dark) { content() } }
        compose.mainClock.advanceTimeBy(500)
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_putty.png")
        dark = true
        compose.mainClock.advanceTimeBy(500)
        compose.onRoot().captureRoboImage("src/test/screenshots/${name}_graphite.png")
    }

    /** Dialogs and sheets live in their own window: capture the whole screen. */
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
