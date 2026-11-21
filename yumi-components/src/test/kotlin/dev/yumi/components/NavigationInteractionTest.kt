package dev.yumi.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Navigation semantics, tab layout and the one swipe behaviour. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationInteractionTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun topAppBarTitleIsHeading() {
        compose.setContent { TestTheme { YumiTopAppBar(title = { Text("Alarms") }, windowInsets = WindowInsets(0)) } }
        compose.onNode(isHeading()).assertExists()
    }

    @Test
    fun navigationBarItemsAreTabsAndSelect() {
        var selected by mutableIntStateOf(0)
        compose.setContent {
            TestTheme {
                YumiNavigationBar(windowInsets = WindowInsets(0)) {
                    listOf("Alarms", "Search").forEachIndexed { i, label ->
                        YumiNavigationBarItem(selected = selected == i, onClick = { selected = i }, icon = {}, label = { Text(label) })
                    }
                }
            }
        }
        compose.onNodeWithText("Search")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .assertIsNotSelected()
            .performClick()
        compose.onNodeWithText("Search").assertIsSelected()
        assertEquals(1, selected)
    }

    @Test
    fun tabsShareWidthEqually() {
        compose.setContent {
            TestTheme {
                YumiTabRow(selectedTabIndex = 0, modifier = Modifier.width(240.dp)) {
                    YumiTab(selected = true, onClick = {}, text = { Text("A") }, modifier = Modifier.testTag("a"))
                    YumiTab(selected = false, onClick = {}, text = { Text("A much longer label") }, modifier = Modifier.testTag("b"))
                    YumiTab(selected = false, onClick = {}, text = { Text("C") }, modifier = Modifier.testTag("c"))
                }
            }
        }
        compose.onNodeWithTag("a").assertWidthIsEqualTo(80.dp)
        compose.onNodeWithTag("b").assertWidthIsEqualTo(80.dp)
    }

    @Test
    fun clickableListItemClicksAndLongClicks() {
        var clicks = 0
        var longClicks = 0
        compose.setContent {
            TestTheme {
                YumiListItem(headlineContent = { Text("07:30") }, onClick = { clicks++ }, onLongClick = { longClicks++ })
            }
        }
        compose.onNodeWithText("07:30").performClick()
        compose.onNodeWithText("07:30").performTouchInput { longClick() }
        assertEquals(1, clicks)
        assertEquals(1, longClicks)
    }

    @Test
    fun swipeTowardStartDismisses_otherDirectionDoesNot() {
        var dismissed = 0
        compose.setContent {
            TestTheme {
                Column(Modifier.width(360.dp)) {
                    YumiSwipeToDismiss(onDismiss = { dismissed++ }, modifier = Modifier.testTag("row")) {
                        YumiListItem(headlineContent = { Text("Swipe me") })
                    }
                }
            }
        }
        compose.onNodeWithTag("row").performTouchInput { swipeRight() }
        compose.waitForIdle()
        assertEquals(0, dismissed)
        compose.onNodeWithTag("row").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        assertEquals(1, dismissed)
    }

    @Test
    fun swipeHasAccessibilityAction() {
        var dismissed = false
        compose.setContent {
            TestTheme {
                YumiSwipeToDismiss(onDismiss = { dismissed = true }, dismissLabel = "Archive", modifier = Modifier.testTag("row")) {
                    YumiListItem(headlineContent = { Text("Row") })
                }
            }
        }
        val actions = compose.onNodeWithTag("row").fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertEquals("Archive", actions.single().label)
        actions.single().action()
        assertTrue(dismissed)
    }

    @Test
    fun dropdownMenuItemClicks() {
        var picked by mutableStateOf("")
        compose.setContent {
            TestTheme {
                YumiDropdownMenu(expanded = true, onDismissRequest = {}) {
                    YumiDropdownMenuItem(text = { Text("Edit") }, onClick = { picked = "Edit" })
                }
            }
        }
        compose.onNodeWithText("Edit").performClick()
        assertEquals("Edit", picked)
    }
}
