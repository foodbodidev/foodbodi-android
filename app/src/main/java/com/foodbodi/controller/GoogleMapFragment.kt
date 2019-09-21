package com.foodbodi.controller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fonfon.kgeohash.GeoHash
import com.foodbodi.*
import com.foodbodi.R
import com.foodbodi.apis.*
import com.foodbodi.model.*
import com.foodbodi.utils.Action
import com.foodbodi.utils.GoogleMapUtils
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//TODO : cache this View so that no need to re-create when navigate back to this
class GoogleMapFragment : Fragment(), LocationListener {
    val TAG = GoogleMapFragment::class.java.simpleName
    private lateinit var supportMapFragment: SupportMapFragment;
    private var recyclerView: RecyclerView? = null;
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var restaurants: ArrayList<Restaurant>
    private var restaurantMarkers: HashMap<String, Marker> = HashMap()
    private var userCurrentLocation: Marker? = null
    private lateinit var googleMap: GoogleMap

    private lateinit var searchBox:EditText
    private var needMoveCamera = true;

    companion object {
        val SEARCH_REQUEST_CODE = 1
    }


    val firestore = FirebaseFirestore.getInstance()


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurants = ArrayList()
    }

    override fun onResume() {
        super.onResume()
        needMoveCamera = true;
        userCurrentLocation = null;
        if (ContextCompat.checkSelfPermission(
                this.activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //TODO : update location
            //locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(
                this.activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            //TODO : remove update location
            //locationManager.removeUpdates(this);
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View = inflater.inflate(R.layout.fodimap, container, false);
        supportMapFragment = SupportMapFragment.newInstance();
        supportMapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it
            googleMap.setOnMarkerClickListener { marker ->
                if (marker.tag != null) {
                    val restaurantId =
                        marker.tag as String;
                    this@GoogleMapFragment.gotoMenuInfoRestaurant(restaurantId);
                }
                false
            }

            restaurantMarkers.clear()
            registerCurrentLocation()


        });

        getChildFragmentManager().beginTransaction().replace(R.id.map, supportMapFragment).commit();


        view.findViewById<ImageButton>(R.id.fab_add_restaurant)!!.setOnClickListener(View.OnClickListener {
            if (CurrentUserProvider.get().isLoggedIn()) {
                invokeAddRestaurantForm()
            } else {
                invokeAuthentication()
            }
        })

        searchBox = view.findViewById<EditText>(R.id.edit_text_search_box)
        searchBox.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, focus: Boolean) {
                if (focus) {
                    val intent = Intent(this@GoogleMapFragment.requireContext(), SearchRestaurantActivity::class.java)
                    startActivityForResult(intent, SEARCH_REQUEST_CODE)
                }
                searchBox.clearFocus()
            }
        })

        view.findViewById<Button>(R.id.btn_su_add).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val intent = Intent(this@GoogleMapFragment.context, RegisterBusinessInformation::class.java)
                intent.putExtra(RegisterBusinessInformation.SU_MODE_PARAM, true)
                startActivity(intent)
            }

        })
        return view;
    }

    @SuppressLint("MissingPermission")
    private fun registerCurrentLocation() {
        if (MainActivity.locationProvider != null) {
            MainActivity.mLocationManager?.requestLocationUpdates(
                MainActivity.locationProvider,
                3000,
                7f,
                this@GoogleMapFragment
            )
        }
    }

    private fun loadRestaurant() {
        if (userCurrentLocation != null) {
            findNearbyRestaurant(
                GoogleMapUtils.LatLng(userCurrentLocation?.position!!.latitude, userCurrentLocation?.position!!.longitude),
                object : Action<ArrayList<Restaurant>> {
                    override fun accept(list: ArrayList<Restaurant>?) {
                        restaurants = list!!
                        ensureListRestaurantView()

                    }

                    override fun deny(data: ArrayList<Restaurant>?, reason: String) {
                        Toast.makeText(this@GoogleMapFragment.context, reason, Toast.LENGTH_LONG).show()
                    }

                })
        } else {
            Toast.makeText(this.context, "Problem when trying to get current location", Toast.LENGTH_LONG).show();
        }
    }

    override fun onLocationChanged(currentLocation: Location?) {
        Log.i(TAG, "User current location : " + currentLocation.toString())
        if (userCurrentLocation == null) {
            userCurrentLocation = googleMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        currentLocation!!.latitude,
                        currentLocation.longitude
                    )
                ).title("You")
            )
        } else {
            userCurrentLocation?.position = LatLng(currentLocation?.latitude!!, currentLocation.longitude)
        }
        loadRestaurant();
        if (needMoveCamera) {
            moveCameraToCurrentLocation()
            needMoveCamera = false
        }

    }

    private fun moveCameraToCurrentLocation() {
        if (userCurrentLocation != null) {
            moveCamera(userCurrentLocation?.position!!.latitude, userCurrentLocation?.position!!.longitude, 12.5f)
        }
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    private fun moveCamera(lat: Double, lng: Double, zoom: Float) {
        var latlng: LatLng = LatLng(lat, lng)
        var cameraPos: CameraPosition = CameraPosition.Builder().target(latlng).zoom(zoom).build()
        var cameraUpdate: CameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPos)
        googleMap.moveCamera(cameraUpdate)
    }

    private fun findNearbyRestaurant(location: GoogleMapUtils.LatLng, callback: Action<ArrayList<Restaurant>>) {
        val geohash = GeoHash(location.lat, location.lng, 5)
        val center = geohash.toString()

        firestore.collection("restaurants").whereArrayContains("neighbour_geohash", center)
            .addSnapshotListener { snapshot, e ->
                run {
                    if (e != null) {
                        callback.deny(null, e.message!!)
                    }

                    if (snapshot != null) {
                        var list = ArrayList<Restaurant>()
                        for (document in snapshot.documents) {
                            val r = document.toObject(Restaurant::class.java);
                            r!!.id = document.id
                            list.add(r)
                        }
                        callback.accept(list)

                    } else {
                        callback.deny(null, "Can not get restaurants in zone $center")
                    }
                }
            }
    }

    private fun invokeAuthentication() {
        startActivity(Intent(context, AuthenticateFlowActivity::class.java))
    }

    private fun invokeAddRestaurantForm() {
        FoodbodiRetrofitHolder.getService()
            .listMineRestaurant(FoodbodiRetrofitHolder.getHeaders(context!!))
            .enqueue(object : Callback<FoodBodiResponse<RestaurantsResponse>> {
                override fun onFailure(call: Call<FoodBodiResponse<RestaurantsResponse>>, t: Throwable) {
                    Toast.makeText(this@GoogleMapFragment.context, "Error when get list restaurants", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<RestaurantsResponse>>,
                    response: Response<FoodBodiResponse<RestaurantsResponse>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE != response.body()?.statusCode()) {
                        Toast.makeText(
                            this@GoogleMapFragment.context,
                            response.body()?.errorMessage(),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        var list: ArrayList<Restaurant> = response.body()!!.data().restaurants
                        if (FoodBodiResponse.SUCCESS_CODE == list.size) {
                            startActivity(Intent(context, RegisterBusinessInformation::class.java))
                        } else {
                            var restaurant = list.get(0) //currently 1 user 1 restaurant

                            val intent = Intent(context, EditRestaurantActivity::class.java)
                            intent.putExtra(EditRestaurantActivity.DATA_SERIALIZE_NAME, restaurant)
                            startActivity(intent)

                        }
                    }
                }

            })
    }

    private fun gotoMenuInfoRestaurant(restaurantId: String) {
        if (CurrentUserProvider.get().isLoggedIn()) {
            val intent = Intent(context, RestaurantDetailActivity::class.java)
            intent.putExtra(RestaurantDetailActivity.RESTAURANT_ID, restaurantId)
            startActivity(intent)
        } else {
            startActivity(Intent(this.context, AuthenticateFlowActivity::class.java));
        }
    }

    private fun ensureListRestaurantView() {
        viewAdapter = MyAdapter(restaurants, object : Action<Restaurant> {
            override fun accept(data: Restaurant?) {
                gotoMenuInfoRestaurant(data?.id!!)
            }

            override fun deny(data: Restaurant?, reason: String) {
            }

        });

        val viewManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView = this@GoogleMapFragment.view!!.findViewById<RecyclerView>(R.id.recycler_restaurant_list)?.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        markRestaurantsOnMap()

    }

    private fun markRestaurantsOnMap() {
        for (r in restaurants) {
            var marker = restaurantMarkers.get(r.id)
            if (marker == null) {
                val markerOption = MarkerOptions()
                markerOption.title(r.name)
                if (r.lat != null && r.lng != null) {
                    marker = googleMap.addMarker(markerOption.position(LatLng(r.lat!!, r.lng!!)));
                    marker.tag = r.id
                    restaurantMarkers.put(r.id!!, marker);
                }
            } else {
                marker.position = LatLng(r.lat!!, r.lng!!)

            }

        }

    }




}

class MyAdapter(private val myDataset: ArrayList<Restaurant>, val itemClickHandler: Action<Restaurant>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyAdapter.MyViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_restaurant_item, parent, false)
        return MyViewHolder(itemView)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val restaurant = myDataset.get(position)
        holder.view.findViewById<TextView>(R.id.restaurant_item_name).setText(restaurant.name);
        RestaurantCategoryProvider.ensureReady(object : Action<Map<String, RestaurantCategory>> {
            override fun deny(data: Map<String, RestaurantCategory>?, reason: String) {
                Toast.makeText(holder.view.context, "Can not resolve category", Toast.LENGTH_SHORT).show()
            }

            override fun accept(data: Map<String, RestaurantCategory>?) {
                val category = data?.get(restaurant.category)
                holder.view.findViewById<TextView>(R.id.restaurant_item_category).setText(category?.name)
            }

        })

        val time = restaurant.open_hour + " ~ " + restaurant.close_hour
        holder.view.findViewById<TextView>(R.id.restaurant_item_time).setText(time)

        val imageView: ImageView = holder.view.findViewById<ImageView>(R.id.restaurant_item_photo)
        if (restaurant.photos.size > 0) {
            Picasso.get().load(restaurant.photos.get(0)).fit().centerInside().into(imageView)
        }

        imageView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                itemClickHandler.accept(restaurant)
            }

        })

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}