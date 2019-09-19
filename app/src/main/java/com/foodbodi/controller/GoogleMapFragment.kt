package com.foodbodi.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//TODO : cache this View so that no need to re-create when navigate back to this
class GoogleMapFragment : Fragment(), LocationListener {

    private lateinit var supportMapFragment: SupportMapFragment;
    private var recyclerView: RecyclerView? = null;
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var restaurants: ArrayList<Restaurant>
    private var restaurantMarkers: HashMap<String, Marker> = HashMap()
    private var userCurrentLocation: Marker? = null
    private lateinit var mLocationManager: LocationManager
    private lateinit var googleMap: GoogleMap

    var MY_PERMISSIONS_REQUEST_LOCATION = 99;
    val firestore = FirebaseFirestore.getInstance()
    var locationProvider: String? = null;


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurants = ArrayList()
        mLocationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager;

    }

    override fun onResume() {
        super.onResume()
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

        val loadRestauranAction: Action<Location> = object : Action<Location> {
            override fun accept(data: Location?) {
                loadRestaurant(data)
                if (data != null) {
                    moveCamera(data.latitude, data.longitude, 12.5f)
                }
            }

            override fun deny(data: Location?, reason: String) {
                print(reason)
            }
        }

        val markCurrentLocation: Action<Location> = object : Action<Location> {
            override fun accept(data: Location?) {
                userCurrentLocation =
                    googleMap.addMarker(MarkerOptions().position(LatLng(data!!.latitude, data.longitude)).title("You"))
            }

            override fun deny(data: Location?, reason: String) {
                Toast.makeText(this@GoogleMapFragment.context, reason, Toast.LENGTH_LONG).show()
            }

        }

        val afterCheckPermissionLocationAction: Action<Any> = object : Action<Any> {
            override fun accept(data: Any?) {
                ensureGetCurrentLocation(loadRestauranAction);
                ensureGetCurrentLocation(markCurrentLocation);
            }

            override fun deny(data: Any?, reason: String) {
            }

        }

        supportMapFragment = SupportMapFragment.newInstance();
        supportMapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it
            restaurantMarkers.clear();
            checkLocationPermission(afterCheckPermissionLocationAction);
            googleMap.setOnMarkerClickListener { marker ->
                if (marker.tag != null) {
                    val restaurantId =
                        marker.tag as String;
                    this@GoogleMapFragment.gotoMenuInfoRestaurant(restaurantId);
                }
                false
            }

        });

        getChildFragmentManager().beginTransaction().replace(R.id.map, supportMapFragment).commit();


        view.findViewById<FloatingActionButton>(R.id.fab_add_restaurant)!!.setOnClickListener(View.OnClickListener {
            if (CurrentUserProvider.get().isLoggedIn()) {
                invokeAddRestaurantForm()
            } else {
                invokeAuthentication()
            }
        })
        return view;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this.context!!,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        ensureGetCurrentLocation(object : Action<Location> {
                            @SuppressLint("MissingPermission")
                            override fun accept(data: Location?) {
                                mLocationManager.requestLocationUpdates(
                                    locationProvider,
                                    3000,
                                    7f,
                                    this@GoogleMapFragment
                                )

                            }

                            override fun deny(data: Location?, reason: String) {
                                Toast.makeText(this@GoogleMapFragment.context, reason, Toast.LENGTH_LONG).show()
                            }

                        })
                    }

                } else {
                    //TODO :disable location feature because user don't allow

                }
            }
        }
    }

    private fun loadRestaurant(location: Location?) {
        if (location != null) {
            findNearbyRestaurant(
                GoogleMapUtils.LatLng(location.latitude, location.longitude),
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
        print("User current location : " + currentLocation.toString())
        if (userCurrentLocation != null && currentLocation?.latitude != null) {
            userCurrentLocation!!.position = LatLng(currentLocation.latitude, currentLocation.longitude)
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

    fun ensureGetCurrentLocation(callback: Action<Location>) {
        if (ActivityCompat.checkSelfPermission(
                this.context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this.context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback.deny(null, "GPS permission denied")
        } else {
            val netWorklocation: Location? = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            val gpsLocation: Location? = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            val passiveLocation: Location? = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (gpsLocation != null) {
                locationProvider = LocationManager.GPS_PROVIDER;
                callback.accept(gpsLocation)
            } else if (netWorklocation != null) {
                locationProvider = LocationManager.NETWORK_PROVIDER
                callback.accept(netWorklocation)
            } else if (passiveLocation != null) {
                locationProvider = LocationManager.PASSIVE_PROVIDER
                callback.accept(passiveLocation);
            } else {
                callback.deny(null, "Can not get the current location")
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
        viewAdapter = MyAdapter(restaurants);
        //TODO : maybe we should cache the recyclerView, to avoid rerender everytime user come back
        val viewManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView = this@GoogleMapFragment.view!!.findViewById<RecyclerView>(R.id.recycler_restaurant_list)?.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager


            // specify an viewAdapter (see also next example)
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

    fun checkLocationPermission(callback: Action<Any>) {
        if (ContextCompat.checkSelfPermission(
                this.context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this.activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this.context)
                    .setTitle(R.string.title_location_permission)
                    .setMessage(R.string.text_location_permission)
                    .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialogInterface, i ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this.activity!!,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                        callback.accept(null)
                    })
                    .create()
                    .show()


            } else {
                val alertDialog = AlertDialog.Builder(this.context);

                alertDialog.setTitle("GPS is settings");
                alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
                alertDialog.setPositiveButton("Settings") { p0, p1 ->
                    val intent = Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.google.android.apps.maps")
                    this@GoogleMapFragment.startActivityForResult(intent, MY_PERMISSIONS_REQUEST_LOCATION);
                };

                alertDialog.setNegativeButton(
                    "Cancel"
                ) { dialog, p1 -> dialog?.cancel(); };
                alertDialog.show();
            }
        } else {
           ensureGetCurrentLocation(object : Action<Location> {
               override fun accept(data: Location?) {
                   callback.accept(data)
               }

               override fun deny(data: Location?, reason: String) {
                       val alertDialog = AlertDialog.Builder(this@GoogleMapFragment.context);

                       alertDialog.setTitle("GPS is settings");
                       alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
                       alertDialog.setPositiveButton("Settings") { p0, p1 ->
                           val intent = Intent(Intent.ACTION_VIEW);
                           intent.setPackage("com.google.android.apps.maps")
                           this@GoogleMapFragment.startActivityForResult(intent, MY_PERMISSIONS_REQUEST_LOCATION);
                       };

                       alertDialog.setNegativeButton(
                           "Cancel"
                       ) { dialog, p1 -> dialog?.cancel(); };
                       alertDialog.show();

               }

           })
        }
    }


}

class MyAdapter(private val myDataset: ArrayList<Restaurant>) :
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
        // set the view's size, margins, paddings and layout parameters
        itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {

            }
        })
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

        val time = restaurant.open_hour + " - " + restaurant.close_hour
        holder.view.findViewById<TextView>(R.id.restaurant_item_time).setText(time)

        val imageView: ImageView = holder.view.findViewById<ImageView>(R.id.restaurant_item_photo)
        if (restaurant.photos.size > 0) {
            Picasso.get().load(restaurant.photos.get(0)).fit().centerInside().into(imageView)
        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}