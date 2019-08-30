package com.foodbodi.apis.requests

import com.google.gson.annotations.SerializedName

class FacebookSignInRequest(token:String, userId:String) {
    @SerializedName("facebook_access_token")
    var googleSignInToken:String? = token

    @SerializedName("user_id")
    var userId:String? = userId
}