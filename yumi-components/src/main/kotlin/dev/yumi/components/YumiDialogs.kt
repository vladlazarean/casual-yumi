package dev.yumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.yumi.components.internal.LocalYumiSurfaceLevel
import dev.yumi.components.internal.YumiSurfaceLevel
import dev.yumi.foundation.YumiTheme

/** Defaults for [YumiDialog] and [YumiAlertDialog]. */
public object YumiDialogDefaults {
    /** Maximum dialog width. */
    public val MaxWidth: Dp = 400.dp

    /** Padding inside the panel. */
    public val ContentPadding: Dp = 24.dp

    /** Panel shape. */
    public val shape: Shape
        @Composable get() = YumiTheme.shapes.card
}

/**
 * A dialog with custom content on a body-coloured panel with a soft drop shadow.
 *
 * The panel is body-coloured, not raised, so pebbles inside keep their full depth. Built on
 * Material 3's `BasicAlertDialog`, which handles the window, scrim, back and outside-tap.
 *
 * @param onDismissRequest called on back press or outside tap.
 * @param modifier modifier for the panel.
 * @param properties window properties.
 * @param content panel content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable ColumnScope.() -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest, properties = properties) {
        DialogPanel(modifier, content)
    }
}

/**
 * An alert dialog with slots, following Material's `AlertDialog` shape.
 *
 * Only the confirm action should be accent: pass a [YumiButtonVariant.Accent] [YumiButton] as
 * [confirmButton] and a [YumiTextButton] or neutral button as [dismissButton]. The overload that
 * takes labels does this for you.
 *
 * @param onDismissRequest called on back press or outside tap.
 * @param confirmButton the confirming action.
 * @param modifier modifier for the panel.
 * @param dismissButton optional dismissing action, placed before [confirmButton].
 * @param icon optional icon above the title.
 * @param title optional title.
 * @param text optional body.
 * @param properties window properties.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
public fun YumiAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest, properties = properties) {
        DialogPanel(modifier) {
            if (icon != null) {
                Box(Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                    CompositionLocalProvider(LocalContentColor provides YumiTheme.colors.ink) { icon() }
                }
            }
            if (title != null) {
                CompositionLocalProvider(
                    LocalContentColor provides YumiTheme.colors.ink,
                    LocalTextStyle provides YumiTheme.typography.headlineSmall,
                ) {
                    Box(Modifier.padding(bottom = 12.dp).align(if (icon != null) Alignment.CenterHorizontally else Alignment.Start)) {
                        title()
                    }
                }
            }
            if (text != null) {
                CompositionLocalProvider(
                    LocalContentColor provides YumiTheme.colors.muted,
                    LocalTextStyle provides YumiTheme.typography.bodyMedium,
                ) {
                    Box(Modifier.padding(bottom = 24.dp)) { text() }
                }
            }
            // FlowRow so the keys stack at 200 % font scale instead of clipping.
            FlowRow(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                dismissButton?.invoke()
                confirmButton()
            }
        }
    }
}

/**
 * An alert dialog from labels. The confirm key is the accent key; dismiss is a flat text key.
 *
 * @param onDismissRequest called on back press, outside tap, or the dismiss key.
 * @param title title text.
 * @param text body text.
 * @param confirmLabel label of the confirm key.
 * @param onConfirm called when confirmed.
 * @param modifier modifier for the panel.
 * @param dismissLabel label of the dismiss key; `null` hides it.
 * @param properties window properties.
 */
@Composable
public fun YumiAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = null,
    properties: DialogProperties = DialogProperties(),
) {
    YumiAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            YumiButton(onClick = onConfirm, variant = YumiButtonVariant.Accent) { Text(confirmLabel) }
        },
        modifier = modifier,
        dismissButton = dismissLabel?.let { label ->
            { YumiTextButton(onClick = onDismissRequest) { Text(label) } }
        },
        title = { Text(title) },
        text = { Text(text) },
        properties = properties,
    )
}

@Composable
private fun DialogPanel(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = YumiDialogDefaults.shape
    val pane = stringResource(R.string.yumi_dialog_pane)
    Column(
        modifier = modifier
            .widthIn(max = YumiDialogDefaults.MaxWidth)
            .semantics { paneTitle = pane }
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 28.dp,
                    color = Color.Black,
                    offset = DpOffset(0.dp, 10.dp),
                    alpha = YumiTheme.depth.shadowAlpha * 1.4f,
                ),
            )
            .background(YumiTheme.colors.body, shape)
            .padding(YumiDialogDefaults.ContentPadding),
    ) {
        CompositionLocalProvider(
            LocalYumiSurfaceLevel provides YumiSurfaceLevel.Body,
            LocalContentColor provides YumiTheme.colors.ink,
        ) {
            content()
        }
    }
}
