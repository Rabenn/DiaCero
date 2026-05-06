package com.unaiIgorRuben.diacero.ui.calendario

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

/**
 * Marca días concreto con un punto bajo la fecha.
 * Los colores deben ser [Int] ARGB válidos (ver [Color.argb]).
 */
class EventDayDecorator(
    private val days: Set<CalendarDay>,
    @field:ColorInt private val dotColor: Int,
    @field:ColorInt private val textColor: Int? = null
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean = days.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(DOT_RADIUS_DP, dotColor))
        textColor?.let { view.addSpan(ForegroundColorSpan(it)) }
    }

    companion object {
        private const val DOT_RADIUS_DP = 8f

        /** Azul primario (#0F6CBD) */
        @ColorInt
        val DEFAULT_DOT_COLOR: Int = Color.argb(255, 15, 107, 189)

        /** Verde secundario / prioritario (#00A884) */
        @ColorInt
        val PRIORITY_DOT_COLOR: Int = Color.argb(255, 0, 168, 132)

        @ColorInt
        val PRIORITY_TEXT_COLOR: Int = "#007A60".toColorInt()
    }
}
