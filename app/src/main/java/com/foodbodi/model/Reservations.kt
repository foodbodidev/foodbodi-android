package com.foodbodi.model

import com.google.gson.annotations.SerializedName



class Reservation  {
    @SerializedName("created_date")
    var created_date: String? = null

    @SerializedName("date_string")
    var date_string: String? = null

    @SerializedName("total")
    var total: Int? = 0

    @SerializedName("restaurant_id")
    var restaurant_id: String? = null

    @SerializedName("owner")
    var owner: String? = null

    @SerializedName("restaurant_name")
    var restaurant_name: String? = null


    @SerializedName("id")
    var id: String? = null

}