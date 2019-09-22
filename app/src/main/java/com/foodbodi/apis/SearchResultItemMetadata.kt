package com.foodbodi.apis

import com.google.gson.annotations.SerializedName

class SearchResultItemMetadata {
    @SerializedName("word")
    var word:String? = null

    @SerializedName("document_id")
    var document_id:String? = null

    @SerializedName("kind")
    var kind:String? = null

    @SerializedName("position")
    var position:String? = null

    @SerializedName("document")
    var document:SearchResultItemDocument? = null

    @SerializedName("id")
    var id:String? = null
}