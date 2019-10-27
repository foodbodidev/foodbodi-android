package com.foodbodi.utils.fitnessAPI

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.foodbodi.controller.ProfileFragment
import com.foodbodi.utils.Action
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DailyTotalResult
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.fitness.result.DataReadResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

class GoogleFitnessAPI() : FitnessAPI {


    val TAG = GoogleFitnessAPI::class.java.simpleName
    private var activity:Activity? = null;
    private var fitnessOptionBuilder =  FitnessOptions.builder();
    private var onPermissionGranted:Action<Any>? = null;
    private var requestCode:Int? = null;
    private var onStepCountDelta:Action<Int>? = null;
    private var onStepCountTotal:Action<Int>? = null;
    var getStepCountTask:GetStepCountTask? = null;


    val stepSensorListener:OnDataPointListener = object : OnDataPointListener {

        override fun onDataPoint(dataPoint: DataPoint) {
            val value = dataPoint.getValue(Field.FIELD_STEPS).asInt()
            if (onStepCountDelta != null) {
                this@GoogleFitnessAPI.onStepCountDelta!!.accept(value)
            }
        }

    }

    override fun readStepCount() : GoogleFitnessAPI {
        fitnessOptionBuilder.addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        return this
    }

    override fun setActivity(activity: Activity):GoogleFitnessAPI {
        this.activity = activity;
        return this;
    }

    override fun onPermissionGranted(cb : Action<Any>) : GoogleFitnessAPI {
        this.onPermissionGranted = cb;
        return this;
    }

    override fun useRequestCode(code:Int) : GoogleFitnessAPI {
        this.requestCode = code;
        return this;
    }


    override fun ensurePermission() {
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this.activity), this.fitnessOptionBuilder.build()!!)) {
            GoogleSignIn.requestPermissions(
                this.activity!!, // your activity
                requestCode!!,
                GoogleSignIn.getLastSignedInAccount(this.activity),
                this.fitnessOptionBuilder.build()!!
                );
        } else {
            onPermissionGranted!!.accept(null)
        }
    }

    override fun consumePermissionGrantResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == this.requestCode) {
                onPermissionGranted!!.accept(null)
            }
        } else {
            onPermissionGranted!!.deny(null, "User denied")
        }
    }

    override fun onStepCountDelta(cb:Action<Int>): GoogleFitnessAPI {
        this.onStepCountDelta = cb;
        return this;
    }

    /* Sensor return so wrong data
    override fun startListenOnStepCountDelta() : GoogleFitnessAPI {
        Fitness.getSensorsClient(this.activity!!, GoogleSignIn.getLastSignedInAccount(this.activity!!)!!)
            .findDataSources(
                DataSourcesRequest.Builder()
                    .setDataSourceTypes(DataSource.TYPE_DERIVED)
                    .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA).build())
            .addOnSuccessListener(object : OnSuccessListener<List<DataSource>> {
                override fun onSuccess(dataSources: List<DataSource>?) {
                    if (dataSources!!.size > 0) {
                        for (dataSource in dataSources) {
                            Log.i(TAG, "Data source found: " + dataSource.toString());
                            Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

                            if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                                Log.i(TAG, "Data source for TYPE_STEP_COUNT_DELTA found!  Registering.");

                                Fitness.getSensorsClient(
                                    this@GoogleFitnessAPI.activity!!,
                                    GoogleSignIn.getLastSignedInAccount(this@GoogleFitnessAPI.activity!!)!!)
                                    .add(
                                        SensorRequest.Builder()
                                            .setDataSource(dataSource)
                                            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                            .setSamplingRate(3, TimeUnit.SECONDS
                                            ).build(), stepSensorListener
                                    )
                                    .addOnCompleteListener(object : OnCompleteListener<Void> {
                                        override fun onComplete(task: Task<Void>) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(this@GoogleFitnessAPI.activity, "Sensor is registered successfully", Toast.LENGTH_LONG).show()
                                                Log.i(TAG, "Listener registered!");
                                            } else {
                                                Toast.makeText(this@GoogleFitnessAPI.activity, "Sensor fails to registered", Toast.LENGTH_LONG).show()
                                                Log.i(TAG, "Listener not registered" + task.exception);
                                            }
                                        }

                                    })
                            }


                        }
                    } else {
                        Toast.makeText(this@GoogleFitnessAPI.activity, "Not found any datasource", Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(exeption: Exception) {
                    Log.i(TAG, exeption.message);
                    Toast.makeText(this@GoogleFitnessAPI.activity, exeption.message, Toast.LENGTH_SHORT).show()
                }

            })
        return this;
    }*/

    override fun startListenOnStepCountDelta() : GoogleFitnessAPI {
        this.getStepCountTask = GetStepCountTask(this.activity!!, this.onStepCountTotal!!)
        this.getStepCountTask!!.execute()
        return this
    }

    override fun onStop() {
        if (getStepCountTask != null) {
            this.getStepCountTask!!.cancel(true)
        }

        /* Not use sensor anymore
        Fitness.getSensorsClient(
            this.activity!!,
            GoogleSignIn.getLastSignedInAccount(this.activity)!!
        )
            .remove(stepSensorListener)
            .addOnCompleteListener(object : OnCompleteListener<Boolean> {
                override fun onComplete(p0: Task<Boolean>) {
                    Log.i(TAG, "Remove stepSensorListener success")
                }

            })*/
    }

    override fun getTodayStepCount(callback:Action<Int>) {
        val midnight: Date = Date(); midnight.hours = 0; midnight.minutes = 0; midnight.seconds = 0
        Toast.makeText(this.activity,"get step count from $midnight to now", Toast.LENGTH_SHORT ).show()
        val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(this.activity!!)
            .addApi(Fitness.HISTORY_API)
            .build()
        googleApiClient.connect()

        val dataReadRequest:DataReadRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(midnight.time, Date().time, TimeUnit.MILLISECONDS)
            .build()

        Fitness.HistoryApi.readData(googleApiClient ,dataReadRequest).setResultCallback(object : ResultCallback<DataReadResult> {
            override fun onResult(p0: DataReadResult) {
                if (p0.status.isSuccess) {
                    val dataPoints :List<DataPoint> = p0.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).dataPoints
                    var total = 0
                    for (point in dataPoints) {
                        total += point.getValue(Field.FIELD_STEPS).asInt()
                    }
                    callback.accept(total)
                } else {
                    callback.deny(null, p0.status.toString())
                }
            }

        }, 10, TimeUnit.SECONDS)


    }

    override fun getStepCountOnDate(year: Int, month: Int, day: Int, callback: Action<Int>) {
        val date = Date(year, month, day)
        val endOfDate = Date(year, month, day); endOfDate.hours = 23; endOfDate.minutes = 59; endOfDate.seconds = 59;

        Log.i(TAG,"Getting step count from $date to $endOfDate ...")
        val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(this.activity!!)
            .addApi(Fitness.HISTORY_API)
            .build()
        googleApiClient.connect()

        val dataReadRequest:DataReadRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(date.time, endOfDate.time, TimeUnit.MILLISECONDS)
            .build()

        Fitness.HistoryApi.readData(googleApiClient ,dataReadRequest).setResultCallback(object : ResultCallback<DataReadResult> {
            override fun onResult(p0: DataReadResult) {
                if (p0.status.isSuccess) {
                    val dataPoints :List<DataPoint> = p0.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).dataPoints
                    var total = 0
                    for (point in dataPoints) {
                        total += point.getValue(Field.FIELD_STEPS).asInt()
                    }
                    Log.i(TAG,"-- Result $total")
                    callback.accept(total)
                } else {
                    callback.deny(null, p0.status.toString())
                }
            }

        }, 10, TimeUnit.SECONDS)
    }

    override fun getStepCountOnDateSync(year: Int, month: Int, day: Int) : Int {
        val date = Date(year, month, day)
        val endOfDate = Date(year, month, day); endOfDate.hours = 23; endOfDate.minutes = 59; endOfDate.seconds = 59;

        Log.i(TAG,"Getting step count from $date to $endOfDate ...")
        val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(this.activity!!)
            .addApi(Fitness.HISTORY_API)
            .build()
        googleApiClient.connect()

        val dataReadRequest:DataReadRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(date.time, endOfDate.time, TimeUnit.MILLISECONDS)
            .build()

        val dataSet:DataSet = Fitness.HistoryApi.readData(googleApiClient ,dataReadRequest).await(10, TimeUnit.MILLISECONDS).getDataSet(DataType.TYPE_STEP_COUNT_DELTA)
        if (dataSet != null && dataSet.dataPoints.size > 0) {
            val dataPoints: List<DataPoint> = dataSet.dataPoints
            var total = 0
            for (point in dataPoints) {
                total += point.getValue(Field.FIELD_STEPS).asInt()
            }
            Log.i(TAG, "-- Result $total")
            return total
        } else {
            return 0;
        }

    }

    override fun onStepCountTotal(cb: Action<Int>): FitnessAPI {
        this.onStepCountTotal = cb
        return this;
    }

    class GetStepCountTask(val activity: Activity, val callback: Action<Int>) : AsyncTask<Void, Int, Int>() {
        override fun onProgressUpdate(vararg values: Int?) {
            var value = values.get(0)
            if (value != null) {
                this.callback.accept(value)
            } else {
                this.callback.deny(null, "Step count is null")
            }
        }

        override fun doInBackground(vararg p0: Void?): Int {
            val midnight: Date = Date(); midnight.hours = 0; midnight.minutes = 0; midnight.seconds = 0
            val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(this.activity)
                .addApi(Fitness.HISTORY_API)
                .build()
            googleApiClient.connect()

            val dataReadRequest:DataReadRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(midnight.time, Date().time, TimeUnit.MILLISECONDS)
                .build()

            while (!isCancelled) {
                val result:PendingResult<DailyTotalResult>  = Fitness.HistoryApi.readDailyTotal(googleApiClient, DataType.TYPE_STEP_COUNT_DELTA);
                val totalResult = result.await(10, TimeUnit.SECONDS)
                if (totalResult.getStatus().isSuccess()) {
                    val totalSet:DataSet? = totalResult.getTotal();
                    val total = if (totalSet == null || totalSet?.isEmpty())  0 else totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.i("GetStepCountTask", "Total steps: " + total);
                    publishProgress(total)
                } else {
                    publishProgress(null)
                    Log.w("GetStepCountTask", "There was a problem getting the step count.");
                }
                Thread.sleep(30000L)
            }
            return 0;
        }

    }






}