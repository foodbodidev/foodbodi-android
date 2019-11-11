package com.foodbodi.model

import com.google.gson.annotations.SerializedName

class CommentRequest {
    init {

    }

    @SerializedName("restaurant_id")
    var restaurant_id:String? = null

    @SerializedName("message")
    var message:String? = null

}