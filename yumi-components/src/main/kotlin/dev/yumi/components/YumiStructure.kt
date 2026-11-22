package dev.yumi.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.BackHandler
import androidx.compose.ui.window.DialogProperties
import dev.yumi.components.internal.LocalYumiSurfaceLevel
import dev.yumi.components.internal.PebbleKey
import dev.yumi.components.internal.YumiSurfaceLevel
import dev.yumi.components.internal.flat
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.tray

// ── Scaffold ───────────────────────────────────────────────────────────────

/**
 * Material 3's `Scaffold` on the matte body, with a slot wired for Yumi toasts.
 *
 * @param modifier modifier for the scaffold.
 * @param topBar usually a [YumiTopAppBar].
 * @param bottomBar usually a [YumiNavigationBar] or [YumiBottomAppBar].
 * @param toastHostState when set, its toasts appear above the bottom bar.
 * @param floatingActionButton usually a [YumiFab]: the screen's one accent key.
 * @param contentWindowInsets insets passed to [content] as padding.
 * @param content the screen; apply the given padding.
 */
@Composable
public fun YumiScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    toastHostState: YumiToastHostState? = null,
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = { if (toastHostState != null) YumiToastHost(toastHostState) },
        floatingActionButton = floatingActionButton,
        containerColor = YumiTheme.colors.body,
        contentColor = YumiTheme.colors.ink,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

// ── Navigation drawer ──────────────────────────────────────────────────────

/** Defaults for [YumiModalNavigationDrawer]. */
public object YumiDrawerDefaults {
    /** Drawer panel shape: rounded on the end edge only. */
    public val shape: Shape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)

    /** Maximum drawer width. */
    public val MaxWidth: Dp = 320.dp
}

/**
 * A modal navigation drawer: a body-coloured panel that slides in from the start edge over a
 * scrim. Built on Material 3's `ModalNavigationDrawer` (gestures, scrim, back). Fill it with
 * [YumiNavigationDrawerItem]s.
 *
 * @param drawerContent the drawer's content.
 * @param modifier modifier for the drawer.
 * @param drawerState open / closed state, for opening from a menu key.
 * @param gesturesEnabled whether swiping opens and closes it.
 * @param content the screen behind the drawer.
 */
@Composable
public fun YumiModalNavigationDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.widthIn(max = YumiDrawerDefaults.MaxWidth),
                drawerShape = YumiDrawerDefaults.shape,
                drawerContainerColor = YumiTheme.colors.body,
                drawerContentColor = YumiTheme.colors.ink,
                drawerTonalElevation = 0.dp,
            ) {
                CompositionLocalProvider(LocalYumiSurfaceLevel provides YumiSurfaceLevel.Body) {
                    Column(Modifier.padding(12.dp).selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        drawerContent()
                    }
                }
            }
        },
        modifier = modifier,
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        scrimColor = YumiBottomSheetDefaults.ScrimColor,
        content = content,
    )
}

/**
 * One destination in a navigation drawer: a full-width flat key. Selected = sunk, accent-ink
 * label and an LED at the end, the same language as navigation-bar items.
 *
 * @param label the destination name.
 * @param selected whether this destination is current.
 * @param onClick called on click.
 * @param modifier modifier for the item.
 * @param icon optional icon before the label.
 * @param badge optional content at the end, e.g. a [YumiBadge].
 */
@Composable
public fun YumiNavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
) {
    val source = remember { MutableInteractionSource() }
    val colors = YumiTheme.colors
    PebbleKey(
        input = Modifier.selectable(
            selected = selected,
            interactionSource = source,
            indication = null,
            role = Role.Tab,
            onClick = onClick,
        ),
        interactionSource = source,
        modifier = modifier.fillMaxWidth(),
        enabled = true,
        shape = YumiTheme.shapes.button,
        containerColor = Color.Transparent,
        contentColor = if (selected) colors.accentInk else colors.ink,
        seatColor = Color.Transparent,
        depth = YumiTheme.depth.flat(),
        selected = selected,
        minHeight = 56.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        if (icon != null) icon()
        Box(Modifier.weight(1f)) {
            CompositionLocalProvider(LocalTextStyle provides YumiTheme.typography.labelLarge) { label() }
        }
        badge?.invoke()
        YumiLed(on = selected)
    }
}

// ── Bottom app bar ─────────────────────────────────────────────────────────

/**
 * A bottom app bar: a recessed strip holding up to four flat or icon keys for the current screen,
 * with an optional [floatingActionButton] (the screen's accent key) at the end.
 *
 * @param modifier modifier for the bar.
 * @param floatingActionButton optional [YumiFab].
 * @param windowInsets insets to pad for.
 * @param actions the keys.
 */
@Composable
public fun YumiBottomAppBar(
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
    windowInsets: WindowInsets = YumiNavigationDefaults.navigationBarWindowInsets,
    actions: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .tray(YumiTheme.colors.tray, RectangleShape, YumiTheme.depth)
            .windowInsetsPadding(windowInsets)
            .heightIn(min = YumiNavigationDefaults.NavigationBarHeight)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalYumiSurfaceLevel provides YumiSurfaceLevel.Recessed) {
            actions()
        }
        if (floatingActionButton != null) {
            Spacer(Modifier.weight(1f))
            floatingActionButton()
        }
    }
}

// ── Side sheet ─────────────────────────────────────────────────────────────

/** Defaults for [YumiModalSideSheet]. */
public object YumiSideSheetDefaults {
    /** Sheet width on large screens. */
    public val MaxWidth: Dp = 400.dp

    /** Sheet shape: rounded on the start edge only. */
    public val shape: Shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
}

/**
 * A modal side sheet sliding in from the end edge over a scrim, for filters or details on tablets
 * and foldables. Body-coloured, so pebbles inside keep their depth. Back and scrim tap dismiss.
 *
 * Material 3 for Compose has no side sheet yet. By default ([fullScreen] `true`) the sheet opens in
 * a full-screen `Dialog` over the whole window, which is what an app screen wants. With
 * [fullScreen] `false` it is drawn inside its parent instead, like `YumiModalNavigationDrawer`:
 * place it last in a `Box` that fills the area it should cover, for example a preview pane.
 *
 * @param visible whether the sheet is shown.
 * @param onDismissRequest called on back or scrim tap.
 * @param modifier modifier for the panel.
 * @param title optional title at the top of the panel.
 * @param fullScreen `true` to open over the whole window, `false` to stay inside the parent.
 * @param content panel content.
 */
@Composable
public fun YumiModalSideSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    fullScreen: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shown = remember { MutableTransitionState(false) }
    shown.targetState = visible
    if (!shown.currentState && !shown.targetState) return
    if (fullScreen) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            SideSheetFrame(shown, onDismissRequest, modifier, title, insets = true, content = content)
        }
    } else {
        BackHandler(enabled = visible, onBack = onDismissRequest)
        SideSheetFrame(shown, onDismissRequest, modifier, title, insets = false, content = content)
    }
}

@Composable
private fun SideSheetFrame(
    shown: MutableTransitionState<Boolean>,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    title: (@Composable () -> Unit)?,
    insets: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    val reduceMotion = YumiTheme.motion.reduceMotion
    val pane = stringResource(R.string.yumi_sheet_pane)
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visibleState = shown, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(YumiBottomSheetDefaults.ScrimColor)
                    .clickable(interactionSource = null, indication = null, onClick = onDismissRequest),
            )
        }
        AnimatedVisibility(
            visibleState = shown,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = if (reduceMotion) fadeIn() else slideInHorizontally { it },
            exit = if (reduceMotion) fadeOut() else slideOutHorizontally { it },
        ) {
            val shape = YumiSideSheetDefaults.shape
            Column(
                modifier
                    .fillMaxHeight()
                    .widthIn(max = YumiSideSheetDefaults.MaxWidth)
                    .semantics { paneTitle = pane }
                    .dropShadow(shape, Shadow(radius = 28.dp, color = Color.Black, offset = DpOffset((-6).dp, 0.dp), alpha = YumiTheme.depth.shadowAlpha * 1.4f))
                    .background(YumiTheme.colors.body, shape)
                    .then(
                        if (insets) {
                            Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.End))
                        } else {
                            Modifier
                        },
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CompositionLocalProvider(
                    LocalYumiSurfaceLevel provides YumiSurfaceLevel.Body,
                    LocalContentColor provides YumiTheme.colors.ink,
                ) {
                    if (title != null) {
                        CompositionLocalProvider(LocalTextStyle provides YumiTheme.typography.headlineSmall) { title() }
                    }
                    content()
                }
            }
        }
    }
}

// ── Standard bottom sheet ──────────────────────────────────────────────────

/**
 * A screen with a standard (non-modal) bottom sheet that peeks above the content and can be
 * dragged up, like a map's details panel. Built on Material 3's `BottomSheetScaffold`, with the
 * body colour, no tint and the recessed Yumi drag handle.
 *
 * @param sheetContent content of the sheet.
 * @param modifier modifier for the scaffold.
 * @param scaffoldState state of the sheet, for expanding from code.
 * @param sheetPeekHeight height of the sheet when collapsed.
 * @param topBar optional top bar.
 * @param content the screen behind the sheet; apply the given padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun YumiBottomSheetScaffold(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    sheetPeekHeight: Dp = 96.dp,
    topBar: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    BottomSheetScaffold(
        sheetContent = {
            CompositionLocalProvider(LocalYumiSurfaceLevel provides YumiSurfaceLevel.Body) { sheetContent() }
        },
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetShape = YumiBottomSheetDefaults.shape,
        sheetContainerColor = YumiTheme.colors.body,
        sheetContentColor = YumiTheme.colors.ink,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 12.dp,
        sheetDragHandle = { YumiBottomSheetDefaults.DragHandle() },
        topBar = topBar,
        containerColor = YumiTheme.colors.body,
        contentColor = YumiTheme.colors.ink,
        content = content,
    )
}
