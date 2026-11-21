package dev.yumi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiHaptics
import dev.yumi.foundation.YumiTheme
import dev.yumi.tokens.YumiAccent

/**
 * Deterministic theme for tests: system sans instead of the downloadable Manrope (no Play
 * services under Robolectric), and injectable haptics.
 */
@Composable
fun TestTheme(
    dark: Boolean = false,
    accent: YumiAccent = YumiAccent.Orange,
    haptics: YumiHaptics = YumiHaptics.None,
    content: @Composable () -> Unit,
) {
    YumiTheme(darkTheme = dark, accent = accent, fontFamily = FontFamily.SansSerif, haptics = haptics) {
        Box(Modifier.background(YumiTheme.colors.body).padding(16.dp)) { content() }
    }
}

/** Counts haptic events so tests can assert on them. */
class RecordingHaptics : YumiHaptics {
    override val enabled: Boolean = true
    var ticks: Int = 0
    val toggles: MutableList<Boolean> = mutableListOf()
    var longPresses: Int = 0
    var confirms: Int = 0
    override fun tick() { ticks++ }
    override fun toggle(on: Boolean) { toggles += on }
    override fun longPress() { longPresses++ }
    override fun confirm() { confirms++ }
}
