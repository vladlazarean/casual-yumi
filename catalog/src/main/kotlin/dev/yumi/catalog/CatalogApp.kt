package dev.yumi.catalog

import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.yumi.components.YumiToastHost
import dev.yumi.components.YumiToastHostState
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.animateSinkAsState
import dev.yumi.foundation.pebble
import dev.yumi.foundation.pebbleClickable
import dev.yumi.foundation.tray
import dev.yumi.tokens.YumiAccent
import dev.yumi.tokens.YumiTokens

private val FontScales = listOf(1f, 1.5f, 2f)

/**
 * Catalog shell: every public component and its states, with Putty / Graphite, accent, RTL and
 * font-scale toggles at the top.
 */
@Composable
fun CatalogApp() {
    val systemDark = isSystemInDarkTheme()
    var dark by rememberSaveable { mutableStateOf(systemDark) }
    var accentIndex by rememberSaveable { mutableIntStateOf(0) }
    var rtl by rememberSaveable { mutableStateOf(false) }
    var fontScale by rememberSaveable { mutableFloatStateOf(1f) }

    // Status and navigation bar icons follow the in-app theme, not the system one.
    val activity = LocalActivity.current as? ComponentActivity
    LaunchedEffect(dark, activity) {
        val bars = if (dark) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        activity?.enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
    }

    val baseDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalDensity provides Density(baseDensity.density, baseDensity.fontScale * fontScale),
    ) {
        YumiTheme(darkTheme = dark, accent = YumiAccent.presets[accentIndex]) {
            val toasts = remember { YumiToastHostState() }
            CompositionLocalProvider(LocalCatalogToasts provides toasts) {
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(YumiTheme.colors.body)
                            .safeDrawingPadding(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(28.dp),
                    ) {
                        item {
                            Text("Casual Yumi", style = YumiTheme.typography.headlineMedium, color = YumiTheme.colors.ink)
                        }
                        item {
                            Section("Theme") {
                                ToggleRow {
                                    CatalogToggle("Putty", selected = !dark) { dark = false }
                                    CatalogToggle("Graphite", selected = dark) { dark = true }
                                }
                                Row(
                                    Modifier.selectableGroup(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    YumiAccent.presets.forEachIndexed { index, accent ->
                                        AccentSwatch(
                                            name = accent.name,
                                            color = if (dark) accent.dark.accent else accent.light.accent,
                                            selected = index == accentIndex,
                                        ) { accentIndex = index }
                                    }
                                }
                                ToggleRow {
                                    CatalogToggle("LTR", selected = !rtl) { rtl = false }
                                    CatalogToggle("RTL", selected = rtl) { rtl = true }
                                }
                                ToggleRow {
                                    FontScales.forEach { scale ->
                                        CatalogToggle("${(scale * 100).toInt()} %", selected = fontScale == scale) {
                                            fontScale = scale
                                        }
                                    }
                                }
                            }
                        }
                        item { FoundationSection() }
                        item { ButtonsSection() }
                        item { SelectionSection() }
                        item { SliderSection() }
                        item { SurfacesSection() }
                        item { FeedbackSection() }
                        item { AdvancedInputSection() }
                        item { NavigationSection() }
                        item { ExtrasSection() }
                        item { ScreensSection() }
                    }
                    YumiToastHost(
                        hostState = toasts,
                        modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding(),
                    )
                }
            }
        }
    }
}

@Composable
private fun FoundationSection() {
    Section("Foundation") {
        Label("Modifier.pebble(): raised, you press it")
        ToggleRow {
            DemoPebble("Neutral", YumiTheme.colors.raised, YumiTheme.colors.ink)
            DemoPebble("Accent", YumiTheme.colors.accent, YumiTheme.colors.onAccent)
            DemoPebble("Charcoal", YumiTheme.colors.charcoal, YumiTheme.colors.onCharcoal)
            DemoPebble("Disabled", YumiTheme.colors.raised, YumiTheme.colors.ink, enabled = false)
        }
        Spacer(Modifier.height(8.dp))
        Label("Modifier.tray(): recessed, you read it")
        Box(
            Modifier
                .fillMaxWidth()
                .tray(YumiTheme.colors.tray, YumiTheme.shapes.tray, YumiTheme.depth)
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ink on tray", color = YumiTheme.colors.ink, style = YumiTheme.typography.titleMedium)
                Text("Muted on tray", color = YumiTheme.colors.muted, style = YumiTheme.typography.bodyMedium)
                Text("Accent ink on tray", color = YumiTheme.colors.accentInk, style = YumiTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        Label("LED")
        Box(
            Modifier
                .size(width = 14.dp, height = 5.dp)
                .background(YumiTheme.colors.accent, YumiTheme.shapes.pill),
        )
    }
}

@Composable
private fun DemoPebble(label: String, fill: Color, content: Color, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val sink by animateSinkAsState(interactionSource, enabled = enabled)
    Box(
        Modifier
            .defaultMinSize(minHeight = YumiTokens.SecondaryMinHeight)
            .pebble(
                color = fill,
                shape = YumiTheme.shapes.button,
                seatColor = YumiTheme.colors.seat,
                depth = YumiTheme.depth,
                sinkOffset = YumiTheme.motion.sinkOffset,
                sink = { sink },
            )
            .pebbleClickable(
                interactionSource = interactionSource,
                enabled = enabled,
                role = Role.Button,
                haptics = YumiTheme.haptics,
                onLongClick = {},
                onClick = {},
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (enabled) content else content.copy(alpha = YumiTokens.DisabledContentAlpha),
            style = YumiTheme.typography.labelLarge,
        )
    }
}

/** Accent picker key: a round pebble with a dot of the accent colour; the chosen one rests sunk. */
@Composable
private fun AccentSwatch(name: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val sink by animateSinkAsState(interactionSource, selected = selected)
    Box(
        Modifier
            .size(48.dp)
            .pebble(
                color = YumiTheme.colors.raised,
                shape = YumiTheme.shapes.icon,
                seatColor = YumiTheme.colors.seat,
                depth = YumiTheme.depth,
                sinkOffset = YumiTheme.motion.sinkOffset,
                sink = { sink },
            )
            .pebbleClickable(
                interactionSource = interactionSource,
                enabled = true,
                role = Role.RadioButton,
                haptics = YumiTheme.haptics,
                onClick = onClick,
            )
            .semantics {
                this.selected = selected
                contentDescription = "$name accent"
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(18.dp).background(color, YumiTheme.shapes.pill))
    }
}

/** Catalog-only toggle: selected pebbles rest sunk with accent-ink text (no colour fill). */
@Composable
private fun CatalogToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val sink by animateSinkAsState(interactionSource, selected = selected)
    Box(
        Modifier
            .defaultMinSize(minHeight = YumiTokens.SecondaryMinHeight)
            .pebble(
                color = YumiTheme.colors.raised,
                shape = YumiTheme.shapes.button,
                seatColor = YumiTheme.colors.seat,
                depth = YumiTheme.depth,
                sinkOffset = YumiTheme.motion.sinkOffset,
                sink = { sink },
            )
            .pebbleClickable(
                interactionSource = interactionSource,
                enabled = true,
                role = Role.RadioButton,
                haptics = YumiTheme.haptics,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) YumiTheme.colors.accentInk else YumiTheme.colors.ink,
            style = YumiTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = YumiTheme.typography.titleLarge, color = YumiTheme.colors.ink)
        content()
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = YumiTheme.typography.labelMedium, color = YumiTheme.colors.muted)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToggleRow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}
