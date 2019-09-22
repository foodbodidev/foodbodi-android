package com.foodbodi.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodbodi.R
import kotlinx.android.synthetic.main.reservation_fragment.*


data class CaloriesIntakeModel(val title: String, val dateTime: String, val titleCalories: String)

class ReservationFragment:Fragment() {



    private  var myDataset: List<CaloriesIntakeModel> = listOf(
        CaloriesIntakeModel("Cheese Rice issue Bongcheon", "2019/05/14", "300 kcal"),
        CaloriesIntakeModel("Cheese Rice issue Bongcheon", "2019/05/14", "300 kcal"),
        CaloriesIntakeModel("Cheese Rice issue Bongcheon", "2019/05/14", "300 kcal"),
        CaloriesIntakeModel("Cheese Rice issue Bongcheon", "2019/05/14", "300 kcal"),
        CaloriesIntakeModel("Cheese Rice issue Bongcheon", "2019/05/14", "300 kcal"),
        CaloriesIntakeModel("Cheese Rice issue Bongcheon", "2019/05/14", "300 kcal")
    )


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.reservation_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // RecyclerView node initialized here
        list_recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = CaloriesIntakeAdapter(myDataset)
        }
    }
}