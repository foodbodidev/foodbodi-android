package com.foodbodi.apis

import com.foodbodi.model.Reservation



import com.google.gson.annotations.SerializedName


class ReservationResponse  {
    @SerializedName("reservations")
    lateinit var reservation: ArrayList<Reservation>

}