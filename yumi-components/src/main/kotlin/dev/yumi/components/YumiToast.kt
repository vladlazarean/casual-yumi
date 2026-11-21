package dev.yumi.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.yumi.foundation.YumiTheme
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/** How long a toast stays up. */
public enum class YumiToastDuration {
    /** About 2.5 s. */
    Short,

    /** About 6 s. */
    Long,

    /** Until dismissed or its action is pressed. Use only with an action. */
    Indefinite,
}

/** Why a toast went away. */
public enum class YumiToastResult {
    /** Timed out or was dismissed. */
    Dismissed,

    /** The user pressed the action. */
    ActionPerformed,
}

/** What a toast shows. */
@Immutable
public class YumiToastVisuals(
    /** Message text. Keep it to one short sentence. */
    public val message: String,
    /** Optional action label, e.g. "Undo". */
    public val actionLabel: String? = null,
    /** How long the toast stays up. */
    public val duration: YumiToastDuration = if (actionLabel == null) YumiToastDuration.Short else YumiToastDuration.Long,
    /** Whether a dismiss affordance is shown (snackbar only). */
    public val withDismissAction: Boolean = false,
) {
    override fun equals(other: Any?): Boolean =
        other is YumiToastVisuals && message == other.message && actionLabel == other.actionLabel &&
            duration == other.duration && withDismissAction == other.withDismissAction

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (actionLabel?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        result = 31 * result + withDismissAction.hashCode()
        return result
    }
}

/** The toast currently on screen, with callbacks to finish it. */
@Stable
public interface YumiToastData {
    /** What to show. */
    public val visuals: YumiToastVisuals

    /** Finishes the toast with [YumiToastResult.ActionPerformed]. */
    public fun performAction()

    /** Finishes the toast with [YumiToastResult.Dismissed]. */
    public fun dismiss()
}

/**
 * Queue of toasts. Calls to [show] suspend until their toast has been shown and finished, so
 * concurrent callers are shown one after another in call order.
 *
 * ```
 * val toasts = remember { YumiToastHostState() }
 * scope.launch {
 *     if (toasts.show("Alarm deleted", actionLabel = "Undo") == YumiToastResult.ActionPerformed) undo()
 * }
 * ```
 */
@Stable
public class YumiToastHostState {
    private val mutex = Mutex()

    /** The toast being shown, or `null`. */
    public var currentToastData: YumiToastData? by mutableStateOf(null)
        private set

    /** Shows a toast and suspends until it finishes. */
    public suspend fun show(
        message: String,
        actionLabel: String? = null,
        duration: YumiToastDuration = if (actionLabel == null) YumiToastDuration.Short else YumiToastDuration.Long,
        withDismissAction: Boolean = false,
    ): YumiToastResult = show(YumiToastVisuals(message, actionLabel, duration, withDismissAction))

    /** Shows a toast with the given [visuals] and suspends until it finishes. */
    public suspend fun show(visuals: YumiToastVisuals): YumiToastResult = mutex.withLock {
        try {
            suspendCancellableCoroutine { continuation ->
                currentToastData = ToastDataImpl(visuals, continuation)
            }
        } finally {
            currentToastData = null
        }
    }

    private class ToastDataImpl(
        override val visuals: YumiToastVisuals,
        private val continuation: CancellableContinuation<YumiToastResult>,
    ) : YumiToastData {
        override fun performAction() {
            if (continuation.isActive) continuation.resume(YumiToastResult.ActionPerformed)
        }

        override fun dismiss() {
            if (continuation.isActive) continuation.resume(YumiToastResult.Dismissed)
        }
    }
}

/**
 * Shows the toasts of [hostState], one at a time, with a short slide + fade (instant when
 * reduced motion is on). Durations are stretched by the user's accessibility timeout setting.
 *
 * Place it at the bottom of the screen above other content, e.g. in a `Box` aligned
 * `BottomCenter`.
 *
 * @param hostState the queue to show.
 * @param modifier modifier for the host.
 * @param toast how to draw one toast; defaults to the charcoal pill [YumiToast]. Pass
 *   `{ YumiSnackbar(it) }` for the full-width snackbar.
 */
@Composable
public fun YumiToastHost(
    hostState: YumiToastHostState,
    modifier: Modifier = Modifier,
    toast: @Composable (YumiToastData) -> Unit = { YumiToast(it) },
) {
    val current = hostState.currentToastData
    val accessibilityManager = LocalAccessibilityManager.current
    LaunchedEffect(current) {
        if (current != null) {
            val millis = current.visuals.timeoutMillis(accessibilityManager)
            if (millis != Long.MAX_VALUE) {
                delay(millis)
                current.dismiss()
            }
        }
    }

    // Keep the last toast around so it can animate out after the state clears. A plain holder,
    // not state: writing state during composition would be a backwards write.
    val lastShown = remember { LastToast() }
    if (current != null) lastShown.data = current
    val visible = remember { MutableTransitionState(false) }
    visible.targetState = current != null

    val reduceMotion = YumiTheme.motion.reduceMotion
    val enter: EnterTransition = if (reduceMotion) {
        EnterTransition.None
    } else {
        fadeIn() + slideInVertically(spring(dampingRatio = 0.8f, stiffness = 600f)) { it / 2 }
    }
    val exit: ExitTransition = if (reduceMotion) ExitTransition.None else fadeOut() + slideOutVertically { it / 2 }

    Box(modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visibleState = visible, enter = enter, exit = exit) {
            lastShown.data?.let { toast(it) }
        }
    }
}

/** Defaults for [YumiToast] and [YumiSnackbar]. */
public object YumiToastDefaults {
    /** Shape of the snackbar. */
    public val SnackbarShape: Shape = RoundedCornerShape(20.dp)

    /** Background: the charcoal (inverse) pebble colour. */
    public val containerColor: Color
        @Composable @ReadOnlyComposable get() = YumiTheme.colors.charcoal

    /** Text on the toast. */
    public val contentColor: Color
        @Composable @ReadOnlyComposable get() = YumiTheme.colors.onCharcoal
}

/**
 * The charcoal pill toast for [data] from a [YumiToastHost].
 *
 * The action is set in bold `onCharcoal`, not accent: the toast is never "the thing you came
 * here to do", so it doesn't spend the screen's orange.
 */
@Composable
public fun YumiToast(data: YumiToastData, modifier: Modifier = Modifier) {
    YumiToast(
        message = data.visuals.message,
        modifier = modifier,
        actionLabel = data.visuals.actionLabel,
        onAction = data::performAction,
        onDismiss = data::dismiss,
    )
}

/**
 * A charcoal pill toast, without a host.
 *
 * @param message message text.
 * @param modifier modifier for the toast.
 * @param actionLabel optional action label.
 * @param onAction called when the action is pressed.
 * @param onDismiss called by the accessibility "dismiss" action.
 */
@Composable
public fun YumiToast(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    ToastSurface(
        shape = YumiTheme.shapes.pill,
        modifier = modifier.widthIn(max = 560.dp),
        onDismiss = onDismiss,
        contentPadding = if (actionLabel == null) 24.dp else 8.dp,
    ) {
        Text(
            message,
            modifier = Modifier.padding(start = if (actionLabel == null) 0.dp else 16.dp, top = 14.dp, bottom = 14.dp).weight(1f, fill = false),
            style = YumiTheme.typography.bodyMedium,
            color = YumiToastDefaults.contentColor,
        )
        if (actionLabel != null) ToastAction(actionLabel, onAction)
    }
}

/**
 * The full-width snackbar for [data] from a [YumiToastHost], for longer messages or when a
 * dismiss button is wanted.
 */
@Composable
public fun YumiSnackbar(data: YumiToastData, modifier: Modifier = Modifier) {
    YumiSnackbar(
        message = data.visuals.message,
        modifier = modifier,
        actionLabel = data.visuals.actionLabel,
        withDismissAction = data.visuals.withDismissAction,
        onAction = data::performAction,
        onDismiss = data::dismiss,
    )
}

/**
 * A full-width charcoal snackbar, without a host.
 *
 * @param message message text; may wrap to two lines.
 * @param modifier modifier for the snackbar.
 * @param actionLabel optional action label.
 * @param withDismissAction whether to show a "Dismiss" key.
 * @param onAction called when the action is pressed.
 * @param onDismiss called when dismissed.
 */
@Composable
public fun YumiSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    onAction: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    ToastSurface(
        shape = YumiToastDefaults.SnackbarShape,
        modifier = modifier.fillMaxWidth(),
        onDismiss = onDismiss,
        contentPadding = 8.dp,
    ) {
        Text(
            message,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp).weight(1f),
            style = YumiTheme.typography.bodyMedium,
            color = YumiToastDefaults.contentColor,
            maxLines = 2,
        )
        if (actionLabel != null) ToastAction(actionLabel, onAction)
        if (withDismissAction) ToastAction(stringResource(R.string.yumi_dismiss), onDismiss)
    }
}

@Composable
private fun ToastSurface(
    shape: Shape,
    modifier: Modifier,
    onDismiss: () -> Unit,
    contentPadding: Dp,
    content: @Composable RowScope.() -> Unit,
) {
    val paneTitle = stringResource(R.string.yumi_toast_pane)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                this.paneTitle = paneTitle
                dismiss { onDismiss(); true }
            }
            .dropShadow(
                shape = shape,
                shadow = Shadow(radius = 16.dp, color = Color.Black, offset = DpOffset(0.dp, 6.dp), alpha = YumiTheme.depth.shadowAlpha),
            )
            .background(YumiToastDefaults.containerColor, shape)
            .padding(horizontal = contentPadding),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun ToastAction(label: String, onClick: () -> Unit) {
    val onCharcoal = YumiToastDefaults.contentColor
    YumiTextButton(
        onClick = onClick,
        colors = YumiButtonColors(Color.Transparent, onCharcoal, Color.Transparent, onCharcoal),
    ) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}

private class LastToast {
    var data: YumiToastData? = null
}

private fun YumiToastVisuals.timeoutMillis(accessibilityManager: AccessibilityManager?): Long {
    val original = when (duration) {
        YumiToastDuration.Short -> 2_500L
        YumiToastDuration.Long -> 6_000L
        YumiToastDuration.Indefinite -> Long.MAX_VALUE
    }
    if (accessibilityManager == null || original == Long.MAX_VALUE) return original
    return accessibilityManager.calculateRecommendedTimeoutMillis(
        originalTimeoutMillis = original,
        containsIcons = false,
        containsText = true,
        containsControls = actionLabel != null || withDismissAction,
    )
}
