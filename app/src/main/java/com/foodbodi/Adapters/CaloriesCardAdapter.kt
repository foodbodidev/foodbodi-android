package com.foodbodi.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.R
import com.foodbodi.model.Food
import android.view.View
import com.bumptech.glide.Glide


open interface CaloriesCartDelegate {
    fun didCaculateTotalCalories(totalCalories: Int)
}


class CaloriesCardAdapter(public var myDataset: List<Food>, public var delegate: CaloriesCartDelegate) :
    RecyclerView.Adapter<CaloriesIntakeViewHolder>(), CaloriesCartDelegate by delegate {


//    open var delegate: CaloriesCartDelegate? = null



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaloriesIntakeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CaloriesIntakeViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CaloriesIntakeViewHolder, position: Int) {
        val data: Food = myDataset[position]
        holder.bind(data)




        holder.addButton?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                data.amount += 1
                holder.amountTextView?.text = data.amount.toString()

                delegate.didCaculateTotalCalories(caculateTotalCalories().toInt())
            }

        })

        holder.subButton?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                data.amount -= 1
                holder.amountTextView?.text = data.amount.toString()

                delegate.didCaculateTotalCalories(caculateTotalCalories().toInt())
            }

        })




    }


    fun caculateTotalCalories(): Double {
        var totalCalories: Double = 0.0
        for (it in myDataset) {
            totalCalories = totalCalories + it.amount.toDouble()*it.calo!!
        }
        return totalCalories
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
    private var imageView: ImageView? = null

    var amountTextView: TextView? = null
    var addButton: Button? = null
    var subButton: Button? = null




    init {
        titleTextView = itemView.findViewById(R.id.cart_item_title)
        caloriesTextView = itemView.findViewById(R.id.cart_colories)
        priceTextView = itemView.findViewById(R.id.cart_price)
        addButton = itemView.findViewById(R.id.add_cart)
        subButton = itemView.findViewById(R.id.sub_cart)
        amountTextView = itemView.findViewById(R.id.amount_cart)
        imageView = itemView.findViewById(R.id.item_img)
    }




    fun bind(data: Food) {
        titleTextView?.text = data.name
        caloriesTextView?.text = data.calo.toString() + "Kcal"
        priceTextView?.text = data.price.toString() + "$"
        amountTextView?.text = data.amount.toString()

        Glide.with(itemView.context)
            .load(data.photo) // Image URL
            .placeholder(R.drawable.place_hoder) // Place holder image
            .error(R.drawable.place_hoder) // On error image
            .into(imageView!!); // ImageView to display image

    }

}
