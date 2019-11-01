package com.foodbodi.utils.fitnessAPI

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.foodbodi.utils.Action
import com.google.android.gms.fitness.FitnessOptions

interface FitnessAPI {

    fun setActivity(activity: Activity): FitnessAPI

    fun setContext(context: Context): FitnessAPI

    fun onPermissionGranted(cb : Action<Any>) : FitnessAPI

    fun useRequestCode(code:Int) :FitnessAPI

    //ask permission then
    fun ensurePermission()

    //must call this function in onActivityResult
    fun consumePermissionGrantResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun onStepCountDelta(cb:Action<Int>) : FitnessAPI

    fun onStepCountTotal(cb:Action<Int>) : FitnessAPI

    fun startListenOnStepCountDelta() : FitnessAPI

    //call in Activity.onPause / Fragment.onStop
    fun onStop()

    //start register sensor listeners
    fun getTodayStepCount(callback:Action<Int>)

    fun getStepCountOnDate(year:Int, month:Int, day:Int, callback: Action<Int>)

    fun getStepCountOnDateSync(year: Int, month: Int, day: Int) : Int

    fun readStepCount() : FitnessAPI
}