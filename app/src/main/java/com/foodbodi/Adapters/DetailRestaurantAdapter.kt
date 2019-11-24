package com.foodbodi.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import android.content.Context;
import com.foodbodi.ChatFragment;
import com.foodbodi.NameOfFoodsFragment;

class DetailRestaurantAdapter(manager: FragmentManager, unitID:String) : FragmentPagerAdapter(manager) {
    private var unitID: String = ""
    private val mFragmentList:ArrayList<Fragment> = ArrayList()
    override fun getItem(position: Int): Fragment {
        if (position == 0){
            return NameOfFoodsFragment.newInstance(this.unitID)
        }else{
            return ChatFragment.newInstance(this.unitID)
        }

    }

    fun addFragment(fragment: Fragment, unitID: String) {
        mFragmentList.add(fragment)
        this.unitID = unitID
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return mFragmentList.count();
    }
}

