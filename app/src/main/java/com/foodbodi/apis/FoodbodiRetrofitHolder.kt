package com.foodbodi.apis

import android.content.Context
import com.foodbodi.model.CurrentUserProvider

class FoodbodiRetrofitHolder {
    var service:FoodbodiService = RetrofitHolder.getInstance().create(FoodbodiService::class.java)
    companion object Holder {
        val holder = FoodbodiRetrofitHolder()

        fun getService() : FoodbodiService {
            return holder.service
        }

        fun getHeaders(context: Context) : Map<String, String> {
            val map = HashMap<String, String>()
            map.put("token", CurrentUserProvider.get().getApiKey(context)!!)
            return map
        }
    }
}