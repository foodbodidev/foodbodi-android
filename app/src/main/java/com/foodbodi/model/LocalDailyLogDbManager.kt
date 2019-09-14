package com.foodbodi.model

import android.content.Context
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import java.io.File
import java.util.*

class LocalDailyLogDbManager {

    companion object {
        val DB_NAME:String = "foodimap-db"
        val DAILYLOG_TABLE:String = "dailylog"
        val DB_VERSION = 1;

        var instance:DB? = null
        fun get(context:Context, username:String, dbName:String):DB? {
            if (instance == null) {
                instance = DBMaker.newFileDB(File(context.filesDir.absolutePath + DB_NAME)).make()
            }

            return instance
        }

        fun getDefaultDb() :DB? {
            return instance
        }

        fun ensureLocalDailyLogRecord(year: Int, month : Int, day : Int, user:String):DailyLog {
            val id = DailyLog.getLocalID(year, month, day, user);
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
            val id = DailyLog.getLocalID(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), user);
            val doc = ensureLocalDailyLogRecord(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), user)
            doc.step = newValue
            val hashMap:HTreeMap<String, DailyLog> = getDefaultDb()!!.getHashMap(DAILYLOG_TABLE)
            hashMap.put(id, doc)
            return doc
        }

        fun getTodayStepCount(username: String):Int {
            val calendar:Calendar = Calendar.getInstance()
            val doc = ensureLocalDailyLogRecord(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), username)
            return if (doc != null && doc.step != null ) doc.step!! else 0
        }

    }
}