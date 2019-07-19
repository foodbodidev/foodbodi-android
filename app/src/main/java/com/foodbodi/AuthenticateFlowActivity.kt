package com.foodbodi

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.LoginResponse
import com.foodbodi.controller.LoginFragment
import com.foodbodi.controller.LoginMethodFragment
import com.foodbodi.controller.RegisterFragment
import com.foodbodi.model.CurrentUserProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthenticateFlowActivity : AppCompatActivity(), AuthenticateFlowController {

    override fun invokeRegisterFlow() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container_authen_flow, RegisterFragment(this)).commit()
    }

    override fun registerSuccess(email:String, password:String) {
        val that:Context = this
        FoodbodiRetrofitHolder.getService().login(email, password)
            .enqueue(object : Callback<FoodBodiResponse<LoginResponse>> {
                override fun onResponse(
                    call: Call<FoodBodiResponse<LoginResponse>>,
                    response: Response<FoodBodiResponse<LoginResponse>>
                ) {
                    if (0 == response.body()?.statusCode()) {
                        val token = response.body()?.data()?.token
                        val profile = response.body()?.data()?.user
                        CurrentUserProvider.instance.setData(token!!, profile!!)

                        val intent:Intent = Intent(that, UpdateBasicInfoActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(that, "Cannot login", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<FoodBodiResponse<LoginResponse>>, t: Throwable) {
                    //TODO : system failure
                }

            })

    }

    override fun registerFail(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSelectLoginMethod(loginMethod: LoginMethod) {
        when(loginMethod) {
            LoginMethod.MANUAL -> getSupportFragmentManager().beginTransaction().replace(R.id.frame_container_authen_flow, LoginFragment(this)).commit()
            LoginMethod.GOOGLE, LoginMethod.FACEBOOK -> Toast.makeText(this,"Coming soon", Toast.LENGTH_LONG).show()
        }
    }

    override fun onLoginSuccess() {
        finish()
    }

    override fun onLoginFail(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticate_flow)
        val transaction: FragmentTransaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container_authen_flow, LoginMethodFragment(this)).commit();
    }
}

interface AuthenticateFlowController {
    fun onSelectLoginMethod(loginMethod: LoginMethod)

    fun onLoginSuccess()
    fun onLoginFail(message:String?)

    fun invokeRegisterFlow()
    fun registerSuccess(email:String, password:String)
    fun registerFail(message:String?)
}

enum class LoginMethod {
    MANUAL, GOOGLE, FACEBOOK
}
