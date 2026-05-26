package com.gymshark.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.gymshark.R
import com.gymshark.databinding.ActivityAuthBinding
import com.gymshark.ui.home.HomeActivity
import com.gymshark.utils.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthActivity : BaseActivity<ActivityAuthBinding>(ActivityAuthBinding::inflate) {

    companion object {
        private const val EXTRA_START = "start_screen"

        enum class Start { LOGIN, REGISTER }

        @JvmStatic
        fun intentLogin(context: Context) =
            Intent(context, AuthActivity::class.java).putExtra(EXTRA_START, Start.LOGIN.name)

        @JvmStatic
        fun intentRegister(context: Context) =
            Intent(context, AuthActivity::class.java).putExtra(EXTRA_START, Start.REGISTER.name)
    }

    private val vm: AuthViewModel by viewModel()

    private val navController: NavController by lazy(LazyThreadSafetyMode.NONE) {
        (supportFragmentManager.findFragmentById(R.id.authNavHost) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (vm.loggedIn.value == true) {
            openHomeAndFinish(); return
        }

        ensureNavHost()

        if (savedInstanceState == null) {
            val start = when (intent.getStringExtra(EXTRA_START)) {
                Start.REGISTER.name -> R.id.registerFragment
                else -> R.id.loginFragment
            }

            val graph = navController.navInflater.inflate(R.navigation.nav_graph).apply {
                setStartDestination(start)
            }

            val handled = navController.handleDeepLink(intent)
            if (!handled) navController.setGraph(graph, bundleOf())
        }

        vm.loggedIn.observe(this) { if (it == true) openHomeAndFinish() }
    }

    private fun ensureNavHost() {
        val existing = supportFragmentManager.findFragmentById(R.id.authNavHost) as? NavHostFragment
        if (existing == null) {
            val host = NavHostFragment.create(R.navigation.nav_graph)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.authNavHost, host)
                setPrimaryNavigationFragment(host)
            }
        }
    }

    private fun openHomeAndFinish() {
        startActivity(
            Intent(this, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
