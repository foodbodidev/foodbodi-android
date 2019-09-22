package com.foodbodi.apis

import com.google.gson.annotations.SerializedName

class SearchResultItemDocument {
    @SerializedName("name")
    var name:String? = null

    @SerializedName("id")
    var id:String? = null

    @SerializedName("restaurant_id")
    var restaurant_id:String? = null

    @SerializedName("photo")
    var photo:String? = null

    @SerializedName("photos")
    var photos:ArrayList<String> = ArrayList()

    @SerializedName("address")
    var address:String? = null

}