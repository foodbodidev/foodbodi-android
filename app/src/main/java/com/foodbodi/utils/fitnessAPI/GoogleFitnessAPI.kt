package com.foodbodi.utils.fitnessAPI

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.foodbodi.controller.ProfileFragment
import com.foodbodi.utils.Action
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.fitness.result.DataReadResponse
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
    private var fitnessOptions:FitnessOptions? = null;
    private var onPermissionGranted:Action<Any>? = null;
    private var requestCode:Int? = null;
    private var onStepCountDelta:Action<Int>? = null;

    val stepSensorListener:OnDataPointListener = object : OnDataPointListener {

        override fun onDataPoint(dataPoint: DataPoint) {
            val value = dataPoint.getValue(Field.FIELD_STEPS).asInt()
            if (onStepCountDelta != null) {
                this@GoogleFitnessAPI.onStepCountDelta!!.accept(value)
            }
        }

    }

    override fun setActivity(activity: Activity):GoogleFitnessAPI {
        this.activity = activity;
        return this;
    }
    override fun fitnessOption(option:FitnessOptions) : GoogleFitnessAPI {
        this.fitnessOptions = option;
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
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this.activity), this.fitnessOptions!!)) {
            GoogleSignIn.requestPermissions(
                this.activity!!, // your activity
                requestCode!!,
                GoogleSignIn.getLastSignedInAccount(this.activity),
                this.fitnessOptions!!
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

    override fun getStepCountDelta() : GoogleFitnessAPI {
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
    }

    override fun onStop() {
        Fitness.getSensorsClient(
            this.activity!!,
            GoogleSignIn.getLastSignedInAccount(this.activity)!!
        )
            .remove(stepSensorListener)
            .addOnCompleteListener(object : OnCompleteListener<Boolean> {
                override fun onComplete(p0: Task<Boolean>) {
                    Log.i(TAG, "Remove stepSensorListener success")
                }

            })
    }

    override fun getTodayStepCount(callback:Action<Int>) {
        val midnight: Date = Date(); midnight.hours = 0;
        var dataSource:DataSource = DataSource.Builder().setAppPackageName("com.google.android.gms")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build();
        Toast.makeText(this.activity,"get step count from $midnight to now", Toast.LENGTH_SHORT ).show()
        val dataReadRequest: DataReadRequest = DataReadRequest.Builder()
            .aggregate(dataSource, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(midnight.time, Date().time, TimeUnit.MILLISECONDS)
            .build();
        Fitness.getHistoryClient(this.activity!!, GoogleSignIn.getLastSignedInAccount(this.activity)!!)
            .readData(dataReadRequest)
            .addOnCompleteListener(object : OnCompleteListener<DataReadResponse> {
                override fun onComplete(dataReadResponse: Task<DataReadResponse>) {
                    val response: DataReadResponse? = dataReadResponse.getResult()
                    if (response != null) {
                        if (response.dataSets.size > 0) {
                            val count = response.dataSets.get(0).dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt()
                            callback.accept(count)
                        } else {
                            callback.accept(0);
                        }
                    } else {
                        callback.deny(null, "Fitness data on step count return null");

                    }
                }

            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(ex: Exception) {
                    callback.deny(null, if (ex.message != null) ex.message!! else "Fitness fail to read step count with no reason")
                }

            })
    }

    override fun getStepCountOnDate(year: Int, month: Int, day: Int, callback: Action<Int>) {

    }




}