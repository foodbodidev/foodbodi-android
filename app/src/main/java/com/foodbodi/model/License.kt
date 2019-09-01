package com.foodbodi.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class License : Serializable{
    @SerializedName("registration_number")
    var registration_number:String? = null

    @SerializedName("representative_name")
    var representative_names:ArrayList<String>? = ArrayList<String>()

    @SerializedName("status")
    var status:LicenseStatus? = null
}

enum class LicenseStatus {
    APPROVED, DENIED
}