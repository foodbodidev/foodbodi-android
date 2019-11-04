package com.foodbodi.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.R
import com.foodbodi.model.Food
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
            (holder as ViewHeaderHolder).showDetails(food)

        }else {
            (holder as ViewFoodHolder).showDetails(food)
        }
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

class ViewFoodHolder(inflater: LayoutInflater, parent: ViewGroup) :
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
            food_item_price.text = food.price.toString();
            food_item_name.text = food.name;
            food_item_kcalo.text = food.calo.toString();
            val imageView: ImageView = photo!!
            if (food?.photo != null) {
                Picasso.get().load(food.photo).into(imageView)
            }
        }
}


