package com.foodbodi

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.foodbodi.Adapters.DetailRestaurantAdapter
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.RestaurantResponse
import com.foodbodi.model.Restaurant
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantDetailActivity: AppCompatActivity(),ChatFragment.OnFragmentInteractionListener, NameOfFoodsFragment.OnFragmentInteractionListener {
    companion object {
        val RESTAURANT_ID = "restaurant_id";
    }



    var data: Restaurant? = null;
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    var eName:TextView? = null
    var eFood:TextView? = null
    var eKcal:TextView? = null
    var eTime:TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail);
        tabLayout = findViewById<TabLayout>(R.id.tabRestaurant)
        viewPager = findViewById<ViewPager>(R.id.viewPagerRestaurant)
        //Add Tab.
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Name of foods"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Chat"))
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = DetailRestaurantAdapter(this, supportFragmentManager, tabLayout!!.tabCount)
        viewPager!!.adapter = adapter


        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
//        initUI();
        getDataRestaurant();

    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

    }

    private  fun initUI(){
//        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
//        viewPager = findViewById<ViewPager>(R.id.viewPager)
//        //Add Tab.
//        tabLayout!!.addTab(tabLayout!!.newTab().setText("Home"))
//        tabLayout!!.addTab(tabLayout!!.newTab().setText("Sport"))
//        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
//        val adapter = DetailRestaurantAdapter(this, supportFragmentManager, tabLayout!!.tabCount)
//        viewPager!!.adapter = adapter
//
//        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
//
//        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                viewPager!!.currentItem = tab.position
//            }
//            override fun onTabUnselected(tab: TabLayout.Tab) {
//
//            }
//            override fun onTabReselected(tab: TabLayout.Tab) {
//
//            }
//        })
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    private  fun getDataRestaurant(){
        val restaurantId = intent.getStringExtra(RESTAURANT_ID)
        FoodbodiRetrofitHolder.getService().getRestaurant(FoodbodiRetrofitHolder.getHeaders(this), restaurantId)
            .enqueue(object : Callback<FoodBodiResponse<RestaurantResponse>> {
                override fun onFailure(call: Call<FoodBodiResponse<RestaurantResponse>>, t: Throwable) {
                    Toast.makeText(this@RestaurantDetailActivity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<RestaurantResponse>>,
                    response: Response<FoodBodiResponse<RestaurantResponse>>
                ) {
                    if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                        data = response.body()?.data()?.restaurant
                        this@RestaurantDetailActivity.updateView()
                    } else {
                        Toast.makeText(this@RestaurantDetailActivity,response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                    }
                }

            })
    }

    private fun updateView() {
        if (data != null) {
            this.eFood = findViewById<TextView>(R.id.text_restaurant_type_food);
            this.eKcal = findViewById<TextView>(R.id.text_restaurant_kcal);
            this.eName = findViewById<TextView>(R.id.text_restaurant_name);
            this.eTime = findViewById<TextView>(R.id.text_restaurant_time);
            //bind data.
            this.eFood!!.text = data!!.category;
            this.eName!!.text = data!!.name;
            this.eTime!!.text = data!!.open_hour + " ~ " + data!!.close_hour;

        } else {

        }
    }

}
