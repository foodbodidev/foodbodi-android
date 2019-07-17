package com.foodbodi.apis

import com.foodbodi.model.Restaurant
import com.google.gson.annotations.SerializedName

class RestaurantsResponse {
    @SerializedName("restaurants")
    lateinit var restaurants:ArrayList<Restaurant>
}