package com.foodbodi

import android.content.Intent
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
                CurrentUserProvider.get().updateRemainCaloToEat(this@SplashScreen)
                finish()
            }

            override fun deny(data: User?, reason: String) {
                startActivityForResult(Intent(this@SplashScreen, GettingStartedActivity::class.java), GettingStartedActivity.GET_START_DONE)
            }
        }, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (GettingStartedActivity.GET_START_DONE == requestCode) {
            finish()
        }
    }
}
