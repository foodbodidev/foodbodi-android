package com.foodbodi.controller

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.foodbodi.AuthenticateFlowActivity
import com.foodbodi.R
import com.foodbodi.model.CurrentUserProvider

class ProfileFragment:Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(R.layout.profile_fragment, container, false);
        view.findViewById<Button>(R.id.btn_logout).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                this@ProfileFragment.activity?.getSharedPreferences(AuthenticateFlowActivity.PREFERENCE_NAME, Context.MODE_PRIVATE)?.edit()
                    ?.remove(AuthenticateFlowActivity.API_KEY_FIELD)?.apply()
                CurrentUserProvider.instance.logout()
            }

        })
        return view;
    }
}