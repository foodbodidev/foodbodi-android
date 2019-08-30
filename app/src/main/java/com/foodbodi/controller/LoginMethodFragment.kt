package com.foodbodi.controller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.facebook.FacebookSdk
import com.foodbodi.AuthenticateFlowController
import com.foodbodi.LoginMethod
import com.foodbodi.R
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton


class LoginMethodFragment(parent: AuthenticateFlowController) : Fragment() {
    private var parent:AuthenticateFlowController = parent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(com.foodbodi.R.layout.authenticate_login_method, container, false);

        view.findViewById<Button>(com.foodbodi.R.id.btn_continue_with_email).setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                parent.onSelectLoginMethod(LoginMethod.MANUAL)
            }

        })


        view.findViewById<SignInButton>(com.foodbodi.R.id.btn_continue_with_google).setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                parent.onSelectLoginMethod(LoginMethod.GOOGLE)
            }

        })

        /*

        view.findViewById<Button>(R.id.btn_continue_with_facebook).setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                parent.onSelectLoginMethod(LoginMethod.FACEBOOK)
            }

        })*/

        return view
    }


}