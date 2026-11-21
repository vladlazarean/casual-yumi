package dev.yumi.components

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Semantics and behaviour of the Milestone 4 inputs. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdvancedInputInteractionTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun rangeSliderHasTwoAdjustableThumbs() {
        var range by mutableStateOf(0.2f..0.8f)
        compose.setContent {
            TestTheme { YumiRangeSlider(value = range, onValueChange = { range = it }) }
        }
        val thumbs = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
        assertEquals(2, thumbs.fetchSemanticsNodes().size)
        thumbs[0].performSemanticsAction(SemanticsActions.SetProgress) { it(0.4f) }
        compose.waitForIdle()
        assertEquals(0.4f, range.start, 0.01f)
    }

    @Test
    fun circularSliderSetProgressSnapsAndTicks() {
        val haptics = RecordingHaptics()
        var value by mutableFloatStateOf(0f)
        compose.setContent {
            TestTheme(haptics = haptics) {
                YumiCircularSlider(value = value, onValueChange = { value = it }, steps = 3, modifier = Modifier.testTag("knob"))
            }
        }
        compose.onNodeWithTag("knob").performSemanticsAction(SemanticsActions.SetProgress) { it(0.46f) }
        compose.waitForIdle()
        assertEquals(0.5f, value, 0.001f)
        assertEquals(1, haptics.ticks)
    }

    @Test
    fun timeDialRingsAreSeparateAdjustableNodes() {
        var hour by mutableIntStateOf(7)
        var minute by mutableIntStateOf(58)
        var confirmed = false
        compose.setContent {
            TestTheme {
                YumiTimeDial(
                    hour = hour,
                    minute = minute,
                    onTimeChange = { h, m -> hour = h; minute = m },
                    onConfirm = { confirmed = true },
                )
            }
        }
        compose.onNodeWithContentDescription("Hours")
            .assert(hasProgressBarRangeInfo(ProgressBarRangeInfo(7f, 0f..23f, steps = 22)))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(9f) }
        compose.waitForIdle()
        assertEquals(9, hour)

        // Minutes past 00 roll the hour forward.
        compose.onNodeWithContentDescription("Minutes").performSemanticsAction(SemanticsActions.SetProgress) { it(2f) }
        compose.waitForIdle()
        assertEquals(10, hour)
        assertEquals(2, minute)

        compose.onNodeWithText("10:02").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun textFieldAcceptsInputAndReportsError() {
        var text by mutableStateOf("")
        compose.setContent {
            TestTheme {
                YumiTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "Name",
                    isError = text.length > 3,
                    supportingText = "Max 3 letters",
                    modifier = Modifier.testTag("field"),
                )
            }
        }
        compose.onNodeWithTag("field").performTextInput("Yumi")
        compose.waitForIdle()
        assertEquals("Yumi", text)
        compose.onNodeWithTag("field").assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, "Max 3 letters"))
    }

    @Test
    fun secureFieldTogglesReveal() {
        val state = TextFieldState("secret")
        compose.setContent { TestTheme { YumiSecureTextField(state = state, label = "Password") } }
        compose.onNodeWithText("Show").performClick()
        compose.onNodeWithText("Hide").assertExists()
    }

    @Test
    fun searchClearEmptiesQuery() {
        var query by mutableStateOf("rain")
        compose.setContent {
            TestTheme { YumiSearchField(query = query, onQueryChange = { query = it }, onSearch = {}) }
        }
        compose.onNodeWithContentDescription("Clear").performClick()
        assertEquals("", query)
    }

    @Test
    fun stepperClampsAndDisablesAtEnds() {
        var value by mutableIntStateOf(29)
        compose.setContent {
            TestTheme { YumiStepper(value = value, onValueChange = { value = it }, range = 1..30, label = "Snooze") }
        }
        compose.onNodeWithContentDescription("Increase").performClick()
        compose.waitForIdle()
        assertEquals(30, value)
        compose.onNodeWithContentDescription("Increase").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Snooze").performSemanticsAction(SemanticsActions.SetProgress) { it(10f) }
        compose.waitForIdle()
        assertEquals(10, value)
    }

    @Test
    fun filterChipTogglesAndSegmentSelects() {
        var rain by mutableStateOf(false)
        var period by mutableStateOf("Day")
        compose.setContent {
            TestTheme {
                androidx.compose.foundation.layout.Column {
                    YumiFilterChip(selected = rain, onClick = { rain = !rain }, label = { Text("Rain") })
                    YumiSegmentedButtonRow(listOf("Day", "Week"), period, { period = it }) { Text(it) }
                }
            }
        }
        compose.onNodeWithText("Rain").assertIsOff().performClick()
        compose.onNodeWithText("Rain").assertIsOn()
        compose.onNodeWithText("Week").performClick()
        compose.onNodeWithText("Week").assertIsSelected()
        assertEquals("Week", period)
    }

    @Test
    fun inputChipRemoveHasLabel() {
        var removed = false
        compose.setContent {
            TestTheme {
                YumiInputChip(label = { Text("Weekdays") }, onRemove = { removed = true }, removeContentDescription = "Remove Weekdays")
            }
        }
        compose.onNode(hasContentDescription("Remove Weekdays")).performClick()
        assertTrue(removed)
    }
}
