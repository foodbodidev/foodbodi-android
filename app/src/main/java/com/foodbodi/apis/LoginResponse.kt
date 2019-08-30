package com.foodbodi.apis

import com.foodbodi.model.User
import com.google.gson.annotations.SerializedName

class LoginResponse {
    @SerializedName("data")
    var user:User? = null

    @SerializedName("token")
    var token:String? = null
}