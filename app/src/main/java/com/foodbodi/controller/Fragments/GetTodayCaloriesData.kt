package com.foodbodi.controller.Fragments

import android.app.Activity
import android.content.Context
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.ReservationResponse
import com.foodbodi.model.DailyLog
import com.foodbodi.model.LocalDailyLogDbManager
import com.foodbodi.model.Reservation
import com.foodbodi.utils.Action
import com.foodbodi.utils.DateString
import com.foodbodi.utils.fitnessAPI.FitnessAPI
import com.foodbodi.utils.fitnessAPI.FitnessAPIFactory
import retrofit2.Response
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

class GetTodayCaloriesData(val username:String, val activity: Activity) {
    fun getRemainCalories(cb:Action<Int>) {

        val todayLog:DailyLog = LocalDailyLogDbManager.ensureLocalDailyLogRecord(DateString.fromCalendar(Calendar.getInstance()), this.username)
        var threshold = todayLog.calo_threshold
        if (threshold == null) threshold = 3000

        val resercations = getReservations()
        val total:Int = resercations.map { item -> item.total }.sumBy { value -> if( value != null) value else 0 }

        val fitnessAPI = FitnessAPIFactory.getByProvider()
        fitnessAPI.getTodayStepCount(object : Action<Int> {
            override fun accept(data: Int?) {
                if (data != null) {
                    cb.accept(threshold - total + data * 25 / 1000)
                } else {
                    cb.accept(threshold - total)
                }
            }

            override fun deny(data: Int?, reason: String) {
                cb.deny(null, reason)
            }

        })


    }

    fun getReservations():ArrayList<Reservation> {
        val response: Response<FoodBodiResponse<ReservationResponse>> = FoodbodiRetrofitHolder.getService().getReservation(FoodbodiRetrofitHolder.getHeaders(activity)).execute()
        val resercationRes:ReservationResponse? = response.body()?.data()
        return resercationRes!!.reservation

    }
}