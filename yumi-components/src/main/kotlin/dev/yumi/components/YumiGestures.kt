package dev.yumi.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.pebble
import dev.yumi.foundation.tray
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ── Pull to refresh ────────────────────────────────────────────────────────

/**
 * Pull to refresh. Pulling lowers a raised round key whose ring fills with the pull; releasing
 * past the threshold spins it while [isRefreshing]. Built on Material 3's `PullToRefreshBox`
 * (nested scrolling, thresholds). TalkBack gets a "Refresh" action on the container.
 *
 * @param isRefreshing whether a refresh is running.
 * @param onRefresh called when the user pulls past the threshold or uses the TalkBack action.
 * @param modifier modifier for the container.
 * @param state pull state.
 * @param content the scrollable content, e.g. a `LazyColumn`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    content: @Composable BoxScope.() -> Unit,
) {
    val refreshLabel = stringResource(R.string.yumi_refresh)
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.semantics {
            customActions = listOf(CustomAccessibilityAction(refreshLabel) { currentOnRefresh(); true })
        },
        state = state,
        indicator = { RefreshKey(state, isRefreshing, Modifier.align(Alignment.TopCenter)) },
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshKey(state: PullToRefreshState, isRefreshing: Boolean, modifier: Modifier) {
    val travel = 72.dp
    val keySize = 44.dp
    Box(
        modifier
            .graphicsLayer {
                val fraction = if (isRefreshing) 1f else state.distanceFraction.coerceIn(0f, 1.3f)
                translationY = travel.toPx() * fraction - keySize.toPx()
                alpha = if (isRefreshing) 1f else (fraction * 2f).coerceIn(0f, 1f)
            }
            .size(keySize)
            .pebble(
                color = YumiTheme.colors.raised,
                shape = CircleShape,
                seatColor = YumiTheme.colors.seat,
                depth = YumiTheme.depth,
                sinkOffset = YumiTheme.motion.sinkOffset,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isRefreshing) {
            YumiCircularProgress(Modifier.size(26.dp))
        } else {
            YumiCircularProgress(progress = { state.distanceFraction.coerceIn(0f, 1f) }, modifier = Modifier.size(26.dp))
        }
    }
}

// ── Drag to reorder ────────────────────────────────────────────────────────

/** Defaults for [YumiReorderableColumn]. */
public object YumiReorderableDefaults {
    /** Gap between rows: enough to read each row as its own key, small enough to stay a list. */
    public val ItemSpacing: Dp = 8.dp
}

/**
 * A column whose rows can be dragged into a new order by a handle. The dragged row lifts (a soft
 * shadow) and the others make room; each crossing ticks. TalkBack gets "Move up" / "Move down"
 * actions on every handle, so reordering never depends on the gesture.
 *
 * For short lists (settings, a playlist being edited). Rows are composed eagerly.
 *
 * @param items the rows, in order.
 * @param onMove called to move the row at `from` to `to` (adjacent); update your list.
 * @param itemKey a stable key per row.
 * @param modifier modifier for the column.
 * @param itemSpacing gap between rows; a dragged row swaps once it passes half a neighbour plus
 * this gap.
 * @param itemContent one row; apply `handle` to the drag handle, e.g. [YumiDragHandle].
 */
@Composable
public fun <T> YumiReorderableColumn(
    items: List<T>,
    onMove: (from: Int, to: Int) -> Unit,
    itemKey: (T) -> Any,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = YumiReorderableDefaults.ItemSpacing,
    itemContent: @Composable (item: T, handle: Modifier) -> Unit,
) {
    val haptics = YumiTheme.haptics
    val heights = remember { mutableStateMapOf<Any, Int>() }
    var draggingKey by remember { mutableStateOf<Any?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val currentItems by rememberUpdatedState(items)
    val currentOnMove by rememberUpdatedState(onMove)
    val moveUp = stringResource(R.string.yumi_move_up)
    val moveDown = stringResource(R.string.yumi_move_down)
    val handleLabel = stringResource(R.string.yumi_reorder_handle)
    val liftShape = YumiTheme.shapes.small
    val liftAlpha = YumiTheme.depth.shadowAlpha
    // A swap moves the row by the neighbour's height plus one gap, so the threshold includes it.
    val spacingPx = with(LocalDensity.current) { itemSpacing.toPx() }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(itemSpacing)) {
        items.forEachIndexed { index, item ->
            val k = itemKey(item)
            key(k) {
                val dragging = draggingKey == k
                val handle = Modifier
                    .semantics {
                        contentDescription = handleLabel
                        customActions = buildList {
                            if (index > 0) add(CustomAccessibilityAction(moveUp) { currentOnMove(index, index - 1); true })
                            if (index < items.lastIndex) add(CustomAccessibilityAction(moveDown) { currentOnMove(index, index + 1); true })
                        }
                    }
                    .pointerInput(k, spacingPx) {
                        detectDragGestures(
                            onDragStart = {
                                draggingKey = k
                                dragOffset = 0f
                                haptics.longPress()
                            },
                            onDragEnd = { draggingKey = null; dragOffset = 0f },
                            onDragCancel = { draggingKey = null; dragOffset = 0f },
                            onDrag = { change, amount ->
                                change.consume()
                                dragOffset += amount.y
                                val list = currentItems
                                val i = list.indexOfFirst { itemKey(it) == k }
                                if (i < 0) return@detectDragGestures
                                if (dragOffset > 0 && i < list.lastIndex) {
                                    val h = (heights[itemKey(list[i + 1])] ?: 0) + spacingPx
                                    if (h > spacingPx && dragOffset > h / 2f) {
                                        currentOnMove(i, i + 1)
                                        dragOffset -= h
                                        haptics.tick()
                                    }
                                } else if (dragOffset < 0 && i > 0) {
                                    val h = (heights[itemKey(list[i - 1])] ?: 0) + spacingPx
                                    if (h > spacingPx && -dragOffset > h / 2f) {
                                        currentOnMove(i, i - 1)
                                        dragOffset += h
                                        haptics.tick()
                                    }
                                }
                            },
                        )
                    }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .onSizeChanged { heights[k] = it.height }
                        .zIndex(if (dragging) 1f else 0f)
                        .graphicsLayer { translationY = if (draggingKey == k) dragOffset else 0f }
                        .then(
                            if (dragging) {
                                Modifier.dropShadow(liftShape, Shadow(radius = 16.dp, color = Color.Black, offset = DpOffset(0.dp, 6.dp), alpha = liftAlpha))
                            } else {
                                Modifier
                            },
                        )
                        .background(YumiTheme.colors.body, liftShape),
                ) { itemContent(item, handle) }
            }
        }
    }
}

/**
 * The grip for [YumiReorderableColumn]: three engraved lines in a 48 dp target.
 *
 * @param modifier pass the `handle` modifier from [YumiReorderableColumn] here.
 */
@Composable
public fun YumiDragHandle(modifier: Modifier = Modifier) {
    Box(modifier.size(48.dp), contentAlignment = Alignment.Center) {
        GlyphIcon(Glyph.DragHandle, color = YumiTheme.colors.muted)
    }
}

// ── Swipe actions ──────────────────────────────────────────────────────────

/**
 * One action revealed by [YumiSwipeActions].
 *
 * @property label what the action does; also its TalkBack action name.
 * @property onClick called when chosen.
 */
@Immutable
public class YumiSwipeAction(
    public val label: String,
    public val onClick: () -> Unit,
)

/** Defaults for [YumiSwipeActions]. */
public object YumiSwipeActionsDefaults {
    /** Width of one revealed action. */
    public val ActionWidth: Dp = 88.dp
}

/**
 * Swipe a row to reveal actions behind it: drag toward the end edge for [startActions], toward
 * the start edge for [endActions]. Unlike [YumiSwipeToDismiss] nothing happens on release: the
 * row latches open (past half the actions' width) and an action runs only when pressed.
 *
 * Use one gesture per list: either swipe-to-dismiss or swipe actions, never both. Every action
 * is also a TalkBack custom action.
 *
 * @param modifier modifier for the row.
 * @param startActions actions revealed at the start edge.
 * @param endActions actions revealed at the end edge.
 * @param content the row, usually a [YumiListItem].
 */
@Composable
public fun YumiSwipeActions(
    modifier: Modifier = Modifier,
    startActions: List<YumiSwipeAction> = emptyList(),
    endActions: List<YumiSwipeAction> = emptyList(),
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val motion = YumiTheme.motion
    val haptics = YumiTheme.haptics
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val density = LocalDensity.current
    val actionPx = with(density) { YumiSwipeActionsDefaults.ActionWidth.toPx() }
    val maxStart = actionPx * startActions.size
    val maxEnd = actionPx * endActions.size
    // Logical offset: positive = moved toward the end (start actions showing). Mirrored by offset in RTL.
    val offset = remember { Animatable(0f) }
    val dragTotal = remember { FloatHolder() }

    fun settleTo(target: Float) {
        scope.launch {
            if (target != 0f && offset.targetValue == 0f) haptics.tick()
            offset.animateTo(target, if (motion.reduceMotion) snap() else spring(motion.pressDampingRatio, 700f))
        }
    }
    fun close() = settleTo(0f)

    val dragState = rememberDraggableState { delta ->
        val logical = if (rtl) -delta else delta
        val next = (dragTotal.value + logical).coerceIn(-maxEnd, maxStart)
        dragTotal.value = next
        scope.launch { offset.snapTo(next) }
    }

    Box(
        modifier
            .fillMaxWidth()
            .semantics {
                customActions = (startActions + endActions).map { action ->
                    CustomAccessibilityAction(action.label) { action.onClick(); true }
                }
            },
    ) {
        Row(
            Modifier
                .matchParentSize()
                .tray(YumiTheme.colors.tray, RectangleShape, YumiTheme.depth),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            startActions.forEach { action -> SwipeActionKey(action) { close(); action.onClick() } }
            Box(Modifier.weight(1f))
            endActions.forEach { action -> SwipeActionKey(action) { close(); action.onClick() } }
        }
        Box(
            Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .background(YumiTheme.colors.body)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = startActions.isNotEmpty() || endActions.isNotEmpty(),
                    onDragStarted = { dragTotal.value = offset.value },
                    onDragStopped = {
                        val v = offset.value
                        val target = when {
                            v > 0 && v > maxStart / 2f -> maxStart
                            v < 0 && abs(v) > maxEnd / 2f -> -maxEnd
                            else -> 0f
                        }
                        dragTotal.value = target
                        settleTo(target)
                    },
                ),
        ) { content() }
    }
}

@Composable
private fun SwipeActionKey(action: YumiSwipeAction, onClick: () -> Unit) {
    YumiTextButton(
        onClick = onClick,
        modifier = Modifier.width(YumiSwipeActionsDefaults.ActionWidth).fillMaxHeight(),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) { Text(action.label, maxLines = 2) }
}

private class FloatHolder {
    var value: Float = 0f
}

// ── Banner ─────────────────────────────────────────────────────────────────

/**
 * An inline banner at the top of a screen's content: a recessed tray with a status LED, a
 * message and up to two flat actions ("You're offline · Retry"). It stays until the situation
 * changes or [onDismiss] is used; for transient news use a toast instead. Announced politely.
 *
 * @param message the message.
 * @param modifier modifier for the banner.
 * @param ledOn `true` lights the status LED (e.g. "action needed"); `null` hides it.
 * @param onDismiss when set, adds a "Dismiss" key.
 * @param actions optional flat keys, usually [YumiTextButton]s.
 */
@Composable
public fun YumiBanner(
    message: String,
    modifier: Modifier = Modifier,
    ledOn: Boolean? = null,
    onDismiss: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    YumiTray(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = YumiTheme.shapes.button,
        contentPadding = PaddingValues(start = 18.dp, end = 6.dp, top = 12.dp, bottom = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (ledOn != null) YumiLed(on = ledOn)
            Text(
                message,
                modifier = Modifier.weight(1f).padding(end = 12.dp, bottom = 6.dp),
                style = YumiTheme.typography.bodyMedium,
                color = YumiTheme.colors.ink,
            )
        }
        if (actions != null || onDismiss != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End)) {
                CompositionLocalProvider(LocalContentColor provides YumiTheme.colors.ink) {
                    actions?.invoke(this)
                    if (onDismiss != null) {
                        YumiTextButton(onClick = onDismiss) { Text(stringResource(R.string.yumi_dismiss)) }
                    }
                }
            }
        }
    }
}
