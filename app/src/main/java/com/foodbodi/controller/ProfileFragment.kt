package com.foodbodi.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.foodbodi.R
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.DailyLog
import com.foodbodi.utils.Action
import com.foodbodi.model.LocalDailyLogDbManager
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.*
import java.util.*
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit


class ProfileFragment : Fragment() {
    val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE: Int = 10

    val myCalendar: Calendar = Calendar.getInstance();

    var selectedDate: DateString =
        DateString(myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DATE))

    var cachNumOfStep = 0;
    var state: State = State(0, 0, 0)


    val fitnessOptions: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()


    val onDateSetListener: DatePickerDialog.OnDateSetListener = object : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(datePickerView: DatePicker?, year: Int, month: Int, day: Int) {
            selectedDate = DateString(year, month, day)
            loadDailyLog()
        }


    };

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View = inflater.inflate(R.layout.profile_fragment, container, false);
        view.findViewById<TextView>(R.id.text_daily_log_date).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                DatePickerDialog(
                    this@ProfileFragment.requireContext(), onDateSetListener,
                    selectedDate.year, selectedDate.month, selectedDate.day
                )
                    .show()
            }
        })


        ensureFitnessService();

        return view;
    }

    private fun ensureFitnessService() {
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this.requireContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this.requireContext()),
                fitnessOptions
            );
        } else {
            this.loadDailyLog();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                this.loadDailyLog()
            }
        }
    }

    private fun loadDailyLog() {

        FoodbodiRetrofitHolder.getService().getDailyLog(
            FoodbodiRetrofitHolder.getHeaders(this@ProfileFragment.requireContext()),
            selectedDate.year.toString(),
            selectedDate.month.toString(),
            selectedDate.day.toString()
        ).enqueue(object : Callback<FoodBodiResponse<DailyLog>> {
                override fun onFailure(call: Call<FoodBodiResponse<DailyLog>>, t: Throwable) {
                    Toast.makeText(this@ProfileFragment.context, "Can not get daily log of ${selectedDate.year}-${selectedDate.month}-${selectedDate.day}", Toast.LENGTH_LONG).show();
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<DailyLog>>,
                    response: Response<FoodBodiResponse<DailyLog>>
                ) {
                    if (isToday()) {
                        getTodayTotalStep(object : Action<Int> {
                            override fun accept(step: Int?) {
                                val lastCachedCount = LocalDailyLogDbManager.getTodayStepCount(CurrentUserProvider.get().getUser()!!.email!!)
                                if (lastCachedCount > step!!) {
                                    cachNumOfStep = lastCachedCount
                                } else cachNumOfStep = step

                                state = State(
                                    response.body()!!.data().calo_threshold,
                                    cachNumOfStep,
                                    response.body()!!.data.total_eat
                                )
                                updateView()
                                ensureSensor()
                            }

                            override fun deny(data: Int?, reason: String) {
                                Toast.makeText(this@ProfileFragment.context, reason, Toast.LENGTH_LONG).show();
                            }

                        })
                    } else {
                        state = State(
                            response.body()!!.data().calo_threshold,
                            response.body()!!.data().step,
                            response.body()!!.data.total_eat
                        )
                        updateView()
                        ensureSensor()
                    }

                }

            })


    }

    private fun isToday():Boolean {
        return selectedDate.year == myCalendar.get(Calendar.YEAR)
                && selectedDate.month == myCalendar.get(Calendar.MONTH)
                && selectedDate.day == myCalendar.get(Calendar.DATE)
    }




    private fun getTodayTotalStep(callback: Action<Int>) {
        val googleSignInAccount = GoogleSignIn.getAccountForExtension(this.requireActivity(), fitnessOptions)
        Fitness.getHistoryClient(this.requireActivity(), googleSignInAccount)
            .readDailyTotal(TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                if (dataSet.dataPoints.size > 0) {
                    callback.accept(dataSet.dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt())
                } else {
                    callback.accept(0)
                }
            }
            .addOnCanceledListener { callback.deny(null, "Can not extract total step from GoogleFit") }
    }



    private fun updateView() {
        view!!.findViewById<TextView>(R.id.text_num_of_step).text = state.step.toString()
        view!!.findViewById<TextView>(R.id.text_daily_log_date).text = selectedDate.getString()
    }

    private fun ensureSensor() {
        val mListener = OnDataPointListener() {
            fun onDataPoint(dataPoint: DataPoint) {
                val value = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                updateCachedStep(value)
            }
        }
        Fitness.getSensorsClient(this.requireActivity(), GoogleSignIn.getLastSignedInAccount(this.requireContext())!!)
            .add(
                SensorRequest.Builder().setDataType(DataType.TYPE_STEP_COUNT_DELTA).setSamplingRate(
                    3,
                    TimeUnit.SECONDS
                ).build(), mListener
            )
            .addOnCompleteListener(object : OnCompleteListener<Void> {
                override fun onComplete(task: Task<Void>) {
                    if (task.isSuccessful()) {
                        print("Listener registered!");
                    } else {
                        print("Listener not registered" + task.exception);
                    }
                }

            })
    }

    private fun updateCachedStep(delta: Int) {
        val newSteps = cachNumOfStep + delta;
        state.step = newSteps
        updateView()
        LocalDailyLogDbManager.updateTodayDailyLogRecord(CurrentUserProvider.get().getUser()?.email!!, newSteps)

    }

    class DateString(var year: Int, var month: Int, var day: Int) {
        fun getString(): String {
            return StringBuilder().append(year.toString()).append("-").append(month.toString()).append("-")
                .append(day.toString()).toString()
        }
    }

    class State(var threshold: Int?, var step: Int?, var totalEat: Int?) {

    }
}