package com.foodbodi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.RestaurantResponse
import com.foodbodi.controller.RestaurantAddMenuFragment
import com.foodbodi.model.*
import com.foodbodi.utils.Action
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddRestaurantActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var restaurantType:RestaurantType = RestaurantType.RESTAURANT
    private lateinit var type_restaurant:Button
    private lateinit var type_foodtruck:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_restaurant)

        ensureRestaurantCategorySpinner()
        ensureRestaurantTypeInput()
        selectType(restaurantType)

        val addMenuFragment = RestaurantAddMenuFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_container_add_menu, addMenuFragment).commit()

        findViewById<Button>(R.id.btn_submit_restaurant).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val restaurant = Restaurant()
                restaurant.name = findViewById<EditText>(R.id.input_restaurant_name).text.toString()
                val category:RestaurantCategory? = findViewById<Spinner>(R.id.spinner_restaurant_category).selectedItem as? RestaurantCategory
                restaurant.category = category?.key
                restaurant.openHour = findViewById<EditText>(R.id.input_restaurent_open_hour).text.toString()
                restaurant.closeHour = findViewById<EditText>(R.id.input_restaurent_close_hour).text.toString()
                restaurant.type = restaurantType

                val foods:ArrayList<Food>? = addMenuFragment.foodList
                if (foods != null && foods.isNotEmpty()) {
                    restaurant.foods = foods
                }
                FoodbodiRetrofitHolder.getService().createRestaurant(FoodbodiRetrofitHolder.getHeaders(), restaurant)
                    .enqueue(object : Callback<FoodBodiResponse<RestaurantResponse>> {
                        override fun onFailure(call: Call<FoodBodiResponse<RestaurantResponse>>, t: Throwable) {
                            //TODO : system failure
                        }

                        override fun onResponse(
                            call: Call<FoodBodiResponse<RestaurantResponse>>,
                            response: Response<FoodBodiResponse<RestaurantResponse>>
                        ) {
                            if (0 == response.body()?.statusCode()) {
                                Toast.makeText(this@AddRestaurantActivity, "New restaurant added", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this@AddRestaurantActivity, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                            }
                        }

                    })

            }

        })
    }


    private fun ensureRestaurantCategorySpinner() {
        val spinner:Spinner = findViewById(R.id.spinner_restaurant_category)
        spinner.onItemSelectedListener = this
        RestaurantCategoryProvider.ensureReady(object : Action<Map<String, RestaurantCategory>> {
            override fun accept(data: Map<String, RestaurantCategory>?) {

                val adapter:CategoryAdapter = CategoryAdapter(this@AddRestaurantActivity, ArrayList(data!!.values))
                spinner.adapter = adapter
            }

            override fun deny(data: Map<String, RestaurantCategory>?, reason: String) {
                Toast.makeText(this@AddRestaurantActivity, reason, Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun ensureRestaurantTypeInput() {
        type_restaurant = findViewById<Button>(R.id.btn_select_type_restaurant)
        type_restaurant.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                restaurantType = RestaurantType.RESTAURANT
                selectType(restaurantType)

            }

        })
        type_foodtruck = findViewById<Button>(R.id.btn_select_type_food_truck)
        type_foodtruck.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                restaurantType = RestaurantType.FOOD_TRUCK
                selectType(restaurantType)
            }

        })
    }

    private fun selectType(type: RestaurantType) {
        val activeColor = ContextCompat.getColor(this@AddRestaurantActivity, R.color.colorPrimary)
        val inactiveColor = ContextCompat.getColor(this@AddRestaurantActivity, R.color.material_grey_100)
        when (type) {
            RestaurantType.RESTAURANT -> {
                type_restaurant.setBackgroundColor(activeColor)
                type_foodtruck.setBackgroundColor(inactiveColor)
            }
            RestaurantType.FOOD_TRUCK -> {
                type_restaurant.setBackgroundColor(inactiveColor)
                type_foodtruck.setBackgroundColor(activeColor)
            }
        }
    }


    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(adapter: AdapterView<*>?, parent: View?, position: Int, id: Long) {

    }


}

class CategoryAdapter(private val context: Context,
                      private val dataSource: ArrayList<RestaurantCategory>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.restaurant_category_spinner_item, parent, false)
        val c = getItem(position)
        view.findViewById<TextView>(R.id.restaurant_category_item_name).setText(c.name)
        return view
    }

    override fun getItem(p0: Int): RestaurantCategory {
        return dataSource.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }

}
