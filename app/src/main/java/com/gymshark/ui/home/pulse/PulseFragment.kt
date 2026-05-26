package com.gymshark.ui.home.pulse

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.*
import android.view.Surface
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.gymshark.R
import com.gymshark.databinding.FragmentPulseBinding
import com.gymshark.ui.home.pulse.heartrate.CameraService
import com.gymshark.ui.home.pulse.heartrate.OutputAnalyzer
import com.gymshark.ui.home.training.TrainingStatViewModel
import com.gymshark.ui.home.training.TrainingViewModel
import com.gymshark.ui.home.training.finish.FinishViewModel
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PulseFragment : BaseFragment<FragmentPulseBinding>(FragmentPulseBinding::inflate) {

    private val trainingVm: TrainingViewModel by activityViewModel()
    private val finishVm: FinishViewModel by activityViewModel()
    private val pulseVm: PulseViewModel by activityViewModel()
    private val statVm: TrainingStatViewModel by activityViewModel()

    private var cameraService: CameraService? = null
    private var analyzer: OutputAnalyzer? = null

    private val requestCodeCamera = 100

    companion object {
        const val MESSAGE_UPDATE_REALTIME = 1
        const val MESSAGE_UPDATE_FINAL = 2
        const val MESSAGE_CAMERA_NOT_AVAILABLE = 3
    }

    @SuppressLint("HandlerLeak")
    private val mainHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_UPDATE_REALTIME -> {
                    binding.tvResult.text = msg.obj.toString()
                }
                MESSAGE_UPDATE_FINAL -> {
                    val pulse = msg.obj.toString().toDouble().toInt()

                    if (pulse > 40) {
                        // pulseVm.addMeasurement(pulse)
                    }

                    binding.tvResult.text =
                        "Min: ${pulseVm.minBpm.value}  " +
                            "Max: ${pulseVm.maxBpm.value}  " +
                            "Avg: ${pulseVm.avgBpm.value}"
                }
                MESSAGE_CAMERA_NOT_AVAILABLE -> {
                    Snackbar.make(
                        binding.root,
                        "Workout data not found",
                        Snackbar.LENGTH_LONG
                    ).show()

                    analyzer?.stop()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraService = CameraService(requireActivity(), mainHandler)
        analyzer = OutputAnalyzer(requireContext(), binding.graphTextureView, mainHandler)

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            requestCodeCamera
        )

        binding.btnStart.setOnClickListener {
            startMeasurement()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            pulseVm.avgBpm.collect { avg ->
                if (avg != null) {
                    binding.tvResult.text =
                        "Min: ${pulseVm.minBpm.value}  " +
                            "Max: ${pulseVm.maxBpm.value}  " +
                            "Avg: $avg"
                }
            }
        }

        binding.btnFinish.setOnClickListener {
            finishVm.registerActivity(System.currentTimeMillis())
            saveAndExit()
        }
    }

    private fun startMeasurement() {
        analyzer = OutputAnalyzer(requireContext(), binding.graphTextureView, mainHandler)

        val textureView = binding.textureView2
        val surfaceTexture = textureView.surfaceTexture

        if (surfaceTexture != null) {
            val previewSurface = Surface(surfaceTexture)

            cameraService?.apply {
                start(previewSurface)
                analyzer?.measurePulse(textureView, this)
            }
        }
    }

    private fun saveAndExit() {
        val navController = findNavController()

        trainingVm.finishTraining(
            title = "Workout",
            finishTime = System.currentTimeMillis(),
            durationSec = statVm.elapsedSeconds.value,
            mood = finishVm.mood.value.name.lowercase(),
            minBpm = pulseVm.minBpm.value,
            maxBpm = pulseVm.maxBpm.value,
            avgBpm = pulseVm.avgBpm.value
        )

        navController.navigate(
            R.id.navStats,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.home_graph, true)
                .build()
        )
    }

    override fun onPause() {
        super.onPause()
        cameraService?.stop()
        analyzer?.stop()
    }

    override fun onDestroyView() {
        cameraService?.stop()
        analyzer?.stop()
        super.onDestroyView()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == requestCodeCamera) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Snackbar.make(
                    binding.root,
                    "Camera permission required",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}
