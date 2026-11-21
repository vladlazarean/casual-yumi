package dev.yumi.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Toast queue and timing, dialog actions, progress semantics. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FeedbackInteractionTest {

    @get:Rule
    val compose = createComposeRule()

    private lateinit var scope: CoroutineScope
    private val toasts = YumiToastHostState()

    private fun setHost() {
        compose.setContent {
            scope = rememberCoroutineScope()
            TestTheme { YumiToastHost(toasts) }
        }
    }

    @Test
    fun toastActionReturnsActionPerformed() {
        setHost()
        var result: YumiToastResult? = null
        scope.launch { result = toasts.show("Alarm deleted", actionLabel = "Undo") }
        compose.waitForIdle()
        compose.onNodeWithText("Undo").performClick()
        compose.waitForIdle()
        assertEquals(YumiToastResult.ActionPerformed, result)
    }

    @Test
    fun shortToastTimesOutAsDismissed() {
        setHost()
        var result: YumiToastResult? = null
        scope.launch { result = toasts.show("Saved") }
        compose.mainClock.advanceTimeBy(3_000)
        compose.waitForIdle()
        assertEquals(YumiToastResult.Dismissed, result)
    }

    @Test
    fun toastsQueueInCallOrder() {
        setHost()
        val shown = mutableListOf<String>()
        scope.launch { toasts.show("First"); shown += "First" }
        scope.launch { toasts.show("Second"); shown += "Second" }
        compose.waitForIdle()
        compose.onNodeWithText("First").assertExists()
        compose.onNodeWithText("Second").assertDoesNotExist()
        compose.mainClock.advanceTimeBy(3_000)
        compose.waitForIdle()
        compose.onNodeWithText("Second").assertExists()
        compose.mainClock.advanceTimeBy(3_000)
        compose.waitForIdle()
        assertEquals(listOf("First", "Second"), shown)
    }

    @Test
    fun indefiniteToastStaysUntilAction() {
        setHost()
        var result: YumiToastResult? = null
        scope.launch { result = toasts.show("Offline", actionLabel = "Retry", duration = YumiToastDuration.Indefinite) }
        compose.mainClock.advanceTimeBy(30_000)
        compose.waitForIdle()
        assertEquals(null, result)
        compose.onNodeWithText("Retry").performClick()
        compose.waitForIdle()
        assertEquals(YumiToastResult.ActionPerformed, result)
    }

    @Test
    fun alertDialogConfirmAndDismiss() {
        var confirmed = false
        var dismissed = false
        compose.setContent {
            var open by remember { mutableStateOf(true) }
            TestTheme {
                if (open) {
                    YumiAlertDialog(
                        onDismissRequest = { dismissed = true; open = false },
                        title = "Delete alarm?",
                        text = "This can't be undone.",
                        confirmLabel = "Delete",
                        onConfirm = { confirmed = true },
                        dismissLabel = "Cancel",
                    )
                }
            }
        }
        compose.onNodeWithText("Delete").performClick()
        assertTrue(confirmed)
        compose.onNodeWithText("Cancel").performClick()
        compose.waitForIdle()
        assertTrue(dismissed)
        compose.onNodeWithText("Delete alarm?").assertDoesNotExist()
    }

    @Test
    fun progressExposesRangeInfo() {
        compose.setContent {
            TestTheme {
                YumiLinearProgress(progress = { 0.4f }, modifier = Modifier.testTag("linear"))
                YumiCircularProgress(modifier = Modifier.testTag("spinner"))
            }
        }
        compose.onNodeWithTag("linear")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.4f, 0f..1f)))
        compose.onNodeWithTag("spinner")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate))
    }
}
