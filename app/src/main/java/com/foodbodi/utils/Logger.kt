package com.foodbodi.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.foodbodi.BuildConfig

class Logger {
    companion object {
        fun isDev():Boolean {
            return BuildConfig.DEBUG
        }
        fun info(tag:String, message:String, context:Context) {
            Log.i(tag, message)
            if (isDev()) {
                Toast.makeText(context, "Info : $message", Toast.LENGTH_LONG).show()
            }
        }
        fun warning(tag:String, message:String, context:Context) {
            Log.w(tag, message)
            if (isDev()) {
                Toast.makeText(context, "Warning : $message", Toast.LENGTH_LONG).show()
            }
        }
        fun error(tag:String, message:String, context:Context) {
            Log.e(tag, message)
            if (isDev()) {
                Toast.makeText(context, "Error : $message", Toast.LENGTH_LONG).show()
            }
        }
    }
}