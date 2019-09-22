package com.foodbodi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.RestaurantResponse
import com.foodbodi.model.Restaurant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantDetailActivity : AppCompatActivity() {
    companion object {
        val RESTAURANT_ID = "restaurant_id";

    }

    var data: Restaurant? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        val restaurantId = intent.getStringExtra(RESTAURANT_ID)

        FoodbodiRetrofitHolder.getService().getRestaurant(FoodbodiRetrofitHolder.getHeaders(this), restaurantId)
            .enqueue(object : Callback<FoodBodiResponse<RestaurantResponse>> {
                override fun onFailure(call: Call<FoodBodiResponse<RestaurantResponse>>, t: Throwable) {
                    Toast.makeText(this@RestaurantDetailActivity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<RestaurantResponse>>,
                    response: Response<FoodBodiResponse<RestaurantResponse>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                        data = response.body()?.data()?.restaurant
                        this@RestaurantDetailActivity.updateView()
                    } else {
                        Toast.makeText(this@RestaurantDetailActivity,response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                    }
                }

            })
    }

    private fun updateView() {
        if (data != null) {
            findViewById<TextView>(R.id.text_restaurant_name).text = data?.name


            var editBtn = findViewById<Button>(R.id.btn_edit_restaurant)
            //editBtn.visibility = is admin account
            editBtn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    val intent: Intent = Intent( this@RestaurantDetailActivity, EditRestaurantActivity::class.java)
                    intent.putExtra(EditRestaurantActivity.DATA_SERIALIZE_NAME, data)
                    startActivity(intent)
                }
            })

        } else {

        }
    }

}
