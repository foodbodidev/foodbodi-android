package com.foodbodi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.foodbodi.model.CurrentUserProvider
import com.foodbodi.model.User
import com.foodbodi.utils.Action

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        CurrentUserProvider.get().loadCurrentUser(object : Action<User> {
            override fun accept(data: User?) {
                finish()
            }

            override fun deny(data: User?, reason: String) {
                finish()
            }
        }, this)
    }
}
