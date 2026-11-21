package dev.yumi.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
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
    var loading by rememberSaveable { mutableStateOf(true) }
    var refreshing by rememberSaveable { mutableStateOf(false) }
    var refreshCount by rememberSaveable { mutableIntStateOf(0) }
    var order by rememberSaveable { mutableStateOf(listOf("Weather", "Calendar", "Outfit", "News")) }
    var sideSheet by rememberSaveable { mutableStateOf(false) }
    var rangeDialog by rememberSaveable { mutableStateOf(false) }
    var bannerShown by rememberSaveable { mutableStateOf(true) }
    var drawerPick by rememberSaveable { mutableStateOf("Alarms") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Extras", style = YumiTheme.typography.titleLarge, color = YumiTheme.colors.ink)

        if (bannerShown) {
            YumiBanner(message = "You're offline. Changes sync when you reconnect.", ledOn = true, onDismiss = { bannerShown = false }, actions = {
                YumiTextButton(onClick = { scope.launch { toasts.show("Retrying…") } }) { Text("Retry") }
            })
        }

        Label("Badges, tooltips, dividers")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            YumiTooltipBox(text = "Notifications") {
                YumiIconButton(onClick = {}) {
                    YumiBadgedBox(badge = { YumiBadge(count = 3) }) {
                        Text("◎", Modifier.clearAndSetSemantics { contentDescription = "Notifications" })
                    }
                }
            }
            YumiRichTooltipBox(title = "Smart snooze", text = "Snooze length adapts to how long you slept.", actionLabel = "Got it") {
                YumiIconButton(onClick = {}) {
                    YumiBadgedBox(badge = { YumiBadge() }) {
                        Text("?", Modifier.clearAndSetSemantics { contentDescription = "What's new" })
                    }
                }
            }
        }
        YumiHorizontalDivider()

        Label("Inputs")
        YumiSelectField(options = listOf("Soft chime", "Morning bell", "Low hum"), selected = sound, onSelect = { sound = it }, label = "Alert sound")
        YumiMultiChoiceSegmentedButtonRow(options = Weekdays, selected = days, onSelectionChange = { days = it }) { Text(it.take(1)) }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            YumiIconToggleButton(checked = muted, onCheckedChange = { muted = it }) {
                Text(if (muted) "🔇" else "🔈", Modifier.clearAndSetSemantics { contentDescription = "Mute" })
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
        YumiRatingBar(rating = rating, onRatingChange = { rating = it })
        YumiButton(onClick = { rangeDialog = true }) { Text("Date range picker") }

        Label("Content")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            YumiAvatar(initials = "VB", contentDescription = "Vlad", online = true)
            YumiAvatar(initials = "AY", contentDescription = "Ana", size = 56.dp, online = false)
        }
        YumiExpandableSection(title = { Text("Advanced options") }, expanded = advancedOpen, onExpandedChange = { advancedOpen = it }) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Vibrate first", Modifier.weight(1f))
                YumiSwitch(checked = true, onCheckedChange = null)
            }
        }
        if (loading) {
            YumiSkeletonLines()
            YumiTextButton(onClick = { loading = false }) { Text("Finish loading") }
        } else {
            YumiEmptyState(title = "No forecasts yet", message = "Pull to refresh below, or add a location.", action = {
                YumiButton(onClick = { loading = true }, variant = YumiButtonVariant.Accent) { Text("Add location") }
            })
        }
        YumiCarousel(items = listOf("Rain jacket", "Umbrella", "Boots", "Scarf"), itemWidth = 220.dp, itemHeight = 120.dp) {
            Text(it, style = YumiTheme.typography.titleMedium, color = YumiTheme.colors.ink)
        }

        Label("Gestures")
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
                    items((1..8).toList()) { i -> YumiListItem(headlineContent = { Text("Pull down · row $i") }, supportingContent = { Text("Refreshed $refreshCount×") }) }
                }
            }
        }
        YumiReorderableColumn(items = order, onMove = { from, to -> order = order.toMutableList().apply { add(to, removeAt(from)) } }, itemKey = { it }) { item, handle ->
            YumiListItem(headlineContent = { Text(item) }, trailingContent = { YumiDragHandle(handle) })
        }
        YumiSwipeActions(
            startActions = listOf(YumiSwipeAction("Pin") { scope.launch { toasts.show("Pinned") } }),
            endActions = listOf(
                YumiSwipeAction("Archive") { scope.launch { toasts.show("Archived") } },
                YumiSwipeAction("Share") { scope.launch { toasts.show("Shared") } },
            ),
        ) {
            YumiListItem(headlineContent = { Text("Swipe either way") }, supportingContent = { Text("Actions latch open; tap one") })
        }

        Label("Structure")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            YumiButton(onClick = { scope.launch { drawerState.open() } }) { Text("Open drawer") }
            YumiButton(onClick = { sideSheet = true }) { Text("Side sheet") }
        }
        Box(Modifier.fillMaxWidth().height(320.dp)) {
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
                    Box(Modifier.weight(1f).fillMaxWidth().padding(20.dp)) {
                        Text("Drawer destination: $drawerPick", color = YumiTheme.colors.muted)
                    }
                    YumiBottomAppBar(windowInsets = WindowInsets(0), floatingActionButton = {
                        YumiFab(onClick = { scope.launch { toasts.show("New alarm") } }) { Text("+") }
                    }) {
                        YumiTextButton(onClick = { scope.launch { drawerState.open() } }) { Text("Menu") }
                        YumiTextButton(onClick = {}) { Text("Search") }
                    }
                }
            }
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

@Composable
private fun Label(text: String) {
    Text(text, style = YumiTheme.typography.labelMedium, color = YumiTheme.colors.muted)
}
