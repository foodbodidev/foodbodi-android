package com.foodbodi.controller

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import com.foodbodi.R
import com.foodbodi.model.Reservation
import com.foodbodi.Adapters.CaloriesCardAdapter
import com.foodbodi.Adapters.Movie

import kotlinx.android.synthetic.main.activity_update_calories.*
import kotlinx.android.synthetic.main.activity_update_calories.card_recycler_view
import kotlinx.android.synthetic.main.reservation_fragment.*
import androidx.recyclerview.widget.RecyclerView




class UpdateCaloriesActivity : AppCompatActivity() {

    private  var myDataset = listOf(
        Movie("Raising Arizona", 1987, "raising_arizona.jpg"),
        Movie("Vampire's Kiss", 1988, "vampires_kiss.png"),
        Movie("Con Air", 1997, "con_air.jpg"),
        Movie("Face/Off", 1997, "face_off.jpg"),
        Movie("National Treasure", 2004, "national_treasure.jpg"),
        Movie("The Wicker Man", 2006, "wicker_man.jpg"),
        Movie("Bad Lieutenant", 2009, "bad_lieutenant.jpg"),
        Movie("Kick-Ass", 2010, "kickass.jpg")

    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_calories)

        viewManager = LinearLayoutManager(this)
        viewAdapter = CaloriesCardAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.card_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            layoutManager = viewManager

            adapter = viewAdapter

        }


    }

}
