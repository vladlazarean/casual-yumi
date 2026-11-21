package dev.yumi.tokens

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {

    @Test
    fun ratioMatchesWcagReference() {
        assertEquals(21f, YumiContrast.ratio(Color.Black, Color.White), 0.01f)
        assertEquals(1f, YumiContrast.ratio(Color.White, Color.White), 0.001f)
    }

    @Test
    fun everyAccentPresetPassesAA() {
        for (accent in YumiAccent.presets) {
            assertTrue("${accent.name} fails WCAG AA", accent.meetsContrastAA())
        }
    }

    @Test
    fun inkAndMutedPassOnEveryReadingSurface() {
        for (colors in listOf(lightYumiColors(), darkYumiColors())) {
            for (surface in listOf(colors.body, colors.raised, colors.tray)) {
                assertAA("ink", colors.ink, surface, colors)
                assertAA("muted", colors.muted, surface, colors)
            }
            assertAA("onCharcoal", colors.onCharcoal, colors.charcoal, colors)
            assertAA("onError", colors.onError, colors.error, colors)
        }
    }

    private fun assertAA(name: String, fg: Color, bg: Color, colors: YumiColors) {
        val ratio = YumiContrast.ratio(fg, bg)
        assertTrue(
            "$name on $bg is $ratio:1 (dark=${colors.isDark}), needs ${YumiContrast.AA_NORMAL_TEXT}",
            ratio >= YumiContrast.AA_NORMAL_TEXT,
        )
    }
}
