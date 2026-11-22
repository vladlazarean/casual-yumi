package dev.yumi.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme

/** One spacing scale for the whole catalog. */
internal object CatalogSpacing {
    /** Between sections. */
    val Section = 48.dp

    /** Between the section title and its first group. */
    val Title = 16.dp

    /** Between groups inside a section. */
    val Group = 28.dp

    /** Between a group label and its first item. */
    val Label = 8.dp

    /** Between items inside a group, and between keys in a row. */
    val Item = 12.dp
}

/** A titled section made of [CatalogGroup]s. */
@Composable
internal fun CatalogSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            title,
            Modifier.semantics { heading() },
            style = YumiTheme.typography.titleLarge,
            color = YumiTheme.colors.ink,
        )
        Spacer(Modifier.height(CatalogSpacing.Title))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CatalogSpacing.Group), content = content)
    }
}

/** A small labelled group of items; the label sits closer to its items than to the group above. */
@Composable
internal fun CatalogGroup(label: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        if (label != null) {
            Text(label, style = YumiTheme.typography.labelMedium, color = YumiTheme.colors.muted)
            Spacer(Modifier.height(CatalogSpacing.Label))
        }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CatalogSpacing.Item), content = content)
    }
}

/** Keys in a wrapping row, centred on one line. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CatalogFlow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(CatalogSpacing.Item),
        verticalArrangement = Arrangement.spacedBy(CatalogSpacing.Item),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

/** Items in a single row, centred vertically. */
@Composable
internal fun CatalogRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(CatalogSpacing.Item),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** A muted line explaining the group above. */
@Composable
internal fun CatalogNote(text: String) {
    Text(text, style = YumiTheme.typography.bodySmall, color = YumiTheme.colors.muted)
}

/** A control with its text label; both are read out together. */
@Composable
internal fun Labeled(label: String, control: @Composable () -> Unit) {
    Row(
        Modifier.semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        control()
        Text(label, color = YumiTheme.colors.ink, style = YumiTheme.typography.bodyMedium)
    }
}
