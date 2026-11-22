package dev.yumi.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiAvatar
import dev.yumi.components.YumiBadge
import dev.yumi.components.YumiBadgedBox
import dev.yumi.components.YumiBanner
import dev.yumi.components.YumiBottomAppBar
import dev.yumi.components.YumiButton
import dev.yumi.components.YumiButtonVariant
import dev.yumi.components.YumiCarousel
import dev.yumi.components.YumiDateRangePickerDialog
import dev.yumi.components.YumiDragHandle
import dev.yumi.components.YumiDropdownMenuItem
import dev.yumi.components.YumiEmptyState
import dev.yumi.components.YumiExpandableSection
import dev.yumi.components.YumiFab
import dev.yumi.components.YumiHorizontalDivider
import dev.yumi.components.YumiIconButton
import dev.yumi.components.YumiIconToggleButton
import dev.yumi.components.YumiListItem
import dev.yumi.components.YumiModalNavigationDrawer
import dev.yumi.components.YumiModalSideSheet
import dev.yumi.components.YumiMultiChoiceSegmentedButtonRow
import dev.yumi.components.YumiNavigationDrawerItem
import dev.yumi.components.YumiPullToRefreshBox
import dev.yumi.components.YumiRatingBar
import dev.yumi.components.YumiReorderableColumn
import dev.yumi.components.YumiRichTooltipBox
import dev.yumi.components.YumiSelectField
import dev.yumi.components.YumiSkeletonLines
import dev.yumi.components.YumiSplitButton
import dev.yumi.components.YumiSwipeAction
import dev.yumi.components.YumiSwipeActions
import dev.yumi.components.YumiSwitch
import dev.yumi.components.YumiTextButton
import dev.yumi.components.YumiTooltipBox
import dev.yumi.components.YumiVerticalDivider
import dev.yumi.foundation.YumiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExtrasSection() {
    val toasts = LocalCatalogToasts.current
    val scope = rememberCoroutineScope()
    var sound by rememberSaveable { mutableStateOf<String?>("Soft chime") }
    var days by rememberSaveable { mutableStateOf(setOf("Mon", "Wed", "Fri")) }
    var muted by rememberSaveable { mutableStateOf(false) }
    var rating by rememberSaveable { mutableIntStateOf(4) }
    var advancedOpen by rememberSaveable { mutableStateOf(false) }
    var vibrateFirst by rememberSaveable { mutableStateOf(true) }
    var loading by rememberSaveable { mutableStateOf(true) }
    var refreshing by rememberSaveable { mutableStateOf(false) }
    var refreshCount by rememberSaveable { mutableIntStateOf(0) }
    var order by rememberSaveable { mutableStateOf(listOf("Weather", "Calendar", "Outfit", "News")) }
    var sideSheet by rememberSaveable { mutableStateOf(false) }
    var rangeDialog by rememberSaveable { mutableStateOf(false) }
    var bannerShown by rememberSaveable { mutableStateOf(true) }
    var drawerPick by rememberSaveable { mutableStateOf("Alarms") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    CatalogSection("Extras") {
        if (bannerShown) {
            CatalogGroup("YumiBanner") {
                YumiBanner(message = "You're offline. Changes sync when you reconnect.", ledOn = true, onDismiss = { bannerShown = false }, actions = {
                    YumiTextButton(onClick = { scope.launch { toasts.show("Retrying…") } }) { Text("Retry") }
                })
            }
        }

        CatalogGroup("Badges, tooltips (long-press), dividers") {
            CatalogRow {
                YumiTooltipBox(text = "Notifications") {
                    YumiIconButton(onClick = {}) {
                        YumiBadgedBox(badge = { YumiBadge(count = 3) }) {
                            CatalogIcon(CatalogIcons.Bell, "Notifications")
                        }
                    }
                }
                YumiVerticalDivider(Modifier.height(32.dp))
                YumiRichTooltipBox(title = "Smart snooze", text = "Snooze length adapts to how long you slept.", actionLabel = "Got it") {
                    YumiIconButton(onClick = {}) {
                        YumiBadgedBox(badge = { YumiBadge() }) {
                            CatalogIcon(CatalogIcons.Info, "What's new")
                        }
                    }
                }
            }
            YumiHorizontalDivider()
        }

        CatalogGroup("YumiSelectField") {
            YumiSelectField(options = listOf("Soft chime", "Morning bell", "Low hum"), selected = sound, onSelect = { sound = it }, label = "Alert sound")
        }
        CatalogGroup("YumiMultiChoiceSegmentedButtonRow") {
            YumiMultiChoiceSegmentedButtonRow(options = Weekdays, selected = days, onSelectionChange = { days = it }) { Text(it.take(1)) }
        }
        CatalogGroup("Icon toggle and split button") {
            CatalogRow {
                YumiIconToggleButton(checked = muted, onCheckedChange = { muted = it }) {
                    CatalogIcon(if (muted) CatalogIcons.VolumeOff else CatalogIcons.VolumeOn, "Mute")
                }
                YumiSplitButton(
                    onClick = { scope.launch { toasts.show("Snoozed 9 min") } },
                    menuContent = { close ->
                        listOf(5, 10, 15).forEach { m ->
                            YumiDropdownMenuItem(text = { Text("Snooze $m min") }, onClick = {
                                close()
                                scope.launch { toasts.show("Snoozed $m min") }
                            })
                        }
                    },
                ) { Text("Snooze") }
            }
        }
        CatalogGroup("YumiRatingBar") {
            YumiRatingBar(rating = rating, onRatingChange = { rating = it })
        }
        CatalogGroup("YumiDateRangePickerDialog") {
            YumiButton(onClick = { rangeDialog = true }) { Text("Date range picker") }
        }

        CatalogGroup("YumiAvatar") {
            CatalogRow {
                YumiAvatar(initials = "VL", contentDescription = "Vlad, online", online = true)
                YumiAvatar(initials = "AY", contentDescription = "Ana, away", size = 56.dp, online = false)
            }
        }
        CatalogGroup("YumiExpandableSection") {
            YumiExpandableSection(title = { Text("Advanced options") }, expanded = advancedOpen, onExpandedChange = { advancedOpen = it }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Vibrate first", Modifier.weight(1f))
                    YumiSwitch(checked = vibrateFirst, onCheckedChange = { vibrateFirst = it })
                }
            }
        }
        CatalogGroup(if (loading) "YumiSkeletonLines" else "YumiEmptyState") {
            if (loading) {
                YumiSkeletonLines()
                YumiTextButton(onClick = { loading = false }) { Text("Finish loading") }
            } else {
                YumiEmptyState(title = "No forecasts yet", message = "Pull to refresh below, or add a location.", action = {
                    YumiButton(onClick = { loading = true }, variant = YumiButtonVariant.Accent) { Text("Add location") }
                })
            }
        }
        CatalogGroup("YumiCarousel") {
            YumiCarousel(items = listOf("Rain jacket", "Umbrella", "Boots", "Scarf"), itemWidth = 220.dp, itemHeight = 120.dp) {
                Text(it, style = YumiTheme.typography.titleMedium, color = YumiTheme.colors.ink)
            }
        }

        CatalogGroup("YumiPullToRefreshBox: pull the list down") {
            Box(Modifier.fillMaxWidth().height(220.dp)) {
                YumiPullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = {
                        refreshing = true
                        scope.launch {
                            delay(1_200)
                            refreshCount++
                            refreshing = false
                        }
                    },
                ) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items((1..8).toList()) { i -> YumiListItem(headlineContent = { Text("Row $i") }, supportingContent = { Text("Refreshed $refreshCount×") }) }
                    }
                }
            }
        }
        CatalogGroup("YumiReorderableColumn: drag a handle") {
            YumiReorderableColumn(items = order, onMove = { from, to -> order = order.toMutableList().apply { add(to, removeAt(from)) } }, itemKey = { it }) { item, handle ->
                YumiListItem(headlineContent = { Text(item) }, trailingContent = { YumiDragHandle(handle) })
            }
        }
        CatalogGroup("YumiSwipeActions") {
            YumiSwipeActions(
                startActions = listOf(YumiSwipeAction("Pin") { scope.launch { toasts.show("Pinned") } }),
                endActions = listOf(
                    YumiSwipeAction("Archive") { scope.launch { toasts.show("Archived") } },
                    YumiSwipeAction("Share") { scope.launch { toasts.show("Shared") } },
                ),
            ) {
                YumiListItem(headlineContent = { Text("Swipe either way") }, supportingContent = { Text("Actions latch open; tap one") })
            }
        }

        CatalogGroup("YumiModalNavigationDrawer and YumiBottomAppBar") {
            // Clip so the closed drawer's edge and shadow stay inside the demo box.
            Box(Modifier.fillMaxWidth().height(320.dp).clipToBounds()) {
                YumiModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        Text("Casual Yumi", Modifier.padding(16.dp), style = YumiTheme.typography.titleLarge)
                        listOf("Alarms", "Outlook", "Settings").forEach { d ->
                            YumiNavigationDrawerItem(
                                label = { Text(d) },
                                selected = drawerPick == d,
                                onClick = {
                                    drawerPick = d
                                    scope.launch { drawerState.close() }
                                },
                                badge = if (d == "Alarms") { { YumiBadge(count = 2) } } else null,
                            )
                        }
                    },
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Column(
                            Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(CatalogSpacing.Item, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(drawerPick, style = YumiTheme.typography.titleMedium, color = YumiTheme.colors.ink)
                            YumiButton(onClick = { scope.launch { drawerState.open() } }) { Text("Open drawer") }
                        }
                        YumiBottomAppBar(windowInsets = WindowInsets(0), floatingActionButton = {
                            YumiFab(onClick = { scope.launch { toasts.show("New alarm") } }) { CatalogIcon(CatalogIcons.Add, "New alarm") }
                        }) {
                            YumiIconButton(onClick = { scope.launch { drawerState.open() } }) { CatalogIcon(CatalogIcons.Menu, "Menu") }
                            YumiIconButton(onClick = { scope.launch { toasts.show("Search") } }) { CatalogIcon(CatalogIcons.Search, "Search") }
                        }
                    }
                }
            }
        }
        CatalogGroup("YumiModalSideSheet: covers the whole screen") {
            YumiButton(onClick = { sideSheet = true }) { Text("Side sheet") }
        }
    }

    YumiModalSideSheet(visible = sideSheet, onDismissRequest = { sideSheet = false }, title = { Text("Filters") }) {
        YumiMultiChoiceSegmentedButtonRow(options = Weekdays, selected = days, onSelectionChange = { days = it }) { Text(it.take(1)) }
        YumiButton(onClick = { sideSheet = false }, variant = YumiButtonVariant.Accent) { Text("Apply") }
    }

    if (rangeDialog) {
        YumiDateRangePickerDialog(
            onDismissRequest = { rangeDialog = false },
            onConfirm = { _, _ ->
                rangeDialog = false
                scope.launch { toasts.show("Range picked") }
            },
        )
    }
}
