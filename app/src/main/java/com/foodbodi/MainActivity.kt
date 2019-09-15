package com.foodbodi

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
        updateActionBar(item.itemId)
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
        false
    }

    fun updateActionBar(itemId:Int) {
        when (itemId) {
            R.id.navigation_fodimap -> {
                supportActionBar?.hide()
            }
            R.id.navigation_reservation -> {
                supportActionBar?.show()
                supportActionBar?.title = resources.getText(R.string.title_reservation)
            }
            R.id.navigation_profile -> {
                supportActionBar?.show()
                supportActionBar?.title = resources.getText(R.string.title_profile)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalDailyLogDbManager.get(this, LOCAL_DB_USER, LOCAL_DB_NAME)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.global_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
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
                return true
            } else -> {
            return super.onOptionsItemSelected(item)
        }
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
