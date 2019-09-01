package com.foodbodi.model

import android.app.Activity
import android.content.Context
import android.view.View
import com.foodbodi.AuthenticateFlowActivity
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.utils.Action
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrentUserProvider private constructor(){
    private var user:User? = null
    init {

    }

    fun isLoggedIn():Boolean {
        return user != null;
    }

    fun loadCurrentUser(callback:Action<User>, context: Context) {
        val apiKey = getApiKey(context);
        if (apiKey != null) {
            FoodbodiRetrofitHolder.getService().getProfile(mapOf(Pair<String, String>("token", apiKey!!)))
                .enqueue(object : Callback<FoodBodiResponse<User>> {
                    override fun onFailure(call: Call<FoodBodiResponse<User>>, t: Throwable) {
                        System.out.println(t)
                        callback.deny(user, "Cannot get profile of user")
                    }

                    override fun onResponse(
                        call: Call<FoodBodiResponse<User>>,
                        response: Response<FoodBodiResponse<User>>
                    ) {
                        var foodBodiResponse: FoodBodiResponse<User>? = response.body()
                        if (FoodBodiResponse.SUCCESS_CODE == foodBodiResponse?.statusCode()) {
                            user = response.body()?.data()
                        } else {
                            user = null
                        }
                        callback.accept(user)

                    }

                })
        }
    }

    fun setApiKey(apiKey:String?, context: Context) {
        context.getSharedPreferences(AuthenticateFlowActivity.PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putString(
            AuthenticateFlowActivity.API_KEY_FIELD, apiKey).apply()

    }

    fun setUserData(user:User?, context: Context) {
        this.user = user;
    }

    fun getApiKey(context: Context):String? {
        return context.getSharedPreferences(AuthenticateFlowActivity.PREFERENCE_NAME, Context.MODE_PRIVATE).getString(AuthenticateFlowActivity.API_KEY_FIELD, null);
    }

    fun logout(context: Context) {
        setApiKey(null, context);
        setUserData(null, context);
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
}