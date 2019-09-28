package com.foodbodi.Adapters

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.R
import com.foodbodi.controller.UpdateCaloriesActivity
import com.foodbodi.model.Food
import com.foodbodi.model.Reservation

data class Movie(val title: String, val year: Int, val image: String)


class CaloriesCardAdapter(private var myDataset: List<Movie>) :
    RecyclerView.Adapter<CaloriesIntakeViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaloriesIntakeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CaloriesIntakeViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CaloriesIntakeViewHolder, position: Int) {
        val data: Movie = myDataset[position]
        holder.bind(data)
    }


    fun reloadData(data : List<Movie>) {
        myDataset = data;
        this.notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}

class CaloriesIntakeViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_calories_card_item, parent, false)) {
    private var titleTextView: TextView? = null
    private var dateTextView: TextView? = null
    private var caloriesButton: Button? = null



    init {
        titleTextView = itemView.findViewById(R.id.nameText)
        dateTextView = itemView.findViewById(R.id.timeText)
        caloriesButton = itemView.findViewById(R.id.caloriesButton)



    }

    fun bind(data: Movie) {
        titleTextView?.text = data.title
        dateTextView?.text = data.title
        caloriesButton?.text = data.title
    }

}
