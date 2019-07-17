package com.foodbodi.apis

class FoodbodiRetrofitHolder {
    var service:FoodbodiService = RetrofitHolder.getInstance().create(FoodbodiService::class.java)
    companion object Holder {
        val holder = FoodbodiRetrofitHolder()

        fun getService() : FoodbodiService {
            return holder.service
        }
    }
}