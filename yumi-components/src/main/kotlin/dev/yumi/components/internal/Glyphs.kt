package dev.yumi.components.internal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The few outlined 1.75 dp glyphs components need, drawn in code so yumi-components doesn't
 * depend on an icon library. Decorative: the owning control carries the accessible label.
 */
internal enum class Glyph { Search, Close, Plus, Minus, Check, ChevronDown, Star, StarFilled, DragHandle }

@Composable
internal fun GlyphIcon(glyph: Glyph, modifier: Modifier = Modifier, size: Dp = 20.dp, color: Color = LocalContentColor.current) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val stroke = Stroke(width = 1.75.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (glyph) {
            Glyph.Search -> {
                drawCircle(color, radius = w * 0.3f, center = Offset(w * 0.44f, w * 0.44f), style = stroke)
                line(color, w * 0.66f, w * 0.66f, w * 0.86f, w * 0.86f, stroke)
            }
            Glyph.Close -> {
                line(color, w * 0.25f, w * 0.25f, w * 0.75f, w * 0.75f, stroke)
                line(color, w * 0.75f, w * 0.25f, w * 0.25f, w * 0.75f, stroke)
            }
            Glyph.Plus -> {
                line(color, w * 0.5f, w * 0.2f, w * 0.5f, w * 0.8f, stroke)
                line(color, w * 0.2f, w * 0.5f, w * 0.8f, w * 0.5f, stroke)
            }
            Glyph.Minus -> line(color, w * 0.2f, w * 0.5f, w * 0.8f, w * 0.5f, stroke)
            Glyph.ChevronDown -> drawPath(
                Path().apply {
                    moveTo(w * 0.25f, w * 0.38f)
                    lineTo(w * 0.5f, w * 0.63f)
                    lineTo(w * 0.75f, w * 0.38f)
                },
                color,
                style = stroke,
            )
            Glyph.Star, Glyph.StarFilled -> {
                val star = starPath(w)
                if (glyph == Glyph.StarFilled) drawPath(star, color) else drawPath(star, color, style = stroke)
            }
            Glyph.DragHandle -> for (row in 0 until 3) {
                val y = w * (0.32f + row * 0.18f)
                line(color, w * 0.22f, y, w * 0.78f, y, stroke)
            }
            Glyph.Check -> drawPath(
                Path().apply {
                    moveTo(w * 0.2f, w * 0.52f)
                    lineTo(w * 0.42f, w * 0.72f)
                    lineTo(w * 0.8f, w * 0.3f)
                },
                color,
                style = stroke,
            )
        }
    }
}

private fun starPath(w: Float): Path = Path().apply {
    val cx = w / 2f
    val cy = w * 0.53f
    val outer = w * 0.42f
    val inner = outer * 0.45f
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val a = Math.toRadians((i * 36.0) - 90.0)
        val x = cx + (r * kotlin.math.cos(a)).toFloat()
        val y = cy + (r * kotlin.math.sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun DrawScope.line(color: Color, x1: Float, y1: Float, x2: Float, y2: Float, stroke: Stroke) {
    drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = stroke.width, cap = StrokeCap.Round)
}
