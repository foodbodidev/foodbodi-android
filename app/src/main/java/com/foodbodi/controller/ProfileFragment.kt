package com.foodbodi.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.foodbodi.R
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.view.MotionEvent
import android.widget.*
import androidx.core.content.ContextCompat
import com.foodbodi.MainActivity
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.controller.Fragments.GetTodayCaloriesData
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.DailyLog
import com.foodbodi.utils.Action
import com.foodbodi.model.LocalDailyLogDbManager
import com.foodbodi.model.User
import com.foodbodi.utils.DateString
import com.foodbodi.utils.ProgressHUD
import com.foodbodi.utils.fitnessAPI.FitnessAPI
import com.foodbodi.utils.fitnessAPI.FitnessAPIFactory
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList


class ProfileFragment : Fragment() {

    val TAG = ProfileFragment::class.java.simpleName

    val myCalendar: Calendar = Calendar.getInstance();

    var selectedDate: DateString =
        DateString.fromCalendar(myCalendar)

    var state: DailyLog = DailyLog()
    var hasPermission = false;


    val onDateSetListener: DatePickerDialog.OnDateSetListener = object : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(datePickerView: DatePicker?, year: Int, month: Int, day: Int) {
            selectedDate = DateString(year, month + 1, day)
            loadDailyLog()
        }

    };

    private val pieChartClickListener: OnChartGestureListener? = object : OnChartGestureListener {
        override fun onChartGestureEnd(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
        }

        override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
        }

        override fun onChartGestureStart(
            me: MotionEvent?,
            lastPerformedGesture: ChartTouchListener.ChartGesture?
        ) {
        }

        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        }

        override fun onChartLongPressed(me: MotionEvent?) {
        }

        override fun onChartDoubleTapped(me: MotionEvent?) {
        }

        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        }

        override fun onChartSingleTapped(me: MotionEvent?) {
            this@ProfileFragment.showUserCaloConfiguration()
        }
    }

    var isRegisterSensor = false;

    override fun onStop() {
        super.onStop()
        MainActivity.fitnessAPI.onStop()
    }

    override fun onResume() {
        super.onResume()
        loadDailyLog()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view: View = inflater.inflate(R.layout.profile_fragment, container, false);
        view.findViewById<LinearLayout>(R.id.image_view_date_container).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                DatePickerDialog(
                    this@ProfileFragment.requireContext(), onDateSetListener,
                    selectedDate.year, selectedDate.month - 1, selectedDate.day
                ).show()
            }
        })

        val manufacturer = android.os.Build.MANUFACTURER
        if (manufacturer.equals("samsung")) {
            view.findViewById<TextView>(R.id.step_count_help_text).setText(R.string.step_count_help_text_samsung_health)
        } else {
            view.findViewById<TextView>(R.id.step_count_help_text).setText(R.string.step_count_help_text_google_fit)
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.fitnessAPI.setActivity(this.requireActivity())
            .readStepCount()
            .useRequestCode(MainActivity.GOOGLE_FIT_PERMISSIONS_REQUEST_CODE)
            .onPermissionGranted(object : Action<Any> {
                override fun accept(data: Any?) {
                    hasPermission = true;
                    this@ProfileFragment.loadDailyLog()
                    CurrentUserProvider.get().updateRemainCaloToEat(this@ProfileFragment.requireActivity())
                }

                override fun deny(data: Any?, reason: String) {
                    Toast.makeText(this@ProfileFragment.requireContext(), reason, Toast.LENGTH_LONG).show()
                }

            })
            .onStepCountTotal(object : Action<Int> {//Samsung Health fire this
            override fun accept(data: Int?) {
                updateStateStepCount(data!!)
            }

                override fun deny(data: Int?, reason: String) {
                    Toast.makeText(this@ProfileFragment.requireContext(), reason, Toast.LENGTH_LONG).show()

                }

            })
        MainActivity.fitnessAPI.ensurePermission()
    }


    private fun loadDailyLog() {
        if (hasPermission) {
            if (isToday()) {
                GetTodayCaloriesData(
                    CurrentUserProvider.get().getUser()?.email!!,
                    this@ProfileFragment.requireActivity()
                )
                    .getTodayData(object : Action<DailyLog> {
                        override fun accept(dailyLog: DailyLog?) {
                            if (dailyLog != null) {
                                state = dailyLog;
                                updateView();

                                if (!isRegisterSensor) {
                                    MainActivity.fitnessAPI.startListenOnStepCountDelta()
                                    isRegisterSensor = true
                                }
                            }
                        }

                        override fun deny(data: DailyLog?, reason: String) {
                            Toast.makeText(this@ProfileFragment.context, reason, Toast.LENGTH_LONG).show();
                        }

                    })
                /*LocalDailyLogDbManager.getDailyLogOfDate(selectedDate,
                this@ProfileFragment.context!!,
                object : Action<DailyLog> {
                    override fun accept(data: DailyLog?) {
                        state = data!!

                        fitnessAPI.getTodayStepCount(object : Action<Int> {
                            override fun accept(stepCount: Int?) {
                                Log.i(TAG, "Today steps $stepCount")
                                state.step = stepCount
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
                    }

                })*/
            } else {
                ProgressHUD.instance.showLoading(getActivity())
                FoodbodiRetrofitHolder.getService().getDailyLog(
                    FoodbodiRetrofitHolder.getHeaders(this@ProfileFragment.requireContext()),
                    selectedDate.year.toString(),
                    selectedDate.month.toString(),
                    selectedDate.day.toString()
                )
                    .enqueue(object : Callback<FoodBodiResponse<DailyLog>> {
                        override fun onResponse(
                            call: Call<FoodBodiResponse<DailyLog>>,
                            response: Response<FoodBodiResponse<DailyLog>>
                        ) {
                            if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                                state = response.body()!!.data();
                                updateView()
                            } else {
                                Toast.makeText(
                                    this@ProfileFragment.requireContext(),
                                    response.body()?.errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<FoodBodiResponse<DailyLog>>, t: Throwable) {
                            ProgressHUD.instance.hideLoading()
                            Toast.makeText(this@ProfileFragment.requireContext(), t.message, Toast.LENGTH_LONG).show()
                        }

                    })
            }
        }

    }

    private fun isToday():Boolean {
        return selectedDate.year == myCalendar.get(Calendar.YEAR)
                && selectedDate.month == myCalendar.get(Calendar.MONTH) + 1
                && selectedDate.day == myCalendar.get(Calendar.DATE)
    }

    private fun updateView() {
        ProgressHUD.instance.hideLoading()
        if (view != null) {
            view!!.findViewById<TextView>(R.id.text_num_of_step).text = state.getStep().toString() + " steps"
            view!!.findViewById<TextView>(R.id.text_daily_log_date).text = selectedDate.getPrettyString()

            val maximum = state.getThreshold()
            var kcaloToConsume =
                if (state.getTotalEat() > state.getBurnedCalo()) state.getTotalEat() - state.getBurnedCalo() else 0.0
            if (kcaloToConsume > state.getThreshold()) kcaloToConsume = state.getThreshold().toDouble()
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
            pieChart.setCenterTextSize(15f)

            pieChart.setRotationAngle(0f);
            // enable rotation of the chart by touch
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);
            val kcalos = ArrayList<PieEntry>()
            kcalos.add(PieEntry(remainKcalo.toFloat(), "Remain calories"))
            kcalos.add(PieEntry(kcaloToConsume.toFloat(), "Calories intake"))
            val pieData: PieData = PieData()
            val dataSet = PieDataSet(kcalos, "")
            dataSet.setColors(
                Arrays.asList(
                    ContextCompat.getColor(this.requireContext(), R.color.edit_text_color),
                    ContextCompat.getColor(this.requireContext(), R.color.colorPrimary)
                )
            )

            pieData.dataSet = dataSet

            pieChart.data = pieData
            pieChart.centerText = "${remainKcalo.toInt()} \n KCAL LEFT"
            pieChart.animateXY(0, 0)
            pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            pieChart.legend.form = Legend.LegendForm.CIRCLE
            pieChart.legend.xEntrySpace = 10f

            pieChart.onChartGestureListener = pieChartClickListener
        }
    }

    private fun updateStateStepCount(delta: Int) {
        Log.i(TAG, "updateStateStepCount $delta")
        state.step = delta
        //LocalDailyLogDbManager.updateTodayDailyLogRecord(CurrentUserProvider.get().getUser()!!, cachNumOfStep)
        if (isToday()) {
            updateView()
        }

    }

    private fun showUserCaloConfiguration() {
        var myDialog = Dialog(this.requireContext())
        myDialog.setContentView(R.layout.calo_configuration)
        val caloThresholdInput = myDialog.findViewById<TextView>(R.id.input_calo_threshold)
        val submitBtn = myDialog.findViewById<Button>(R.id.btn_submit_calo_config)
        caloThresholdInput.setText(CurrentUserProvider.get().getUser()?.daily_calo.toString())
        submitBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val currentUser = CurrentUserProvider.get().getUser();
                if (currentUser != null) {
                    currentUser.daily_calo = caloThresholdInput.text.toString().toInt()
                    val updateData = User()
                    updateData.daily_calo = currentUser.daily_calo

                    FoodbodiRetrofitHolder.getService().updateProfile(
                        FoodbodiRetrofitHolder.getHeaders(this@ProfileFragment.requireContext()),
                        updateData
                    ).enqueue(object : Callback<FoodBodiResponse<User>> {
                        override fun onFailure(call: Call<FoodBodiResponse<User>>, t: Throwable) {
                            Toast.makeText(this@ProfileFragment.requireContext(), t.message, Toast.LENGTH_LONG ).show()
                        }

                        override fun onResponse(
                            call: Call<FoodBodiResponse<User>>,
                            response: Response<FoodBodiResponse<User>>
                        ) {
                            if (FoodBodiResponse.SUCCESS_CODE == response.body()?.statusCode()) {
                                myDialog.hide()
                                state.calo_threshold = currentUser.daily_calo
                                updateView()
                                CurrentUserProvider.get().updateRemainCaloToEat(this@ProfileFragment.requireActivity())
                            } else {
                                Toast.makeText(this@ProfileFragment.requireContext(), response.body()?.errorMessage, Toast.LENGTH_LONG ).show()
                            }
                        }

                    })
                } else {

                }
            }

        })
        myDialog.show()
    }

}