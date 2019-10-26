package com.foodbodi.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.foodbodi.apis.FoodBodiResponse
import com.foodbodi.apis.FoodbodiRetrofitHolder
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.DailyLog
import com.foodbodi.model.LocalDailyLogDbManager
import com.foodbodi.utils.DateString
import com.foodbodi.utils.fitnessAPI.FitnessAPIFactory
import org.mapdb.DB
import org.mapdb.HTreeMap
import retrofit2.Response
import java.util.*

class SyncDailyLogWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    val TAG = SyncDailyLogWorker::class.java.simpleName
    var fitnessAPI = FitnessAPIFactory.getByProvider();

    override fun doWork(): Result {
        Log.i(TAG,"Begin sync up daily log...")
        val db:DB? = LocalDailyLogDbManager.get(this.applicationContext)
        if (db != null) {
            val hashMap: HTreeMap<String, DailyLog> =
                LocalDailyLogDbManager.getDefaultDb()!!.getHashMap(LocalDailyLogDbManager.DAILYLOG_TABLE)

            val keys:Set<String> = hashMap.keys
            if (keys.size == 0) {
                Log.i(TAG, "Nothing to sync")
                return Result.success();
            }

            val user = CurrentUserProvider.get().getUser()

            if (user != null) {
                val calendar: Calendar = Calendar.getInstance();
                val firstDateToSync = LocalDailyLogDbManager.getNextSyncDate(user.email!!)
                val today:DateString = DateString.fromCalendar(calendar)
                if (firstDateToSync == null) {
                    Log.i(TAG, "App is run the first time, no need to sync. Set last sync to today")
                    LocalDailyLogDbManager.setNextSyncDate(today, user.email!!)
                    return Result.success();
                } else {
                    var dateToSync = firstDateToSync;
                    while (dateToSync!!.getTimeStamp() < today.getTimeStamp()) {
                        Log.i(TAG, "Update remote record " + dateToSync.getString())
                        var log = hashMap.get(dateToSync.getString());
                        if (log == null) {
                            log = DailyLog()
                        }
                        val stepCount = fitnessAPI.getStepCountOnDateSync(
                            dateToSync.year,
                            dateToSync.month,
                            dateToSync.day
                        )
                        log.step = stepCount
                        val response: Response<FoodBodiResponse<DailyLog>> =
                            FoodbodiRetrofitHolder.getService()
                                .updateDailyLog(
                                    FoodbodiRetrofitHolder.getHeaders(applicationContext),
                                    hashMap.get(dateToSync.getString())!!,
                                    dateToSync.year.toString(),
                                    dateToSync.month.toString(),
                                    dateToSync.day.toString()
                                ).execute()
                        if (response.isSuccessful) {
                            val code: Number = response.body()!!.statusCode()
                            if (FoodBodiResponse.SUCCESS_CODE == code) {
                                hashMap.remove(dateToSync.getString())
                                Log.i(TAG, "Update dailylog of ${dateToSync.getString()} success")
                            } else {
                                Log.i(TAG, response.body()?.errorMessage())
                            }

                        } else {
                            Log.i(TAG, response.toString())
                        }
                        dateToSync = DateString.getNextDate(dateToSync)
                    }

                    LocalDailyLogDbManager.setNextSyncDate(dateToSync, user.email!!)

                    Log.i(TAG, "Commiting hashdb... ")
                    LocalDailyLogDbManager.instance?.commit()
                }

            }
        }
        return Result.success()
    }

}