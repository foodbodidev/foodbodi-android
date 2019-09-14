package com.foodbodi.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.lang.StringBuilder

class DailyLog : Serializable {

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
    }

}