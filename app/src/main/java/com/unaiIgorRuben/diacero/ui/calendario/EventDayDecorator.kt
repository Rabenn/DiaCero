package com.unaiIgorRuben.diacero.ui.calendario

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

/**
 * Decorador para un día específico en el calendario.
 * Permite dibujar múltiples puntos y opcionalmente cambiar el color del texto.
 */
class EventDayDecorator(
    private val day: CalendarDay,
    private val colors: List<Int> = emptyList(),
    private val extraCount: Int = 0,
    private val isPriority: Boolean = false
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return this.day == day
    }

    override fun decorate(view: DayViewFacade) {
        if (colors.isNotEmpty()) {
            view.addSpan(MultiDotSpan(colors = colors, extraCount = extraCount))
        }
        
        if (isPriority) {
            view.addSpan(ForegroundColorSpan(Color.RED))
        }
    }
}
