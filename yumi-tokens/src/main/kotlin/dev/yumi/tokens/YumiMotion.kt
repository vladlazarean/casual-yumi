package dev.yumi.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp

/**
 * Physical motion: press = sink + shadow swap. No ripples anywhere.
 *
 * @property sinkOffset how far a pebble travels down when pressed.
 * @property pressDampingRatio damping of the press spring.
 * @property pressStiffness stiffness of the press spring.
 * @property reduceMotion when `true`, state changes are instant (no spring). `YumiTheme` also
 *   turns this on when the system animator duration scale is 0.
 */
@Immutable
public class YumiMotion(
    public val sinkOffset: Dp = YumiTokens.SinkOffset,
    public val pressDampingRatio: Float = YumiTokens.PressDampingRatio,
    public val pressStiffness: Float = YumiTokens.PressStiffness,
    public val reduceMotion: Boolean = false,
) {

    /** Returns a copy with the given values replaced. */
    public fun copy(
        sinkOffset: Dp = this.sinkOffset,
        pressDampingRatio: Float = this.pressDampingRatio,
        pressStiffness: Float = this.pressStiffness,
        reduceMotion: Boolean = this.reduceMotion,
    ): YumiMotion = YumiMotion(sinkOffset, pressDampingRatio, pressStiffness, reduceMotion)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is YumiMotion) return false
        return sinkOffset == other.sinkOffset && pressDampingRatio == other.pressDampingRatio &&
            pressStiffness == other.pressStiffness && reduceMotion == other.reduceMotion
    }

    override fun hashCode(): Int {
        var result = sinkOffset.hashCode()
        result = 31 * result + pressDampingRatio.hashCode()
        result = 31 * result + pressStiffness.hashCode()
        result = 31 * result + reduceMotion.hashCode()
        return result
    }

    override fun toString(): String =
        "YumiMotion(sinkOffset=$sinkOffset, damping=$pressDampingRatio, stiffness=$pressStiffness, reduceMotion=$reduceMotion)"
}
