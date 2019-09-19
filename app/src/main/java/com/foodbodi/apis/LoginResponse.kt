package com.foodbodi.apis

import com.foodbodi.model.User
import com.google.gson.annotations.SerializedName

class LoginResponse {
    @SerializedName("user")
    var user:User? = null

    @SerializedName("token")
    var token:String? = null
}