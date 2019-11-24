package com.foodbodi.utils.fitnessAPI

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.foodbodi.utils.Action
import com.foodbodi.utils.Logger
import com.google.android.gms.fitness.FitnessOptions
import android.content.DialogInterface
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.foodbodi.MainActivity
import com.samsung.android.sdk.healthdata.*
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey
import java.util.*
import kotlin.collections.HashSet
import com.samsung.android.sdk.shealth.Shealth
import androidx.core.content.ContextCompat.startActivity
import com.samsung.android.sdk.shealth.tracker.TrackerManager
import com.samsung.android.sdk.shealth.tracker.TrackerInfo


class SamsungFitnessAPI : FitnessAPI {


    private var activity:Activity? = null;
    private var context:Context? = null;
    private var healthDataStore:HealthDataStore? = null;
    private var connectionError:HealthConnectionErrorResult? = null
    private var permissionKeys:HashSet<HealthPermissionManager.PermissionKey> = HashSet<HealthPermissionManager.PermissionKey>()
    private val TAG = SamsungFitnessAPI::class.java.simpleName
    private var onPermissionGranted:Action<Any>? = null;
    private var onStepCountDelta:Action<Int>? = null;
    private var onStepCountTotal:Action<Int>? = null;
    private val STORE_URL = "market://details?id=com.sec.android.app.shealth"
    private val TRACKER_ID = "foodbodi.com"
    var getStepCountTask:GetStepCountTask? = null;



    private var mConnectionListener:HealthDataStore.ConnectionListener = object : HealthDataStore.ConnectionListener {
        override fun onConnected() {
            Logger.info(TAG, "Samsung HeathDaraStore connected", this@SamsungFitnessAPI.activity!!)
            val pmsManager = HealthPermissionManager(healthDataStore)

            try {
                // Check whether the permissions that this application needs are acquired
                val resultMap = pmsManager.isPermissionAcquired(permissionKeys)

                if (resultMap.containsValue(java.lang.Boolean.FALSE)) {
                    // Request the permission for reading step counts if it is not acquired
                    pmsManager.requestPermissions(permissionKeys, this@SamsungFitnessAPI.activity).setResultListener(permissionListener)
                } else {
                   onPermissionGranted!!.accept(null)
                }
            } catch (e: Exception) {
                Logger.error(TAG, e.javaClass.name + " - " + e.message, this@SamsungFitnessAPI.activity!!)
                Logger.error(TAG, "Permission setting fails.", this@SamsungFitnessAPI.activity!!)
                onPermissionGranted!!.deny(e, e.message!!)
            }

        }

        override fun onConnectionFailed(error: HealthConnectionErrorResult?) {
            Logger.error(TAG, error.toString(), this@SamsungFitnessAPI.activity!!)
            showConnectionFailureDialog(error!!);
        }

        override fun onDisconnected() {
            Logger.warning(TAG, "Samsung HeathDaraStore disconnected", this@SamsungFitnessAPI.activity!!)

        }

    }

    private var permissionListener = object : HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult> {
        override fun onResult(result: HealthPermissionManager.PermissionResult?) {
            Logger.info(TAG, "Permission callback is received.", this@SamsungFitnessAPI.activity!!)
            val resultMap = result!!.getResultMap()

            if (resultMap.containsValue(java.lang.Boolean.FALSE)) {
                // Requesting permission fails
                onPermissionGranted!!.deny(null, "User denied permission")
            } else {
                // Get the current step count and display it
                onPermissionGranted!!.accept(null)
            }
        }

    }

    override fun readStepCount(): FitnessAPI {
        permissionKeys.add(HealthPermissionManager.PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ))
        return this;
    }


    override fun setActivity(activity: Activity): FitnessAPI {
        this.activity = activity;
        this.setContext(activity.baseContext)
        return this
    }

    override fun setContext(context: Context): FitnessAPI {
        this.context = context;
        return this;
    }

    override fun onPermissionGranted(cb: Action<Any>): FitnessAPI {
        this.onPermissionGranted = cb
        return this
    }

    override fun useRequestCode(code: Int): FitnessAPI {
        return this
    }

    override fun ensurePermission() {
        healthDataStore = HealthDataStore(this@SamsungFitnessAPI.activity, mConnectionListener)
        healthDataStore!!.connectService()

    }

    override fun consumePermissionGrantResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    override fun onStepCountDelta(cb: Action<Int>): FitnessAPI {
        this.onStepCountDelta = cb
        return this
    }

    override fun onStepCountTotal(cb: Action<Int>): FitnessAPI {
        this.onStepCountTotal = cb
        return this
    }

    override fun startListenOnStepCountDelta(): FitnessAPI {
        val mHandler:Handler = Handler(Looper.getMainLooper());

        this.getStepCountTask = GetStepCountTask(this.healthDataStore!!, this.onStepCountTotal!!)
        this.getStepCountTask!!.execute()
        return this
    }

    override fun onStop() {
        if (this.getStepCountTask != null) {
            this.getStepCountTask!!.cancel(true)
        }
        if (healthDataStore != null) {
            healthDataStore!!.disconnectService()
        }
    }

    override fun getTodayStepCount(callback: Action<Int>) {
        val today = Date(); today.hours = 0; today.minutes = 0; today.seconds = 0;
        // Create a filter for today's steps from all source devices
        val filter:HealthDataResolver.Filter = HealthDataResolver.Filter.and(
                HealthDataResolver.Filter.eq("day_time",today.time),
        HealthDataResolver.Filter.eq("source_type", -2));

        val request:HealthDataResolver.ReadRequest = HealthDataResolver.ReadRequest.Builder()
            .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
            .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET, today.time, Date().time)
            .build()
        val mRdResult:HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>  = object : HealthResultHolder.ResultListener<HealthDataResolver.ReadResult> {
            override fun onResult(result: HealthDataResolver.ReadResult) {
                var totalCount:Int = 0;

                try {
                    val iterator:Iterator<HealthData> = result.iterator();
                    if (iterator.hasNext()) {
                        val data:HealthData = iterator.next();
                        totalCount = data.getInt("count");
                    }
                    callback.accept(totalCount)
                } catch (e:java.lang.Exception) {
                    callback.deny(null, "Process Samsung read result fail")
                } finally {
                    result.close();
                }
            };
        }
        val mResolver = HealthDataResolver(healthDataStore, null);
        try {
            mResolver.read(request).setResultListener(mRdResult)
        } catch ( e:Exception) {
            Logger.error(TAG, "Read Samsung step count fail ${e.message}", this@SamsungFitnessAPI.context!!)
        }
    }

    override fun getStepCountOnDate(year: Int, month: Int, day: Int, callback: Action<Int>) {
        val date = Date(year, month, day)
        val endOfDate = Date(year, month, day); endOfDate.hours = 23; endOfDate.minutes = 59; endOfDate.seconds = 59;
        val healhDataResolver:HealthDataResolver = HealthDataResolver(healthDataStore, null)
        val request:HealthDataResolver.ReadRequest = HealthDataResolver.ReadRequest.Builder()
            .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
            .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET, date.time, endOfDate.time)
            .build()

        val mRdResult:HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>  = object : HealthResultHolder.ResultListener<HealthDataResolver.ReadResult> {
            override fun onResult(result: HealthDataResolver.ReadResult) {
                try {
                    val iterator: Iterator<HealthData> = result.iterator();

                    var total = 0;
                    while (iterator.hasNext()) {
                        val data: HealthData = iterator.next();
                        val stepDelta = data.getInt(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                        total += stepDelta
                    }
                    callback.accept(total)
                } catch (e: Exception) {
                    callback.deny(null, "Process Samsung read result fail")
                } finally {
                    result.close()
                }
            };
        }
        try {
            healhDataResolver.read(request).setResultListener(mRdResult)
        } catch (e:java.lang.Exception) {
            Log.e(TAG, "Read Samsung step count fail ${e.message}")
        }
    }

    override fun getStepCountOnDateSync(year: Int, month: Int, day: Int): Int {
        val r: RunnableCallback<Int> = object : RunnableCallback<Int>() {
            override fun run() {
                getStepCountOnDate(year, month, day, object : Action<Int> {
                    override fun accept(data: Int?) {
                        if (data != null) value = data;
                        else {
                            Log.e(TAG, "Read Samsung step count fail : data is null")
                        }
                    }

                    override fun deny(data: Int?, reason: String) {
                        Log.e(TAG, "Read Samsung step count fail : $reason")
                    }

                })
            }

        }
        val thread:Thread = Thread(r)

        thread.start();
        thread.join();
        return if (r.value == null) 0 else r.value!!

    }

    private fun showConnectionFailureDialog(error: HealthConnectionErrorResult) {

        val alert = AlertDialog.Builder(this@SamsungFitnessAPI.activity)
        connectionError = error
        var message = "Connection with Samsung Health is not available"

        if (connectionError!!.hasResolution()) {
            when (error.errorCode) {
                HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED -> message = "Please install Samsung Health"
                HealthConnectionErrorResult.OLD_VERSION_PLATFORM -> message = "Please upgrade Samsung Health"
                HealthConnectionErrorResult.PLATFORM_DISABLED -> message = "Please enable Samsung Health"
                HealthConnectionErrorResult.USER_AGREEMENT_NEEDED -> message = "Please agree with Samsung Health policy"
                else -> message = "Please make Samsung Health available"
            }
        }

        alert.setMessage(message)

        alert.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
            if (connectionError!!.hasResolution()) {
                connectionError!!.resolve(this@SamsungFitnessAPI.activity)
            }
        })

        if (error.hasResolution()) {
            alert.setNegativeButton("Cancel", null)
        }

        alert.show()
    }

    abstract class RunnableCallback<T> : Runnable {
        var value: T? = null;

    }

    class GetStepCountTask(val heathDataStore: HealthDataStore, val callback: Action<Int>) : AsyncTask<Void, Int, Int>() {
        override fun onProgressUpdate(vararg values: Int?) {
            var value = values.get(0)
            if (value != null) {
                this.callback.accept(value)
            } else {
                this.callback.deny(null, "Step count is null")
            }
        }

        override fun doInBackground(vararg p0: Void?): Int {

            val today = Date(); today.hours = 0; today.minutes = 0; today.seconds = 0;
            // Create a filter for today's steps from all source devices
            val request:HealthDataResolver.ReadRequest = HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET, today.time, Date().time)
                .build()


            val mResolver = HealthDataResolver(this.heathDataStore, null);
            var totalCount = 0;
            while(!isCancelled) {
                try {
                    val result:HealthDataResolver.ReadResult = mResolver.read(request).await()
                    try {
                        val iterator:Iterator<HealthData> = result.iterator();
                        if (iterator.hasNext()) {
                            val data:HealthData = iterator.next();
                            totalCount = data.getInt("count");
                        }
                        publishProgress(totalCount)
                    } catch (e:java.lang.Exception) {
                        Log.d("GetStepCountTask", "Process Samsung read result fail: ${e.message}")
                        publishProgress(null)
                    } finally {
                        result.close();
                    }

                } catch ( e:Exception) {
                    Log.d("GetStepCountTask", "Read Samsung step count fail ${e.message}")
                }

                Thread.sleep(30000L)
            }

            return 0;
        }

    }

}