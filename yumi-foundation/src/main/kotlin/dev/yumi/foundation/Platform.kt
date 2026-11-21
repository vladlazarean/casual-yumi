package dev.yumi.foundation

import android.provider.Settings
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.platform.LocalContext

// The only android.* imports in yumi-foundation live in this file and YumiTypography.kt.

/**
 * `true` when the user turned animations off (animator duration scale 0).
 * Compose already snaps animations in that case; reading it lets Yumi pick `snap()` explicitly,
 * so state changes are instant rather than "a spring with zero duration".
 */
@Composable
internal fun isSystemReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}

/** Indication that draws nothing. Installed by [YumiTheme] so no foundation clickable ripples. */
internal object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        object : Modifier.Node() {}

    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = System.identityHashCode(this)
}
