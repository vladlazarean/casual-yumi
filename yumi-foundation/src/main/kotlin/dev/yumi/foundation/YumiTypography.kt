package dev.yumi.foundation

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

/** Type families shipped with Casual Yumi. */
public object YumiFonts {

    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.yumi_gms_fonts_certs,
    )

    private val manrope = GoogleFont("Manrope")

    /**
     * Manrope, fetched through Google Play services downloadable fonts (nothing bundled in the
     * AAR). Until it arrives, or on devices without Play services, text falls back to the system
     * sans-serif, so layout never blocks on the network.
     */
    public val Manrope: FontFamily = FontFamily(
        Font(googleFont = manrope, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = manrope, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = manrope, fontProvider = provider, weight = FontWeight.SemiBold),
        Font(googleFont = manrope, fontProvider = provider, weight = FontWeight.Bold),
    )
}

/**
 * The Material 3 type scale with every style set in [fontFamily]. One family, M3 sizes and
 * weights, so mixed M3 + Yumi screens stay typographically coherent.
 */
public fun yumiTypography(fontFamily: FontFamily = YumiFonts.Manrope): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = fontFamily),
    )
}
