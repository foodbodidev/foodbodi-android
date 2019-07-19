package com.foodbodi.apis

import com.foodbodi.model.Restaurant
import com.foodbodi.model.RestaurantCategory
import com.foodbodi.model.User
import retrofit2.Call
import retrofit2.http.*

interface FoodbodiService {

    @GET("profile")
    fun getProfile(@HeaderMap headers:Map<String, String>):Call<FoodBodiResponse<User>>

    @GET("restaurant/list")
    fun listRestaurant():Call<FoodBodiResponse<RestaurantsResponse>>;

    @GET("metadata/restaurant_category")
    fun getRestaurantCategories():Call<FoodBodiResponse<Map<String, RestaurantCategory>>>

    @FormUrlEncoded
    @POST("login")
    fun login(@Field("email") email:String, @Field("password") password:String):Call<FoodBodiResponse<LoginResponse>>

    @FormUrlEncoded
    @POST("register")
    fun register(@Field("email") email:String, @Field("password") password:String, @Field("first_name") firstName:String, @Field("last_name") lastName:String):Call<FoodBodiResponse<LoginResponse>>

    @FormUrlEncoded
    @POST("profile")
    fun updateProfile(@HeaderMap headers: Map<String, String>,  @Field("age") age:String, @Field("height") height:Int, @Field("weight") weight:Double, @Field("target_weight") targetWeight:Double) : Call<FoodBodiResponse<User>>
}