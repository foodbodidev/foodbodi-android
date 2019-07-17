package com.foodbodi.model

import com.google.gson.annotations.SerializedName

class RestaurantCategory{
    @SerializedName("name")
    lateinit var name:String

    @SerializedName("key")
    lateinit var key:String

}