package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Badge, tooltip, divider and the common-app extras, in Putty and Graphite. */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w420dp-h900dp-xhdpi")
class ExtrasScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun badgesAndDividers() = snapshot("badges_dividers") {
        Column(Modifier.width(388.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                YumiBadgedBox(badge = { YumiBadge() }) { GlyphIcon(Glyph.Search, size = 28.dp) }
                YumiBadgedBox(badge = { YumiBadge(count = 3) }) { GlyphIcon(Glyph.Check, size = 28.dp) }
                YumiBadgedBox(badge = { YumiBadge(count = 120) }) { GlyphIcon(Glyph.Plus, size = 28.dp) }
            }
            YumiHorizontalDivider()
            YumiHorizontalDivider(startIndent = 56.dp)
            Row(Modifier.height(40.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Left")
                YumiVerticalDivider()
                Text("Right")
            }
        }
    }

    @Test
    fun inputs() = snapshot("extra_inputs") {
        Column(Modifier.width(388.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            YumiSelectField(options = listOf("Soft chime", "Bell"), selected = "Soft chime", onSelect = {}, label = "Alert sound")
            YumiSelectField(options = listOf("A", "B"), selected = null, onSelect = {}, label = "Empty", placeholder = "Choose one")
            YumiMultiChoiceSegmentedButtonRow(
                options = listOf("M", "T", "W", "T ", "F", "S", "S "),
                selected = setOf("M", "W", "F"),
                onSelectionChange = {},
            ) { Text(it.trim()) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                YumiIconToggleButton(checked = true, onCheckedChange = {}) { GlyphIcon(Glyph.Check) }
                YumiIconToggleButton(checked = false, onCheckedChange = {}) { GlyphIcon(Glyph.Check) }
                YumiSplitButton(onClick = {}, menuContent = {}) { Text("Snooze") }
            }
            YumiRatingBar(rating = 4, onRatingChange = {})
            YumiRatingBar(rating = 3, onRatingChange = null)
        }
    }

    @Test
    fun content() = snapshot("extra_content") {
        Column(Modifier.width(388.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                YumiAvatar(initials = "VB", contentDescription = "Vlad")
                YumiAvatar(initials = "AY", contentDescription = "Ana", size = 56.dp, online = true)
                YumiAvatar(initials = "Z", contentDescription = null, online = false)
            }
            YumiSkeletonLines()
            YumiExpandableSection(title = { Text("Advanced") }, expanded = true, onExpandedChange = {}) { Text("Hidden settings") }
            YumiExpandableSection(title = { Text("Collapsed") }, expanded = false, onExpandedChange = {}) { Text("Not shown") }
            YumiPagerIndicator(pageCount = 5, currentPage = 1)
            YumiEmptyState(title = "No alarms yet", message = "Add one and it shows up here.", action = {
                YumiButton(onClick = {}, variant = YumiButtonVariant.Accent) { Text("Add alarm") }
            })
        }
    }

    @Test
    fun carouselAndBanner() = snapshot("carousel_banner") {
        Column(Modifier.width(388.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            YumiBanner(message = "You're offline. Changes sync when you reconnect.", ledOn = true, onDismiss = {}, actions = {
                YumiTextButton(onClick = {}) { Text("Retry") }
            })
            YumiCarousel(items = listOf("One", "Two", "Three"), itemWidth = 240.dp, itemHeight = 120.dp) { Text(it) }
            YumiSwipeActions(endActions = listOf(YumiSwipeAction("Archive") {}), startActions = listOf(YumiSwipeAction("Pin") {})) {
                YumiListItem(headlineContent = { Text("Swipe either way") })
            }
            YumiReorderableColumn(items = listOf("First", "Second"), onMove = { _, _ -> }, itemKey = { it }) { item, handle ->
                YumiListItem(headlineContent = { Text(item) }, trailingContent = { YumiDragHandle(handle) })
            }
        }
    }

    @Test
    fun structure() = snapshot("structure") {
        Column(Modifier.width(388.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            YumiBottomAppBar(windowInsets = WindowInsets(0), floatingActionButton = {
                YumiFab(onClick = {}) { GlyphIcon(Glyph.Plus) }
            }) {
                YumiIconButton(onClick = {}) { GlyphIcon(Glyph.Search) }
                YumiIconButton(onClick = {}) { GlyphIcon(Glyph.Check) }
            }
            Column(Modifier.fillMaxWidth()) {
                YumiNavigationDrawerItem(label = { Text("Alarms") }, selected = true, onClick = {}, badge = { YumiBadge(count = 2) })
                YumiNavigationDrawerItem(label = { Text("Settings") }, selected = false, onClick = {})
            }
        }
    }

    @Test
    fun sideSheet() = screenSnapshot("side_sheet") {
        Box(Modifier.size(400.dp)) {
            YumiModalSideSheet(visible = true, onDismissRequest = {}, title = { Text("Filters") }) {
                YumiSwitch(checked = true, onCheckedChange = {})
                YumiButton(onClick = {}, variant = YumiButtonVariant.Accent) { Text("Apply") }
            }
        }
    }

    @Test
    fun dateRangePicker() = screenSnapshot("date_range_picker") {
        YumiDateRangePickerDialog(
            onDismissRequest = {},
            onConfirm = { _, _ -> },
            // A month in the past, so Material's "today" ring never lands in the screenshot.
            initialSelectedStartDateMillis = 953_078_400_000L, // 2000-03-15
            initialSelectedEndDateMillis = 953_424_000_000L, // 2000-03-19
        )
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
