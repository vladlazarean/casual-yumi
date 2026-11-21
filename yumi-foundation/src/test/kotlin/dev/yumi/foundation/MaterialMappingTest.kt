package dev.yumi.foundation

import dev.yumi.tokens.YumiAccent
import dev.yumi.tokens.darkYumiColors
import dev.yumi.tokens.lightYumiColors
import org.junit.Assert.assertEquals
import org.junit.Test

class MaterialMappingTest {

    @Test
    fun coreRolesMapOntoYumiTokens() {
        for (colors in listOf(lightYumiColors(), darkYumiColors(YumiAccent.Sky))) {
            val scheme = colors.toColorScheme()
            assertEquals(colors.body, scheme.background)
            assertEquals(colors.body, scheme.surface)
            assertEquals(colors.raised, scheme.surfaceContainerHigh)
            assertEquals(colors.tray, scheme.surfaceDim)
            assertEquals(colors.seat, scheme.outlineVariant)
            assertEquals(colors.ink, scheme.onSurface)
            assertEquals(colors.muted, scheme.onSurfaceVariant)
            assertEquals(colors.accent, scheme.primary)
            assertEquals(colors.onAccent, scheme.onPrimary)
            assertEquals(colors.accentInk, scheme.tertiary)
            assertEquals(colors.charcoal, scheme.inverseSurface)
        }
    }

    @Test
    fun elevationNeverTintsWithTheAccent() {
        val colors = lightYumiColors()
        assertEquals(colors.body, colors.toColorScheme().surfaceTint)
    }
}
