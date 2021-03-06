package com.foodbodi.model

import com.foodbodi.utils.DateString
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.StringBuilder
import java.util.*

class DailyLog : Serializable {
    init {

    }

    @SerializedName("id")
    var id:String? = null

    @SerializedName("step")
    var step:Int? = null

    @SerializedName("calo_threshold")
    var calo_threshold:Int? = null

    @SerializedName("total_eat")
    var total_eat:Int? = null

    @SerializedName("owner")
    var owner:String? = null

    companion object {
        fun getLocalID(year:Int, month:Int, day:Int, user:String):String {
            return StringBuilder().append(String.format("%04d-%02d-%02d", year, month, day)).append("-").append(user).toString()
        }

        fun getLocalID(dateString: DateString, user:String):String {
            return Companion.getLocalID(dateString.year, dateString.month, dateString.day, user)
        }
        fun stepToKCalo(step:Int) :Double {
            return 25.0 * (step / 1000);
        }
    }

    fun getStep():Int {
        return if (step != null) step!! else 0
    }

    fun getBurnedCalo() : Double {
        return stepToKCalo(getStep())
    }

    fun getTotalEat() : Int {
        return if (total_eat != null) total_eat!! else 0
    }

    fun getThreshold() : Int {
        return if (calo_threshold != null) calo_threshold!! else 3000
    }

}