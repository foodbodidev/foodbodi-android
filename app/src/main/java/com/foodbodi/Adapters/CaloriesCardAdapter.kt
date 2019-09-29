package com.foodbodi.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.R
import com.foodbodi.model.Food


class CaloriesCardAdapter(private var myDataset: List<Food>) :
    RecyclerView.Adapter<CaloriesIntakeViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaloriesIntakeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CaloriesIntakeViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CaloriesIntakeViewHolder, position: Int) {
        val data: Food = myDataset[position]
        holder.bind(data)
    }


    fun reloadData(data : List<Food>) {
        myDataset = data;
        this.notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}

class CaloriesIntakeViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_calories_cart_item, parent, false)) {
    private var titleTextView: TextView? = null
    private var caloriesTextView: TextView? = null
    private var priceTextView: TextView? = null




    init {
        titleTextView = itemView.findViewById(R.id.cart_item_title)
        caloriesTextView = itemView.findViewById(R.id.cart_colories)
        priceTextView = itemView.findViewById(R.id.cart_price)
    }

    fun bind(data: Food) {
        titleTextView?.text = data.name
        caloriesTextView?.text = data.name
        priceTextView?.text = data.name
    }

}
