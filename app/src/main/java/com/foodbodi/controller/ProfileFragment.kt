package com.foodbodi.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.foodbodi.R
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
import com.foodbodi.utils.DateString
import com.foodbodi.utils.fitnessAPI.FitnessAPI
import com.foodbodi.utils.fitnessAPI.FitnessAPIFactory
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {
    val TAG = ProfileFragment::class.java.simpleName
    val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE: Int = 10

    val myCalendar: Calendar = Calendar.getInstance();

    var selectedDate: DateString =
        DateString.fromCalendar(myCalendar)

    var cachNumOfStep = 0;
    var state: DailyLog = DailyLog()


    val onDateSetListener: DatePickerDialog.OnDateSetListener = object : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(datePickerView: DatePicker?, year: Int, month: Int, day: Int) {
            selectedDate = DateString(year, month, day)
            loadDailyLog()
        }

    };

    val fitnessAPI:FitnessAPI = FitnessAPIFactory.getByProvider()
    var isRegisterSensor = false;

    override fun onStop() {
        super.onStop()
        fitnessAPI.onStop()
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View = inflater.inflate(R.layout.profile_fragment, container, false);
        view.findViewById<TextView>(R.id.text_daily_log_date).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                DatePickerDialog(
                    this@ProfileFragment.requireContext(), onDateSetListener,
                    selectedDate.year, selectedDate.month, selectedDate.day
                ).show()
            }
        })

        fitnessAPI.setActivity(this.requireActivity())
            .readStepCount()
            .useRequestCode(GOOGLE_FIT_PERMISSIONS_REQUEST_CODE)
            .onPermissionGranted(object : Action<Any> {
                override fun accept(data: Any?) {
                    this@ProfileFragment.loadDailyLog()
                }

                override fun deny(data: Any?, reason: String) {
                    Toast.makeText(this@ProfileFragment.requireContext(), reason, Toast.LENGTH_LONG).show()
                }

            })
            .onStepCountDelta(object : Action<Int> {
                override fun accept(data: Int?) {
                    updateCachedStep(data!!)
                }

                override fun deny(data: Int?, reason: String) {
                    Toast.makeText(this@ProfileFragment.requireContext(), reason, Toast.LENGTH_LONG).show()

                }

            })
        fitnessAPI.ensurePermission()

        return view;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fitnessAPI.consumePermissionGrantResult(requestCode, resultCode, data)
    }

    private fun loadDailyLog() {

        if (isToday()) {
            LocalDailyLogDbManager.getDailyLogOfDate(selectedDate,
                this@ProfileFragment.context!!,
                object : Action<DailyLog> {
                    override fun accept(data: DailyLog?) {
                        state = data!!

                        fitnessAPI.getTodayStepCount(object : Action<Int> {
                            override fun accept(stepCount: Int?) {
                                Toast.makeText(this@ProfileFragment.requireContext(), "Today steps $stepCount", Toast.LENGTH_SHORT).show()
                                if (state.getStep() == null || state.getStep() < stepCount!!) {
                                    state.step = stepCount
                                }
                                cachNumOfStep = data.getStep();
                                updateView()

                                if (!isRegisterSensor) {
                                    fitnessAPI.startListenOnStepCountDelta()
                                    isRegisterSensor = true
                                }
                            }

                            override fun deny(data: Int?, reason: String) {
                                Toast.makeText(this@ProfileFragment.requireContext(), reason, Toast.LENGTH_LONG).show()
                            }

                        })
                    }

                    override fun deny(data: DailyLog?, reason: String) {
                        Toast.makeText(this@ProfileFragment.context, reason, Toast.LENGTH_LONG).show();
                    }

                })
        } else {
            FoodbodiRetrofitHolder.getService().getDailyLog(FoodbodiRetrofitHolder.getHeaders(this@ProfileFragment.requireContext()), selectedDate.year.toString(), selectedDate.month.toString(), selectedDate.day.toString())
                .enqueue(object : Callback<FoodBodiResponse<DailyLog>> {
                    override fun onResponse(
                        call: Call<FoodBodiResponse<DailyLog>>,
                        response: Response<FoodBodiResponse<DailyLog>>
                    ) {
                        if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                            state = response.body()!!.data();
                            updateView()
                        } else {
                            Toast.makeText(this@ProfileFragment.requireContext(), response.body()?.errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<FoodBodiResponse<DailyLog>>, t: Throwable) {
                        Toast.makeText(this@ProfileFragment.requireContext(), t.message, Toast.LENGTH_LONG).show()
                    }

                })
        }

    }

    private fun isToday():Boolean {
        return selectedDate.year == myCalendar.get(Calendar.YEAR)
                && selectedDate.month == myCalendar.get(Calendar.MONTH) + 1
                && selectedDate.day == myCalendar.get(Calendar.DATE)
    }

    private fun updateView() {
        if (view != null) {
            view!!.findViewById<TextView>(R.id.text_num_of_step).text = state.getStep().toString() + " steps"
            view!!.findViewById<TextView>(R.id.text_daily_log_date).text = selectedDate.getString()

            val maximum = state.getThreshold()
            val kcaloToConsume = if (state.getTotalEat() > state.getBurnedCalo()) state.getTotalEat() - state.getBurnedCalo() else 0.0
            val remainKcalo = maximum - kcaloToConsume

            val pieChart: PieChart = view!!.findViewById(R.id.pie_chart_kcalo)
            pieChart.setUsePercentValues(false)
            pieChart.setDrawSlicesUnderHole(true)
            pieChart.setDrawEntryLabels(false)


            pieChart.description = null
            pieChart.setHoleColor(ContextCompat.getColor(this.requireContext(), R.color.White));

            pieChart.setTransparentCircleColor(ContextCompat.getColor(this.requireContext(), R.color.White));

            pieChart.setHoleRadius(60f);

            pieChart.setDrawCenterText(true);

            pieChart.setRotationAngle(0f);
            // enable rotation of the chart by touch
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);
            val kcalos = ArrayList<PieEntry>()
            kcalos.add(PieEntry(remainKcalo.toFloat(), "Remain calories"))
            kcalos.add(PieEntry(kcaloToConsume.toFloat(), "Calories intake"))
            val pieData: PieData = PieData()
            val dataSet = PieDataSet(kcalos, "Calories (kcalo)")
            dataSet.setColors(Arrays.asList(
                ContextCompat.getColor(this.requireContext(), R.color.edit_text_color),
                    ContextCompat.getColor(this.requireContext(), R.color.colorPrimary)
                )
            )
            pieData.dataSet = dataSet

            pieChart.data = pieData
            pieChart.centerText = "$remainKcalo kcal left"
            pieChart.animateXY(0, 0)
        }
    }

    private fun updateCachedStep(delta: Int) {
        Toast.makeText(this@ProfileFragment.requireContext(), delta.toString(), Toast.LENGTH_SHORT).show()
        cachNumOfStep += delta;
        state.step = cachNumOfStep
        LocalDailyLogDbManager.updateTodayDailyLogRecord(CurrentUserProvider.get().getUser()!!, cachNumOfStep)
        if (isToday()) {
            updateView()
        }

    }

}