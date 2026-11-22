package dev.yumi.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiDropdownMenu
import dev.yumi.components.YumiDropdownMenuItem
import dev.yumi.components.YumiIconButton
import dev.yumi.components.YumiListItem
import dev.yumi.components.YumiNavigationBar
import dev.yumi.components.YumiNavigationBarItem
import dev.yumi.components.YumiNavigationRail
import dev.yumi.components.YumiNavigationRailItem
import dev.yumi.components.YumiSwipeToDismiss
import dev.yumi.components.YumiSwitch
import dev.yumi.components.YumiTab
import dev.yumi.components.YumiTabRow
import dev.yumi.components.YumiToastResult
import dev.yumi.components.YumiTopAppBar
import dev.yumi.foundation.YumiTheme
import kotlinx.coroutines.launch

private val Destinations = listOf("Alarms", "Outlook", "Settings")
private val DestinationIcons = listOf(CatalogIcons.Alarm, CatalogIcons.Cloud, CatalogIcons.Settings)
private val InitialAlarms = listOf("06:45 · Gym", "07:30 · Weekdays", "09:00 · Weekend")

@Composable
internal fun NavigationSection() {
    val toasts = LocalCatalogToasts.current
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var destination by rememberSaveable { mutableIntStateOf(0) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var alarms by rememberSaveable { mutableStateOf(InitialAlarms) }
    var armed by rememberSaveable { mutableStateOf(setOf("07:30 · Weekdays")) }

    CatalogSection("Navigation and lists") {
        CatalogGroup("YumiTopAppBar with a YumiDropdownMenu") {
            YumiTopAppBar(
                title = { Text("Alarms") },
                windowInsets = WindowInsets(0),
                actions = {
                    Box {
                        YumiIconButton(onClick = { menuOpen = true }) { CatalogIcon(CatalogIcons.More, "More options") }
                        YumiDropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            YumiDropdownMenuItem(text = { Text("Sort by time") }, onClick = { menuOpen = false })
                            YumiDropdownMenuItem(text = { Text("Restore deleted") }, onClick = {
                                alarms = InitialAlarms
                                menuOpen = false
                            })
                            YumiDropdownMenuItem(text = { Text("Export") }, onClick = {}, enabled = false)
                        }
                    }
                },
            )
        }

        CatalogGroup("YumiTabRow") {
            YumiTabRow(selectedTabIndex = tab) {
                listOf("Today", "Week", "Month").forEachIndexed { i, label ->
                    YumiTab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
        }

        CatalogGroup("YumiListItem in YumiSwipeToDismiss") {
            Column {
                alarms.forEach { alarm ->
                    key(alarm) {
                        YumiSwipeToDismiss(onDismiss = {
                            alarms = alarms - alarm
                            scope.launch {
                                if (toasts.show("Deleted $alarm", actionLabel = "Undo") == YumiToastResult.ActionPerformed) {
                                    alarms = InitialAlarms.filter { it in alarms || it == alarm }
                                }
                            }
                        }) {
                            YumiListItem(
                                headlineContent = { Text(alarm.substringBefore(" · ")) },
                                supportingContent = { Text(alarm.substringAfter(" · ")) },
                                trailingContent = {
                                    YumiSwitch(
                                        checked = alarm in armed,
                                        onCheckedChange = { on -> armed = if (on) armed + alarm else armed - alarm },
                                    )
                                },
                                onClick = { scope.launch { toasts.show("Open $alarm") } },
                                onLongClick = { scope.launch { toasts.show("Edit $alarm") } },
                            )
                        }
                    }
                }
            }
            CatalogNote("Swipe a row toward the start edge to delete; long-press to edit.")
        }

        CatalogGroup("YumiNavigationBar") {
            YumiNavigationBar(windowInsets = WindowInsets(0)) {
                Destinations.forEachIndexed { i, label ->
                    YumiNavigationBarItem(
                        selected = destination == i,
                        onClick = { destination = i },
                        icon = { CatalogIcon(DestinationIcons[i], null) },
                        label = { Text(label) },
                    )
                }
            }
        }

        CatalogGroup("YumiNavigationRail, for wide screens") {
            Row(Modifier.fillMaxWidth().height(260.dp)) {
                YumiNavigationRail(windowInsets = WindowInsets(0)) {
                    Destinations.forEachIndexed { i, label ->
                        YumiNavigationRailItem(
                            selected = destination == i,
                            onClick = { destination = i },
                            icon = { CatalogIcon(DestinationIcons[i], null) },
                            label = { Text(label) },
                        )
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text(Destinations[destination], style = YumiTheme.typography.titleMedium, color = YumiTheme.colors.muted)
                }
            }
        }
    }
}
