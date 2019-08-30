package com.foodbodi.apis

import com.foodbodi.apis.requests.FacebookSignInRequest
import com.foodbodi.apis.requests.GoogleSignInRequest
import com.foodbodi.apis.requests.ImportFoodRequest
import com.foodbodi.apis.requests.LoginRequest
import com.foodbodi.model.Restaurant
import com.foodbodi.model.RestaurantCategory
import com.foodbodi.model.User
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface FoodbodiService {

    @GET("profile")
    fun getProfile(@HeaderMap headers:Map<String, String>):Call<FoodBodiResponse<User>>

    @GET("restaurant/list")
    fun listRestaurant():Call<FoodBodiResponse<RestaurantsResponse>>;

    @GET("metadata/restaurant_category")
    fun getRestaurantCategories():Call<FoodBodiResponse<Map<String, RestaurantCategory>>>

    @Headers("Content-Type: application/json")
    @POST("login")
    fun login(@Body loginRequest: LoginRequest):Call<FoodBodiResponse<LoginResponse>>

    @Headers("Content-Type: application/json")
    @POST("googleSignIn")
    fun googleSignIn(@Body loginRequest: GoogleSignInRequest):Call<FoodBodiResponse<LoginResponse>>

    @Headers("Content-Type: application/json")
    @POST("facebookSignIn")
    fun facebookSignIn(@Body loginRequest: FacebookSignInRequest):Call<FoodBodiResponse<LoginResponse>>

    @Headers("Content-Type: application/json")
    @POST("register")
    fun register(@Body user: User):Call<FoodBodiResponse<LoginResponse>>

    @Headers("Content-Type: application/json")
    @POST("profile")
    fun updateProfile(@HeaderMap headers: Map<String, String>, @Body user: User) : Call<FoodBodiResponse<User>>

    @Headers("Content-Type: application/json")
    @POST("restaurant")
    fun createRestaurant(@HeaderMap headers: Map<String, String>, @Body restaurant: Restaurant) : Call<FoodBodiResponse<RestaurantResponse>>

    @Headers("Content-Type: application/json")
    @POST("currentFood/import")
    fun importFoods(@HeaderMap headers: Map<String, String>, @Body importFoodRequest: ImportFoodRequest) : Call<FoodBodiResponse<Restaurant>>

    @Multipart
    @POST("upload/photo")
    fun uploadPhoto(@Query("filename") filename:String, @Part fileData:MultipartBody.Part) : Call<FoodBodiResponse<UploadResponse>>
}