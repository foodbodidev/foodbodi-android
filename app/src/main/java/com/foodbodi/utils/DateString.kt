package com.foodbodi.utils

import java.lang.StringBuilder
import java.util.*

class DateString(var year: Int, var month: Int, var day: Int) {
    fun getString(): String {
        return StringBuilder().append(year.toString()).append("-").append((month + 1).toString()).append("-")
            .append(day.toString()).toString()
    }

    companion object {
        fun fromCalendar(calendar: Calendar) : DateString {
            return DateString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE))
        }
    }
}