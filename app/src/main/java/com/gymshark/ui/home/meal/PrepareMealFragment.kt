package com.gymshark.ui.home.meal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.data.db.entity.FoodEntity
import com.gymshark.databinding.FragmentPrepareMealBinding
import com.gymshark.domain.models.MealType
import com.gymshark.domain.models.Nutriments
import com.gymshark.domain.models.Product
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PrepareMealFragment : Fragment() {

    private var _binding: FragmentPrepareMealBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MealViewModel by viewModel()
    private var foodList: List<FoodEntity> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrepareMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Product>("scanned_food")
            ?.observe(viewLifecycleOwner) { product ->

                binding.actProductName.setText(product.productName ?: "")

                val n = product.nutriments

                binding.etCalories.setText(n?.energyKcal100g?.toString() ?: "")
                binding.etProtein.setText(n?.proteins100g?.toString() ?: "")
                binding.etFat.setText(n?.fat100g?.toString() ?: "")
                binding.etCarbs.setText(n?.carbohydrates100g?.toString() ?: "")
            }


        setupMealTypeDropdown()
        setupScanButton()
        setupTextWatchers()
        setupSaveButton()
        loadProductsFromDb()
    }


    private fun setupMealTypeDropdown() {
        val mealTypes = MealType.entries.map { it.mealType }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mealTypes
        )

        binding.actMealType.setAdapter(adapter)
    }


    private fun setupScanButton() {
        binding.tilProductName.setEndIconOnClickListener {
            findNavController().navigate(R.id.navMeal)
        }
    }

    private fun loadProductsFromDb() {
        lifecycleScope.launch {
            foodList = viewModel.getAllLocalFoods()

            val names = foodList.map { it.name }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                names
            )

            binding.actProductName.threshold = 1
            binding.actProductName.setAdapter(adapter)

            binding.actProductName.setOnItemClickListener { _, _, position, _ ->

                val selectedFood = foodList[position]

                binding.etCalories.setText(selectedFood.calories.toString())
                binding.etProtein.setText(selectedFood.proteins.toString())
                binding.etFat.setText(selectedFood.fats.toString())
                binding.etCarbs.setText(selectedFood.carbs.toString())
            }
        }
    }


    private fun setupSaveButton() {
        binding.btnSaveMeal.setOnClickListener {

            val mealType = binding.actMealType.text.toString().trim()
            val productName = binding.actProductName.text.toString().trim()

            val calories = binding.etCalories.text.toString().toFloatOrNull()
            val protein = binding.etProtein.text.toString().toFloatOrNull()
            val fat = binding.etFat.text.toString().toFloatOrNull()
            val carbs = binding.etCarbs.text.toString().toFloatOrNull()

            if (mealType.isBlank() || productName.isBlank()) return@setOnClickListener

            viewModel.saveMeal(
                mealType = mealType,
                productName = productName,
                calories = calories,
                protein = protein,
                fat = fat,
                carbs = carbs
            )

            findNavController().popBackStack()
        }
    }


    private fun setupTextWatchers() {

        val watcher = object : android.text.TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        binding.actMealType.addTextChangedListener(watcher)
        binding.actProductName.addTextChangedListener(watcher)
        binding.etCalories.addTextChangedListener(watcher)
        binding.etProtein.addTextChangedListener(watcher)
        binding.etFat.addTextChangedListener(watcher)
        binding.etCarbs.addTextChangedListener(watcher)
    }

    private fun validateForm() {

        val isValid =
            binding.actMealType.text?.isNotBlank() == true &&
                    binding.actProductName.text?.isNotBlank() == true &&
                    binding.etCalories.text?.isNotBlank() == true &&
                    binding.etProtein.text?.isNotBlank() == true &&
                    binding.etFat.text?.isNotBlank() == true &&
                    binding.etCarbs.text?.isNotBlank() == true

        binding.btnSaveMeal.isEnabled = isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}