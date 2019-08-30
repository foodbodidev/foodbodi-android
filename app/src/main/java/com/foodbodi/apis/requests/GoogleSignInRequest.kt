package com.foodbodi.apis.requests

import com.google.gson.annotations.SerializedName

class GoogleSignInRequest (token:String) {
    @SerializedName("google_id_token")
    var googleSignInToken:String? = token
}