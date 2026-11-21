package dev.yumi.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.yumi.tokens.YumiTokens

// Debug source set: these previews are never part of the published release AAR.

@PreviewLightDark
@Composable
private fun PebbleStatesPreview() {
    YumiTheme {
        Column(
            Modifier
                .background(YumiTheme.colors.body)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PreviewPebble("Raised", sink = 0f, color = YumiTheme.colors.raised, content = YumiTheme.colors.ink)
                PreviewPebble("Sunk", sink = 1f, color = YumiTheme.colors.raised, content = YumiTheme.colors.ink)
            }
            PreviewPebble("Accent", sink = 0f, color = YumiTheme.colors.accent, content = YumiTheme.colors.onAccent)
            Box(
                Modifier
                    .fillMaxWidth()
                    .tray(YumiTheme.colors.tray, YumiTheme.shapes.tray, YumiTheme.depth)
                    .padding(20.dp),
            ) {
                Text("Tray: recessed, you read it", color = YumiTheme.colors.muted)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun InteractivePebblePreview() {
    YumiTheme {
        val interactionSource = remember { MutableInteractionSource() }
        val sink by animateSinkAsState(interactionSource)
        Box(Modifier.background(YumiTheme.colors.body).padding(24.dp)) {
            Box(
                Modifier
                    .size(YumiTokens.IconPebbleSize)
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
                        role = Role.Button,
                        haptics = YumiTheme.haptics,
                        onClick = {},
                    ),
                contentAlignment = Alignment.Center,
            ) { Text("OK", color = YumiTheme.colors.ink) }
        }
    }
}

@Composable
private fun PreviewPebble(label: String, sink: Float, color: Color, content: Color) {
    Box(
        Modifier
            .defaultMinSize(minHeight = YumiTokens.SecondaryMinHeight, minWidth = 120.dp)
            .pebble(
                color = color,
                shape = YumiTheme.shapes.button,
                seatColor = YumiTheme.colors.seat,
                depth = YumiTheme.depth,
                sinkOffset = YumiTheme.motion.sinkOffset,
                sink = { sink },
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = content, style = YumiTheme.typography.labelLarge) }
}
