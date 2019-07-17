package com.foodbodi.model

import android.widget.Toast
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.utils.Action
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantCategoryProvider private constructor(){
    private var map:Map<String, RestaurantCategory>? = null
     private var tasks:ArrayList<Action<Map<String, RestaurantCategory>>> = ArrayList()
    init {
        FoodbodiRetrofitHolder.getService().getRestaurantCategories()
            .enqueue(object : Callback<FoodBodiResponse<Map<String, RestaurantCategory>>> {
                override fun onFailure(call: Call<FoodBodiResponse<Map<String, RestaurantCategory>>>, t: Throwable) {
                    System.out.println(t)
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<Map<String, RestaurantCategory>>>,
                    response: Response<FoodBodiResponse<Map<String, RestaurantCategory>>>
                ) {
                    map = response.body()?.data()

                    for (task in tasks) {
                        task.accept(map)
                        tasks.remove(task)
                    }
                }

            })
    }

    fun queue(action:Action<Map<String, RestaurantCategory>>) {
        System.out.println("Queue task on RestaurantCategoryProvider")
        tasks.add(action)
    }

    companion object Instance {
        private var instance:RestaurantCategoryProvider? = null
        fun getInstance():RestaurantCategoryProvider? {
            if (instance == null)  {
                instance = RestaurantCategoryProvider()
            }
            return instance
        }
        fun ensureReady(action:Action<Map<String, RestaurantCategory>>) {
            if (isReady()) {
                action.accept(getInstance()?.map)
            } else {
                getInstance()!!.queue(action)
            }
        }

        fun isReady():Boolean {
            return instance != null && instance?.map != null
        }
    }

}