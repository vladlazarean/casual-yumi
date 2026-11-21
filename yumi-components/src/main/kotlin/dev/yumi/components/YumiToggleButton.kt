package dev.yumi.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import dev.yumi.components.internal.PebbleKey
import dev.yumi.foundation.YumiTheme

/**
 * A latching key: it stays pushed in while [checked], and its LED lights.
 *
 * Use for on/off actions that live in a row of keys (mute, repeat, "read it to me"). For a
 * setting in a list, prefer [YumiSwitch].
 *
 * @param checked whether the key is latched in.
 * @param onCheckedChange called with the new state when toggled.
 * @param modifier modifier for the key.
 * @param enabled whether the key responds to input.
 * @param shape outline of the key.
 * @param colors colours for each state.
 * @param contentPadding padding around [content].
 * @param interactionSource source of press interactions; `null` creates one internally.
 * @param content label; an LED is drawn before it.
 */
@Composable
public fun YumiToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = YumiButtonDefaults.shape,
    colors: YumiButtonColors = YumiButtonDefaults.colors(YumiButtonVariant.Neutral),
    contentPadding: PaddingValues = YumiButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val haptics = YumiTheme.haptics
    val state = stringResource(if (checked) R.string.yumi_state_on else R.string.yumi_state_off)
    PebbleKey(
        input = Modifier
            .toggleable(
                value = checked,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = { on ->
                    haptics.toggle(on)
                    onCheckedChange(on)
                },
            )
            .semantics { stateDescription = state },
        interactionSource = source,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        containerColor = colors.containerColor(enabled),
        contentColor = colors.contentColor(enabled),
        selected = checked,
        minHeight = YumiButtonDefaults.MinHeight,
        contentPadding = contentPadding,
    ) {
        YumiLed(on = checked && enabled)
        content()
    }
}
