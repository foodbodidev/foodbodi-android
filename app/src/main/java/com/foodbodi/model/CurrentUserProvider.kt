package com.foodbodi.model

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Toast
import com.foodbodi.AuthenticateFlowActivity
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.controller.Fragments.GetTodayCaloriesData
import com.foodbodi.utils.Action
import com.foodbodi.utils.ProgressHUD
import com.foodbodi.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrentUserProvider private constructor(){
    private var user:User? = null
    private var status:Status = Status.NOT_RUN
    private var remainCaloToEat:Double? = null;
    val callbacks:ArrayList<Action<User>> = ArrayList()
    init {

    }

    fun isLoggedIn():Boolean {
        return status == Status.LOGGED_IN
    }

    fun loadCurrentUser(callback:Action<User>, context: Context) {
        val apiKey = getApiKey(context);
        registerCallback(callback)
        if (apiKey != null) {
            status = Status.RUNNING
            FoodbodiRetrofitHolder.getService().getProfile(mapOf(Pair<String, String>("token", apiKey)), true)
                .enqueue(object : Callback<FoodBodiResponse<User>> {
                    override fun onFailure(call: Call<FoodBodiResponse<User>>, t: Throwable) {
                        System.out.println(t)
                        status = Status.NOT_LOGGED_IN
                        for (item in this@CurrentUserProvider.callbacks) {
                            callback.deny(user, "Cannot get profile of user")
                        }
                    }

                    override fun onResponse(
                        call: Call<FoodBodiResponse<User>>,
                        response: Response<FoodBodiResponse<User>>
                    ) {
                        var foodBodiResponse: FoodBodiResponse<User>? = response.body()
                        if (FoodBodiResponse.SUCCESS_CODE == foodBodiResponse?.statusCode()) {
                            user = response.body()?.data()
                            status = Status.LOGGED_IN
                        } else {
                            user = null
                            status = Status.NOT_LOGGED_IN
                        }
                        flushCallbacks()

                    }

                })
        } else {
            status = Status.NOT_LOGGED_IN
            callback.deny(null, "Api token is absence")
        }
    }

    fun registerCallback(callback:Action<User>) {
        callbacks.add(callback)
    }

    fun setApiKey(apiKey:String?, context: Context) {
        context.getSharedPreferences(AuthenticateFlowActivity.PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putString(
            AuthenticateFlowActivity.API_KEY_FIELD, apiKey).apply()

    }

    fun setUserData(user:User?, context: Context, flushCallback:Boolean) {
        this.user = user;
        status = Status.LOGGED_IN
        if (flushCallback) {
            flushCallbacks()
        }
    }

    fun getUser() : User? {
        return this.user
    }

    fun getApiKey(context: Context):String? {
        return context.getSharedPreferences(AuthenticateFlowActivity.PREFERENCE_NAME, Context.MODE_PRIVATE).getString(AuthenticateFlowActivity.API_KEY_FIELD, null);
    }

    fun logout(context: Context, callback: Action<User>) {
        setApiKey(null, context);
        setUserData(null, context, false);
        status = Status.NOT_LOGGED_IN
        callback.accept(null)
    }

    fun updateRemainCaloToEat(activity:Activity) {
        GetTodayCaloriesData(get().getUser()?.email!!, activity)
            .getRemainCalories(object : Action<Int> {
                override fun accept(limit: Int?) {
                    this@CurrentUserProvider.remainCaloToEat = limit!!.toDouble()
                }

                override fun deny(data: Int?, reason: String) {
                    Toast.makeText(activity,reason, Toast.LENGTH_LONG).show()
                }

            })
    }

    fun getRemainCaloToEat():Double {
        if (this.remainCaloToEat == null) return 0.0 else return this.remainCaloToEat!!;
    }

    private fun flushCallbacks() {
        for (item in this@CurrentUserProvider.callbacks) {
            item.accept(user)
        }
        callbacks.clear()
    }

    companion object Holder {
        private var instance:CurrentUserProvider? = null
        fun get():CurrentUserProvider {
            if (instance == null) {
                instance = CurrentUserProvider()
            }
            return instance!!
        }
    }

    enum class Status {
        NOT_RUN, RUNNING, LOGGED_IN, NOT_LOGGED_IN
    }
}