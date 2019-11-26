package com.foodbodi

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.foodbodi.Adapters.DetailRestaurantAdapter
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.apis.RestaurantResponse
import com.foodbodi.model.CaloSegment
import com.foodbodi.model.Restaurant
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class RestaurantDetailActivity: AppCompatActivity(),ChatFragment.OnFragmentInteractionListener, NameOfFoodsFragment.OnFragmentInteractionListener {
    companion object {
        val RESTAURANT_ID = "restaurant_id";
    }
    var capturedRestaurantPhotos:ArrayList<String> = ArrayList()

    var data: Restaurant? = null;
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    var eName:TextView? = null
    var eFood:TextView? = null
    var eKcal:TextView? = null
    var eTime:TextView? = null
    lateinit var photoPager:ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail);
        tabLayout = findViewById<TabLayout>(R.id.tabRestaurant)
        viewPager = findViewById<ViewPager>(R.id.viewPagerRestaurant)
        //Add Tab.
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Name of foods"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Chat"))
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
        val restaurantId = intent.getStringExtra(RESTAURANT_ID)
        val adapter = DetailRestaurantAdapter(this.supportFragmentManager, restaurantId);
        var nameOfFoodFragment:NameOfFoodsFragment = NameOfFoodsFragment()
        var chatFragment:ChatFragment = ChatFragment();
        adapter.addFragment(nameOfFoodFragment, restaurantId);
        adapter.addFragment(chatFragment, restaurantId);

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
        this.initUI()
        getDataRestaurant()

    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

    }

    private  fun initUI(){
        this.ensurePhotoBanner()
    }
    private fun ensurePhotoBanner() {
        photoPager = findViewById<ViewPager>(R.id.pager_restaurant_photo)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        photoPager.adapter = pagerAdapter
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
                ) = if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                    data = response.body()?.data()?.restaurant
                    capturedRestaurantPhotos = data!!.photos
                    for (url in capturedRestaurantPhotos) {
                        var view: RestaurantPhotoItem = RestaurantPhotoItem(url)
                        addRestaurantPhoto(view)
                    }

                    this@RestaurantDetailActivity.updateView()
                } else {
                    Toast.makeText(this@RestaurantDetailActivity,response.body()?.errorMessage(), Toast.LENGTH_LONG).show()
                }

            })
    }
    private fun addRestaurantPhoto(view:RestaurantPhotoItem) {
        val adapter = photoPager.adapter as RestaurantDetailActivity.ScreenSlidePagerAdapter
        adapter.photoViews.add(view)
        adapter.notifyDataSetChanged()
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
            this.eKcal!!.text = data!!.getAvgCalos().toString();
             data!!.getCaloSegment()
            when (data!!.getCaloSegment()) {
                CaloSegment.LOW -> {
                    this.eKcal!!.setTextColor(ContextCompat.getColor(baseContext, R.color.low_calo));
                }
                CaloSegment.MEDIUM -> {
                    this.eKcal!!.setTextColor(ContextCompat.getColor(baseContext, R.color.medium_calo));

                }
                CaloSegment.HIGH -> {
                   this.eKcal!!.setTextColor(ContextCompat.getColor(baseContext, R.color.high_calo));

                }
            }

        } else {

        }
    }

}
