package com.foodbodi.apis

import com.foodbodi.apis.requests.*
import com.foodbodi.model.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface FoodbodiService {

    @GET("profile")
    fun getProfile(@HeaderMap headers:Map<String, String?>):Call<FoodBodiResponse<User>>

    @GET("restaurant/list")
    fun listRestaurant():Call<FoodBodiResponse<RestaurantsResponse>>;

    @GET("restaurant/{id}")
    fun getRestaurant(@HeaderMap headers: Map<String, String?>, @Path("id") id:String) : Call<FoodBodiResponse<RestaurantResponse>>

    @GET("restaurant/mine")
    fun listMineRestaurant(@HeaderMap headers:Map<String, String?>):Call<FoodBodiResponse<RestaurantsResponse>>;

    @GET("metadata/restaurant_category")
    fun getRestaurantCategories():Call<FoodBodiResponse<HashMap<String, RestaurantCategory>>>

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
    fun updateProfile(@HeaderMap headers: Map<String, String?>, @Body user: User) : Call<FoodBodiResponse<User>>

    @Headers("Content-Type: application/json")
    @POST("restaurant")
    fun createRestaurant(@HeaderMap headers: Map<String, String?>, @Body restaurant: Restaurant) : Call<FoodBodiResponse<Restaurant>>

    @Headers("Content-Type: application/json")
    @PUT("restaurant/{restaurant_id}")
    fun updateRestaurant(@HeaderMap headers: Map<String, String?>, @Body restaurant: Restaurant, @Path("restaurant_id") restaurant_id:String) : Call<FoodBodiResponse<RestaurantResponse>>

    @Headers("Content-Type: application/json")
    @POST("currentFood/import")
    fun importFoods(@HeaderMap headers: Map<String, String?>, @Body importFoodRequest: ImportFoodRequest) : Call<FoodBodiResponse<Restaurant>>

    @GET("restaurant/{restaurant_id}/foods")
    fun listFood(@HeaderMap headers: Map<String, String?>, @Path("restaurant_id") restaurant_id: String) : Call<FoodBodiResponse<Restaurant>>

    @Multipart
    @POST("upload/photo")
    fun uploadPhoto(@Query("filename") filename:String, @Part fileData:MultipartBody.Part) : Call<FoodBodiResponse<UploadResponse>>

    @Headers("Content-Type: application/json")
    @POST("food")
    fun createFood(@HeaderMap headers: Map<String, String?>, @Body food: Food) : Call<FoodBodiResponse<FoodResponse>>

    @Headers("Content-Type: application/json")
    @DELETE("food/{id}")
    fun deleteFood(@HeaderMap headers: Map<String, String?>, @Path("id") id: String, @Query("restaurant_id") restaurant_id:String) : Call<FoodBodiResponse<FoodResponse>>

    @GET("dailylog/{year}/{month}/{day}")
    fun getDailyLog(@HeaderMap headers: Map<String, String?>, @Path("year") year: String,  @Path("month") month: String,  @Path("day") day: String) :Call<FoodBodiResponse<DailyLog>>

    @Headers("Content-Type: application/json")
    @POST("dailylog/{year}/{month}/{day}")
    fun updateDailyLog(@HeaderMap headers: Map<String, String?>,@Body dailylog: DailyLog,  @Path("year") year: String,  @Path("month") month: String,  @Path("day") day: String ) :Call<FoodBodiResponse<DailyLog>>

    @Headers("Content-Type: application/json")
    @GET("search")
    fun searchRestaurant(@HeaderMap headers: Map<String, String?>, @Query("q") query: String): Call<FoodBodiResponse<ArrayList<SearchResultItem>>>

    @Headers("Content-Type: application/json")
    @GET("reservation/mine")
    fun getReservation(@HeaderMap headers: Map<String, String?>): Call<FoodBodiResponse<ReservationResponse>>

    @Headers("Content-Type: application/json")
    @GET("reservation/{reservation_id}")
    fun getReservationById(@HeaderMap headers: Map<String, String?>, @Path("reservation_id") reservation_id: String): Call<FoodBodiResponse<FoodCardResonse>>


    @Headers("Content-Type: application/json")
    @PUT("reservation/{reservation_id}")
    fun updateReservationById(@HeaderMap headers: Map<String, String?>,@Body request: ReservationRequest,  @Path("reservation_id") reservation_id: String): Call<FoodBodiResponse<UpdateCaloriesResponse>>
}