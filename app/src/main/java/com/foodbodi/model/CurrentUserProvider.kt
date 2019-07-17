package com.foodbodi.model

import android.app.Activity
import android.content.Context
import android.view.View
import com.foodbodi.utils.Action

class CurrentUserProvider private constructor(){
    private lateinit var user:User
    init {

    }

    companion object Holder {
        private var instance:CurrentUserProvider? = null
        fun getInstance(apiKey:String):CurrentUserProvider? {
            if (instance == null) {
                instance = CurrentUserProvider()

            }
            return instance
        }

        fun ensureData(action:Action<User>) {

        }

        fun isReady() {

        }


    }
}