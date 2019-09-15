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
import android.graphics.Color
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.DailyLog
import com.foodbodi.utils.Action
import com.foodbodi.model.LocalDailyLogDbManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
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
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {
    val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE: Int = 10

    val myCalendar: Calendar = Calendar.getInstance();

    var selectedDate: DateString =
        DateString(myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DATE))

    var cachNumOfStep = 0;
    var state: DailyLog = DailyLog()


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

    var stepSensorListener:OnDataPointListener? = null

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

                                state.calo_threshold = response.body()!!.data().calo_threshold;
                                state.step = cachNumOfStep
                                state.total_eat = response.body()!!.data.total_eat

                                updateView()
                                ensureSensor()
                            }

                            override fun deny(data: Int?, reason: String) {
                                Toast.makeText(this@ProfileFragment.context, reason, Toast.LENGTH_LONG).show();
                            }

                        })
                    } else {
                        state.calo_threshold = response.body()!!.data().calo_threshold
                        state.step = response.body()!!.data().step
                        state.total_eat = response.body()!!.data.total_eat
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

        val maximum = state.getThreshold()
        val kcaloToConsume = state.getTotalEat() - state.getStep()
        val remainKcalo = maximum - kcaloToConsume

        val pieChart:PieChart = view!!.findViewById(R.id.pie_chart_kcalo)
        pieChart.setUsePercentValues(true)
        pieChart.setDrawSlicesUnderHole(true)
        pieChart.description = null
        pieChart.setHoleColor(ContextCompat.getColor(this.requireContext(), R.color.White));

        pieChart.setTransparentCircleColor(ContextCompat.getColor(this.requireContext(), R.color.White));

        pieChart.setHoleRadius(40f);
        pieChart.centerTextRadiusPercent = 0.5f

        pieChart.setDrawCenterText(true);
        pieChart.setCenterTextSize(resources.getDimension(R.dimen.text_medium))

        pieChart.setRotationAngle(0f);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart
        val kcalos = ArrayList<PieEntry>()
        kcalos.add(PieEntry(remainKcalo.toFloat(), "Remain calories"))
        kcalos.add(PieEntry(kcaloToConsume.toFloat(), "Calories intake"))
        val pieData:PieData = PieData()
        val dataSet = PieDataSet(kcalos, "Calories (kcalo)")
        dataSet.setColors(Arrays.asList(Color.GRAY, ContextCompat.getColor(this.requireContext(), R.color.colorPrimary)))
        pieData.dataSet = dataSet

        pieChart.data = pieData
        pieChart.centerText = "$remainKcalo kcal left"
        pieChart.animateXY(500, 500)
    }

    private fun ensureSensor() {
        if (stepSensorListener == null) {
            stepSensorListener = OnDataPointListener() {
                fun onDataPoint(dataPoint: DataPoint) {
                    val value = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                    updateCachedStep(value)
                }
            }
            Fitness.getSensorsClient(
                this.requireActivity(),
                GoogleSignIn.getLastSignedInAccount(this.requireContext())!!
            )
                .add(
                    SensorRequest.Builder().setDataType(DataType.TYPE_STEP_COUNT_DELTA).setSamplingRate(
                        3,
                        TimeUnit.SECONDS
                    ).build(), stepSensorListener
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
    }

    private fun updateCachedStep(delta: Int) {
        val newSteps = cachNumOfStep + delta;
        state.step = newSteps
        LocalDailyLogDbManager.updateTodayDailyLogRecord(CurrentUserProvider.get().getUser()?.email!!, newSteps)
        if (isToday()) {
            updateView()
        }

    }

    class DateString(var year: Int, var month: Int, var day: Int) {
        fun getString(): String {
            return StringBuilder().append(year.toString()).append("-").append(month.toString()).append("-")
                .append(day.toString()).toString()
        }
    }

}