package com.foodbodi.Base

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.foodbodi.R
import com.foodbodi.Section
import com.foodbodi.UpdateBasicInfoController
import com.foodbodi.model.Gender
import com.foodbodi.model.User
import android.widget.EditText
import com.foodbodi.utils.Utils
import android.view.MotionEvent
import androidx.core.view.children


open class BaseFragment: Fragment() {

    var progressDialog: ProgressDialog? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (view != null && activity != null) {
            setupLayout(view!!)
        }
    }

    //dismiss keyboard when clicking outside
    private fun setupLayout(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view is EditText)) {
            view.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (activity != null ) {
                        Utils.hideSoftKeyboard(activity!!)
                    }
                    return false
                }
            })
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {

            for(viewElement in view.children) {
                setupLayout(viewElement)
            }
        }
    }



    fun showLoading(context: FragmentActivity?) {
        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("Loading")
        progressDialog?.show()
    }

    fun hideLoading() {
        progressDialog?.hide()
    }


}