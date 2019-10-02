package com.foodbodi.controller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.foodbodi.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.TextView
import com.foodbodi.model.Reservation
import android.graphics.Color
import android.util.Log

class CaloriesIntakeAdapter(private var myDataset: List<Reservation>) :
    RecyclerView.Adapter<CaloriesIntakeViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaloriesIntakeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CaloriesIntakeViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CaloriesIntakeViewHolder, position: Int) {
        val data: Reservation = myDataset[position]


        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val context = p0?.context
                val updateCaloriesIntent = Intent(context, UpdateCaloriesActivity::class.java)
                updateCaloriesIntent.putExtra("reservation_id", data.id)
                context?.startActivity(updateCaloriesIntent)
                Log.d("RecyclerView", "CLICK!")
            }

        })


        holder.bind(data)
    }

    fun reloadData(data : List<Reservation>) {
        myDataset = data;
        this.notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}

class CaloriesIntakeViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_calories_intake_iterm, parent, false)) {
    private var titleTextView: TextView? = null
    private var dateTextView: TextView? = null
    private var caloriesButton: Button? = null



    init {
        titleTextView = itemView.findViewById(R.id.nameText)
        dateTextView = itemView.findViewById(R.id.timeText)
        caloriesButton = itemView.findViewById(R.id.caloriesButton)



    }

    fun bind(data: Reservation) {
        titleTextView?.text = data.restaurant_name
        dateTextView?.text = data.date_string
        caloriesButton?.text = data.total.toString() + "kcal"

        var color = Color.parseColor("#808000")

        val total = data.total

        if (total != null) {
            if (total < 300) {
                color =  Color.parseColor("#7398de")

            } else if (total > 500) {
                color =  Color.parseColor("#e95975")
            } else {
                color =  Color.parseColor("#fbd402")
            }
        }

        caloriesButton?.setBackgroundColor(color)
    }
    
}

