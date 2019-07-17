package com.foodbodi.model

import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class Restaurant {
    @SerializedName("name")
    var name:String = "Unnamed";

    @SerializedName("id")
    lateinit var id:String

    @SerializedName("category")
    lateinit var category:String

    @SerializedName("photo")
    var photo:String = ""

    @SerializedName("open_hour")
    var openHour:String = ""

    @SerializedName("close_hour")
    var closeHour:String = ""
}