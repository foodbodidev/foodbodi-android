package com.foodbodi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.facebook.login.LoginManager
import com.foodbodi.controller.GoogleMapFragment
import com.foodbodi.controller.ProfileFragment
import com.foodbodi.controller.ReservationFragment
import com.foodbodi.model.*
import com.foodbodi.utils.Action
import com.foodbodi.utils.Utils
import com.foodbodi.utils.fitnessAPI.FitnessAPI
import com.foodbodi.utils.fitnessAPI.FitnessAPIFactory
import com.foodbodi.workers.SyncDailyLogWorker
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    val firestore = FirebaseFirestore.getInstance()
    private var doubleBackToExitPressedOnce = false
    private var mhandle:Handler = Handler()
    private var cancelDoubleBackToExitRunnable:Runnable = Runnable {
        doubleBackToExitPressedOnce = false;
    }
    lateinit var toolBar:View
    companion object {
        var MY_PERMISSIONS_REQUEST_LOCATION = 99;
        var mLocationManager: LocationManager? = null;
        var locationProvider: String? = null;
        val fitnessAPI: FitnessAPI = FitnessAPIFactory.getByProvider()
        val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE: Int = 10
        var backToExit = false;

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

        fun createToolbar(activity:Activity, root:ViewGroup):View {
            var toolbar: View? = activity.layoutInflater. inflate(R.layout.custom_action_bar, null, false)
            toolbar!!.findViewById<ImageButton>(R.id.action_bar_back_btn)
                .setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        backToExit = false
                        activity.onBackPressed()
                    }

                })
            toolbar.findViewById<ImageButton>(R.id.action_bar_logout)
                .setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        Utils.showAlert("Do you want to logout?", activity) {
                            activity.getSharedPreferences(AuthenticateFlowActivity.PREFERENCE_NAME, Context.MODE_PRIVATE)?.edit()
                                ?.remove(AuthenticateFlowActivity.API_KEY_FIELD)?.apply()
                            CurrentUserProvider.get().logout(activity, object : Action<User> {
                                override fun accept(data: User?) {
                                    LoginManager.getInstance().logOut()
                                    val intent:Intent = activity.intent;
                                    activity.finish()
                                    activity.startActivity(intent)
                                }

                                override fun deny(data: User?, reason: String) {
                                }

                            })
                        }
                    }

                })
            root.addView(toolbar, 0)
            return toolbar
        }
    }
    private lateinit var googleMapFragment: GoogleMapFragment;
    private  var profileFragment =  ProfileFragment()
    private var reservationFragment =  ReservationFragment()

    lateinit var navView: BottomNavigationView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        updateActionBar(item.itemId)
        when (item.itemId) {
            R.id.navigation_fodimap -> {
                loadFragment(googleMapFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_reservation -> {
                if (CurrentUserProvider.get().isLoggedIn()) {
                    loadFragment(reservationFragment)
                } else {
                    startActivity(Intent(this, AuthenticateFlowActivity::class.java))
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                if (CurrentUserProvider.get().isLoggedIn()) {
                    loadFragment(profileFragment)
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
                toolBar.visibility = View.GONE
            }
            R.id.navigation_reservation -> {
                toolBar.visibility = View.VISIBLE
                toolBar.findViewById<TextView>(R.id.action_bar_title).text = resources.getText(R.string.title_reservation)
            }
            R.id.navigation_profile -> {
                toolBar.visibility = View.VISIBLE
                toolBar.findViewById<TextView>(R.id.action_bar_title).text = resources.getText(R.string.title_profile)
            }
        }
    }

    override fun onBackPressed() {
        if (backToExit) {
            if (doubleBackToExitPressedOnce) {
                finish();
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
            mhandle.postDelayed(cancelDoubleBackToExitRunnable, 2000)
        } else {
            super.onBackPressed()
        }
        backToExit = true; //back on toolbar or back from button
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mhandle != null){
            mhandle.removeCallbacks(cancelDoubleBackToExitRunnable);
        }
    }

    override fun onResume() {
        super.onResume()
        if (CurrentUserProvider.get().isLoggedIn()) {
            firestore.collection("notifications").whereEqualTo("receiver", CurrentUserProvider.get().getUser()?.email)
                .addSnapshotListener{ snapshot, e ->
                    run {
                        if (e != null) {
                            Toast.makeText(this, "Error while fetch notification : ${e.message}", Toast.LENGTH_LONG).show()
                        } else if (snapshot != null) {
                            for (document in snapshot.documents) {
                                val notification = document.toObject(Notification::class.java);
                                Toast.makeText(this@MainActivity, notification?.message, Toast.LENGTH_LONG).show()
                            }

                        }
                    }
                }
        }
    }


    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager;
        locationProvider = mLocationManager?.getBestProvider(Criteria(), true)
        LocalDailyLogDbManager.get(this)

        RestaurantCategoryProvider.getInstance()
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, SplashScreen::class.java))

        toolBar = createToolbar(this, findViewById<LinearLayout>(R.id.container))
        navView = findViewById(R.id.nav_view)

        googleMapFragment = GoogleMapFragment();
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        if (checkLocationPermission()) {
            navView.findViewById<View>(R.id.navigation_fodimap).performClick();
        } else {

        }
        CurrentUserProvider.get().registerCallback(object : Action<User> {
            override fun accept(data: User?) {
                if (data != null) {
                    LocalDailyLogDbManager.updateTodayDailyLogRecord(data!!)
                    syncData()
                }
            }

            override fun deny(data: User?, reason: String) {
            }

        })

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.global_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                Utils.showAlert("Do you want to logout?", this) {
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

                return true

            } else -> {
            return super.onOptionsItemSelected(item)
        }
        }
    }

    fun loadFragment(fragment: Fragment) {
        var transaction:FragmentTransaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment)
        transaction.addToBackStack(null);
        transaction.commit();
    }

    fun syncData() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncDailyLogWorker>().build()
        WorkManager.getInstance(this).enqueue(syncRequest)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            MainActivity.fitnessAPI.consumePermissionGrantResult(requestCode, resultCode, data)
        }
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
