package com.foodbodi.model

import com.google.gson.annotations.SerializedName

class User {
    @SerializedName("email")
    var email:String = ""

    @SerializedName("first_name")
    var firstName:String = ""

    @SerializedName("last_name")
    var lastName:String = ""

    @SerializedName("age")
    var age:Long = 0

    @SerializedName("height")
    var height:Int = 0

    @SerializedName("weight")
    var weight:Double = 0.0

    @SerializedName("target_weight")
    var targetWeight:Double = 0.0

    @SerializedName("sex")
    var sex:String = ""
}