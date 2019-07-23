package com.foodbodi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.controller.UpdateBasicInfoActivity.SelectGenderFragment
import com.foodbodi.controller.UpdateBasicInfoActivity.UpdateBasicInfoFragment
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateBasicInfoActivity : AppCompatActivity(), UpdateBasicInfoController {
    val profile = User()

    override fun onNext(from: Section) {
        when(from) {
            Section.SELECT_GENDER -> {
                val transaction: FragmentTransaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_container_update_basic_info, UpdateBasicInfoFragment(this, profile));
                transaction.addToBackStack(null);
                transaction.commit();
            }
            Section.UPDATE_INFO -> {
                submit()
            }
        }
    }

    override fun onBack() {
    }

    override fun submit() {
        val token = CurrentUserProvider.instance.getApiKey()
        if (token == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_LONG).show()
        } else {
            val headers = HashMap<String, String>()
            headers.put("token", token)
            FoodbodiRetrofitHolder.getService().updateProfile(headers, profile)
                .enqueue(object : Callback<FoodBodiResponse<User>> {
                    override fun onFailure(call: Call<FoodBodiResponse<User>>, t: Throwable) {
                        //TODO : //system failure
                    }

                    override fun onResponse(
                        call: Call<FoodBodiResponse<User>>,
                        response: Response<FoodBodiResponse<User>>
                    ) {
                        if (0 == response.body()?.statusCode()) {
                            val intent = Intent(this@UpdateBasicInfoActivity, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@UpdateBasicInfoActivity, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                        }
                    }

                })
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_basic_info)

        var transaction:FragmentTransaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container_update_basic_info,
            SelectGenderFragment(this, profile)
        );
        transaction.addToBackStack(null);
        transaction.commit();

    }
}

interface UpdateBasicInfoController {
    fun onBack()

    fun onNext(from: Section)

    fun submit()
}

enum class Section {
    SELECT_GENDER, UPDATE_INFO
}
