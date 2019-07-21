package com.foodbodi.controller

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.R
import com.foodbodi.model.Food
import com.foodbodi.model.Restaurant
import com.foodbodi.model.RestaurantCategory
import com.foodbodi.model.RestaurantCategoryProvider
import com.foodbodi.utils.Action
import com.foodbodi.utils.PhotoGetter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_food_item.*
import java.util.*
import kotlin.collections.ArrayList

class RestaurantAddMenuFragment : Fragment() {
    var foodList = ArrayList<Food>()
    var foodAdapter:FoodAdapter = FoodAdapter(foodList)
    private val TAKE_PHOTO_CODE = 2
    private var photo_name = Date().toString()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.add_menu_fragment, container, false)
        val listFoodView = view.findViewById<RecyclerView>(R.id.list_added_food)
        val viewManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        listFoodView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager!!


            // specify an viewAdapter (see also next example)
            adapter = foodAdapter

        }

        view.findViewById<Button>(R.id.btn_add_food).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val food:Food = Food()
                food.name = view.findViewById<EditText>(R.id.input_food_name).text?.toString()
                food.price = view.findViewById<EditText>(R.id.input_food_price).text?.toString()?.toDouble()
                food.calo = view.findViewById<EditText>(R.id.input_food_kcalo).text?.toString()?.toDouble()

                foodList.add(food)
                foodAdapter.notifyDataSetChanged()
            }

        })

        view.findViewById<FloatingActionButton>(R.id.fab_food_photo).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                startActivityForResult(PhotoGetter(this@RestaurantAddMenuFragment.context!!).getPickPhotoIntent(photo_name), TAKE_PHOTO_CODE)
            }

        })
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (TAKE_PHOTO_CODE == requestCode && data != null) {
            val bitmap: Bitmap? = PhotoGetter(this.context!!).getBitmap(data, photo_name)
            if (bitmap != null) {
                val drawable: BitmapDrawable = BitmapDrawable(this.resources, bitmap)
                view?.findViewById<ImageView>(R.id.food_item_photo)!!.setBackground(drawable)
            } else {
                Toast.makeText(this.context, "Error when show photo", Toast.LENGTH_LONG).show()
            }
        }
    }
}

class FoodAdapter(private val myDataset: ArrayList<Food>) :
    RecyclerView.Adapter<FoodAdapter.MyViewHolder>() {


    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): FoodAdapter.MyViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_food_item, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val food = myDataset.get(position)
        holder.view.findViewById<TextView>(R.id.food_item_name).setText(food.name);

        if (food.price != null) {
            holder.view.findViewById<TextView>(R.id.food_item_price)
                .setText(holder.view.context.getString(R.string.money_format, food.price))
        }
        if (food.calo != null) {
            holder.view.findViewById<TextView>(R.id.food_item_kcalo)
                .setText(holder.view.context.getString(R.string.kcalo_format, food.calo))
        }

        val imageView: ImageView = holder.view.findViewById<ImageView>(R.id.food_item_photo)
        if (food.photo != null) {
            Picasso.get().load(food.photo).into(imageView)
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}