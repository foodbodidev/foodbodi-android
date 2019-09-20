package com.foodbodi.model

import android.content.Context
import android.widget.Toast
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.utils.Action
import com.foodbodi.utils.DateString
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.Year
import java.util.*

class LocalDailyLogDbManager {

    companion object {
        val DB_NAME:String = "foodimap-db"
        val DAILYLOG_TABLE:String = "dailylog"
        val DB_VERSION = 1;

        var cachNumOfStep = 0;

        var instance:DB? = null
        fun get(context:Context):DB? {
            if (instance == null) {
                instance = DBMaker.newFileDB(File(context.filesDir.absolutePath + DB_NAME)).make()
            }

            return instance
        }

        fun getDefaultDb() :DB? {
            return instance
        }

        fun ensureLocalDailyLogRecord(dateString: DateString, user:String):DailyLog {
            val id = DailyLog.getLocalID(dateString, user);
            var log = DailyLog()
            log.owner = user
            val hashMap:HTreeMap<String, DailyLog> =
                getDefaultDb()!!.getHashMap(DAILYLOG_TABLE)
            if (!hashMap.containsKey(id)) {
                hashMap.put(id, log)
            }
            return hashMap.get(id)!!
        }

        fun updateTodayDailyLogRecord(user:String, newValue: Int):DailyLog {
            val calendar:Calendar = Calendar.getInstance()
            val id = DailyLog.getLocalID(DateString.fromCalendar(calendar), user);
            val doc = ensureLocalDailyLogRecord(DateString.fromCalendar(calendar), user)
            doc.step = newValue
            val hashMap:HTreeMap<String, DailyLog> = getDefaultDb()!!.getHashMap(DAILYLOG_TABLE)
            hashMap.put(id, doc)
            return doc
        }

        fun getTodayStepCount(username: String):Int {
            val calendar:Calendar = Calendar.getInstance()
            val doc = ensureLocalDailyLogRecord(DateString.fromCalendar(calendar), username)
            return if (doc != null && doc.step != null ) doc.step!! else 0
        }

        fun getDailyLogOfDate(dateString: DateString, context: Context, callback: Action<DailyLog>) {
            val year = dateString.year
            val month = dateString.month
            val date = dateString.day
            FoodbodiRetrofitHolder.getService().getDailyLog(
                FoodbodiRetrofitHolder.getHeaders(context),
               year.toString(),
                month.toString(),
                date.toString()
            ).enqueue(object : Callback<FoodBodiResponse<DailyLog>> {
                override fun onFailure(call: Call<FoodBodiResponse<DailyLog>>, t: Throwable) {
                    callback.deny(null, "Can not get daily log of ${year}-${month}-${date}")
                }

                override fun onResponse(
                    call: Call<FoodBodiResponse<DailyLog>>,
                    response: Response<FoodBodiResponse<DailyLog>>
                ) {
                    if (isToday(year, month, date)) {
                        getTodayTotalStep(context, object : Action<Int> {
                            override fun accept(step: Int?) {
                                val lastCachedCount = getTodayStepCount(CurrentUserProvider.get().getUser()!!.email!!)
                                if (lastCachedCount > step!!) {
                                    cachNumOfStep = lastCachedCount
                                } else cachNumOfStep = step
                                val result = DailyLog()
                                result.calo_threshold = response.body()!!.data().calo_threshold;
                                result.step = cachNumOfStep
                                result.total_eat = response.body()!!.data.total_eat
                                callback.accept(result)

                            }

                            override fun deny(data: Int?, reason: String) {
                                callback.deny(null, reason)
                            }

                        })
                    } else {
                        val result = DailyLog()
                        result.calo_threshold = response.body()!!.data().calo_threshold;
                        result.step = response.body()!!.data.step
                        result.total_eat = response.body()!!.data.total_eat
                        callback.accept(result)
                    }

                }

            })
        }

        public fun isToday(year: Int, month: Int, date: Int):Boolean {
            val myCalendar:Calendar = Calendar.getInstance()
            return year == myCalendar.get(Calendar.YEAR)
                    && month == myCalendar.get(Calendar.MONTH)
                    && date == myCalendar.get(Calendar.DATE)
        }

        private fun getTodayTotalStep(context: Context, callback: Action<Int>) {
            val fitnessOptions: FitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build()
            val googleSignInAccount = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            Fitness.getHistoryClient(context, googleSignInAccount)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener { dataSet ->
                    if (dataSet.dataPoints.size > 0) {
                        callback.accept(dataSet.dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt())
                    } else {
                        callback.accept(0)
                    }
                }
                .addOnCanceledListener { callback.deny(null, "Can not extract total step from GoogleFit") }
        }


    }
}