package com.gymshark.ui.home

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.core.view.doOnLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.R
import com.gymshark.databinding.ActivityHomeBinding
import com.gymshark.utils.BaseActivity

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    private var contentBottomInset = 0

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
        setupContentInsets()
        applyHomeInsets()
    }

    private fun setupBottomNavigation(navController: NavController) = with(binding) {
        morphingBottomNav.setOnTabSelectedListener { index ->
            val destinationId = when (index) {
                1 -> R.id.navStats
                2 -> R.id.prepareMealFragment
                3 -> R.id.navProfile
                else -> R.id.navHome
            }
            navigateTopLevel(navController, destinationId)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val selectedIndex = when (destination.id) {
                R.id.navStats -> 1
                R.id.prepareMealFragment,
                R.id.navMeal -> 2
                R.id.navProfile,
                R.id.profileSettingsFragment,
                R.id.trainingPlanEditorFragment -> 3
                else -> 0
            }

            morphingBottomNav.setSelectedTab(selectedIndex, animate = false)
        }
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

            contentBottomInset = nav.bottom + 108.dp
            applyContentBottomInsetToCurrentFragment()

            insets
        }

        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun setupContentInsets() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment

        navHost.childFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    fragment: Fragment,
                    view: View,
                    savedInstanceState: Bundle?
                ) {
                    applyContentBottomInset(view)
                }
            },
            true
        )
    }

    private fun applyContentBottomInsetToCurrentFragment() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment

        navHost.childFragmentManager.fragments
            .lastOrNull { it.view != null }
            ?.view
            ?.let(::applyContentBottomInset)
    }

    private fun applyContentBottomInset(root: View) {
        val scrollTargets = mutableListOf<View>()
        root.collectBottomInsetTargets(scrollTargets)

        if (scrollTargets.isEmpty()) {
            root.updateBottomPaddingKeepingBase(contentBottomInset)
            return
        }

        scrollTargets.forEach { target ->
            target.updateBottomPaddingKeepingBase(contentBottomInset)
            when (target) {
                is RecyclerView -> target.clipToPadding = false
                is ScrollView -> target.clipToPadding = false
                is NestedScrollView -> target.clipToPadding = false
            }
        }
    }

    private fun View.collectBottomInsetTargets(targets: MutableList<View>) {
        if (this is RecyclerView) {
            if (isVerticalRecyclerView()) {
                targets += this
            }
            return
        }

        if (this is ScrollView || this is NestedScrollView) {
            targets += this
            return
        }

        if (this is ViewGroup) {
            for (index in 0 until childCount) {
                getChildAt(index).collectBottomInsetTargets(targets)
            }
        }
    }

    private fun RecyclerView.isVerticalRecyclerView(): Boolean {
        val linearLayoutManager = layoutManager as? LinearLayoutManager
        return linearLayoutManager?.orientation != LinearLayoutManager.HORIZONTAL
    }

    private fun View.updateBottomPaddingKeepingBase(extraBottom: Int) {
        val base = getTag(R.id.tag_home_content_padding) as? PaddingSnapshot
            ?: PaddingSnapshot(paddingLeft, paddingTop, paddingRight, paddingBottom).also {
                setTag(R.id.tag_home_content_padding, it)
            }

        updatePadding(
            left = base.left,
            top = base.top,
            right = base.right,
            bottom = base.bottom + extraBottom
        )
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private data class PaddingSnapshot(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )
}
