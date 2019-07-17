package com.foodbodi.apis

import com.foodbodi.model.Restaurant
import com.foodbodi.model.RestaurantCategory
import retrofit2.Call
import retrofit2.http.GET

interface FoodbodiService {

    @GET("restaurant/list")
    fun listRestaurant():Call<FoodBodiResponse<RestaurantsResponse>>;

    @GET("metadata/restaurant_category")
    fun getRestaurantCategories():Call<FoodBodiResponse<Map<String, RestaurantCategory>>>

}