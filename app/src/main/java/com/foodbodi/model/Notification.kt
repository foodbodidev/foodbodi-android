package com.foodbodi.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Notification : Serializable {
    @SerializedName("message")
    var message:String? = null

    @SerializedName("read")
    var read:Boolean? = false

    @SerializedName("receiver")
    var receiver:String? = null

    @SerializedName("type")
    var type:String? = null
}