package com.foodbodi.utils

import android.app.ProgressDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


open class ProgressHUD {


    companion object {
        private var INSTANCE: ProgressHUD? = null

        val instance: ProgressHUD
            get() {
                if (INSTANCE == null) {
                    INSTANCE = ProgressHUD()
                }

                return INSTANCE!!
            }
    }


    var progressDialog: ProgressDialog? = null



    fun showLoading(context: FragmentActivity?) {
        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("Loading")
        progressDialog?.show()
    }

    fun hideLoading() {
        progressDialog?.hide()
    }


}