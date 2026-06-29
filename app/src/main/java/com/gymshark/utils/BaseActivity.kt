package com.gymshark.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    protected lateinit var binding: VB
        private set

    private val screenName: String
        get() = this::class.java.simpleName.ifEmpty {
            this::class.java.name
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        Log.d("ScreenLog", "$screenName -> onCreate")

        binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)
        applySystemBarsInsets(binding.root)
    }

    override fun onStart() {
        super.onStart()
        Log.d("ScreenLog", "$screenName -> onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("ScreenLog", "$screenName -> onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("ScreenLog", "$screenName -> onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("ScreenLog", "$screenName -> onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ScreenLog", "$screenName -> onDestroy")
    }

    protected fun applySystemBarsInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())

            v.updatePadding(
                top = status.top,
                bottom = if (imeVisible) ime.bottom else nav.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(view)
    }

    protected fun applyInsetsTo(view: View) = applySystemBarsInsets(view)
}