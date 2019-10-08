package com.foodbodi.utils

import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat

class DateString(var year: Int, var month: Int, var day: Int) {
    fun getString(): String {
        return StringBuilder().append(year.toString()).append("-").append((month + 1).toString()).append("-")
            .append(day.toString()).toString()
    }

    companion object {
        fun fromCalendar(calendar: Calendar) : DateString {
            return DateString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE))
        }

        fun fromString(string: String) : DateString? {
            val parts:List<String> = string.split("-")
            if (parts.size != 3) {
                return null;
            } else {
                return DateString(parts.get(0).toInt(), parts.get(1).toInt(), parts.get(2).toInt())
            }
        }
    }
}

object DateUtils {
    @JvmStatic
    fun toSimpleString(date: Date) : String {
        val format = SimpleDateFormat("yyyy-mm-dd")
        return format.format(date)
    }
}