package com.foodbodi.controller.UpdateBasicInfoActivity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.foodbodi.R
import com.foodbodi.Section
import com.foodbodi.UpdateBasicInfoController
import com.foodbodi.model.User

class UpdateBasicInfoFragment(updateBasicInfoController: UpdateBasicInfoController, profile:User) : Fragment() {
    val updateBasicInfoController = updateBasicInfoController
    val profile = profile
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.basic_info_fragment, container, false)
        view.findViewById<Button>(R.id.btn_user_info_next).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                profile.age = view.findViewById<EditText>(R.id.input_age).text.toString().toInt()
                profile.height = view.findViewById<EditText>(R.id.input_height).text.toString().toInt()
                profile.weight = view.findViewById<EditText>(R.id.input_weight).text.toString().toDouble()
                profile.targetWeight = view.findViewById<EditText>(R.id.input_target_weight).text.toString().toDouble()
                updateBasicInfoController.onNext(Section.UPDATE_INFO)
            }

        })
        return view
    }
}