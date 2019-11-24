package com.foodbodi.model

import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.io.Serializable;

class Restaurant : Serializable {
    @SerializedName("name")
    var name:String? = null

    @SerializedName("id")
     var id:String? = null

    @SerializedName("category")
    var category:String? = null

    @SerializedName("photos")
    var photos:ArrayList<String> = ArrayList()

    @SerializedName("open_hour")
    var open_hour:String? = null

    @SerializedName("close_hour")
    var close_hour:String? = null

    @SerializedName("type")
    var type:RestaurantType? = null

    @SerializedName("foods")
    var foods:ArrayList<Food>? = null

    @SerializedName("address")
    var address:String? = null

    @SerializedName("lat")
    var lat:Double? = null

    @SerializedName("lng")
    var lng:Double? = null

    @SerializedName("geohash")
    var geohash:String? = null

    @SerializedName("phone")
    var phone = null

    @SerializedName("license")
    var license:License? = null

    @SerializedName("calo_values")
    var calo_values:ArrayList<Double> = ArrayList<Double>()


    fun getCaloSegment() : CaloSegment {
        var sum = 0.0;
        var avg = 0.0;
        if (calo_values.size > 0) {
            for (value in calo_values) {
                sum += value
            }
            avg = sum / calo_values.size
            return getCaloSegment(avg)
        } else {
            return CaloSegment.LOW
        }

    }

    companion object {
       fun getCaloSegment(calo:Double):CaloSegment {
           if (calo >= 600) {
               return CaloSegment.HIGH
           } else if (calo >= 400) {
               return CaloSegment.MEDIUM
           } else {
               return CaloSegment.LOW
           }
       }
    }
}

enum class RestaurantType {
    RESTAURANT, FOOD_TRUCK
}

enum class CaloSegment {
    LOW, MEDIUM, HIGH
}