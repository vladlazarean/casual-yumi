package dev.yumi.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.LocalYumiSurfaceLevel
import dev.yumi.components.internal.NavKey
import dev.yumi.components.internal.YumiSurfaceLevel
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.tray

/** Defaults for the navigation components. */
public object YumiNavigationDefaults {
    /** Height of [YumiTopAppBar], excluding insets. */
    public val TopAppBarHeight: Dp = 72.dp

    /** Height of [YumiNavigationBar], excluding insets. */
    public val NavigationBarHeight: Dp = 80.dp

    /** Width of [YumiNavigationRail]. */
    public val NavigationRailWidth: Dp = 88.dp

    /** Insets applied by [YumiTopAppBar]. */
    public val topAppBarWindowInsets: WindowInsets
        @Composable get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)

    /** Insets applied by [YumiNavigationBar]. */
    public val navigationBarWindowInsets: WindowInsets
        @Composable get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)

    /** Insets applied by [YumiNavigationRail]. */
    public val navigationRailWindowInsets: WindowInsets
        @Composable get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
}

/**
 * A top app bar on the matte body: no tint, no elevation, no colour change on scroll. The title
 * is marked as a heading for TalkBack. Put a [YumiIconButton] in [navigationIcon] and up to
 * two icon pebbles (or a menu) in [actions].
 *
 * @param title the title, usually a `Text`.
 * @param modifier modifier for the bar.
 * @param navigationIcon optional key before the title (back, menu).
 * @param actions keys after the title.
 * @param windowInsets insets to pad for; defaults to the status bar and horizontal cutouts.
 */
@Composable
public fun YumiTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = YumiNavigationDefaults.topAppBarWindowInsets,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .heightIn(min = YumiNavigationDefaults.TopAppBarHeight)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigationIcon?.invoke()
        if (navigationIcon == null) Spacer(Modifier.width(4.dp))
        Box(Modifier.weight(1f).semantics { heading() }) {
            CompositionLocalProvider(
                LocalContentColor provides YumiTheme.colors.ink,
                LocalTextStyle provides YumiTheme.typography.titleLarge,
            ) { title() }
        }
        actions()
    }
}

/**
 * A navigation bar: a recessed strip along the bottom, like the remote's lower panel, holding
 * flat keys. Use [YumiNavigationBarItem] inside; 3–5 destinations.
 *
 * @param modifier modifier for the bar.
 * @param windowInsets insets to pad for; defaults to the navigation bar.
 * @param content the items.
 */
@Composable
public fun YumiNavigationBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = YumiNavigationDefaults.navigationBarWindowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .tray(YumiTheme.colors.tray, RectangleShape, YumiTheme.depth)
            .windowInsetsPadding(windowInsets)
            .heightIn(min = YumiNavigationDefaults.NavigationBarHeight)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalYumiSurfaceLevel provides YumiSurfaceLevel.Recessed) { content() }
    }
}

/**
 * One destination in a [YumiNavigationBar]. Selected = sunk, accent-ink icon and label, LED
 * underline. Same key as rail items and tabs, so navigation feels identical everywhere.
 *
 * @param selected whether this destination is current.
 * @param onClick called on click.
 * @param icon the destination icon, with a `null` contentDescription (the label names it).
 * @param label the destination label.
 * @param modifier modifier for the item.
 * @param enabled whether the item responds to input.
 * @param alwaysShowLabel `false` shows the label only when selected.
 */
@Composable
public fun RowScope.YumiNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
) {
    NavKey(
        selected = selected,
        onClick = onClick,
        modifier = modifier.weight(1f),
        enabled = enabled,
        icon = icon,
        label = label,
        showLabel = alwaysShowLabel,
        minHeight = 64.dp,
        role = Role.Tab,
    )
}

/**
 * A navigation rail for wide screens: a recessed strip along the start edge holding flat keys.
 *
 * @param modifier modifier for the rail.
 * @param header optional content above the items, e.g. a [YumiFab].
 * @param windowInsets insets to pad for.
 * @param content the items.
 */
@Composable
public fun YumiNavigationRail(
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null,
    windowInsets: WindowInsets = YumiNavigationDefaults.navigationRailWindowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .tray(YumiTheme.colors.tray, RectangleShape, YumiTheme.depth)
            .windowInsetsPadding(windowInsets)
            .width(YumiNavigationDefaults.NavigationRailWidth)
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .selectableGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (header != null) {
            // The header (often an accent FAB) is the rail's one raised key.
            header()
            Spacer(Modifier.height(8.dp))
        }
        CompositionLocalProvider(LocalYumiSurfaceLevel provides YumiSurfaceLevel.Recessed) { content() }
    }
}

/**
 * One destination in a [YumiNavigationRail]. Behaves exactly like [YumiNavigationBarItem].
 *
 * @param selected whether this destination is current.
 * @param onClick called on click.
 * @param icon the destination icon.
 * @param label the destination label.
 * @param modifier modifier for the item.
 * @param enabled whether the item responds to input.
 * @param alwaysShowLabel `false` shows the label only when selected.
 */
@Composable
public fun YumiNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
) {
    NavKey(
        selected = selected,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        icon = icon,
        label = label,
        showLabel = alwaysShowLabel,
        minHeight = 64.dp,
        role = Role.Tab,
    )
}

/**
 * A row of tabs with equal widths, on the body with a seat hairline underneath.
 *
 * Unlike Material there is no sliding indicator: the selected [YumiTab] itself sinks and lights
 * its LED, the same as every other selected key.
 *
 * @param selectedTabIndex index of the selected tab. Kept for Material API symmetry; each tab's
 *   own `selected` flag drives its look.
 * @param modifier modifier for the row.
 * @param tabs the [YumiTab]s.
 */
@Composable
public fun YumiTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) {
    val seat = YumiTheme.colors.seat
    Layout(
        content = tabs,
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup()
            .drawBehind {
                val y = size.height - 0.5.dp.toPx()
                drawLine(seat, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            },
    ) { measurables, constraints ->
        // Equal widths: every tab is the same size key, regardless of label length.
        val count = measurables.size.coerceAtLeast(1)
        val tabWidth = constraints.maxWidth / count
        val placeables = measurables.map {
            it.measure(Constraints(minWidth = tabWidth, maxWidth = tabWidth, minHeight = 0, maxHeight = constraints.maxHeight))
        }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(constraints.maxWidth, height) {
            placeables.forEachIndexed { i, p -> p.placeRelative(i * tabWidth, (height - p.height) / 2) }
        }
    }
}

/**
 * One tab in a [YumiTabRow].
 *
 * @param selected whether this tab is selected.
 * @param onClick called on click.
 * @param modifier modifier for the tab.
 * @param enabled whether the tab responds to input.
 * @param text the tab label.
 * @param icon optional icon above the label.
 */
@Composable
public fun YumiTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    NavKey(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        label = text,
        showLabel = true,
        minHeight = 48.dp,
        role = Role.Tab,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    )
}
