package com.foodbodi

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import android.R.attr.data
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import com.foodbodi.utils.PhotoGetter
import com.google.android.material.floatingactionbutton.FloatingActionButton


class AddRestaurantActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var restaurantType:RestaurantType = RestaurantType.RESTAURANT
    private lateinit var type_restaurant:Button
    private lateinit var type_foodtruck:Button
    private val AUTOCOMPLETE_PLACE_CODE = 1
    private val TAKE_PHOTO_CODE = 2
    private var photo_name = Date().toString()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (AUTOCOMPLETE_PLACE_CODE == requestCode && data != null) {
            if (AutocompleteActivity.RESULT_OK == resultCode) {
                val place:Place = Autocomplete.getPlaceFromIntent(data)
                this@AddRestaurantActivity.findViewById<EditText>(R.id.input_restaurant_address).setText(place.address)

            } else if (AutocompleteActivity.RESULT_ERROR == resultCode) {
                val status = Autocomplete.getStatusFromIntent(data)
                Toast.makeText(this@AddRestaurantActivity, status.statusMessage, Toast.LENGTH_LONG).show()
            }
        } else if (TAKE_PHOTO_CODE == requestCode && data != null) {
            val bitmap:Bitmap? = PhotoGetter(this).getBitmap(data, photo_name)
            if (bitmap != null) {
                val drawable:BitmapDrawable = BitmapDrawable(this.resources, bitmap)
                findViewById<FrameLayout>(R.id.frame_container_restaurant_photo)
                    .setBackground(drawable)
            } else {
                Toast.makeText(this, "Error when show photo", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_restaurant)
        Places.initialize(this, "AIzaSyDKbAhGAfKDxorYtTW4SWGn05t-K8fKu94") //TODO : secure the api key
        val placesClient:PlacesClient = Places.createClient(this)
        findViewById<EditText>(R.id.input_restaurant_address).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var fields:List<Place.Field> = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@AddRestaurantActivity)
                startActivityForResult(intent, AUTOCOMPLETE_PLACE_CODE)
            }
            })

        ensureRestaurantCategorySpinner()
        ensureRestaurantTypeInput()
        ensureCameraInput()
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

    private fun ensureCameraInput() {
        findViewById<FloatingActionButton>(R.id.fab_restaurant_photo).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivityForResult(PhotoGetter(this@AddRestaurantActivity).getPickPhotoIntent(photo_name), TAKE_PHOTO_CODE)
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
