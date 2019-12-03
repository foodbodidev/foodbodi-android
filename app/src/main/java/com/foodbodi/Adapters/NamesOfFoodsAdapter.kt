package com.foodbodi.Adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.R
import com.foodbodi.controller.UpdateCaloriesActivity
import com.foodbodi.model.CaloSegment
import com.foodbodi.model.Food
import com.foodbodi.model.Restaurant
import com.squareup.picasso.Picasso


class NamesOfFoodsAdapter(foods:ArrayList<Food>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var foodDisplay:ArrayList<Food> = ArrayList();
    private var TYPE_TEXT = 1;
    private  var TYPE_FOOD = 2;

    init {
        this.foodDisplay = foods;
    }
    fun reloadData(food:ArrayList<Food>){
        this.foodDisplay = food;
        this.notifyDataSetChanged();
    }

    override fun getItemCount(): Int {

        return foodDisplay.size;
    }

    override fun getItemViewType(position: Int): Int {
        var food:Food =  foodDisplay.get(position);
        if (food.restaurant_id!!.length > 0){
            return TYPE_FOOD;
        }else{
            return TYPE_TEXT;
        }
        return -1;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        var food: Food = foodDisplay.get(position);
        if (food.restaurant_id!!.length > 0) {
//            var viewType = holder as ViewHeaderHolder;
//            viewType.showDetails(food);

            (holder as ViewFoodHolder).showDetails(food)
        }else {
            (holder as ViewHeaderHolder).showDetails(food)
        }

        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val context = p0?.context
                val updateCaloriesIntent = Intent(context, UpdateCaloriesActivity::class.java)
                val foodFilter = ArrayList<Food>()
                for (element in foodDisplay) {
                    if (element.restaurant_id != null) {

                        if (element.restaurant_id!!.isNotEmpty()) {
                            foodFilter.add(element)
                        }

                    }
                }
                updateCaloriesIntent.putExtra("foodDisplay", foodFilter)
                updateCaloriesIntent.putExtra("isUpdateCalories", false)
                updateCaloriesIntent.putExtra("reservation_id", food.id)
                updateCaloriesIntent.putExtra("restaurant_id", food.restaurant_id)

                context?.startActivity(updateCaloriesIntent)
                Log.d("RecyclerView", "CLICK!")
            }

        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var layout = 0;
        var viewHolder: RecyclerView.ViewHolder;
            if (viewType == TYPE_TEXT) {
                val inflater = LayoutInflater.from(parent.context)
                viewHolder = ViewHeaderHolder(inflater,parent);

            } else {
                val inflater = LayoutInflater.from(parent.context)
                viewHolder = ViewFoodHolder(inflater,parent);
            }
            return viewHolder;
        }
    }

class ViewHeaderHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.layout_text_menu, parent, false)) {
        lateinit var tvAnimalType:TextView;
        init{
            this.tvAnimalType = itemView.findViewById(R.id.txtName);
        }
        fun showDetails(food:Food){
            tvAnimalType.text = food.name;
        }

    }

class ViewFoodHolder(val inflater: LayoutInflater, val parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.list_food_item, parent, false))  {
        lateinit var food_item_price:TextView
        lateinit var food_item_name : TextView
        lateinit var food_item_kcalo : TextView;
        lateinit var photo :ImageView;

    init{
            this.food_item_price = itemView.findViewById(R.id.food_item_price);
            this.food_item_name = itemView.findViewById(R.id.food_item_name);
            this.food_item_kcalo = itemView.findViewById(R.id.food_item_kcalo);
            this.photo = itemView.findViewById(R.id.food_item_photo);
        }
        fun showDetails(food:Food){
            food_item_price.text = inflater.context.getString(R.string.money_format, food.price);
            food_item_name.text = food.name;
            food_item_kcalo.text = inflater.context.getString(R.string.kcalo_format, food.calo);
            val imageView: ImageView = photo
            if (food.photo != null) {
                Picasso.get().load(food.photo).into(imageView)
            }
            val caloSegment: CaloSegment = Restaurant.getCaloSegment(food.calo!!)
            when(caloSegment) {
                CaloSegment.LOW -> food_item_kcalo.setTextColor(inflater.context.resources.getColor(R.color.low_calo))
                CaloSegment.MEDIUM -> food_item_kcalo.setTextColor(inflater.context.resources.getColor(R.color.medium_calo))
                CaloSegment.HIGH -> food_item_kcalo.setTextColor(inflater.context.resources.getColor(R.color.high_calo))
            }
        }
}


