package dev.yumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme

// Debug source set: never part of the published release AAR.
// Dialogs and sheets open their own window, so they are covered by screenshot tests, not previews.

@PreviewLightDark
@Composable
private fun ToastPreview() {
    YumiTheme {
        Column(
            Modifier.background(YumiTheme.colors.body).padding(20.dp).width(360.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            YumiToast(message = "Alarm saved")
            YumiToast(message = "Alarm deleted", actionLabel = "Undo")
            YumiSnackbar(message = "Couldn't reach the server.", actionLabel = "Retry", withDismissAction = true)
        }
    }
}

@PreviewLightDark
@Composable
private fun ProgressPreview() {
    YumiTheme {
        Column(
            Modifier.background(YumiTheme.colors.body).padding(20.dp).width(320.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            YumiLinearProgress(progress = { 0.62f })
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                YumiCircularProgress(progress = { 0.75f })
                YumiCircularProgress(progress = { 0.25f })
            }
        }
    }
}
