package com.foodbodi.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import android.content.Context;
import com.foodbodi.ChatFragment;
import com.foodbodi.NameOfFoodsFragment;

class DetailRestaurantAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        if (position == 0){
            var nameOfFoodsFragment = NameOfFoodsFragment()
            nameOfFoodsFragment.restaurant_id = ""
            return nameOfFoodsFragment
        }else{
            return ChatFragment()
        }

    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}

