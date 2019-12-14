package com.foodbodi.utils

import android.annotation.SuppressLint
import java.io.Serializable
import java.lang.StringBuilder
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList
import java.text.SimpleDateFormat

class DateString(var year: Int, var month: Int, var day: Int) : Serializable {
    fun getString(): String {
        return StringBuilder().append(year.toString()).append("-").append((month).toString()).append("-")
            .append(day.toString()).toString()
    }

    @SuppressLint("SimpleDateFormat")
    fun getPrettyString() : String {
        val date:Date = Date(getTimeStamp())
        val today:Boolean = Calendar.getInstance().get(Calendar.YEAR) == year
                && Calendar.getInstance().get(Calendar.MONTH) + 1 == month
                && Calendar.getInstance().get(Calendar.DATE) == day
        if (today) {
            return SimpleDateFormat("'Today' MMM dd, yyyy").format(date)
        } else {
            val dateFormat:SimpleDateFormat = SimpleDateFormat("' 'EEE, MMM dd, yyyy") //hardcode to keep text length always the same
            return dateFormat.format(date)
        }
    }

    fun getTimeStamp() : Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DATE, day)
        return calendar.time.time
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
            var calendar = Calendar.getInstance();
            calendar.set(date.year, date.month - 1, date.day);
            calendar.add(Calendar.DATE, 1)
            return DateString.fromCalendar(calendar)
        }
    }
}