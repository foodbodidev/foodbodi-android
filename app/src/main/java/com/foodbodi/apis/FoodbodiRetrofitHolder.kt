package com.foodbodi.apis

import com.foodbodi.model.CurrentUserProvider

class FoodbodiRetrofitHolder {
    var service:FoodbodiService = RetrofitHolder.getInstance().create(FoodbodiService::class.java)
    companion object Holder {
        val holder = FoodbodiRetrofitHolder()

        fun getService() : FoodbodiService {
            return holder.service
        }

        fun getHeaders() : Map<String, String> {
            val map = HashMap<String, String>()
            map.put("token", CurrentUserProvider.instance.getApiKey()!!)
            return map
        }
    }
}