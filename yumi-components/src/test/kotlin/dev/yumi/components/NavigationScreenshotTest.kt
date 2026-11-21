package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
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
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Milestone 5: app bar, navigation bar and rail, tabs, menu, list items, in Putty and Graphite. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w420dp-h900dp-xhdpi")
class NavigationScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun bars() = snapshot("bars") {
        Column(Modifier.width(388.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            YumiTopAppBar(
                title = { Text("Alarms") },
                navigationIcon = { YumiIconButton(onClick = {}) { GlyphIcon(Glyph.Close) } },
                actions = { YumiIconButton(onClick = {}) { GlyphIcon(Glyph.Plus) } },
                windowInsets = WindowInsets(0),
            )
            YumiTabRow(selectedTabIndex = 1) {
                YumiTab(selected = false, onClick = {}, text = { Text("Today") })
                YumiTab(selected = true, onClick = {}, text = { Text("Week") })
                YumiTab(selected = false, onClick = {}, text = { Text("Month") })
            }
            YumiNavigationBar(windowInsets = WindowInsets(0)) {
                YumiNavigationBarItem(selected = true, onClick = {}, icon = { GlyphIcon(Glyph.Check) }, label = { Text("Alarms") })
                YumiNavigationBarItem(selected = false, onClick = {}, icon = { GlyphIcon(Glyph.Search) }, label = { Text("Search") })
                YumiNavigationBarItem(selected = false, onClick = {}, icon = { GlyphIcon(Glyph.Plus) }, label = { Text("New") }, enabled = false)
            }
        }
    }

    @Test
    fun rail() = snapshot("rail") {
        Box(Modifier.height(420.dp)) {
            YumiNavigationRail(
                header = { YumiFab(onClick = {}) { GlyphIcon(Glyph.Plus) } },
                windowInsets = WindowInsets(0),
            ) {
                YumiNavigationRailItem(selected = true, onClick = {}, icon = { GlyphIcon(Glyph.Check) }, label = { Text("Alarms") })
                YumiNavigationRailItem(selected = false, onClick = {}, icon = { GlyphIcon(Glyph.Search) }, label = { Text("Search") })
            }
        }
    }

    @Test
    fun listItems() = snapshot("list_items") {
        Column(Modifier.width(388.dp)) {
            YumiListItem(headlineContent = { Text("Read-only row") })
            YumiListItem(
                headlineContent = { Text("07:30 · Weekdays") },
                supportingContent = { Text("Morning run") },
                overlineContent = { Text("NEXT") },
                trailingContent = { YumiSwitch(checked = true, onCheckedChange = null) },
                onClick = {},
            )
            YumiListItem(headlineContent = { Text("Disabled") }, onClick = {}, enabled = false)
            YumiSwipeToDismiss(onDismiss = {}) {
                YumiListItem(headlineContent = { Text("Swipe me") }, supportingContent = { Text("Toward the start edge") })
            }
        }
    }

    @Test
    fun dropdownMenu() = screenSnapshot("dropdown_menu") {
        Row {
            Box {
                YumiIconButton(onClick = {}) { GlyphIcon(Glyph.Plus) }
                YumiDropdownMenu(expanded = true, onDismissRequest = {}) {
                    YumiDropdownMenuItem(text = { Text("Edit") }, onClick = {})
                    YumiDropdownMenuItem(text = { Text("Duplicate") }, onClick = {}, trailingIcon = { Text("⌘D") })
                    YumiDropdownMenuItem(text = { Text("Delete") }, onClick = {}, enabled = false)
                }
            }
        }
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
