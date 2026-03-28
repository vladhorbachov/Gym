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
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentMealBinding
import com.gymshark.ui.home.meal.barcode.BarcodeAnalyzer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.Executors


class MealFragment : Fragment(R.layout.fragment_meal) {
    private val mealViewModel: MealViewModel by viewModel()
    private var _binding: FragmentMealBinding? = null
    private val binding get() = _binding!!
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isScanned = false
    private var lastBarcode: String? = null
    private var lastScanTime = 0L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, s: Bundle?
    ): View {
        _binding = FragmentMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkCameraPermission()

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
                .also { analyzer ->

                    analyzer.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->

                        if (isScanned) return@BarcodeAnalyzer

                        val now = System.currentTimeMillis()
                        if (now - lastScanTime < 1500) return@BarcodeAnalyzer

                        lastScanTime = now
                        isScanned = true
                        lastBarcode = barcode

                        binding.tvResult.text = "Scanning..."

                        mealViewModel.clearProduct()
                        mealViewModel.getFood(barcode)

                        viewLifecycleOwner.lifecycleScope.launch {
                            mealViewModel.productState
                                .filterNotNull()
                                .first()
                                .let { product ->

                                    binding.tvResult.text = "Found!"

                                    findNavController().previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("scanned_food", product)

                                    findNavController().popBackStack()
                                }
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
    override fun onResume() {
        super.onResume()
        isScanned = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

}