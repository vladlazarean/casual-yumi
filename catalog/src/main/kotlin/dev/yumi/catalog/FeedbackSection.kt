package dev.yumi.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiAlertDialog
import dev.yumi.components.YumiButton
import dev.yumi.components.YumiButtonVariant
import dev.yumi.components.YumiCircularProgress
import dev.yumi.components.YumiDialog
import dev.yumi.components.YumiLinearProgress
import dev.yumi.components.YumiModalBottomSheet
import dev.yumi.components.YumiRadioGroup
import dev.yumi.components.YumiSlider
import dev.yumi.components.YumiToastDuration
import dev.yumi.components.YumiToastHostState
import dev.yumi.components.YumiToastResult
import dev.yumi.foundation.YumiTheme
import kotlinx.coroutines.launch

/** The catalog's single toast queue, hosted at the bottom of the screen. */
internal val LocalCatalogToasts = staticCompositionLocalOf { YumiToastHostState() }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun FeedbackSection() {
    val toasts = LocalCatalogToasts.current
    val scope = rememberCoroutineScope()
    var progress by rememberSaveable { mutableFloatStateOf(0.62f) }
    var alertOpen by rememberSaveable { mutableStateOf(false) }
    var dialogOpen by rememberSaveable { mutableStateOf(false) }
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var repeatMode by rememberSaveable { mutableStateOf("Weekdays") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Feedback and dialogs", style = YumiTheme.typography.titleLarge, color = YumiTheme.colors.ink)

        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            YumiButton(onClick = { scope.launch { toasts.show("Alarm saved") } }) { Text("Toast") }
            YumiButton(onClick = {
                scope.launch {
                    val result = toasts.show("Alarm deleted", actionLabel = "Undo")
                    if (result == YumiToastResult.ActionPerformed) toasts.show("Restored")
                }
            }) { Text("Toast + action") }
            YumiButton(onClick = {
                scope.launch {
                    toasts.show(
                        message = "Couldn't reach the server. Showing the last saved data.",
                        actionLabel = "Retry",
                        duration = YumiToastDuration.Indefinite,
                        withDismissAction = true,
                    )
                }
            }) { Text("Long, indefinite") }
            YumiButton(onClick = { repeat(3) { i -> scope.launch { toasts.show("Queued ${i + 1} of 3") } } }) { Text("Queue 3") }
        }

        Text("Progress", style = YumiTheme.typography.labelMedium, color = YumiTheme.colors.muted)
        YumiLinearProgress(progress = { progress })
        YumiLinearProgress()
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            YumiCircularProgress(progress = { progress })
            YumiCircularProgress()
        }
        YumiSlider(value = progress, onValueChange = { progress = it })

        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            YumiButton(onClick = { alertOpen = true }) { Text("Alert dialog") }
            YumiButton(onClick = { dialogOpen = true }) { Text("Custom dialog") }
            YumiButton(onClick = { sheetOpen = true }) { Text("Bottom sheet") }
        }
    }

    if (alertOpen) {
        YumiAlertDialog(
            onDismissRequest = { alertOpen = false },
            title = "Delete alarm?",
            text = "The 07:30 alarm and its settings will be removed.",
            confirmLabel = "Delete",
            onConfirm = {
                alertOpen = false
                scope.launch { toasts.show("Alarm deleted") }
            },
            dismissLabel = "Cancel",
        )
    }

    if (dialogOpen) {
        YumiDialog(onDismissRequest = { dialogOpen = false }) {
            Text("Snooze length", style = YumiTheme.typography.headlineSmall)
            Text("${(progress * 30).toInt().coerceAtLeast(1)} min", color = YumiTheme.colors.muted)
            YumiSlider(value = progress, onValueChange = { progress = it }, steps = 5, modifier = Modifier.padding(vertical = 12.dp))
            YumiButton(
                onClick = { dialogOpen = false },
                variant = YumiButtonVariant.Accent,
                modifier = Modifier.align(Alignment.End),
            ) { Text("Done") }
        }
    }

    if (sheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        YumiModalBottomSheet(onDismissRequest = { sheetOpen = false }, sheetState = sheetState) {
            Column(
                Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Repeat", style = YumiTheme.typography.titleLarge)
                YumiRadioGroup(
                    options = listOf("Once", "Weekdays", "Every day"),
                    selected = repeatMode,
                    onSelect = { repeatMode = it },
                ) { Text(it) }
                YumiButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { sheetOpen = false }
                    },
                    variant = YumiButtonVariant.Accent,
                ) { Text("Save") }
            }
        }
    }
}
