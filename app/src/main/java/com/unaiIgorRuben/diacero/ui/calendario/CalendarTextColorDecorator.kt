package com.unaiIgorRuben.diacero.ui.calendario

import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CalendarTextColorDecorator(
    private val textColor: Int
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean = true

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(textColor))
    }
}
