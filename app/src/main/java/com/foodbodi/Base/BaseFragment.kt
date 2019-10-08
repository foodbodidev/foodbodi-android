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




open class BaseFragment: Fragment() {

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