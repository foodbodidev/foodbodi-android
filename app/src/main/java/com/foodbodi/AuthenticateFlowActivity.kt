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
import com.foodbodi.apis.requests.LoginRequest
import com.foodbodi.controller.LoginFragment
import com.foodbodi.controller.LoginMethodFragment
import com.foodbodi.controller.RegisterFragment
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.facebook.login.LoginResult
import com.foodbodi.apis.requests.FacebookSignInRequest
import com.foodbodi.apis.requests.GoogleSignInRequest
import com.foodbodi.model.LocalDailyLogDbManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class AuthenticateFlowActivity : AppCompatActivity(), AuthenticateFlowController {
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val GOOGLE_SIGN_IN_RESULT_CODE = 1
    companion object VAR {
        val PREFERENCE_NAME:String = "Foodbodi"
        val API_KEY_FIELD = "api_key"
    }
    override fun invokeRegisterFlow() {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container_authen_flow, RegisterFragment(this)).commit()
    }

    override fun registerSuccess(profile: User) {
        val that:Context = this
        FoodbodiRetrofitHolder.getService().login(LoginRequest(profile.email!!, profile.password!!))
            .enqueue(object : Callback<FoodBodiResponse<LoginResponse>> {
                override fun onResponse(
                    call: Call<FoodBodiResponse<LoginResponse>>,
                    response: Response<FoodBodiResponse<LoginResponse>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                        val token = response.body()?.data()?.token
                        val data:User? = response.body()?.data()?.user
                        CurrentUserProvider.get().setApiKey(token!!, that)
                        CurrentUserProvider.get().setUserData(data!!, that, true)

                        val intent = Intent(that, UpdateBasicInfoActivity::class.java)
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

    override fun onSelectLoginMethod(loginMethod: LoginMethod, payload: Any?) {
        when(loginMethod) {
            LoginMethod.MANUAL -> getSupportFragmentManager().beginTransaction().replace(
                R.id.frame_container_authen_flow,
                LoginFragment(this)
            ).commit()
            LoginMethod.GOOGLE -> {
                val signInIntent = mGoogleSignInClient!!.getSignInIntent()
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN_RESULT_CODE)
            }
            LoginMethod.FACEBOOK -> {
                val loginResult: LoginResult = payload as LoginResult
                handleFbSignInResult(loginResult)
            }
        }
    }

    override fun onLoginSuccess(apiKey:String?, user: User?) {
        if (apiKey == null) {
            Toast.makeText(this, "Can not extract api key for further requests", Toast.LENGTH_LONG).show()
        } else {
            CurrentUserProvider.get().setApiKey(apiKey, this)
            CurrentUserProvider.get().setUserData(user, this, true)
            val email = user!!.email
            LocalDailyLogDbManager.updateTodayDailyLogRecord(email!!, 0)
            finish()
        }
    }

    override fun onLoginFail(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticate_flow)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestIdToken(resources.getString(R.string.google_oauth_server_client_id))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        val transaction: FragmentTransaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container_authen_flow, LoginMethodFragment(this)).commit();

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN_RESULT_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleFbSignInResult(loginResult: LoginResult) {
        val that = this;
        FoodbodiRetrofitHolder.getService().facebookSignIn(FacebookSignInRequest(loginResult.accessToken.token, loginResult.accessToken.userId))
            .enqueue(object : Callback<FoodBodiResponse<LoginResponse>> {
                override fun onResponse(
                    call: Call<FoodBodiResponse<LoginResponse>>,
                    response: Response<FoodBodiResponse<LoginResponse>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                        CurrentUserProvider.get().setApiKey(response.body()?.data()?.token, that);
                        CurrentUserProvider.get().setUserData(response.body()?.data()?.user, that, true);
                        ensureUserBasicInfo(response.body()?.data()?.user)
                    } else {
                        onLoginFail(response.body()?.errorMessage());
                    }
                }

                override fun onFailure(call: Call<FoodBodiResponse<LoginResponse>>, t: Throwable) {
                    Toast.makeText(this@AuthenticateFlowActivity, t.message, Toast.LENGTH_LONG).show()
                }

            })

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        val that = this;
        var googleToken:String? = null
        try {
            val account = completedTask.getResult(ApiException::class.java)
            googleToken = account?.idToken
        } catch (e: ApiException) {
            Toast.makeText(this, "GoogleSignIn fail code " + e.message , Toast.LENGTH_LONG).show();
        }
        if (googleToken != null) {
            FoodbodiRetrofitHolder.getService().googleSignIn(GoogleSignInRequest(googleToken))
                .enqueue(object : Callback<FoodBodiResponse<LoginResponse>> {
                    override fun onFailure(call: Call<FoodBodiResponse<LoginResponse>>, t: Throwable) {
                        Toast.makeText(this@AuthenticateFlowActivity, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<FoodBodiResponse<LoginResponse>>,
                        response: Response<FoodBodiResponse<LoginResponse>>
                    ) {
                        if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                            CurrentUserProvider.get().setApiKey(response.body()?.data()?.token, that);
                            CurrentUserProvider.get().setUserData(response.body()?.data()?.user, that, true);
                            ensureUserBasicInfo(response.body()?.data()?.user)
                        } else {
                            onLoginFail(response.body()?.errorMessage());
                        }
                    }

                })
        } else {
            Toast.makeText(this, "Can not extract google token to login", Toast.LENGTH_LONG).show();

        }

    }

    fun ensureUserBasicInfo(user: User?) {
        if (user?.isProfileReady()!!) {
            finish()
        } else {
            val intent = Intent(this, UpdateBasicInfoActivity::class.java)
            startActivity(intent)
        }
    }
}

interface AuthenticateFlowController {
    fun onSelectLoginMethod(loginMethod: LoginMethod, payload:Any?)

    fun onLoginSuccess(apiKey:String?, user: User?)
    fun onLoginFail(message:String?)


    fun invokeRegisterFlow()
    fun registerSuccess(profile:User)
    fun registerFail(message:String?)
}

enum class LoginMethod {
    MANUAL, GOOGLE, FACEBOOK
}
