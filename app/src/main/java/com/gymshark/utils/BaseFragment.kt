package com.gymshark.utils

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    protected var _binding: VB? = null
        private set

    protected val binding: VB
        get() = requireNotNull(_binding) {
            "Binding is only valid between onCreateView and onDestroyView."
        }

    private val screenName: String
        get() = this::class.java.simpleName.ifEmpty {
            this::class.java.name
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ScreenLog", "$screenName -> onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("ScreenLog", "$screenName -> onCreateView")

        _binding = bindingInflater.invoke(inflater, container, false)
        return binding.root
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

    override fun onDestroyView() {
        Log.d("ScreenLog", "$screenName -> onDestroyView")
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ScreenLog", "$screenName -> onDestroy")
    }
}