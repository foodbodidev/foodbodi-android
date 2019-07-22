package com.foodbodi.model

import android.app.Activity
import android.content.Context
import android.view.View
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.utils.Action
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrentUserProvider private constructor(){
    private var user:User? = null
    private var apiKey:String? = null
    init {

    }

    fun isLoggedIn():Boolean {
        return user != null;
    }

    fun loadCurrentUser(key:String?, callback:Action<User>) {
        apiKey = key
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
                        if (0 == foodBodiResponse?.statusCode()) {
                            user = response.body()?.data()
                        } else {
                            user = null
                        }
                        callback.accept(user)

                    }

                })
        }
    }

    fun setData(apiKey:String, profile:User) {
        this.apiKey = apiKey
        this.user = profile
    }

    fun getApiKey():String? {
        return this.apiKey
    }

    fun logout() {
        user = null
        apiKey = null
    }

    companion object Holder {
        var instance:CurrentUserProvider = CurrentUserProvider()
    }
}