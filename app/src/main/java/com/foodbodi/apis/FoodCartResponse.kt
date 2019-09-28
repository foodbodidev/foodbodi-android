package com.foodbodi.apis

import com.foodbodi.model.Reservation
import com.foodbodi.model.Food
import com.google.gson.annotations.SerializedName


class FoodCardResonse  {
    @SerializedName("foods")
    lateinit var foods: HashMap<String, Food>

}