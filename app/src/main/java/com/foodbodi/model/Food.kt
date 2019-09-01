package com.foodbodi.model

import com.google.gson.annotations.SerializedName

class Food {
    @SerializedName("name")
    var name:String? = null

    @SerializedName("photo")
    var photo:String? = null

    @SerializedName("id")
    var id:String? = null

    @SerializedName("price")
    var price:Double? = null

    @SerializedName("calo")
    var calo:Double? = null

    @SerializedName("restaurant_id")
    var restaurant_id:String? = null

}