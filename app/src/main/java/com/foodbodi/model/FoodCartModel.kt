package com.foodbodi.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable



class FoodCartModel : Serializable {

    @SerializedName("food_id")
    var food_id:String? = null

    @SerializedName("amount")
    var amount: Int = 0


}