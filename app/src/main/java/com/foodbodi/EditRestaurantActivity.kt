package com.foodbodi

import android.annotation.SuppressLint
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
import android.net.Uri
import android.text.TextUtils
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.foodbodi.apis.*
import com.foodbodi.utils.*
import com.foodbodi.utils.picasso_transformation.RoundCornerImageTransformer
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.util.*
import android.app.TimePickerDialog
import android.text.InputType
import org.checkerframework.checker.nullness.compatqual.NullableType
import java.text.SimpleDateFormat


class EditRestaurantActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var restaurantType:RestaurantType = RestaurantType.RESTAURANT
    private lateinit var typeRestaurantBtn: Button
    private lateinit var typeFoodtruckBtn: Button
    private val TAKE_PHOTO_CODE = 2
    private val TAKE_FOOD_PHOTO_CODE = 3
    private val CROP_FOOD_PHOTO_CODE = 4
    private val CROP_RESTAURANT_PHOTO_CODE = 5

    private var restaurantPhotoGetter:PhotoGetter? = null
    var restaurant = Restaurant()

    private var foodPhotoGetter: PhotoGetter? = null
    val currentFood: Food = Food()

    var capturedFoodPhoto:String? = null
    var capturedRestaurantPhotos:ArrayList<String> = ArrayList()


    lateinit var foodListView:DynamicLinearLayoutController
    lateinit var photoPager:ViewPager

    lateinit var restaurantPhotoProgressBar:ProgressBar
    lateinit var foodPhotoProgressBar:ProgressBar

    lateinit var addFoodBtn:Button
    lateinit var addFoodSection:LinearLayout

    companion object {
        val DATA_SERIALIZE_NAME:String = "restaurant"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentContext = this;
        setContentView(R.layout.activity_add_restaurant)

        restaurantPhotoProgressBar = findViewById(R.id.fab_restaurant_photo_loading)
        foodPhotoProgressBar = findViewById(R.id.fab_food_photo_loading)
        ensurePhotoBanner()
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
                                Toast.makeText(this@EditRestaurantActivity, "Update successfully", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this@EditRestaurantActivity, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                            }
                        }

                    })

            }

        })


        fillData()

        actionClickOnTextView()


    }


    private fun actionClickOnTextView() {

        val openHourTextView = findViewById<TextView>(R.id.input_restaurent_open_hour)
        val closeHourTextView = findViewById<TextView>(R.id.input_restaurent_close_hour)

        openHourTextView.inputType = InputType.TYPE_NULL
        closeHourTextView.inputType = InputType.TYPE_NULL


        openHourTextView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val cal = Calendar.getInstance()
                val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    TimePickerDialog.BUTTON_NEGATIVE
                    openHourTextView.text = SimpleDateFormat("HH:mm").format(cal.time)
                }
                TimePickerDialog(this@EditRestaurantActivity, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }

        })


        findViewById<TextView>(R.id.input_restaurent_close_hour).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val cal = Calendar.getInstance()
                val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    TimePickerDialog.BUTTON_NEGATIVE
                    closeHourTextView.text = SimpleDateFormat("HH:mm").format(cal.time)
                }
                TimePickerDialog(this@EditRestaurantActivity, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (TAKE_PHOTO_CODE == requestCode) {
            UCrop.of(restaurantPhotoGetter!!.getPickImageResultUri(data)!!, restaurantPhotoGetter!!.getCroppedImageOutputUri()!!)
                .withAspectRatio(3f,2f)
                .start(this, CROP_RESTAURANT_PHOTO_CODE)
        } else if (TAKE_FOOD_PHOTO_CODE == requestCode) {
            UCrop.of(restaurantPhotoGetter!!.getPickImageResultUri(data)!!, restaurantPhotoGetter!!.getCroppedImageOutputUri()!!)
                .withAspectRatio(1f,1f)
                .start(this, CROP_FOOD_PHOTO_CODE)
        } else if (CROP_RESTAURANT_PHOTO_CODE == requestCode && data != null) {
            var resultUri:Uri? = UCrop.getOutput(data);
            val bitmap:Bitmap? = restaurantPhotoGetter!!.getBitmapFromURL(resultUri!!)
            if (bitmap != null) {
                restaurantPhotoProgressBar.setVisibility(View.VISIBLE)
                restaurantPhotoProgressBar.startAnimation(RotateAnimation(0f, 360f))

                uploadBitmapThenLoadToImageView(bitmap, restaurantPhotoGetter!!.photo_name, object : Action<String> {
                    override fun accept(mediaLink: String?) {
                        var view:RestaurantPhotoItem = RestaurantPhotoItem(mediaLink!!)
                        addRestaurantPhoto(view)
                        restaurantPhotoProgressBar.setVisibility(View.INVISIBLE)
                        capturedRestaurantPhotos.add(mediaLink!!)
                    }

                    override fun deny(data: String?, reason: String) {
                        Toast.makeText(this@EditRestaurantActivity, reason, Toast.LENGTH_LONG).show()
                    }

                })

            } else {
                Toast.makeText(this, "Error when show photo", Toast.LENGTH_LONG).show()
            }
        } else if (CROP_FOOD_PHOTO_CODE == requestCode && data != null) {
            var resultUri:Uri? = UCrop.getOutput(data);
            val bitmap:Bitmap? = restaurantPhotoGetter!!.getBitmapFromURL(resultUri!!)
            if (bitmap != null) {
                foodPhotoProgressBar.visibility = View.VISIBLE
                foodPhotoProgressBar.startAnimation(RotateAnimation(0f, 360f))
                val imageView = this@EditRestaurantActivity.findViewById<ImageView>(R.id.image_food_image)
                uploadBitmapThenLoadToImageView(bitmap, foodPhotoGetter!!.photo_name,
                    object : Action<String> {
                        override fun deny(data: String?, reason: String) {
                            foodPhotoProgressBar.visibility = View.INVISIBLE
                            Toast.makeText(this@EditRestaurantActivity, reason, Toast.LENGTH_LONG).show()
                        }

                        override fun accept(data: String?) {
                            capturedFoodPhoto = data
                            foodPhotoProgressBar.visibility = View.INVISIBLE
                            PhotoGetter.loadImageFromURL(data!!, imageView)
                        }

                    })
            }
        }
        else if (UCrop.RESULT_ERROR == requestCode) {
            Toast.makeText(this, "Crop image problem!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun addRestaurantPhoto(view:RestaurantPhotoItem) {
        val adapter = photoPager.adapter as ScreenSlidePagerAdapter
        adapter.photoViews.add(view)
        adapter.notifyDataSetChanged()
    }

    private fun fillData() {
        val restaurantData:Restaurant = intent.getSerializableExtra(DATA_SERIALIZE_NAME) as Restaurant
        restaurant = restaurantData
        capturedRestaurantPhotos = restaurant.photos
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
        findViewById<TextView>(R.id.input_restaurent_open_hour).setText(restaurantData.open_hour)
        findViewById<TextView>(R.id.input_restaurent_close_hour).setText(restaurantData.close_hour)

        if (restaurantData.id != null) {
            FoodbodiRetrofitHolder.getService()
                .listFood(FoodbodiRetrofitHolder.getHeaders(this@EditRestaurantActivity), restaurantData.id!!)
                .enqueue(object : Callback<FoodBodiResponse<Restaurant>> {
                    override fun onFailure(call: Call<FoodBodiResponse<Restaurant>>, t: Throwable) {
                        Toast.makeText(this@EditRestaurantActivity, "List foods fail :" + t.message, Toast.LENGTH_LONG)
                            .show()
                    }

                    override fun onResponse(
                        call: Call<FoodBodiResponse<Restaurant>>,
                        response: Response<FoodBodiResponse<Restaurant>>
                    ) {
                        if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                            val list: ArrayList<Food> = response.body()?.data()?.foods!!
                            for (food in list) {
                                foodListView.addItem("FOOD", food)
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

        for (url in capturedRestaurantPhotos) {
            var view:RestaurantPhotoItem = RestaurantPhotoItem(url)
            addRestaurantPhoto(view)
        }
    }

    private fun getData() : Restaurant {
        //restaurant.name = findViewById<EditText>(R.id.input_restaurant_name).text.toString()
        val category:RestaurantCategory? = findViewById<Spinner>(R.id.spinner_restaurant_category).selectedItem as? RestaurantCategory
        val updateData = Restaurant()
        updateData.category = category?.key
        updateData.open_hour = findViewById<EditText>(R.id.input_restaurent_open_hour).text.toString()
        updateData.close_hour = findViewById<EditText>(R.id.input_restaurent_close_hour).text.toString()
        updateData.type = restaurantType
        updateData.photos = capturedRestaurantPhotos
        // restaurant.address = findViewById<EditText>(R.id.input_restaurant_address).text.toString()
        return updateData
    }


    private fun ensurePhotoBanner() {
        photoPager = findViewById<ViewPager>(R.id.pager_restaurant_photo)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        photoPager.adapter = pagerAdapter
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
        addFoodSection = findViewById(R.id.add_food_section)
        foodPhotoGetter = PhotoGetter(this)
        foodListView = object : DynamicLinearLayoutController(findViewById<LinearLayout>(R.id.list_added_food), R.id.food_item_container, R.id.food_item_content) {
            override fun onItemLeftSwipe(pos: Int, view: View) {
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
                    price.setTextColor(resources.getColor(R.color.text_grey))
                }
                if (food?.calo != null) {
                    kcalo!!.setText(view?.context?.getString(R.string.kcalo_format, food.calo))
                    val caloSegment:CaloSegment = Restaurant.getCaloSegment(food.calo!!)
                    when(caloSegment) {
                        CaloSegment.LOW -> kcalo.setTextColor(resources.getColor(R.color.low_calo))
                        CaloSegment.MEDIUM -> kcalo.setTextColor(resources.getColor(R.color.medium_calo))
                        CaloSegment.HIGH -> kcalo.setTextColor(resources.getColor(R.color.high_calo))
                    }

                }

                val imageView: ImageView = photo!!
                if (food?.photo != null) {
                    Picasso.get().load(food.photo).transform(RoundCornerImageTransformer(5, 0)).into(imageView)
                }

                return view
            }

        })

        addFoodBtn = findViewById<Button>(R.id.btn_add_food)
        addFoodBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val food = ensureFoodInputData()
                if (food != null) {
                    FoodbodiRetrofitHolder.getService().createFood(FoodbodiRetrofitHolder.getHeaders(this@EditRestaurantActivity), food)
                        .enqueue(object : Callback<FoodBodiResponse<FoodResponse>> {
                            override fun onFailure(call: Call<FoodBodiResponse<FoodResponse>>, t: Throwable) {
                                Utils.showAlert(t.message!!, this@EditRestaurantActivity)

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
                                Utils.showAlert(t.message!!, this@EditRestaurantActivity)

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
        findViewById<EditText>(R.id.input_food_name).setText("")
        findViewById<EditText>(R.id.input_food_price).setText("")
        findViewById<EditText>(R.id.input_food_kcalo).setText("")
        findViewById<ImageView>(R.id.image_food_image).setImageDrawable(null)
    }

    private fun ensureFoodInputData():Food? {
        val nameInput = findViewById<EditText>(R.id.input_food_name)
        val priceInput = findViewById<EditText>(R.id.input_food_price)
        val kcaloInput = findViewById<EditText>(R.id.input_food_kcalo)
        val imageInput = findViewById<ImageView>(R.id.image_food_image)
        var food:Food = Food()
        food.restaurant_id = restaurant.id
        food.photo = capturedFoodPhoto
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

    fun uploadBitmapThenLoadToImageView(bitmap: Bitmap, filename:String, callback:Action<String>) {
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
                        callback.accept(mediaLink)
                    } else {
                        callback.deny(null,"Can't extract media link for uploaded photo")
                    }
                } else {
                    callback.deny(null, response.body()?.errorMessage()!!)
                }
            }

        })
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        var photoViews = ArrayList<RestaurantPhotoItem>()
        override fun getCount(): Int {
            return photoViews.size
        }

        override fun getItem(position: Int): Fragment {
            return photoViews.get(position)
        }
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