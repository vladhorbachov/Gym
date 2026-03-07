package com.gymshark.ui.home

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.gymshark.R
import com.gymshark.databinding.ActivityHomeBinding
import com.gymshark.utils.BaseActivity

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)

        val navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment)
            .navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navHome,
                R.id.navStats,
                R.id.navMeal,
                R.id.navProfile
            )
        )

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.bnMainNav.setupWithNavController(navController)

        applyInsetsTo(binding.root)
    }
}