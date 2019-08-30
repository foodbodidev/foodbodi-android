package com.foodbodi

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
import android.graphics.Bitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.apis.UploadResponse
import com.foodbodi.utils.PhotoGetter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody


class AddRestaurantActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var restaurantType:RestaurantType = RestaurantType.RESTAURANT
    private lateinit var typeRestaurantBtn: Button
    private lateinit var typeFoodtruckBtn: Button
    private val AUTOCOMPLETE_PLACE_CODE = 1
    private val TAKE_PHOTO_CODE = 2
    private var restaurantPhotoGetter:PhotoGetter? = null
    val restaurant = Restaurant()

    private val TAKE_FOOD_PHOTO_CODE = 3
    private var foodPhotoGetter: PhotoGetter? = null
    val currentFood: Food = Food()
    var foodList = ArrayList<Food>()
    var foodAdapter: FoodAdapter = FoodAdapter(foodList)


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
            val bitmap:Bitmap? = restaurantPhotoGetter!!.getBitmap(data)
            if (bitmap != null) {
                val imageView = this@AddRestaurantActivity.findViewById<ImageView>(R.id.img_restaurant_photo)
                uploadBitmapThenLoadToImageView(bitmap, imageView, restaurantPhotoGetter!!.photo_name)
                
            } else {
                Toast.makeText(this, "Error when show photo", Toast.LENGTH_LONG).show()
            }
        } else if (TAKE_FOOD_PHOTO_CODE == requestCode && data != null) {
            val bitmap: Bitmap? = foodPhotoGetter!!.getBitmap(data)
            if (bitmap != null) {
                val imageView = this@AddRestaurantActivity.findViewById<ImageView>(R.id.image_food_image)
                uploadBitmapThenLoadToImageView(bitmap, imageView, foodPhotoGetter!!.photo_name)
            }
        }
    }

    private fun uploadBitmapThenLoadToImageView(bitmap: Bitmap, imageView: ImageView, filename:String) {
        val jpegBytes = PhotoGetter.bitmapToJPEG(bitmap)
        val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), jpegBytes)
        val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

        FoodbodiRetrofitHolder.holder.service.uploadPhoto(filename, body).enqueue(object :
            Callback<FoodBodiResponse<UploadResponse>> {
            override fun onFailure(call: Call<FoodBodiResponse<UploadResponse>>, t: Throwable) {
                Toast.makeText(this@AddRestaurantActivity, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<FoodBodiResponse<UploadResponse>>,
                response: Response<FoodBodiResponse<UploadResponse>>
            ) {
                if (0 == response.body()?.statusCode()) {
                    val mediaLink = response.body()?.data()?.mediaLink
                    if (mediaLink != null) {
                        currentFood.photo = mediaLink
                        Picasso.get().load(mediaLink).fit().into(imageView)

                    } else {
                        Toast.makeText(
                            this@AddRestaurantActivity,
                            "Can't extract media link for uploaded photo",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@AddRestaurantActivity,
                        response.body()?.errorMessage(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentContext = this;
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
        ensureAddMenuView()


        findViewById<Button>(R.id.btn_submit_restaurant).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                restaurant.name = findViewById<EditText>(R.id.input_restaurant_name).text.toString()
                val category:RestaurantCategory? = findViewById<Spinner>(R.id.spinner_restaurant_category).selectedItem as? RestaurantCategory
                restaurant.category = category?.key
                restaurant.openHour = findViewById<EditText>(R.id.input_restaurent_open_hour).text.toString()
                restaurant.closeHour = findViewById<EditText>(R.id.input_restaurent_close_hour).text.toString()
                restaurant.type = restaurantType
                restaurant.address = findViewById<EditText>(R.id.input_restaurant_address).text.toString()


                if (foodList.isNotEmpty()) {
                    restaurant.foods = foodList
                }
                FoodbodiRetrofitHolder.getService().createRestaurant(FoodbodiRetrofitHolder.getHeaders(currentContext), restaurant)
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
        typeRestaurantBtn = findViewById<Button>(R.id.btn_select_type_restaurant)
        typeRestaurantBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                restaurantType = RestaurantType.RESTAURANT
                selectType(restaurantType)

            }

        })
        typeFoodtruckBtn = findViewById<Button>(R.id.btn_select_type_food_truck)
        typeFoodtruckBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                restaurantType = RestaurantType.FOOD_TRUCK
                selectType(restaurantType)
            }

        })
    }

    private fun ensureCameraInput() {
        restaurantPhotoGetter = PhotoGetter(this)
        findViewById<FloatingActionButton>(R.id.fab_restaurant_photo).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivityForResult(restaurantPhotoGetter!!.getPickPhotoIntent(), TAKE_PHOTO_CODE)
            }

        })
    }



    private fun selectType(type: RestaurantType) {
        val activeColor = ContextCompat.getColor(this@AddRestaurantActivity, R.color.colorPrimary)
        val inactiveColor = ContextCompat.getColor(this@AddRestaurantActivity, R.color.gray_color)
        when (type) {
            RestaurantType.RESTAURANT -> {
                typeRestaurantBtn.setTextColor(activeColor)
                typeFoodtruckBtn.setTextColor(inactiveColor)
            }
            RestaurantType.FOOD_TRUCK -> {
                typeRestaurantBtn.setTextColor(inactiveColor)
                typeFoodtruckBtn.setTextColor(activeColor)
            }
        }
    }

    private fun ensureAddMenuView() {
        foodPhotoGetter = PhotoGetter(this)
        val listFoodView = findViewById<RecyclerView>(R.id.list_added_food)
        val viewManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        listFoodView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager


            // specify an viewAdapter (see also next example)
            adapter = foodAdapter

        }

        findViewById<Button>(R.id.btn_add_food).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                currentFood.name = findViewById<EditText>(R.id.input_food_name).text?.toString()
                currentFood.price = findViewById<EditText>(R.id.input_food_price).text?.toString()?.toDouble()
                currentFood.calo = findViewById<EditText>(R.id.input_food_kcalo).text?.toString()?.toDouble()

                foodList.add(currentFood)
                foodAdapter.notifyDataSetChanged()

                clearAddFoodForm()
            }

        })

        findViewById<FloatingActionButton>(R.id.fab_food_photo).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                startActivityForResult(restaurantPhotoGetter!!.getPickPhotoIntent(), TAKE_FOOD_PHOTO_CODE)
            }

        })
    }

    private fun clearAddFoodForm() {

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
