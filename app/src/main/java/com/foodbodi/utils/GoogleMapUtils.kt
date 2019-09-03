package com.foodbodi.utils

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder

import androidx.core.app.ActivityCompat
import android.content.DialogInterface
import android.R
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat



class GoogleMapUtils {
    companion object {
        fun convertAddressToLatLng(address: String, context: Context): LatLng? {
            var geoCoder: Geocoder = Geocoder(context)
            var result: List<Address> = geoCoder.getFromLocationName(address, 1)
            if (result.size > 0) {
                val lat = result.get(0).latitude
                val lng = result.get(0).longitude
                return LatLng(lat, lng)
            } else {
                return null
            }

        }
    }

    class LatLng constructor(lat:Double, lng:Double) {
        var lat:Double = lat
        var lng:Double = lng
    }


}