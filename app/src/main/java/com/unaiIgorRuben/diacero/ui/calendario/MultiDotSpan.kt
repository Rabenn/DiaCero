package com.unaiIgorRuben.diacero.ui.calendario

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

/**
 * Span personalizado para dibujar múltiples puntos de colores y un contador de eventos extra
 * debajo del número del día en el calendario.
 */
class MultiDotSpan(
    private val radius: Float = 6f,
    private val colors: List<Int>,
    private val extraCount: Int = 0
) : LineBackgroundSpan {

    override fun drawBackground(
        canvas: Canvas, paint: Paint,
        left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int, lnum: Int
    ) {
        val oldColor = paint.color
        val oldStyle = paint.style
        val oldTextSize = paint.textSize
        
        val totalDots = colors.size
        val dotSpacing = radius * 2.8f
        
        val extraText = if (extraCount > 0) "+$extraCount" else null
        
        // Configurar tamaño para medir el texto extra
        paint.textSize = radius * 3f
        val extraTextWidth = if (extraText != null) paint.measureText(extraText) else 0f
        
        // Calcular el ancho total ocupado por los puntos y el texto
        val totalWidth = (totalDots * radius * 2) + 
                        ((totalDots - 1) * (dotSpacing - radius * 2)) + 
                        (if (extraText != null) extraTextWidth + 6f else 0f)
        
        var currentX = (left + right) / 2f - totalWidth / 2f + radius
        val dotY = bottom.toFloat() + radius + 4f

        paint.style = Paint.Style.FILL
        
        // Dibujar cada punto con su color correspondiente
        for (color in colors) {
            paint.color = color
            canvas.drawCircle(currentX, dotY, radius, paint)
            currentX += dotSpacing
        }

        // Dibujar el "+N" si hay eventos extra
        if (extraText != null) {
            paint.color = Color.GRAY
            canvas.drawText(extraText, currentX, dotY + radius, paint)
        }

        // Restaurar estado original del paint
        paint.color = oldColor
        paint.style = oldStyle
        paint.textSize = oldTextSize
    }
}
