package com.example.myapplication.Decorators

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import org.threeten.bp.LocalDate

class AppointmentDecorator(private val context: Context, private val dates: Set<LocalDate>) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(LocalDate.of(day.year, day.month + 1, day.day))
    }

    override fun decorate(view: DayViewFacade) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.book)
        drawable?.let { view.setBackgroundDrawable(it) }
    }
}
