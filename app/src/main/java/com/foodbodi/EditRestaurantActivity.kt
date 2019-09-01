package com.foodbodi

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.foodbodi.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import com.foodbodi.apis.*
import com.foodbodi.utils.*
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody


class EditRestaurantActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var restaurantType:RestaurantType = RestaurantType.RESTAURANT
    private lateinit var typeRestaurantBtn: Button
    private lateinit var typeFoodtruckBtn: Button
    private val TAKE_PHOTO_CODE = 2
    private var restaurantPhotoGetter:PhotoGetter? = null
    var restaurant = Restaurant()

    private val TAKE_FOOD_PHOTO_CODE = 3
    private var foodPhotoGetter: PhotoGetter? = null
    val currentFood: Food = Food()

    val DATA_SERIALIZE_NAME:String = "restaurant"

    lateinit var foodListView:DynamicLinearLayoutController

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (TAKE_PHOTO_CODE == requestCode && data != null) {
            val bitmap:Bitmap? = restaurantPhotoGetter!!.getBitmap(data)
            if (bitmap != null) {
                val imageView = this@EditRestaurantActivity.findViewById<ImageView>(R.id.img_restaurant_photo)
                uploadBitmapThenLoadToImageView(bitmap, imageView, restaurantPhotoGetter!!.photo_name)
                
            } else {
                Toast.makeText(this, "Error when show photo", Toast.LENGTH_LONG).show()
            }
        } else if (TAKE_FOOD_PHOTO_CODE == requestCode && data != null) {
            val bitmap: Bitmap? = foodPhotoGetter!!.getBitmap(data)
            if (bitmap != null) {
                val imageView = this@EditRestaurantActivity.findViewById<ImageView>(R.id.image_food_image)
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
                Toast.makeText(this@EditRestaurantActivity, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<FoodBodiResponse<UploadResponse>>,
                response: Response<FoodBodiResponse<UploadResponse>>
            ) {
                if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                    val mediaLink = response.body()?.data()?.mediaLink
                    if (mediaLink != null) {
                        currentFood.photo = mediaLink
                        Picasso.get().load(mediaLink).fit().into(imageView)

                    } else {
                        Toast.makeText(
                            this@EditRestaurantActivity,
                            "Can't extract media link for uploaded photo",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@EditRestaurantActivity,
                        response.body()?.errorMessage(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        })
    }

    private fun fillData() {
        val restaurantData:Restaurant = intent.getSerializableExtra(DATA_SERIALIZE_NAME) as Restaurant
        restaurant = restaurantData
        findViewById<TextView>(R.id.input_restaurant_name).setText(restaurantData.name)
        findViewById<TextView>(R.id.input_restaurant_address).setText(restaurantData.address)
        if (restaurantData.type != null) {
            selectType(restaurantData.type!!)
        }
        if (restaurantData.category != null) {
            RestaurantCategoryProvider.ensureReady(object : Action<Map<String, RestaurantCategory>> {
                override fun accept(data: Map<String, RestaurantCategory>?) {
                    findViewById<Spinner>(R.id.spinner_restaurant_category).setSelection(RestaurantCategoryProvider.getInstance()?.getPositionOf(restaurantData.category)!!)
                }

                override fun deny(data: Map<String, RestaurantCategory>?, reason: String) {
                    Toast.makeText(this@EditRestaurantActivity, reason, Toast.LENGTH_LONG).show()
                }

            })
        }
        findViewById<TextView>(R.id.input_restaurent_open_hour).setText(restaurantData.openHour)
        findViewById<TextView>(R.id.input_restaurent_close_hour).setText(restaurantData.closeHour)

        FoodbodiRetrofitHolder.getService().listFood(FoodbodiRetrofitHolder.getHeaders(this@EditRestaurantActivity), restaurantData.id!!)
            .enqueue(object : Callback<FoodBodiResponse<Restaurant>> {
                override fun onFailure(call: Call<FoodBodiResponse<Restaurant>>, t: Throwable) {
                    Toast.makeText(this@EditRestaurantActivity, "List foods fail :" + t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<Restaurant>>,
                    response: Response<FoodBodiResponse<Restaurant>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                        val list:ArrayList<Food> = response.body()?.data()?.foods!!
                        for (food in list) {
                            foodListView.addItem("FOOD", food)
                        }

                    } else {
                        Toast.makeText(this@EditRestaurantActivity, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                    }
                }

            })
    }

    private fun getData() : Restaurant {
        //restaurant.name = findViewById<EditText>(R.id.input_restaurant_name).text.toString()
        val category:RestaurantCategory? = findViewById<Spinner>(R.id.spinner_restaurant_category).selectedItem as? RestaurantCategory
        val updateData = Restaurant()
        updateData.category = category?.key
        updateData.openHour = findViewById<EditText>(R.id.input_restaurent_open_hour).text.toString()
        updateData.closeHour = findViewById<EditText>(R.id.input_restaurent_close_hour).text.toString()
        updateData.type = restaurantType
        // restaurant.address = findViewById<EditText>(R.id.input_restaurant_address).text.toString()
        return updateData
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentContext = this;
        setContentView(R.layout.activity_add_restaurant)

        ensureRestaurantCategorySpinner()
        ensureRestaurantTypeInput()
        ensureCameraInput()
        selectType(restaurantType)
        ensureAddMenuView()


        findViewById<Button>(R.id.btn_submit_restaurant).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val updateData = getData()
                FoodbodiRetrofitHolder.getService().updateRestaurant(FoodbodiRetrofitHolder.getHeaders(currentContext), updateData,  restaurant.id!!)
                    .enqueue(object : Callback<FoodBodiResponse<RestaurantResponse>> {
                        override fun onFailure(call: Call<FoodBodiResponse<RestaurantResponse>>, t: Throwable) {
                            Toast.makeText(this@EditRestaurantActivity, "Update restaurant fail : " + t.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(
                            call: Call<FoodBodiResponse<RestaurantResponse>>,
                            response: Response<FoodBodiResponse<RestaurantResponse>>
                        ) {
                            if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                                Toast.makeText(this@EditRestaurantActivity, "New restaurant added", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this@EditRestaurantActivity, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                            }
                        }

                    })

            }

        })

        fillData()
    }


    private fun ensureRestaurantCategorySpinner() {
        val spinner:Spinner = findViewById(R.id.spinner_restaurant_category)
        spinner.onItemSelectedListener = this
        RestaurantCategoryProvider.ensureReady(object : Action<Map<String, RestaurantCategory>> {
            override fun accept(data: Map<String, RestaurantCategory>?) {

                val adapter:CategoryAdapter = CategoryAdapter(this@EditRestaurantActivity, ArrayList(data!!.values))
                spinner.adapter = adapter
            }

            override fun deny(data: Map<String, RestaurantCategory>?, reason: String) {
                Toast.makeText(this@EditRestaurantActivity, reason, Toast.LENGTH_LONG).show()
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
        findViewById<ImageButton>(R.id.fab_restaurant_photo).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivityForResult(restaurantPhotoGetter!!.getPickPhotoIntent(), TAKE_PHOTO_CODE)
            }

        })
    }



    private fun selectType(type: RestaurantType) {
        val activeColor = ContextCompat.getColor(this@EditRestaurantActivity, R.color.colorPrimary)
        val inactiveColor = ContextCompat.getColor(this@EditRestaurantActivity, R.color.gray_color)
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
        foodListView = object : DynamicLinearLayoutController(findViewById<LinearLayout>(R.id.list_added_food)) {
            override fun onItemLeftSwipe(pos: Int, view: View) {
                askToDeleteFood(pos, view)
            }

            override fun onItemRightSwipe(pos: Int, view: View) {
                askToDeleteFood(pos, view)
            }

        }
        foodListView.setRenderer("FOOD", object : Renderer<Any> {
            override fun getView(data: Any): View? {
                val inflater = LayoutInflater.from(this@EditRestaurantActivity)
                val view = inflater.inflate(R.layout.list_food_item, null)
                var name = view.findViewById<TextView>(R.id.food_item_name)
                var price = view.findViewById<TextView>(R.id.food_item_price)
                var kcalo = view.findViewById<TextView>(R.id.food_item_kcalo)
                var photo = view.findViewById<ImageView>(R.id.food_item_photo)

                var food = data as Food
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

        })

        findViewById<Button>(R.id.btn_add_food).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val food = ensureFoodInputData()
                if (food != null) {
                    FoodbodiRetrofitHolder.getService().createFood(FoodbodiRetrofitHolder.getHeaders(this@EditRestaurantActivity), food)
                        .enqueue(object : Callback<FoodBodiResponse<FoodResponse>> {
                            override fun onFailure(call: Call<FoodBodiResponse<FoodResponse>>, t: Throwable) {

                            }

                            override fun onResponse(
                                call: Call<FoodBodiResponse<FoodResponse>>,
                                response: Response<FoodBodiResponse<FoodResponse>>
                            ) {
                                foodListView.addItem("FOOD", food)
                                clearAddFoodForm()
                            }

                        })
                }
            }

        })

        findViewById<ImageButton>(R.id.fab_food_photo).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                startActivityForResult(restaurantPhotoGetter!!.getPickPhotoIntent(), TAKE_FOOD_PHOTO_CODE)
            }

        })
    }

    private fun askToDeleteFood(pos:Int, view: View) {
        AlertDialog.Builder(this)
            .setMessage("Delete ?")
            .setPositiveButton("Yes",object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    val food:Food = foodListView.data.get(pos) as Food
                    FoodbodiRetrofitHolder.getService().deleteFood(FoodbodiRetrofitHolder.getHeaders(this@EditRestaurantActivity), food.id!!, food.restaurant_id!!)
                        .enqueue(object : Callback<FoodBodiResponse<FoodResponse>> {
                            override fun onFailure(call: Call<FoodBodiResponse<FoodResponse>>, t: Throwable) {

                            }

                            override fun onResponse(
                                call: Call<FoodBodiResponse<FoodResponse>>,
                                response: Response<FoodBodiResponse<FoodResponse>>
                            ) {
                                if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                                    foodListView.removeItem(pos)
                                } else {
                                    Toast.makeText(this@EditRestaurantActivity, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                                }
                            }

                        })
                }

            })
            .setNegativeButton("No",object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    view.setTranslationX(0f)
                }

            }).show()
    }

    private fun clearAddFoodForm() {

    }

    private fun ensureFoodInputData():Food? {
        val nameInput = findViewById<EditText>(R.id.input_food_name)
        val priceInput = findViewById<EditText>(R.id.input_food_price)
        val kcaloInput = findViewById<EditText>(R.id.input_food_kcalo)
        var food:Food = Food()
        food.restaurant_id = restaurant.id
        if (TextUtils.isEmpty(nameInput.text)) {
            nameInput.setError("Food name is required")
            return null
        } else food.name = nameInput.text.toString()
        if (TextUtils.isEmpty(priceInput.text)) {
            priceInput.setError("Price is required")
            return null
        } else food.price = priceInput.text.toString().toDouble()
        if (TextUtils.isEmpty(kcaloInput.text)) {
            kcaloInput.setError("Kcalo is required")
            return null
        } else food.calo = kcaloInput.text.toString().toDouble()

        return food
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
