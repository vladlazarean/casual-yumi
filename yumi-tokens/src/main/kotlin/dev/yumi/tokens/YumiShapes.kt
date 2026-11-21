package dev.yumi.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape

/**
 * Casual Yumi has three shape families: circle, 24 dp pill-rect, tray. Everything else is a
 * size of one of those.
 *
 * @property small fields, chips, checkbox wells.
 * @property button text pebbles.
 * @property icon round icon pebbles, knobs, thumbs.
 * @property card raised cards.
 * @property tray recessed trays.
 * @property pill fully rounded: LEDs, toasts, switch tracks.
 */
@Immutable
public class YumiShapes(
    public val small: Shape = RoundedCornerShape(YumiTokens.SmallCornerRadius),
    public val button: Shape = RoundedCornerShape(YumiTokens.ButtonCornerRadius),
    public val icon: Shape = CircleShape,
    public val card: Shape = RoundedCornerShape(YumiTokens.CardCornerRadius),
    public val tray: Shape = RoundedCornerShape(YumiTokens.TrayCornerRadius),
    public val pill: Shape = CircleShape,
) {

    /** Returns a copy with the given values replaced. */
    public fun copy(
        small: Shape = this.small,
        button: Shape = this.button,
        icon: Shape = this.icon,
        card: Shape = this.card,
        tray: Shape = this.tray,
        pill: Shape = this.pill,
    ): YumiShapes = YumiShapes(small, button, icon, card, tray, pill)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is YumiShapes) return false
        return small == other.small && button == other.button && icon == other.icon &&
            card == other.card && tray == other.tray && pill == other.pill
    }

    override fun hashCode(): Int {
        var result = small.hashCode()
        result = 31 * result + button.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + card.hashCode()
        result = 31 * result + tray.hashCode()
        result = 31 * result + pill.hashCode()
        return result
    }

    override fun toString(): String = "YumiShapes(small=$small, button=$button, tray=$tray)"
}
