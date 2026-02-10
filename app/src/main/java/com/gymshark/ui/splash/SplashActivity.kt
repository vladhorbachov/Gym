package com.gymshark.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gymshark.R
import com.gymshark.domain.models.ExerciseJson
import com.gymshark.ui.auth.AuthActivity
import com.gymshark.ui.home.HomeActivity
import com.gymshark.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.InputStreamReader

class SplashActivity : AppCompatActivity(R.layout.activity_splash) {

    private val firebaseAuth: FirebaseAuth by inject()
    private val splashViewModel: SplashViewModel by viewModel()
    private var launchTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchTime = System.currentTimeMillis()
        observe()
        readAndInsertExercise()
    }

    private fun routeNext() {


        if (isFinishing || isDestroyed) return

        val next = if (isLoggedIn()) HomeActivity::class.java else AuthActivity::class.java

        startActivity(
            Intent(this, next).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        finish()
    }

    private fun checkTime() {
        val diff = Constants.SPLASH_DELAY_MS - launchTime
        if (diff > 0) {
            lifecycleScope.launch {
                delay(diff)
                routeNext()
            }
        } else {
            routeNext()
        }
    }

    private fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    private fun readAndInsertExercise() {
        val inputStream = resources.openRawResource(R.raw.exercises)
        val reader = InputStreamReader(inputStream)

        splashViewModel.saveAllExercise(Gson().fromJson(reader, ExerciseJson::class.java))
    }

    private fun observe() {
        with(splashViewModel) {
            saveExercise.observe(this@SplashActivity) {
                if (it) {
                    checkTime()
                }
            }
        }
    }

}