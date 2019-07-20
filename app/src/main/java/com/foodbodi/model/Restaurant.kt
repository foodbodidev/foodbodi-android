package com.foodbodi.model

import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class Restaurant {
    @SerializedName("name")
    var name:String? = null

    @SerializedName("id")
     var id:String? = null

    @SerializedName("category")
    var category:String? = null

    @SerializedName("photo")
    var photo:String? = null

    @SerializedName("open_hour")
    var openHour:String? = null

    @SerializedName("close_hour")
    var closeHour:String? = null

    @SerializedName("type")
    var type:RestaurantType? = null

    @SerializedName("foods")
    var foods:ArrayList<Food>? = null
}

enum class RestaurantType {
    RESTAURANT, FOOD_TRUCK
}