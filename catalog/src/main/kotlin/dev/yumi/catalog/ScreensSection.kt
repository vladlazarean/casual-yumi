package dev.yumi.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiBottomSheetScaffold
import dev.yumi.components.YumiFab
import dev.yumi.components.YumiListItem
import dev.yumi.components.YumiNavigationBar
import dev.yumi.components.YumiNavigationBarItem
import dev.yumi.components.YumiScaffold
import dev.yumi.components.YumiSnackbar
import dev.yumi.components.YumiSwitch
import dev.yumi.components.YumiTextButton
import dev.yumi.components.YumiToast
import dev.yumi.components.YumiToastHostState
import dev.yumi.components.YumiTopAppBar
import dev.yumi.foundation.YumiTheme
import kotlinx.coroutines.launch

private val Tabs = listOf("Alarms" to CatalogIcons.Alarm, "Outlook" to CatalogIcons.Cloud, "Settings" to CatalogIcons.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScreensSection() {
    val scope = rememberCoroutineScope()
    val screenToasts = remember { YumiToastHostState() }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var armed by rememberSaveable { mutableStateOf(true) }
    var snackbarShown by rememberSaveable { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Screens", style = YumiTheme.typography.titleLarge, color = YumiTheme.colors.ink)

        Label("YumiScaffold: top bar, bottom bar, FAB and its own toast host")
        Box(Modifier.fillMaxWidth().height(420.dp).clipToBounds()) {
            YumiScaffold(
                topBar = { YumiTopAppBar(title = { Text(Tabs[tab].first) }, windowInsets = WindowInsets(0)) },
                bottomBar = {
                    YumiNavigationBar(windowInsets = WindowInsets(0)) {
                        Tabs.forEachIndexed { i, (label, icon) ->
                            YumiNavigationBarItem(
                                selected = tab == i,
                                onClick = { tab = i },
                                icon = { CatalogIcon(icon, null) },
                                label = { Text(label) },
                            )
                        }
                    }
                },
                toastHostState = screenToasts,
                floatingActionButton = {
                    YumiFab(onClick = { scope.launch { screenToasts.show("New alarm") } }) {
                        CatalogIcon(CatalogIcons.Add, "New alarm")
                    }
                },
                contentWindowInsets = WindowInsets(0),
            ) { padding ->
                Column(Modifier.padding(padding)) {
                    YumiListItem(
                        headlineContent = { Text("07:30") },
                        supportingContent = { Text("Weekdays") },
                        trailingContent = { YumiSwitch(checked = armed, onCheckedChange = { armed = it }) },
                    )
                    YumiListItem(headlineContent = { Text("09:00") }, supportingContent = { Text("Weekend") })
                }
            }
        }

        Label("YumiBottomSheetScaffold: drag the sheet up")
        Box(Modifier.fillMaxWidth().height(360.dp).clipToBounds()) {
            YumiBottomSheetScaffold(
                sheetContent = {
                    Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Next alarm 07:30", style = YumiTheme.typography.titleMedium)
                        Text("Rain likely, take a jacket.", color = YumiTheme.colors.muted)
                        Text("Sunrise 07:41 · Sunset 16:52", color = YumiTheme.colors.muted)
                    }
                },
                sheetPeekHeight = 96.dp,
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                    Text("Map or main content", color = YumiTheme.colors.muted)
                }
            }
        }

        Label("YumiToast and YumiSnackbar without a host")
        YumiToast(message = "Alarm saved")
        if (snackbarShown) {
            YumiSnackbar(
                message = "Couldn't reach the server. Showing the last saved data.",
                actionLabel = "Retry",
                withDismissAction = true,
                onAction = { scope.launch { screenToasts.show("Retrying…") } },
                onDismiss = { snackbarShown = false },
            )
        } else {
            YumiTextButton(onClick = { snackbarShown = true }) { Text("Show the snackbar again") }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = YumiTheme.typography.labelMedium, color = YumiTheme.colors.muted)
}
