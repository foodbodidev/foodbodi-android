package com.foodbodi.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.foodbodi.AuthenticateFlowController
import com.foodbodi.R
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment(parent:AuthenticateFlowController) : Fragment() {
    private var parent:AuthenticateFlowController = parent
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(R.layout.authenticate_user_account_info, container, false);
        view.findViewById<Button>(R.id.btn_user_info_signup).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val email = view.findViewById<EditText>(R.id.input_email).text.toString()
                val password = view.findViewById<EditText>(R.id.input_password).text.toString()
                val firstName = view.findViewById<EditText>(R.id.input_firstname).text.toString()
                val lastName = view.findViewById<EditText>(R.id.input_lastname).text.toString()

                FoodbodiRetrofitHolder.getService().register(email, password, firstName, lastName).enqueue(object : Callback<FoodBodiResponse<LoginResponse>> {
                    override fun onFailure(call: Call<FoodBodiResponse<LoginResponse>>, t: Throwable) {
                        //TODO : system failure
                    }

                    override fun onResponse(
                        call: Call<FoodBodiResponse<LoginResponse>>,
                        response: Response<FoodBodiResponse<LoginResponse>>
                    ) {
                        if (0 == response.body()?.statusCode()) {
                            parent.registerSuccess(email, password)
                        } else {
                            parent.registerFail(response.body()?.errorMessage())
                        }
                    }

                })

            }

        })

        return view
    }
}