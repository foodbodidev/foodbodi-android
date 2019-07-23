package com.foodbodi.controller.UpdateBasicInfoActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.foodbodi.R
import com.foodbodi.Section
import com.foodbodi.UpdateBasicInfoController
import com.foodbodi.model.Gender
import com.foodbodi.model.User

class SelectGenderFragment(updateBasicInfoController: UpdateBasicInfoController, profile:User): Fragment() {
    val updateBasicInfoController = updateBasicInfoController
    val profile = profile
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.authenticate_select_gender, container, false)
        view.findViewById<Button>(R.id.select_gender_male).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                profile.sex = Gender.MALE
                updateBasicInfoController.onNext(Section.SELECT_GENDER)
            }

        })

        view.findViewById<Button>(R.id.select_gender_female).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                profile.sex = Gender.FEMALE
                updateBasicInfoController.onNext(Section.SELECT_GENDER)
            }

        })
        return view
    }


}