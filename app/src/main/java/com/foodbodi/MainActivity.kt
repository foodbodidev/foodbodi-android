package com.foodbodi

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    companion object {
        var MY_PERMISSIONS_REQUEST_LOCATION = 99;
        var mLocationManager: LocationManager? = null;
        var locationProvider: String? = null;


        fun ensureGetLastLocation(context: Context, callback: Action<Location>) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                callback.deny(null, "GPS permission denied")
            } else {
                val location = mLocationManager?.getLastKnownLocation(locationProvider);
                callback.accept(location)
            }

        }
    }
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

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager;
        locationProvider = mLocationManager?.getBestProvider(Criteria(), true)

        LocalDailyLogDbManager.get(this, LOCAL_DB_USER, LOCAL_DB_NAME)

        RestaurantCategoryProvider.getInstance()
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, SplashScreen::class.java))

        navView = findViewById(R.id.nav_view)

        googleMapFragment = GoogleMapFragment();
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        if (checkLocationPermission()) {
            navView.findViewById<View>(R.id.navigation_fodimap).performClick();
        } else {

        }

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationProvider = mLocationManager?.getBestProvider(Criteria(), true)
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                    }
                } else {
                    //TODO :disable location feature because user don't allow

                }
            }
        }
    }

    fun checkLocationPermission():Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.title_location_permission)
                .setMessage(R.string.text_location_permission)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialogInterface, i ->
                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION
                    )
                })
                .create()
                .show()
            return false

        } else {
           return true
        }
    }
}
