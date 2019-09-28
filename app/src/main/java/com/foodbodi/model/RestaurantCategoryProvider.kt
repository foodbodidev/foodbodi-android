package com.foodbodi.model

import android.util.Log
import android.widget.Toast
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.utils.Action
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.function.Consumer

class RestaurantCategoryProvider private constructor(){
    val TAG = RestaurantCategoryProvider::class.java.simpleName
    private var map:HashMap<String, RestaurantCategory>? = null
    private var tasks:ArrayList<Action<Map<String, RestaurantCategory>>> = ArrayList()
    init {
        FoodbodiRetrofitHolder.getService().getRestaurantCategories()
            .enqueue(object : Callback<FoodBodiResponse<HashMap<String, RestaurantCategory>>> {
                override fun onFailure(call: Call<FoodBodiResponse<HashMap<String, RestaurantCategory>>>, t: Throwable) {
                    System.out.println(t)
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<HashMap<String, RestaurantCategory>>>,
                    response: Response<FoodBodiResponse<HashMap<String, RestaurantCategory>>>
                ) {
                    map = response.body()?.data

                    for (task in tasks) {
                        task.accept(map)
                    }

                    tasks.clear()
                }

            })
    }

    fun queue(action:Action<Map<String, RestaurantCategory>>) {
        Log.i(TAG,"Queue task on RestaurantCategoryProvider")
        tasks.add(action)
    }

    fun getPositionOf(value:String?):Int? {
        val values:ArrayList<RestaurantCategory> =ArrayList(map!!.values)
        if (values != null) {
            for (i in 0..values.size) {
                if (values.get(i).key.equals(value)) return i
            }
        }
        return null;
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