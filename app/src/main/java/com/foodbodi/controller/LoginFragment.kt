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
import com.foodbodi.apis.requests.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment(parent:AuthenticateFlowController):Fragment() {
    private var parent:AuthenticateFlowController = parent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(R.layout.authenticate_login, container, false);
        view.findViewById<Button>(R.id.btn_login).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val email = view.findViewById<EditText>(R.id.input_email_login).text.toString()
                val password = view.findViewById<EditText>(R.id.input_password_login).text.toString()
                FoodbodiRetrofitHolder.getService().login(LoginRequest(email, password))
                    .enqueue(object : Callback<FoodBodiResponse<LoginResponse>> {
                        override fun onFailure(call: Call<FoodBodiResponse<LoginResponse>>, t: Throwable) {
                            //TODO : system failure
                        }

                        override fun onResponse(
                            call: Call<FoodBodiResponse<LoginResponse>>,
                            response: Response<FoodBodiResponse<LoginResponse>>
                        ) {
                            if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                                parent.onLoginSuccess(response.body()?.data()?.token, response.body()?.data()?.user);
                            } else {
                                parent.onLoginFail(response.body()?.errorMessage());
                            }
                        }

                    })
            }

        })

        view.findViewById<Button>(R.id.btn_register).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                parent.invokeRegisterFlow()
            }

        })
        return view
    }

}