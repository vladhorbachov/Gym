package com.gymshark.ui.home.meal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gymshark.R
import com.gymshark.databinding.FragmentMealBinding
import com.gymshark.ui.home.meal.barcode.BarcodeAnalyzer
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executors


class MealFragment : Fragment(R.layout.fragment_meal) {
    private val mealViewModel: MealViewModel by viewModel()
    private var _binding: FragmentMealBinding? = null
    private val binding get() = _binding!!
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isScanned = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, s: Bundle?
    ): View {
        _binding = FragmentMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkCameraPermission()

        observeFood()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }

            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }



    private fun observeFood() {
        viewLifecycleOwner.lifecycleScope.launch {
            mealViewModel.nutrimentsState.collect {
                binding.tvResult.text = it.toString()
//                    "energyKcal100g = ${it?.energyKcal100g.toString()}, " +
//                            "\n proteins100g = ${it?.proteins100g.toString()}, " +
//                            "\n fat100g = ${it?.fat100g.toString()}," +
//                            "\n carbohydrates100g = ${it?.carbohydrates100g.toString()}"
            }
        }
    }


    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                onCameraPermissionDenied()
            }
        }


    private fun onCameraPermissionDenied() {
        Toast.makeText(
            requireContext(),
            "Camera permission denied",
            Toast.LENGTH_SHORT
        ).show()
    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        if (!isScanned) {
                            isScanned = true
                            binding.tvResult.text = "Scanned: $barcode"
                            mealViewModel.getFood(barcode)
                        }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }


}