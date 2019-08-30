package com.foodbodi.controller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foodbodi.AddRestaurantActivity
import com.foodbodi.AuthenticateFlowActivity
import com.foodbodi.R
import com.foodbodi.apis.*
import com.foodbodi.controller.FodiMap.RestaurantInfoMenuActivity
import com.foodbodi.model.*
import com.foodbodi.utils.Action
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//TODO : cache this View so that no need to re-create when navigate back to this
class GoogleMapFragment : Fragment(){

    private lateinit var supportMapFragment: SupportMapFragment;
    private  var recyclerView: RecyclerView? = null;
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var restaurants:ArrayList<Restaurant>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restaurants = ArrayList();

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(R.layout.fodimap, container, false);
        supportMapFragment = SupportMapFragment.newInstance();
        supportMapFragment.getMapAsync(OnMapReadyCallback {
            val latLng = LatLng(1.289545, 103.849972)
            it.addMarker(
                MarkerOptions().position(latLng)
                    .title("Singapore")
            )
            it.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        })
        getChildFragmentManager().beginTransaction().replace(R.id.map, supportMapFragment).commit();

        FoodbodiRetrofitHolder.getService().listRestaurant().enqueue(object :Callback<FoodBodiResponse<RestaurantsResponse>>{
            override fun onFailure(call: Call<FoodBodiResponse<RestaurantsResponse>>, t: Throwable) {
                System.out.println(t.stackTrace)
                Toast.makeText(context, "Can not load list of restaurants", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(
                call: Call<FoodBodiResponse<RestaurantsResponse>>,
                response: Response<FoodBodiResponse<RestaurantsResponse>>
            ) {
                var list = response.body()?.data()?.restaurants
                if (list != null) {
                    restaurants = list
                }
                ensureListRestaurantView(view)

            }

        })


        view.findViewById<FloatingActionButton>(R.id.fab_add_restaurant)!!.setOnClickListener(View.OnClickListener {
            if (CurrentUserProvider.get().isLoggedIn()) {
                invokeAddRestaurantForm()
            } else {
                val apiKey = CurrentUserProvider.get().getApiKey(context!!);
                if (apiKey != null) {
                    CurrentUserProvider.get().loadCurrentUser(
                        object : Action<User> {
                            override fun accept(data: User?) {
                                if (data == null) {
                                    invokeAuthentication()
                                } else {
                                    invokeAddRestaurantForm()
                                }
                            }

                            override fun deny(data: User?, reason: String) {
                                Toast.makeText(context, reason, Toast.LENGTH_LONG).show()
                            }

                        }, context!!)
                } else {
                    invokeAuthentication()
                }
            }
        })
        return view;
    }

    private fun invokeAuthentication() {
        startActivity(Intent(context, AuthenticateFlowActivity::class.java))
    }

    private fun invokeAddRestaurantForm() {
        startActivity(Intent(context, AddRestaurantActivity::class.java))
    }
    public fun gotoMenuInfoRestaurant(){
        startActivity(Intent(context, RestaurantInfoMenuActivity::class.java))
    }

    private fun ensureListRestaurantView(view: View) {
        viewAdapter = MyAdapter(restaurants);
        //TODO : maybe we should cache the recyclerView, to avoid rerender everytime user come back
        val viewManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_restaurant_list)?.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager


            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }


    }
}

class MyAdapter(private val myDataset: ArrayList<Restaurant>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        // create a new view
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_restaurant_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        itemView.setOnClickListener(object:View.OnClickListener{
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

        val time = restaurant.openHour + " - " + restaurant.closeHour
        holder.view.findViewById<TextView>(R.id.restaurant_item_time).setText(time)

        val imageView:ImageView = holder.view.findViewById<ImageView>(R.id.restaurant_item_photo)
        Picasso.get().load(restaurant.photo).fit().centerInside().into(imageView)


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}