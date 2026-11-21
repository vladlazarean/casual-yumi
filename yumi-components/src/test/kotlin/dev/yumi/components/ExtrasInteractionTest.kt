package dev.yumi.components

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Semantics and behaviour of badge, select, toggles, rating, expandable, reorder, swipe actions. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExtrasInteractionTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun badgeAnnouncesCount() {
        compose.setContent { TestTheme { YumiBadge(count = 3) } }
        compose.onNodeWithContentDescription("3 new").assertExists()
    }

    @Test
    fun selectFieldOpensAndPicks() {
        var picked by mutableStateOf<String?>(null)
        compose.setContent {
            TestTheme {
                YumiSelectField(options = listOf("Soft chime", "Bell"), selected = picked, onSelect = { picked = it }, label = "Alert sound", modifier = Modifier.testTag("select"))
            }
        }
        compose.onNodeWithText("Alert sound").performClick()
        compose.onNodeWithText("Bell").performClick()
        assertEquals("Bell", picked)
    }

    @Test
    fun multiChoiceSegmentsToggleIndependently() {
        var days by mutableStateOf(setOf("Mon"))
        compose.setContent {
            TestTheme {
                YumiMultiChoiceSegmentedButtonRow(options = listOf("Mon", "Tue"), selected = days, onSelectionChange = { days = it }) { Text(it) }
            }
        }
        compose.onNodeWithText("Tue").assertIsOff().performClick()
        compose.onNodeWithText("Tue").assertIsOn()
        compose.onNodeWithText("Mon").assertIsOn()
        assertEquals(setOf("Mon", "Tue"), days)
    }

    @Test
    fun iconToggleLatches() {
        var on by mutableStateOf(false)
        compose.setContent {
            TestTheme {
                YumiIconToggleButton(checked = on, onCheckedChange = { on = it }, modifier = Modifier.testTag("mute")) { GlyphIcon(Glyph.Check) }
            }
        }
        compose.onNodeWithTag("mute").assertIsOff().performClick()
        compose.onNodeWithTag("mute").assertIsOn()
    }

    @Test
    fun ratingStarsAreRadiosAndDisplayModeIsOneNode() {
        var rating by mutableIntStateOf(2)
        compose.setContent {
            TestTheme {
                Column {
                    YumiRatingBar(rating = rating, onRatingChange = { rating = it })
                    YumiRatingBar(rating = 4, onRatingChange = null, modifier = Modifier.testTag("display"))
                }
            }
        }
        compose.onNodeWithContentDescription("4 stars")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            .performClick()
        assertEquals(4, rating)
        compose.onNodeWithContentDescription("4 stars").assertIsSelected()
        compose.onNodeWithTag("display").assert(hasContentDescription("4 of 5"))
    }

    @Test
    fun expandableSectionTogglesAndDescribesState() {
        var open by mutableStateOf(false)
        compose.setContent {
            TestTheme {
                YumiExpandableSection(title = { Text("Advanced") }, expanded = open, onExpandedChange = { open = it }) { Text("Hidden") }
            }
        }
        compose.onNodeWithText("Hidden").assertDoesNotExist()
        compose.onNodeWithText("Advanced")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))
            .performClick()
        compose.onNodeWithText("Hidden").assertExists()
    }

    @Test
    fun reorderHandlesHaveMoveActions() {
        var items by mutableStateOf(listOf("A", "B", "C"))
        compose.setContent {
            TestTheme {
                YumiReorderableColumn(
                    items = items,
                    onMove = { from, to -> items = items.toMutableList().apply { add(to, removeAt(from)) } },
                    itemKey = { it },
                ) { item, handle -> YumiListItem(headlineContent = { Text(item) }, trailingContent = { YumiDragHandle(handle) }) }
            }
        }
        val first = compose.onAllNodesWithContentDescription("Reorder")[0]
        val actions = first.fetchSemanticsNode().config[SemanticsActions.CustomActions]
        assertEquals(listOf("Move down"), actions.map { it.label })
        actions.single().action()
        compose.waitForIdle()
        assertEquals(listOf("B", "A", "C"), items)
    }

    @Test
    fun swipeActionsRevealAndRunOnlyOnPress() {
        var archived = false
        compose.setContent {
            TestTheme {
                Column(Modifier.width(360.dp)) {
                    YumiSwipeActions(endActions = listOf(YumiSwipeAction("Archive") { archived = true }), modifier = Modifier.testTag("row")) {
                        YumiListItem(headlineContent = { Text("Row") })
                    }
                }
            }
        }
        compose.onNodeWithTag("row").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        assertTrue("revealing must not run the action", !archived)
        compose.onNodeWithText("Archive").performClick()
        assertTrue(archived)
    }

    @Test
    fun pagerIndicatorAnnouncesPage() {
        compose.setContent { TestTheme { YumiPagerIndicator(pageCount = 5, currentPage = 1, modifier = Modifier.testTag("dots")) } }
        compose.onNodeWithTag("dots").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Page 2 of 5"))
    }
}
