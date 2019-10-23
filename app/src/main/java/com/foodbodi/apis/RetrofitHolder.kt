package com.foodbodi.apis

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitHolder private constructor() {
    private var retrofit:Retrofit = retrofit2.Retrofit.Builder()
    .baseUrl("https://foodbodi-prod.appspot.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    companion object Holder {
        val instance = RetrofitHolder();
        fun getInstance():Retrofit {
            return instance.retrofit
        }
    }


}