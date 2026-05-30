package com.gymshark.ui.home

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.gymshark.R
import com.gymshark.databinding.ActivityHomeBinding
import com.gymshark.utils.BaseActivity

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setSupportActionBar(binding.toolbar)

        val navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment)
            .navController

//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navHome,
//                R.id.navStats,
//                R.id.navMeal,
//                R.id.navProfile
//            )
//        )

//        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        setupBottomNavigation(navController)
        setupBottomNavBlur()
        applyHomeInsets()
    }

    private fun setupBottomNavigation(navController: NavController) = with(binding) {
        navHomeItem.setOnClickListener { navigateTopLevel(navController, R.id.navHome) }
        navStatsItem.setOnClickListener { navigateTopLevel(navController, R.id.navStats) }
        navMealItem.setOnClickListener { navigateTopLevel(navController, R.id.prepareMealFragment) }
        navProfileItem.setOnClickListener { navigateTopLevel(navController, R.id.navProfile) }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val selectedItem = when (destination.id) {
                R.id.navStats -> navStatsItem
                R.id.prepareMealFragment,
                R.id.navMeal -> navMealItem
                R.id.navProfile -> navProfileItem
                else -> navHomeItem
            }

            listOf(navHomeItem, navStatsItem, navMealItem, navProfileItem)
                .forEach { updateTabSelection(it, it == selectedItem) }
        }
    }

    private fun updateTabSelection(tabContent: View, isSelected: Boolean) {
        val tabContainer = tabContent.parent as? View
        tabContent.isSelected = isSelected
        tabContainer?.isSelected = isSelected

        val targetScale = if (isSelected) 1f else 0.96f
        val targetAlpha = if (isSelected) 1f else 0.42f

        tabContent.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .alpha(targetAlpha)
            .setDuration(180L)
            .start()
    }

    private fun navigateTopLevel(navController: NavController, destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(navController.graph.startDestinationId, false, true)
            .build()

        navController.navigate(destinationId, null, options)
    }

    private fun setupBottomNavBlur() {
        binding.root.doOnLayout {
            binding.navBackdropBlur.setBlurSource(findViewById(R.id.nav_host))
        }
    }

    private fun applyHomeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            root.updatePadding(
                top = status.top,
                bottom = 0
            )

            binding.bottomNavGlass.updateLayoutParams<FrameLayout.LayoutParams> {
                bottomMargin = nav.bottom + 16.dp
            }

            insets
        }

        ViewCompat.requestApplyInsets(binding.root)
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
