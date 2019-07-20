package com.foodbodi.apis.requests

import com.foodbodi.model.Food
import com.google.gson.annotations.SerializedName

class ImportFoodRequest {
    @SerializedName("restaurant_id")
    var restaurantId:String? = null

    @SerializedName("foods")
    var foodList:ArrayList<Food>? = null
}