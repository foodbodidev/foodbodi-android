package com.foodbodi.apis

import com.google.gson.annotations.SerializedName

class FoodBodiResponse<T : Any> {
    companion object {
        val SUCCESS_CODE = 0
    }
    @SerializedName("status_code")
    var statusCode:Int = 0

    @SerializedName("data")
    lateinit var data:T

    @SerializedName("message")
    lateinit var errorMessage:String

    fun statusCode(): Int {
        return this.statusCode;
    }

    fun errorMessage() :String {
        return errorMessage
    }

    fun data():T {
        return data;
    }

}