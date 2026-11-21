package dev.yumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import dev.yumi.foundation.YumiTheme

// Debug source set: never part of the published release AAR.

@PreviewLightDark
@Composable
private fun NavigationPreview() {
    YumiTheme {
        Column(
            Modifier.background(YumiTheme.colors.body).width(390.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YumiTopAppBar(
                title = { Text("Alarms") },
                actions = { YumiIconButton(onClick = {}) { GlyphIcon(Glyph.Plus) } },
                windowInsets = WindowInsets(0),
            )
            YumiTabRow(selectedTabIndex = 0) {
                YumiTab(selected = true, onClick = {}, text = { Text("Today") })
                YumiTab(selected = false, onClick = {}, text = { Text("Week") })
            }
            YumiListItem(
                headlineContent = { Text("07:30") },
                supportingContent = { Text("Weekdays") },
                trailingContent = { YumiSwitch(checked = true, onCheckedChange = null) },
                onClick = {},
            )
            YumiNavigationBar(windowInsets = WindowInsets(0)) {
                YumiNavigationBarItem(selected = true, onClick = {}, icon = { GlyphIcon(Glyph.Check) }, label = { Text("Alarms") })
                YumiNavigationBarItem(selected = false, onClick = {}, icon = { GlyphIcon(Glyph.Search) }, label = { Text("Search") })
            }
        }
    }
}
