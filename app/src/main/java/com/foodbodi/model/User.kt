package com.foodbodi.model

import com.google.gson.annotations.SerializedName

class User {
    @SerializedName("email")
    var email:String? = null

    @SerializedName("first_name")
    var firstName:String? = null

    @SerializedName("last_name")
    var lastName:String? = null

    @SerializedName("age")
    var age:Int = 0

    @SerializedName("height")
    var height:Int = 0

    @SerializedName("weight")
    var weight:Double = 0.0

    @SerializedName("target_weight")
    var targetWeight:Double = 0.0

    @SerializedName("sex")
    var sex:String? = null

    @SerializedName("password")
    var password:String? = null
}