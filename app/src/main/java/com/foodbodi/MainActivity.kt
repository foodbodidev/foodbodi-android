package com.foodbodi

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.foodbodi.controller.GoogleMapFragment
import com.foodbodi.controller.ProfileFragment
import com.foodbodi.controller.ReservationFragment
import com.foodbodi.model.*
import com.foodbodi.utils.Action

class MainActivity : AppCompatActivity() {

     val LOCAL_DB_USER = "foodbodi-db-user"
     val LOCAL_DB_NAME = "foodbodi-local-db"
    private lateinit var googleMapFragment: GoogleMapFragment;
    lateinit var navView: BottomNavigationView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_fodimap -> {
                loadFragment(googleMapFragment);
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_reservation -> {
                val reservationFragment:ReservationFragment = ReservationFragment();
                loadFragment(reservationFragment);
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                if (CurrentUserProvider.get().isLoggedIn()) {
                    loadFragment(ProfileFragment());
                } else {
                    startActivity(Intent(this, AuthenticateFlowActivity::class.java))
                }
                return@OnNavigationItemSelectedListener true
            }
        }
        updateActionBar(item.itemId)
        false
    }

    fun updateActionBar(itemId:Int) {
        val actionBar = supportActionBar?.customView;
        val title:TextView? = actionBar?.findViewById<TextView>(R.id.app_title)
        val backBtn = actionBar?.findViewById<ImageButton>(R.id.btn_back)
        val logOutBtn = actionBar?.findViewById<ImageButton>(R.id.btn_logout)
        when (itemId) {
            R.id.navigation_fodimap -> {
                logOutBtn?.visibility = View.INVISIBLE
                actionBar?.visibility = View.INVISIBLE
                title?.text = resources.getText(R.string.title_fodimap)
                getActionBar()?.hide()

            }
            R.id.navigation_reservation -> {
                logOutBtn?.visibility = View.INVISIBLE
                actionBar?.visibility = View.VISIBLE
                title?.text = resources.getText(R.string.title_reservation)
                getActionBar()?.show()
            }
            R.id.navigation_profile -> {
                logOutBtn?.visibility = View.VISIBLE
                actionBar?.visibility = View.VISIBLE
                title?.text = resources.getText(R.string.title_profile)
                getActionBar()?.show()
            }
        }
    }



    @SuppressLint("WrongConstant")
    fun ensureAppBar() {
        getSupportActionBar()?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()?.setCustomView(R.layout.action_bar_menu);
        supportActionBar?.customView?.findViewById<ImageButton>(R.id.btn_logout)?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                this@MainActivity.getSharedPreferences(AuthenticateFlowActivity.PREFERENCE_NAME, Context.MODE_PRIVATE)?.edit()
                    ?.remove(AuthenticateFlowActivity.API_KEY_FIELD)?.apply()
                CurrentUserProvider.get().logout(this@MainActivity, object : Action<User> {
                    override fun accept(data: User?) {
                        navView.findViewById<View>(R.id.navigation_fodimap).performClick();
                        LoginManager.getInstance().logOut()
                    }

                    override fun deny(data: User?, reason: String) {
                    }

                })

            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalDailyLogDbManager.get(this, LOCAL_DB_USER, LOCAL_DB_NAME)
        ensureAppBar()

        RestaurantCategoryProvider.getInstance()
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, SplashScreen::class.java))

        navView = findViewById(R.id.nav_view)

        googleMapFragment = GoogleMapFragment();
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        navView.findViewById<View>(R.id.navigation_fodimap).performClick();

        if (CurrentUserProvider.get().isLoggedIn()) {
            LocalDailyLogDbManager.updateTodayDailyLogRecord(CurrentUserProvider.get().getUser()?.email!!, 0)
            syncData()
        }


    }

    fun loadFragment(fragment: Fragment) {
        var transaction:FragmentTransaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    fun syncData() {

    }



}
