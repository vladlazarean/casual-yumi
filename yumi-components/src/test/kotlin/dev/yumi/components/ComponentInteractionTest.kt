package dev.yumi.components

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Interaction and semantics: roles, state descriptions, touch targets, haptics. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComponentInteractionTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun buttonClicksLongClicksAndHasButtonRole() {
        var clicks = 0
        var longClicks = 0
        compose.setContent {
            TestTheme {
                YumiButton(onClick = { clicks++ }, onLongClick = { longClicks++ }) { Text("Snooze") }
            }
        }
        val node = compose.onNodeWithText("Snooze")
        node.assertHasClickAction()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertHeightIsAtLeast(48.dp)
        node.performClick()
        node.performTouchInput { longClick() }
        assertEquals(1, clicks)
        assertEquals(1, longClicks)
    }

    @Test
    fun disabledButtonDoesNotClick() {
        var clicks = 0
        compose.setContent {
            TestTheme { YumiButton(onClick = { clicks++ }, enabled = false) { Text("Off") } }
        }
        compose.onNodeWithText("Off").assertIsNotEnabled().performClick()
        assertEquals(0, clicks)
    }

    @Test
    fun switchTogglesWithStateDescriptionAndHaptic() {
        val haptics = RecordingHaptics()
        compose.setContent {
            var on by remember { mutableStateOf(false) }
            TestTheme(haptics = haptics) {
                YumiSwitch(checked = on, onCheckedChange = { on = it }, modifier = Modifier.testTag("switch"))
            }
        }
        val node = compose.onNodeWithTag("switch")
        node.assertIsOff()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Off"))
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
        node.performClick()
        node.assertIsOn().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "On"))
        assertEquals(listOf(true), haptics.toggles)
    }

    @Test
    fun checkboxHasMinimumTouchTargetAndToggles() {
        compose.setContent {
            var checked by remember { mutableStateOf(false) }
            TestTheme {
                YumiCheckbox(checked = checked, onCheckedChange = { checked = it }, modifier = Modifier.testTag("box"))
            }
        }
        val node = compose.onNodeWithTag("box")
        node.assertIsOff().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        node.performClick()
        node.assertIsOn()
    }

    @Test
    fun triStateCheckboxReportsIndeterminate() {
        compose.setContent {
            TestTheme {
                YumiTriStateCheckbox(state = ToggleableState.Indeterminate, onClick = {}, modifier = Modifier.testTag("tri"))
            }
        }
        compose.onNodeWithTag("tri")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Indeterminate))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Partially checked"))
    }

    @Test
    fun radioGroupRowsAreSelectable() {
        compose.setContent {
            var selected by remember { mutableStateOf("Putty") }
            TestTheme {
                YumiRadioGroup(options = listOf("Putty", "Graphite"), selected = selected, onSelect = { selected = it }) {
                    Text(it)
                }
            }
        }
        compose.onNodeWithText("Graphite").assertIsNotSelected().performClick()
        compose.onNodeWithText("Graphite").assertIsSelected()
        compose.onNodeWithText("Putty").assertIsNotSelected()
    }

    @Test
    fun sliderExposesRangeAndSetProgressAndTicksOnSteps() {
        val haptics = RecordingHaptics()
        var value by mutableFloatStateOf(0f)
        compose.setContent {
            TestTheme(haptics = haptics) {
                YumiSlider(value = value, onValueChange = { value = it }, steps = 3, modifier = Modifier.testTag("slider"))
            }
        }
        val node = compose.onNodeWithTag("slider")
        node.assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
        node.performSemanticsAction(SemanticsActions.SetProgress) { it(0.5f) }
        compose.waitForIdle()
        assertEquals(0.5f, value, 0.01f)
        assertEquals(1, haptics.ticks)
        node.assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo(0.5f, 0f..1f, steps = 3),
            ),
        )
    }

    @Test
    fun toggleButtonLatchesAndDescribesState() {
        compose.setContent {
            var on by remember { mutableStateOf(false) }
            TestTheme {
                YumiToggleButton(checked = on, onCheckedChange = { on = it }) { Text("Mute") }
            }
        }
        val node = compose.onNodeWithText("Mute")
        node.assertIsOff().performClick()
        node.assertIsOn().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "On"))
    }
}
