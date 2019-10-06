package com.foodbodi.apis

import com.foodbodi.model.Reservation
import com.foodbodi.model.Food
import com.google.gson.annotations.SerializedName


class FoodCardResonse  {
    @SerializedName("foods")
    lateinit var foods: HashMap<String, Food>
    @SerializedName("reservation")
    lateinit var reservation: ReservationAmount

}

class ReservationAmount  {
    @SerializedName("total")
    var total: Int = 0
    @SerializedName("foods")
    var foods: ArrayList<FoodAmount> = ArrayList()
}

class FoodAmount  {
    @SerializedName("food_id")
    lateinit var food_id: String
    @SerializedName("amount")
    var amount: Int = 0

}


class UpdateCaloriesResponse {
    @SerializedName("total")
    lateinit var total: String
}