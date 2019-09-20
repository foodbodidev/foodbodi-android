package com.foodbodi.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.DailyLog
import com.foodbodi.model.LocalDailyLogDbManager
import com.foodbodi.utils.DateString
import org.mapdb.DB
import org.mapdb.HTreeMap
import java.util.*

class SyncDailyLogWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    val TAG = SyncDailyLogWorker::class.java.simpleName

    override fun doWork(): Result {
        Log.i(TAG,"Begin sync up daily log...")
        val db:DB? = LocalDailyLogDbManager.get(this.applicationContext)
        if (db != null) {
            val hashMap: HTreeMap<String, DailyLog> =
                LocalDailyLogDbManager.getDefaultDb()!!.getHashMap(LocalDailyLogDbManager.DAILYLOG_TABLE)

            val keys = hashMap.keys
            val user = CurrentUserProvider.get().getUser()
            if (user != null) {
                for (key in keys) {
                    val calendar: Calendar = Calendar.getInstance();
                    val todayId = DailyLog.getLocalID(DateString.fromCalendar(calendar), user.email!!)
                    if (todayId.equals(key)) {
                        Log.i(TAG, "Ignore today record " + key)
                    } else {
                        Log.i(TAG, "Update remote record " + key)
                    }
                }
            }
        }
        return Result.success()
    }

}