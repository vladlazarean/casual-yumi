package dev.yumi.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.LocalYumiSurfaceLevel
import dev.yumi.components.internal.YumiSurfaceLevel
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.tray

/** Defaults for [YumiModalBottomSheet]. */
public object YumiBottomSheetDefaults {
    /** Sheet shape: generous top corners, like the remote's inset tray. */
    public val shape: Shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

    /** Scrim over the screen behind the sheet. */
    public val ScrimColor: Color = Color.Black.copy(alpha = 0.32f)

    /** The drag handle: a short recessed slot, like the remote's LED slot. */
    @Composable
    public fun DragHandle() {
        val description = stringResource(R.string.yumi_sheet_handle)
        Box(
            Modifier
                .padding(vertical = 14.dp)
                .semantics { contentDescription = description }
                .size(width = 40.dp, height = 6.dp)
                .tray(
                    color = YumiTheme.colors.tray,
                    shape = YumiTheme.shapes.pill,
                    depth = YumiTheme.depth.copy(trayInsetRadius = 2.dp, trayInsetOffset = DpOffset(0.dp, 1.dp)),
                ),
        )
    }
}

/**
 * A modal bottom sheet on the matte body colour, with a recessed drag handle and no tonal tint.
 *
 * Built on Material 3's `ModalBottomSheet` (gestures, scrim, back handling, insets). The sheet
 * is body-coloured, not raised, so pebbles inside keep their full depth; the accent key belongs
 * to the sheet's primary action.
 *
 * @param onDismissRequest called when the user swipes the sheet away, taps the scrim or presses back.
 * @param modifier modifier for the sheet.
 * @param sheetState state of the sheet, for programmatic show / hide.
 * @param content sheet content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = YumiBottomSheetDefaults.shape,
        containerColor = YumiTheme.colors.body,
        contentColor = YumiTheme.colors.ink,
        tonalElevation = 0.dp,
        scrimColor = YumiBottomSheetDefaults.ScrimColor,
        dragHandle = { YumiBottomSheetDefaults.DragHandle() },
    ) {
        CompositionLocalProvider(LocalYumiSurfaceLevel provides YumiSurfaceLevel.Body) {
            content()
        }
    }
}
