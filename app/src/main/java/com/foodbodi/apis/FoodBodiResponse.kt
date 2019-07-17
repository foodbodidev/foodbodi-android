package com.foodbodi.apis

import com.google.gson.annotations.SerializedName

class FoodBodiResponse<T : Any> {
    @SerializedName("status_code")
    lateinit var statusCode:String

    @SerializedName("data")
    lateinit var data:T

    @SerializedName("message")
    lateinit var errorMessage:String

    fun statusCode(): String {
        return this.statusCode;
    }

    fun errorMessage() :String {
        return errorMessage
    }

    fun data():T {
        return data;
    }

}