package com.foodbodi.utils.fitnessAPI

import android.app.Activity
import android.content.Intent
import com.foodbodi.utils.Action
import com.google.android.gms.fitness.FitnessOptions

interface FitnessAPI {

    fun setActivity(activity: Activity): FitnessAPI

    fun fitnessOption(option: FitnessOptions) : FitnessAPI

    fun onPermissionGranted(cb : Action<Any>) : FitnessAPI

    fun useRequestCode(code:Int) :FitnessAPI

    fun ensurePermission()

    //must call this function in onActivityResult
    fun consumePermissionGrantResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun onStepCountDelta(cb:Action<Int>) : FitnessAPI

    fun getStepCountDelta() : FitnessAPI

    fun onStop()

    fun getTodayStepCount(callback:Action<Int>)

    fun getStepCountOnDate(year:Int, month:Int, day:Int, callback: Action<Int>)
}