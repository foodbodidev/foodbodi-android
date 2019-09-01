package com.foodbodi.utils

import kotlin.math.ln

class GoogleMapUtils {
    companion object {
        fun convertAddressToLatLng(address:String):LatLng {
            //TODO : call API
            return LatLng(10.0,10.0)
        }
    }

    class LatLng constructor(lat:Double, lng:Double) {
        var lat:Double = lat
        var lng:Double = lng
    }
}