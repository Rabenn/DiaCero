package com.unaiIgorRuben.diacero.ui.calendario

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class EventDayDecorator(
    private val days: Set<CalendarDay>,
    private val dotColor: Int = DEFAULT_DOT_COLOR,
    private val textColor: Int? = null
) : DayViewDecorator {

    companion object {
        const val DEFAULT_DOT_COLOR = Color.GRAY
        const val PRIORITY_DOT_COLOR = Color.RED
        const val PRIORITY_TEXT_COLOR = Color.RED
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return days.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(5f, dotColor))
        textColor?.let {
            view.addSpan(ForegroundColorSpan(it))
        }
    }
}
