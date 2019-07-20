package com.foodbodi.apis

import com.foodbodi.model.Restaurant
import com.google.gson.annotations.SerializedName

class RestaurantResponse {
    @SerializedName("restaurant")
    var restaurant:Restaurant? = null
}