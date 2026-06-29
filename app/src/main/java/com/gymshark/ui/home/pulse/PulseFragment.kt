package com.gymshark.ui.home.pulse

import android.Manifest
import android.annotation.SuppressLint
import android.os.*
import android.view.Surface
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startMeasurement()
            } else {
                Snackbar.make(
                    binding.root,
                    "Camera permission required",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

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
                    binding.tvHint.text = "Hold steady. The signal is being analyzed."
                }
                MESSAGE_UPDATE_FINAL -> {
                    val pulse = msg.obj.toString().toDouble().toInt()

                    if (pulse > 40) {
                        pulseVm.addMeasurement(pulse)
                    }

                    renderMetrics()
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

        setupMetrics()
        playIntro()

        binding.btnBack.setOnClickListener {
            it.pressPulse()
            findNavController().navigateUp()
        }

        binding.btnStart.setOnClickListener {
            it.pressPulse()
            startMeasurementWithPermission()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pulseVm.avgBpm.collect { avg ->
                    if (avg != null) {
                        renderMetrics()
                    }
                }
            }
        }

        binding.btnFinish.setOnClickListener {
            it.pressPulse()
            finishVm.registerActivity(System.currentTimeMillis())
            saveAndExit()
        }
    }

    private fun setupMetrics() = with(binding) {
        metricMin.tvLabel.text = "min"
        metricAvg.tvLabel.text = "avg"
        metricMax.tvLabel.text = "max"
    }

    private fun startMeasurementWithPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            startMeasurement()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startMeasurement() {
        binding.tvResult.text = "Measuring..."
        binding.tvHint.text = "Cover the camera fully and avoid movement."
        binding.cameraFrame.animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(180L)
            .withEndAction {
                binding.cameraFrame.animate().scaleX(1f).scaleY(1f).setDuration(220L).start()
            }
            .start()

        analyzer = OutputAnalyzer(requireContext(), binding.graphTextureView, mainHandler)

        val textureView = binding.textureView2
        val surfaceTexture = textureView.surfaceTexture

        if (surfaceTexture != null) {
            val previewSurface = Surface(surfaceTexture)

            cameraService?.apply {
                start(previewSurface)
                analyzer?.measurePulse(textureView, this)
            }
        } else {
            binding.tvHint.text = "Camera preview is preparing. Try again in a moment."
        }
    }

    private fun renderMetrics() = with(binding) {
        val min = pulseVm.minBpm.value
        val avg = pulseVm.avgBpm.value
        val max = pulseVm.maxBpm.value

        metricMin.tvValue.text = min?.toString() ?: "-"
        metricAvg.tvValue.text = avg?.toString() ?: "-"
        metricMax.tvValue.text = max?.toString() ?: "-"
        tvResult.text = avg?.let { "$it bpm" } ?: "Ready to measure"
        tvHint.text = if (avg == null) {
            "Camera access is requested only when measurement starts."
        } else {
            "Pulse captured. Save to finish the workout."
        }
    }

    private fun saveAndExit() {
        val navController = findNavController()
        val durationSec = statVm.elapsedSeconds.value

        trainingVm.finishTraining(
            title = "Workout",
            finishTime = System.currentTimeMillis(),
            durationSec = durationSec,
            mood = finishVm.mood.value.name.lowercase(),
            minBpm = pulseVm.minBpm.value,
            maxBpm = pulseVm.maxBpm.value,
            avgBpm = pulseVm.avgBpm.value
        )
        statVm.stopAndReset()

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
    private fun playIntro() = with(binding) {
        listOf(btnBack, tvKicker, tvTitle, tvSubtitle, cardPulse, actionRow)
            .forEachIndexed { index, target ->
                target.alpha = 0f
                target.translationY = 24f
                target.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(index * 55L)
                    .setDuration(260L)
                    .start()
            }
    }

    private fun View.pressPulse() {
        animate().scaleX(0.97f).scaleY(0.97f).setDuration(80L).withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
        }.start()
    }
}
