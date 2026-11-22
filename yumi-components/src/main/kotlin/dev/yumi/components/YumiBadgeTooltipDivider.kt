package dev.yumi.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.yumi.foundation.YumiTheme
import kotlinx.coroutines.launch

// ── Badge ──────────────────────────────────────────────────────────────────

/** Defaults for [YumiBadge]. */
public object YumiBadgeDefaults {
    /** Diameter of the dot badge. */
    public val DotSize: Dp = 8.dp

    /** Height of the count badge. */
    public val CountHeight: Dp = 16.dp
}

/**
 * A badge: "something new here".
 *
 * - `count == null`: a small accent dot, an LED that means "unseen".
 * - `count != null`: a charcoal pill with the number (capped at "[maxCount]+"). Counts are
 *   charcoal, not orange, so a screen full of badges doesn't spend the accent.
 *
 * Place it with [YumiBadgedBox].
 *
 * @param modifier modifier for the badge.
 * @param count number to show, or `null` for a dot.
 * @param maxCount largest number shown before "+".
 * @param contentDescription what the badge means; defaults to "New" / "3 new".
 */
@Composable
public fun YumiBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    maxCount: Int = 99,
    contentDescription: String? = null,
) {
    val description = contentDescription
        ?: if (count == null) stringResource(R.string.yumi_new) else stringResource(R.string.yumi_new_count, count)
    val colors = YumiTheme.colors
    if (count == null) {
        Box(
            modifier
                .clearAndSetSemantics { this.contentDescription = description }
                .size(YumiBadgeDefaults.DotSize)
                .background(colors.accent, YumiTheme.shapes.pill),
        )
    } else {
        Box(
            modifier
                .clearAndSetSemantics { this.contentDescription = description }
                .sizeIn(minWidth = YumiBadgeDefaults.CountHeight, minHeight = YumiBadgeDefaults.CountHeight)
                .background(colors.charcoal, YumiTheme.shapes.pill)
                .padding(horizontal = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (count > maxCount) "$maxCount+" else count.toString(),
                style = YumiTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp, textAlign = TextAlign.Center),
                color = colors.onCharcoal,
                maxLines = 1,
            )
        }
    }
}

/**
 * Anchors a [YumiBadge] to the top-end corner of [content] (an icon, a navigation icon).
 *
 * The badge is centred on the corner, so it sits mostly outside a 24 dp icon's glyph instead of
 * covering it, and stays clear of the edge of a round icon key. Placement
 * mirrors in RTL. The badge draws outside the box's bounds, like Material's.
 *
 * @param badge the badge.
 * @param modifier modifier for the box.
 * @param content the anchor, usually an icon.
 */
@Composable
public fun YumiBadgedBox(
    badge: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Layout(
        content = {
            Box(contentAlignment = Alignment.Center, content = content)
            Box(content = badge)
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val anchor = measurables[0].measure(constraints)
        val badgePlaceable = measurables[1].measure(Constraints())
        val isDot = badgePlaceable.height <= YumiBadgeDefaults.DotSize.roundToPx()
        // Centre of the badge, relative to the anchor's top-end corner.
        // The count badge sits a touch lower so it stays inside a round 56 dp key.
        val centreX = anchor.width
        val centreY = if (isDot) 0 else 2.dp.roundToPx()
        layout(anchor.width, anchor.height) {
            anchor.placeRelative(0, 0)
            badgePlaceable.placeRelative(centreX - badgePlaceable.width / 2, centreY - badgePlaceable.height / 2, zIndex = 1f)
        }
    }
}

// ── Tooltips ───────────────────────────────────────────────────────────────

/**
 * A plain tooltip: a small charcoal pill naming an icon-only key, shown on long press, hover and
 * keyboard focus. Built on Material 3's `TooltipBox` (positioning, timing, accessibility).
 *
 * Long press is also Yumi's "edit" gesture. Wrap only keys that have no `onLongClick`; for keys
 * that do, the tooltip still appears on hover and focus.
 *
 * @param text the hint, e.g. "Settings".
 * @param modifier modifier for the anchor box.
 * @param content the anchor, usually a [YumiIconButton].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiTooltipBox(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Box(
                Modifier
                    .widthIn(max = 240.dp)
                    .background(YumiTheme.colors.charcoal, YumiTheme.shapes.pill)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(text, style = YumiTheme.typography.bodySmall, color = YumiTheme.colors.onCharcoal)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}

/**
 * A rich tooltip: a body-coloured panel with a title, text and an optional flat action, for
 * explaining a feature in place. It stays until dismissed.
 *
 * @param title short title.
 * @param text the explanation.
 * @param modifier modifier for the anchor box.
 * @param actionLabel optional action label, e.g. "Learn more".
 * @param onAction called when the action is pressed; the tooltip closes.
 * @param content the anchor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiRichTooltipBox(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val state = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    val shape = YumiTheme.shapes.card
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            Column(
                Modifier
                    .widthIn(max = 300.dp)
                    .dropShadow(shape, Shadow(radius = 20.dp, color = Color.Black, offset = DpOffset(0.dp, 8.dp), alpha = YumiTheme.depth.shadowAlpha * 1.4f))
                    .background(YumiTheme.colors.body, shape)
                    .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 6.dp),
            ) {
                Text(title, style = YumiTheme.typography.titleSmall, color = YumiTheme.colors.ink)
                Text(
                    text,
                    modifier = Modifier.padding(top = 4.dp, end = 8.dp, bottom = if (actionLabel == null) 8.dp else 0.dp),
                    style = YumiTheme.typography.bodyMedium,
                    color = YumiTheme.colors.muted,
                )
                if (actionLabel != null) {
                    YumiTextButton(
                        onClick = {
                            onAction()
                            scope.launch { state.dismiss() }
                        },
                        modifier = Modifier.align(Alignment.End),
                    ) { Text(actionLabel) }
                }
            }
        },
        state = state,
        modifier = modifier,
        content = content,
    )
}

// ── Divider ────────────────────────────────────────────────────────────────

/**
 * A horizontal divider engraved into the body: a seat-coloured line with a faint highlight under
 * it, like a seam in the remote's casing. Not a flat grey line.
 *
 * @param modifier modifier for the divider; fills the width.
 * @param startIndent space before the line, e.g. to align with list text.
 */
@Composable
public fun YumiHorizontalDivider(modifier: Modifier = Modifier, startIndent: Dp = 0.dp) {
    val seat = YumiTheme.colors.seat
    val highlight = Color.White.copy(alpha = YumiTheme.depth.highlightAlpha * 0.6f)
    Canvas(
        modifier
            .fillMaxWidth()
            .padding(start = startIndent)
            .height(2.dp),
    ) {
        val px = 1.dp.toPx()
        drawLine(seat, Offset(0f, px / 2f), Offset(size.width, px / 2f), strokeWidth = px)
        drawLine(highlight, Offset(0f, px * 1.5f), Offset(size.width, px * 1.5f), strokeWidth = px)
    }
}

/**
 * A vertical engraved divider, e.g. between two keys in a toolbar.
 *
 * @param modifier modifier for the divider; fills the height.
 */
@Composable
public fun YumiVerticalDivider(modifier: Modifier = Modifier) {
    val seat = YumiTheme.colors.seat
    val highlight = Color.White.copy(alpha = YumiTheme.depth.highlightAlpha * 0.6f)
    Canvas(modifier.fillMaxHeight().width(2.dp)) {
        val px = 1.dp.toPx()
        drawLine(seat, Offset(px / 2f, 0f), Offset(px / 2f, size.height), strokeWidth = px)
        drawLine(highlight, Offset(px * 1.5f, 0f), Offset(px * 1.5f, size.height), strokeWidth = px)
    }
}
