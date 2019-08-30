package com.foodbodi.controller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.foodbodi.AuthenticateFlowController
import com.foodbodi.LoginMethod
import com.foodbodi.R
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton




class LoginMethodFragment(parent: AuthenticateFlowController) : Fragment() {
    private var parent:AuthenticateFlowController = parent
    var callbackManager:CallbackManager = CallbackManager.Factory.create()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(com.foodbodi.R.layout.authenticate_login_method, container, false);
        val currentContext = this.context;
        view.findViewById<Button>(com.foodbodi.R.id.btn_continue_with_email).setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                parent.onSelectLoginMethod(LoginMethod.MANUAL, null)
            }

        })


        view.findViewById<SignInButton>(com.foodbodi.R.id.btn_continue_with_google).setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                parent.onSelectLoginMethod(LoginMethod.GOOGLE, null)
            }

        })


        var fbLoginBtn:LoginButton = view.findViewById<Button>(R.id.btn_continue_with_facebook)!! as LoginButton
        fbLoginBtn.setReadPermissions("email");
        fbLoginBtn.setFragment(this)
        fbLoginBtn.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                parent.onSelectLoginMethod(LoginMethod.FACEBOOK, result)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {
                parent.onLoginFail(error?.message)
            }

        })


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}