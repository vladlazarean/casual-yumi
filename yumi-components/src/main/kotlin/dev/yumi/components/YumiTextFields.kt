package dev.yumi.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.yumi.components.internal.Glyph
import dev.yumi.components.internal.GlyphIcon
import dev.yumi.foundation.YumiTheme
import dev.yumi.foundation.tray
import dev.yumi.tokens.YumiTokens

/** Defaults for [YumiTextField], [YumiSecureTextField] and [YumiSearchField]. */
public object YumiTextFieldDefaults {
    /** Minimum height of the recessed field. */
    public val MinHeight: Dp = 56.dp

    /** Width of the focus / error rim. */
    public val RimWidth: Dp = 2.dp

    /** Field shape. */
    public val shape: Shape
        @Composable get() = YumiTheme.shapes.small
}

/**
 * A text field: a recessed well, because you read what's in it. Focus draws a 2 dp accent-ink
 * rim; an error draws the rim and supporting text in the error colour.
 *
 * The label sits above the well (not floating) so it never animates over the value or clips at
 * 200 % font scale. Label, value and supporting text are one TalkBack node, as in Material.
 *
 * @param value current text.
 * @param onValueChange called with the new text.
 * @param modifier modifier for the whole field (label, well, supporting text).
 * @param enabled whether the field is editable and focusable.
 * @param readOnly whether the text can be selected but not edited.
 * @param label label above the well.
 * @param placeholder hint shown while empty.
 * @param leadingIcon content before the text.
 * @param trailingIcon content after the text.
 * @param supportingText help or error text below the well.
 * @param isError whether the value is invalid.
 * @param shape well shape.
 * @param singleLine whether the field is one line.
 * @param maxLines maximum lines when not [singleLine].
 * @param keyboardOptions IME options.
 * @param keyboardActions IME action handlers.
 * @param visualTransformation display transformation of the value.
 * @param interactionSource source of focus interactions; `null` creates one internally.
 */
@Composable
public fun YumiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    shape: Shape = YumiTextFieldDefaults.shape,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val errorMessage = supportingText.orEmpty()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .alpha(if (enabled) 1f else YumiTokens.DisabledContentAlpha)
            .semantics { if (isError) error(errorMessage) },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = YumiTheme.typography.bodyLarge.copy(color = YumiTheme.colors.ink),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        interactionSource = source,
        cursorBrush = SolidColor(YumiTheme.colors.accentInk),
        decorationBox = { innerTextField ->
            FieldFrame(
                label = label,
                placeholder = placeholder,
                showPlaceholder = value.isEmpty(),
                supportingText = supportingText,
                isError = isError,
                focused = focused,
                shape = shape,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                innerTextField = innerTextField,
            )
        },
    )
}

/**
 * A password field on the same recessed well, built on `BasicSecureTextField` (obfuscation,
 * no copy / cut, password keyboard). A flat "Show" / "Hide" key toggles visibility.
 *
 * @param state text state; hoist with `rememberTextFieldState()`.
 * @param modifier modifier for the whole field.
 * @param enabled whether the field is editable and focusable.
 * @param label label above the well.
 * @param placeholder hint shown while empty.
 * @param supportingText help or error text below the well.
 * @param isError whether the value is invalid.
 * @param leadingIcon content before the text.
 * @param keyboardOptions IME options; defaults to a password keyboard with a Done action.
 * @param onKeyboardAction IME action handler.
 * @param interactionSource source of focus interactions; `null` creates one internally.
 */
@Composable
public fun YumiSecureTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
    onKeyboardAction: KeyboardActionHandler? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    var revealed by rememberSaveable { mutableStateOf(false) }
    val shape = YumiTextFieldDefaults.shape
    val errorMessage = supportingText.orEmpty()
    BasicSecureTextField(
        state = state,
        modifier = modifier
            .alpha(if (enabled) 1f else YumiTokens.DisabledContentAlpha)
            .semantics { if (isError) error(errorMessage) },
        enabled = enabled,
        textStyle = YumiTheme.typography.bodyLarge.copy(color = YumiTheme.colors.ink),
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        interactionSource = source,
        cursorBrush = SolidColor(YumiTheme.colors.accentInk),
        textObfuscationMode = if (revealed) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
        decorator = TextFieldDecorator { innerTextField ->
            FieldFrame(
                label = label,
                placeholder = placeholder,
                showPlaceholder = state.text.isEmpty(),
                supportingText = supportingText,
                isError = isError,
                focused = focused,
                shape = shape,
                leadingIcon = leadingIcon,
                trailingIcon = {
                    YumiTextButton(
                        onClick = { revealed = !revealed },
                        enabled = enabled,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(stringResource(if (revealed) R.string.yumi_hide_password else R.string.yumi_show_password))
                    }
                },
                innerTextField = innerTextField,
            )
        },
    )
}

/**
 * A search field: a pill-shaped recessed well with a search glyph, a clear key while there is
 * text, and the Search IME action.
 *
 * @param query current query.
 * @param onQueryChange called with the new query.
 * @param onSearch called with the query on the IME Search action.
 * @param modifier modifier for the field.
 * @param enabled whether the field is editable.
 * @param placeholder hint shown while empty.
 */
@Composable
public fun YumiSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = stringResource(R.string.yumi_search),
) {
    val clearLabel = stringResource(R.string.yumi_clear)
    YumiTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        enabled = enabled,
        placeholder = placeholder,
        shape = YumiTheme.shapes.pill,
        leadingIcon = { GlyphIcon(Glyph.Search) },
        trailingIcon = if (query.isEmpty()) {
            null
        } else {
            {
                YumiTextButton(
                    onClick = { onQueryChange("") },
                    enabled = enabled,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.semantics { contentDescription = clearLabel },
                ) { GlyphIcon(Glyph.Close) }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
    )
}

@Composable
internal fun FieldFrame(
    label: String?,
    placeholder: String?,
    showPlaceholder: Boolean,
    supportingText: String?,
    isError: Boolean,
    focused: Boolean,
    shape: Shape,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    innerTextField: @Composable () -> Unit,
) {
    val colors = YumiTheme.colors
    val rim = when {
        isError -> colors.error
        focused -> colors.accentInk
        else -> null
    }
    Column(Modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                label,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                style = YumiTheme.typography.labelMedium,
                color = colors.muted,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = YumiTextFieldDefaults.MinHeight)
                .tray(colors.tray, shape, YumiTheme.depth.copy(trayInsetRadius = 6.dp))
                .then(if (rim != null) Modifier.border(YumiTextFieldDefaults.RimWidth, rim, shape) else Modifier)
                .padding(start = if (leadingIcon != null) 14.dp else 16.dp, end = if (trailingIcon != null) 4.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalContentColor provides colors.muted) {
                leadingIcon?.invoke()
            }
            Box(Modifier.weight(1f).padding(vertical = 14.dp).defaultMinSize(minHeight = 24.dp), contentAlignment = Alignment.CenterStart) {
                if (showPlaceholder && placeholder != null) {
                    Text(placeholder, style = YumiTheme.typography.bodyLarge, color = colors.muted)
                }
                innerTextField()
            }
            CompositionLocalProvider(LocalContentColor provides colors.ink) {
                trailingIcon?.invoke()
            }
        }
        if (supportingText != null) {
            Text(
                supportingText,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                style = YumiTheme.typography.bodySmall,
                color = if (isError) colors.error else colors.muted,
            )
        }
    }
}
