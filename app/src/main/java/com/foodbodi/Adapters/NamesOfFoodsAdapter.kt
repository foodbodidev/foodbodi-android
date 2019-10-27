package com.foodbodi.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foodbodi.R
import com.foodbodi.model.Food
import com.squareup.picasso.Picasso

class NamesOfFoodsAdapter(context: Context,var items :
List<Food>) : ArrayAdapter<Food>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.list_food_item, parent, false)
        var name = view.findViewById<TextView>(R.id.food_item_name)
        var price = view.findViewById<TextView>(R.id.food_item_price)
        var kcalo = view.findViewById<TextView>(R.id.food_item_kcalo)
        var photo = view.findViewById<ImageView>(R.id.food_item_photo)

        var food = items[position]
        name!!.setText(food?.name);

        if (food?.price != null) {
            price!!.setText(view?.context?.getString(R.string.money_format, food.price))
        }
        if (food?.calo != null) {
            kcalo!!.setText(view?.context?.getString(R.string.kcalo_format, food.calo))
        }

        val imageView: ImageView = photo!!
        if (food?.photo != null) {
            Picasso.get().load(food.photo).into(imageView)
        }
        return view
    }
}

