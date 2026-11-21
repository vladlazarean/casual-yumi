package dev.yumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import dev.yumi.foundation.YumiTheme

// Debug source set: never part of the published release AAR.

@Composable
private fun ExtrasSurface(content: @Composable () -> Unit) {
    YumiTheme {
        Column(
            Modifier.background(YumiTheme.colors.body).padding(20.dp).width(360.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) { content() }
    }
}

@PreviewLightDark
@Composable
private fun BadgeDividerPreview() = ExtrasSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        YumiBadgedBox(badge = { YumiBadge() }) { GlyphIcon(Glyph.Search, size = 28.dp) }
        YumiBadgedBox(badge = { YumiBadge(count = 7) }) { GlyphIcon(Glyph.Check, size = 28.dp) }
        YumiAvatar(initials = "VB", contentDescription = "Vlad", online = true)
    }
    YumiHorizontalDivider()
    YumiBanner(message = "You're offline.", ledOn = true, onDismiss = {})
}

@PreviewLightDark
@Composable
private fun ExtraInputsPreview() = ExtrasSurface {
    YumiSelectField(options = listOf("Soft chime", "Bell"), selected = "Soft chime", onSelect = {}, label = "Alert sound")
    YumiMultiChoiceSegmentedButtonRow(options = listOf("M", "W", "F"), selected = setOf("W"), onSelectionChange = {}) { Text(it) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        YumiIconToggleButton(checked = true, onCheckedChange = {}) { GlyphIcon(Glyph.Check) }
        YumiSplitButton(onClick = {}, menuContent = {}) { Text("Snooze") }
    }
    YumiRatingBar(rating = 4, onRatingChange = {})
}

@PreviewLightDark
@Composable
private fun ExtraContentPreview() = ExtrasSurface {
    YumiSkeletonLines()
    YumiExpandableSection(title = { Text("Advanced") }, expanded = true, onExpandedChange = {}) { Text("Hidden settings") }
    YumiPagerIndicator(pageCount = 4, currentPage = 0)
    YumiEmptyState(title = "No alarms yet", message = "Add one and it shows up here.")
}
