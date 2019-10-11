package com.foodbodi.model

import android.content.Context
import android.util.Log
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
        val TAG = LocalDailyLogDbManager::class.java.simpleName
        val DB_NAME:String = "foodimap-db"
        val DAILYLOG_TABLE:String = "dailylog"

        var cachNumOfStep = 0;

        var instance:DB? = null
        fun get(context:Context):DB? {
            if (instance == null) {
                val path = context.filesDir.absolutePath +"/" + DB_NAME
                Log.i(TAG, "Opening db at $path"  )
                instance = DBMaker.newFileDB(File(path)).closeOnJvmShutdown().transactionDisable().make()
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
                Log.i(TAG, "Add dailylog $id")
                hashMap.put(id, log)
                instance?.commit()
            } else {
                Log.i(TAG,"Dailylog $id exists")
            }
            return hashMap.get(id)!!
        }

        fun updateTodayDailyLogRecord(user:User) : DailyLog{
            return Companion.updateTodayDailyLogRecord(user, null)
        }

        fun updateTodayDailyLogRecord(user:User, newValue: Int?):DailyLog {
            val calendar:Calendar = Calendar.getInstance()
            val id = DailyLog.getLocalID(DateString.fromCalendar(calendar), user.email!!);
            val doc = ensureLocalDailyLogRecord(DateString.fromCalendar(calendar), user.email!!)
            Log.i(TAG, "Update step $id to ${newValue}")
            if (newValue != null) {
                doc.step = newValue
            }
            if (doc.calo_threshold == null) {
                doc.calo_threshold = user.daily_calo
                Log.i(TAG, "Update calo threshold $id to ${doc.calo_threshold}")
            }
            val hashMap:HTreeMap<String, DailyLog> = getDefaultDb()!!.getHashMap(DAILYLOG_TABLE)
            hashMap.put(id, doc)
            instance?.commit()
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

                    val result = DailyLog()
                    result.calo_threshold = response.body()!!.data().calo_threshold;
                    result.step = response.body()!!.data.step
                    result.total_eat = response.body()!!.data.total_eat
                    callback.accept(result)

                }

            })
        }

        public fun isToday(year: Int, month: Int, date: Int):Boolean {
            val myCalendar:Calendar = Calendar.getInstance()
            return year == myCalendar.get(Calendar.YEAR)
                    && month == myCalendar.get(Calendar.MONTH)
                    && date == myCalendar.get(Calendar.DATE)
        }


    }
}