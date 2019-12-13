package com.foodbodi.apis

import com.foodbodi.model.Reservation



import com.google.gson.annotations.SerializedName


class ReservationResponse  {
    @SerializedName("reservations")
    var reservation: ArrayList<Reservation> = ArrayList()
    @SerializedName("cursor")
    var cursor: String = ""

}