package com.foodbodi.model

import com.google.gson.annotations.SerializedName

class CommentRequest {
    init {

    }

    @SerializedName("restaurant_id")
    var restaurant_id:String? = null

    @SerializedName("message")
    var message:String? = null

    constructor(restaurant_id: String, content: String) {
        this.restaurant_id = restaurant_id;
        this.message = content;

    }
}