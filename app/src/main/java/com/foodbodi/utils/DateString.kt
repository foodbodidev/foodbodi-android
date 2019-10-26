package com.foodbodi.utils

import java.io.Serializable
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat

class DateString(var year: Int, var month: Int, var day: Int) : Serializable {
    fun getString(): String {
        return StringBuilder().append(year.toString()).append("-").append((month + 1).toString()).append("-")
            .append(day.toString()).toString()
    }

    fun getTimeStamp() : Long {
        return Date(year, month, day).time
    }

    companion object {
        val DAY_OFFSET = 24 * 60 * 60 * 1000;
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

        fun getNextDate(date:DateString) : DateString {
            val ts = date.getTimeStamp() + DAY_OFFSET
            val nextDate = Date(ts)
            return DateString(nextDate.year, nextDate.month + 1, nextDate.day)
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