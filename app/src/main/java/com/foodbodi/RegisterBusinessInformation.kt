package com.foodbodi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.RestaurantResponse
import com.foodbodi.model.License
import com.foodbodi.model.Restaurant
import com.foodbodi.utils.GoogleMapUtils
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RegisterBusinessInformation : AppCompatActivity() {
    private val AUTOCOMPLETE_PLACE_CODE = 1
    private var SU_mode = false
    companion object {
        val SU_MODE_PARAM:String = "SU"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (AUTOCOMPLETE_PLACE_CODE == requestCode && data != null) {
            if (AutocompleteActivity.RESULT_OK == resultCode) {
                val place:Place = Autocomplete.getPlaceFromIntent(data)
                this@RegisterBusinessInformation.findViewById<EditText>(R.id.input_restaurant_address).setText(place.address)

            } else if (AutocompleteActivity.RESULT_ERROR == resultCode) {
                val status = Autocomplete.getStatusFromIntent(data)
                Toast.makeText(this@RegisterBusinessInformation, status.statusMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_business_information)
        SU_mode = intent.getBooleanExtra(SU_MODE_PARAM, false)

        Places.initialize(this, this.getString(R.string.google_map_key)) //TODO : secure the api key
        val placesClient: PlacesClient = Places.createClient(this)
        findViewById<EditText>(R.id.input_restaurant_address).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var fields:List<Place.Field> = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@RegisterBusinessInformation)
                startActivityForResult(intent, AUTOCOMPLETE_PLACE_CODE)
            }
        })

        findViewById<Button>(R.id.btn_submit_business_info).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val name = findViewById<TextView>(R.id.input_restaurant_name).text.toString()
                val address = findViewById<TextView>(R.id.input_restaurant_address).text.toString()
                val registration_no = findViewById<TextView>(R.id.input_restaurant_registration_number).text.toString()
                val representaive_name = findViewById<TextView>(R.id.input_restaurant_representative).text.toString()

                val info = Restaurant()
                info.name = name
                info.address = address
                val latlng:GoogleMapUtils.LatLng? = GoogleMapUtils.convertAddressToLatLng(address, this@RegisterBusinessInformation)
                info.lat = latlng?.lat
                info.lng = latlng?.lng

                val license = License()
                license.registration_number = registration_no
                license.representative_names!!.add(representaive_name)

                info.license = license

                FoodbodiRetrofitHolder.getService().createRestaurant(FoodbodiRetrofitHolder.getHeaders(this@RegisterBusinessInformation), info)
                    .enqueue(object : Callback<FoodBodiResponse<Restaurant>> {
                        override fun onFailure(call: Call<FoodBodiResponse<Restaurant>>, t: Throwable) {
                            Toast.makeText(this@RegisterBusinessInformation, "Create restaurant fail", Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(
                            call: Call<FoodBodiResponse<Restaurant>>,
                            response: Response<FoodBodiResponse<Restaurant>>
                        ) {
                            if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                                if (SU_mode) {
                                    val restaurant = response.body()?.data()
                                    val intent = Intent(this@RegisterBusinessInformation, EditRestaurantActivity::class.java)
                                    intent.putExtra(EditRestaurantActivity.DATA_SERIALIZE_NAME, restaurant)
                                    startActivity(intent)
                                } else {
                                    val intent =
                                        Intent(this@RegisterBusinessInformation, NotifyWaitingForApproval::class.java)
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                }
                            } else {
                                Toast.makeText(this@RegisterBusinessInformation, response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                            }
                        }

                    })

            }

        })
    }
}
