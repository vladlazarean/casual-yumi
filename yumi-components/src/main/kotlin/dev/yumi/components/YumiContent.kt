package dev.yumi.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import dev.yumi.components.internal.PebbleKey
import dev.yumi.components.internal.flat
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.pebbleClickable
import dev.yumi.foundation.tray

// ── Avatar ─────────────────────────────────────────────────────────────────

/** Defaults for [YumiAvatar]. */
public object YumiAvatarDefaults {
    /** Default diameter. */
    public val Size: Dp = 40.dp
}

/**
 * An avatar: a round recessed well (a person is information, not a key) holding [initials] or an
 * [image]. An optional status LED sits on the bottom-end edge.
 *
 * @param initials one or two letters shown when there is no image.
 * @param contentDescription who this is, e.g. "Vlad".
 * @param modifier modifier for the avatar.
 * @param size diameter.
 * @param online `true` / `false` shows a lit / dark status LED; `null` hides it.
 * @param image optional image content; it is clipped to the circle.
 */
@Composable
public fun YumiAvatar(
    initials: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = YumiAvatarDefaults.Size,
    online: Boolean? = null,
    image: (@Composable () -> Unit)? = null,
) {
    val colors = YumiTheme.colors
    Box(
        modifier
            .size(size)
            .then(if (contentDescription != null) Modifier.clearAndSetSemantics { this.contentDescription = contentDescription } else Modifier),
    ) {
        Box(
            Modifier
                .size(size)
                .tray(colors.tray, CircleShape, YumiTheme.depth.copy(trayInsetRadius = size / 8, trayInsetOffset = DpOffset(0.dp, 1.5.dp)))
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (image != null) {
                image()
            } else {
                Text(
                    initials.take(2).uppercase(),
                    style = (if (size >= 56.dp) YumiTheme.typography.titleLarge else YumiTheme.typography.labelLarge)
                        .copy(textAlign = TextAlign.Center),
                    color = colors.ink,
                    maxLines = 1,
                )
            }
        }
        if (online != null) {
            val dot = (size / 4).coerceAtLeast(8.dp)
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(dot)
                    .background(colors.body, CircleShape)
                    .padding(2.dp)
                    .background(if (online) colors.accent else colors.seat, CircleShape),
            )
        }
    }
}

// ── Skeleton ───────────────────────────────────────────────────────────────

/**
 * A loading placeholder: a recessed block that breathes slowly while content loads (still with
 * reduced motion). Size it like the content it stands in for.
 *
 * @param modifier modifier giving the placeholder its size.
 * @param shape outline of the placeholder.
 */
@Composable
public fun YumiSkeleton(modifier: Modifier = Modifier, shape: Shape = YumiTheme.shapes.small) {
    val reduce = YumiTheme.motion.reduceMotion
    val alpha = if (reduce) {
        null
    } else {
        rememberInfiniteTransition(label = "yumiSkeleton").animateFloat(
            initialValue = 1f,
            targetValue = 0.55f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "yumiSkeletonAlpha",
        )
    }
    Box(
        modifier
            .graphicsLayer { this.alpha = alpha?.value ?: 1f }
            .background(YumiTheme.colors.tray, shape),
    )
}

/**
 * Placeholder lines for text that is loading, announced once as "Loading".
 *
 * @param modifier modifier for the block.
 * @param lines number of lines; the last one is shorter.
 */
@Composable
public fun YumiSkeletonLines(modifier: Modifier = Modifier, lines: Int = 3) {
    val loading = stringResource(R.string.yumi_loading)
    Column(
        modifier.clearAndSetSemantics { contentDescription = loading },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(lines) { i ->
            val fraction = when {
                i == lines - 1 && lines > 1 -> 0.6f
                i % 2 == 1 -> 0.85f
                else -> 1f
            }
            YumiSkeleton(Modifier.fillMaxWidth(fraction).height(14.dp), YumiTheme.shapes.pill)
        }
    }
}

// ── Empty state ────────────────────────────────────────────────────────────

/**
 * An empty state: a large recessed tray explaining why there is nothing here and, optionally,
 * the one action that fixes it (that action may be the screen's accent key).
 *
 * @param title what is empty, e.g. "No alarms yet".
 * @param modifier modifier for the tray.
 * @param message optional explanation.
 * @param icon optional illustration or icon above the title.
 * @param action optional key, e.g. an accent [YumiButton] "Add alarm".
 */
@Composable
public fun YumiEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    YumiTray(modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp)) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (icon != null) {
                CompositionLocalProvider(LocalContentColor provides YumiTheme.colors.muted) { icon() }
            }
            Text(title, style = YumiTheme.typography.titleLarge.copy(textAlign = TextAlign.Center), color = YumiTheme.colors.ink)
            if (message != null) {
                Text(message, style = YumiTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center), color = YumiTheme.colors.muted)
            }
            if (action != null) {
                Box(Modifier.padding(top = 8.dp)) { action() }
            }
        }
    }
}

// ── Expandable section ─────────────────────────────────────────────────────

/**
 * An expandable section: a full-width flat header key with a chevron, revealing [content] below.
 * TalkBack reads "Expanded" / "Collapsed".
 *
 * @param title the header content, usually a `Text`.
 * @param expanded whether the content is shown.
 * @param onExpandedChange called with the new state.
 * @param modifier modifier for the section.
 * @param enabled whether the header responds to input.
 * @param content the revealed content.
 */
@Composable
public fun YumiExpandableSection(
    title: @Composable () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val reduce = YumiTheme.motion.reduceMotion
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = if (reduce) snap() else tween(180),
        label = "yumiExpandChevron",
    )
    val state = stringResource(if (expanded) R.string.yumi_expanded else R.string.yumi_collapsed)
    Column(modifier.fillMaxWidth()) {
        PebbleKey(
            input = Modifier
                .pebbleClickable(
                    interactionSource = source,
                    enabled = enabled,
                    role = Role.Button,
                    haptics = YumiTheme.haptics,
                    onClick = { onExpandedChange(!expanded) },
                )
                .semantics { stateDescription = state },
            interactionSource = source,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            shape = YumiTheme.shapes.small,
            containerColor = Color.Transparent,
            contentColor = YumiTheme.colors.ink,
            seatColor = Color.Transparent,
            depth = YumiTheme.depth.flat(),
            minHeight = 56.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Box(Modifier.weight(1f)) {
                CompositionLocalProvider(LocalTextStyle provides YumiTheme.typography.titleMedium.copy(textAlign = TextAlign.Start)) { title() }
            }
            GlyphIcon(Glyph.ChevronDown, Modifier.rotate(rotation), color = YumiTheme.colors.muted)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = if (reduce) EnterTransition.None else expandVertically() + fadeIn(),
            exit = if (reduce) ExitTransition.None else shrinkVertically() + fadeOut(),
        ) {
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), content = content)
        }
    }
}

// ── Pager indicator ────────────────────────────────────────────────────────

/**
 * Page dots as a row of LEDs: the current page is a lit pill, the others are dark dots.
 * Announced as "Page 2 of 5".
 *
 * @param pageCount number of pages.
 * @param currentPage the current page, 0-based.
 * @param modifier modifier for the row.
 */
@Composable
public fun YumiPagerIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.yumi_page_of, currentPage + 1, pageCount)
    val colors = YumiTheme.colors
    Row(
        modifier.clearAndSetSemantics { stateDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { i ->
            val current = i == currentPage
            Box(
                Modifier
                    .size(width = if (current) 14.dp else 5.dp, height = 5.dp)
                    .background(if (current) colors.accent else colors.seat, YumiTheme.shapes.pill),
            )
        }
    }
}

// ── Carousel ───────────────────────────────────────────────────────────────

/** Defaults for [YumiCarousel]. */
public object YumiCarouselDefaults {
    /** Width of one item. */
    public val ItemWidth: Dp = 280.dp

    /** Gap between items. */
    public val ItemSpacing: Dp = 12.dp
}

/**
 * A horizontal carousel of recessed frames that snaps item by item, with LED page dots below.
 * Frames are recessed because media is something you look at; put a [YumiCard] inside if items
 * are pressable.
 *
 * @param items the items.
 * @param modifier modifier for the carousel.
 * @param state scroll state.
 * @param itemWidth width of one item.
 * @param itemHeight height of one item.
 * @param showIndicator whether to show [YumiPagerIndicator] below.
 * @param contentPadding padding around the row; keeps the first item off the edge.
 * @param itemContent content for one item.
 */
@Composable
public fun <T> YumiCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    itemWidth: Dp = YumiCarouselDefaults.ItemWidth,
    itemHeight: Dp = 180.dp,
    showIndicator: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
    itemContent: @Composable (T) -> Unit,
) {
    val fling = rememberSnapFlingBehavior(lazyListState = state)
    val current by remember(state) { derivedStateOf { state.firstVisibleItemIndex } }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LazyRow(
            state = state,
            flingBehavior = fling,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(YumiCarouselDefaults.ItemSpacing),
        ) {
            itemsIndexed(items) { _, item ->
                Box(
                    Modifier
                        .width(itemWidth)
                        .height(itemHeight)
                        .tray(YumiTheme.colors.tray, YumiTheme.shapes.card, YumiTheme.depth)
                        .clip(YumiTheme.shapes.card),
                    contentAlignment = Alignment.Center,
                ) { itemContent(item) }
            }
        }
        if (showIndicator && items.size > 1) {
            YumiPagerIndicator(pageCount = items.size, currentPage = current)
        }
    }
}
