package com.foodbodi.apis.requests

import com.foodbodi.model.Food
import com.foodbodi.model.FoodCartModel
import com.google.gson.annotations.SerializedName


class ReservationRequest {
    @SerializedName("restaurant_id")
    var restaurantId:String? = null

    @SerializedName("date_string")
    var date_string:String? = null

    @SerializedName("foods")
    var foods: ArrayList<FoodCartModel> = ArrayList()

}