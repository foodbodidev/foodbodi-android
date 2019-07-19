package com.foodbodi

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateBasicInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_basic_info)
        val that:Context = this
        findViewById<Button>(R.id.btn_user_info_next).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val token = CurrentUserProvider.instance.getApiKey()
                if (token == null) {
                    Toast.makeText(that, "Please login", Toast.LENGTH_LONG).show()
                } else {
                    val headers = HashMap<String, String>()
                    headers.put("token", token)
                    val age: String = findViewById<EditText>(R.id.input_age).text.toString()
                    val height: Int = findViewById<EditText>(R.id.input_height).text.toString().toInt()
                    val weight: Double = findViewById<EditText>(R.id.input_age).text.toString().toDouble()
                    val target_weight: Double = findViewById<EditText>(R.id.input_age).text.toString().toDouble()
                    FoodbodiRetrofitHolder.getService().updateProfile(headers, age, height, weight, target_weight)
                        .enqueue(object : Callback<FoodBodiResponse<User>> {
                            override fun onFailure(call: Call<FoodBodiResponse<User>>, t: Throwable) {
                                //TODO : //system failure
                            }

                            override fun onResponse(
                                call: Call<FoodBodiResponse<User>>,
                                response: Response<FoodBodiResponse<User>>
                            ) {
                                if (0 == response.body()?.statusCode()) {
                                    val intent = Intent(that, MainActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(that, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                                }
                            }

                        })
                }
            }

        })
    }
}
