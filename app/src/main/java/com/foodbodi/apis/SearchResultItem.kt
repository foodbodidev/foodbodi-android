package com.foodbodi.apis

import com.google.gson.annotations.SerializedName

class SearchResultItem {
    @SerializedName("data")
    var data:SearchResultItemMetadata? = null

    @SerializedName("count")
    var count:Int? = null

}