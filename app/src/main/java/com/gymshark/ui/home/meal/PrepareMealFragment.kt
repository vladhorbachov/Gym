package com.gymshark.ui.home.meal

import android.os.Bundle
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gymshark.R
import com.gymshark.data.db.entity.FoodEntity
import com.gymshark.data.db.entity.MealInfoEntity
import com.gymshark.databinding.FragmentPrepareMealBinding
import com.gymshark.databinding.ModalNutritionGoalsBinding
import com.gymshark.domain.models.Product
import com.gymshark.ui.home.meal.adapter.NutritionListItem
import com.gymshark.ui.home.meal.adapter.TodayMealsAdapter
import com.gymshark.utils.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class PrepareMealFragment : BaseFragment<FragmentPrepareMealBinding>(FragmentPrepareMealBinding::inflate) {

    private val viewModel: MealViewModel by viewModel()
    private var foodList: List<FoodEntity> = emptyList()
    private lateinit var mealsAdapter: TodayMealsAdapter
    private var isHistoryMode = false
    private var query = ""
    private var nutritionGoals = NutritionGoals.default()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyInsets()
        setupHeader()
        setupMealTypeDropdown()
        setupQuickActions()
        setupGoalsAction()
        nutritionGoals = loadGoals()
        setupTextWatchers()
        setupSaveButton()
        setupMealsList()
        setupSegmentedControl()
        observeScannedProduct()
        observeMeals()
        loadProductsFromDb()

        viewModel.loadTodayMeals()
        viewModel.loadAllHistoryMeals()
        validateForm()
        renderFormVisibility(show = false, animate = false)
        playIntro()
    }

    private fun setupHeader() {
        binding.tvTodayDate.text = "Today - ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())}"
    }

    private fun observeScannedProduct() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Product>("scanned_food")
            ?.observe(viewLifecycleOwner) { product ->
                handleScannedProduct(product)
                findNavController().currentBackStackEntry
                    ?.savedStateHandle
                    ?.remove<Product>("scanned_food")
            }
    }

    private fun setupMealTypeDropdown() {
        val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Other")
        binding.actMealType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mealTypes)
        )
    }

    private fun setupQuickActions() = with(binding) {
        btnScanProduct.setOnClickListener { onScanProductClicked() }
        btnAddManual.setOnClickListener { renderFormVisibility(!addMealCard.isVisible, animate = true) }
        btnEmptyAdd.setOnClickListener { renderFormVisibility(show = true, animate = true) }
    }

    private fun setupGoalsAction() {
        binding.btnEditGoals.setOnClickListener {
            showEditGoalsSheet()
        }
    }

    private fun onScanProductClicked() {
        // Scanner is already implemented in MealFragment. Replace this navigation if you later
        // move scanning into an inline scanner or a dedicated scanner Activity.
        findNavController().navigate(R.id.navMeal)
    }

    private fun handleScannedProduct(product: Product) {
        renderFormVisibility(show = true, animate = true)
        binding.actProductName.setText(product.productName.orEmpty())
        val nutriments = product.nutriments
        binding.etCalories.setText(nutriments?.energyKcal100g?.clean().orEmpty())
        binding.etProtein.setText(nutriments?.proteins100g?.clean().orEmpty())
        binding.etFat.setText(nutriments?.fat100g?.clean().orEmpty())
        binding.etCarbs.setText(nutriments?.carbohydrates100g?.clean().orEmpty())
        if (binding.actMealType.text.isNullOrBlank()) binding.actMealType.setText("Snack", false)
        validateForm()
    }

    private fun setupMealsList() {
        mealsAdapter = TodayMealsAdapter { meal ->
            viewModel.deleteMeal(meal)
        }
        binding.rvTodayMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodayMeals.adapter = mealsAdapter
    }

    private fun setupSegmentedControl() {
        binding.mealSegment.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            isHistoryMode = checkedId == R.id.btnHistory
            binding.tilSearch.isVisible = isHistoryMode
            renderMeals()
        }
    }

    private fun observeMeals() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayMealsState.collect {
                renderSummary(it)
                if (!isHistoryMode) renderMeals()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historyMealsState.collect {
                if (isHistoryMode) renderMeals()
            }
        }
    }

    private fun loadProductsFromDb() {
        lifecycleScope.launch {
            foodList = viewModel.getAllLocalFoods()
            val names = foodList.map { it.name }
            binding.actProductName.threshold = 1
            binding.actProductName.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            )
            binding.actProductName.setOnItemClickListener { _, _, position, _ ->
                foodList.getOrNull(position)?.let { selectedFood ->
                    binding.etCalories.setText(selectedFood.calories.toString())
                    binding.etProtein.setText(selectedFood.proteins.clean())
                    binding.etFat.setText(selectedFood.fats.clean())
                    binding.etCarbs.setText(selectedFood.carbs.clean())
                    validateForm()
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveMeal.setOnClickListener {
            if (!validateForm(showErrors = true)) return@setOnClickListener

            viewModel.saveMeal(
                mealType = binding.actMealType.text.toString().trim(),
                productName = binding.actProductName.text.toString().trim(),
                calories = binding.etCalories.text.toString().toFloatOrNull(),
                protein = binding.etProtein.text.toString().toFloatOrNull(),
                fat = binding.etFat.text.toString().toFloatOrNull(),
                carbs = binding.etCarbs.text.toString().toFloatOrNull()
            )

            binding.btnSaveMeal.text = "Saved"
            binding.btnSaveMeal.animate().scaleX(0.98f).scaleY(0.98f).setDuration(80L).withEndAction {
                binding.btnSaveMeal.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
                binding.btnSaveMeal.text = "Save meal"
            }.start()
            clearForm()
            validateForm()
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        }

        listOf(
            binding.actMealType,
            binding.actProductName,
            binding.etCalories,
            binding.etProtein,
            binding.etFat,
            binding.etCarbs,
            binding.etSearch
        ).forEach { it.addTextChangedListener(watcher) }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                query = s?.toString().orEmpty()
                renderMeals()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun validateForm(showErrors: Boolean = false): Boolean {
        val mealTypeValid = binding.actMealType.text?.isNotBlank() == true
        val nameValid = binding.actProductName.text?.isNotBlank() == true
        val caloriesValid = (binding.etCalories.text?.toString()?.toFloatOrNull() ?: -1f) >= 0f
        val proteinValid = binding.etProtein.text?.toString()?.toFloatOrNull() != null
        val fatValid = binding.etFat.text?.toString()?.toFloatOrNull() != null
        val carbsValid = binding.etCarbs.text?.toString()?.toFloatOrNull() != null

        if (showErrors) {
            binding.tilMealType.error = if (mealTypeValid) null else "Choose a meal type"
            binding.tilProductName.error = if (nameValid) null else "Enter product name"
            binding.tilCalories.error = if (caloriesValid) null else "Enter calories"
            binding.tilProtein.error = if (proteinValid) null else "Required"
            binding.tilFat.error = if (fatValid) null else "Required"
            binding.tilCarbs.error = if (carbsValid) null else "Required"
        } else {
            listOf(binding.tilMealType, binding.tilProductName, binding.tilCalories, binding.tilProtein, binding.tilFat, binding.tilCarbs)
                .forEach { it.error = null }
        }

        val valid = mealTypeValid && nameValid && caloriesValid && proteinValid && fatValid && carbsValid
        binding.btnSaveMeal.isEnabled = valid
        binding.btnSaveMeal.alpha = if (valid) 1f else 0.45f
        return valid
    }

    private fun renderSummary(meals: List<MealInfoEntity>) = with(binding) {
        val calories = meals.sumOf { (it.energyKcal100g ?: 0f).toDouble() }.roundToInt()
        val protein = meals.sumOf { (it.proteins100g ?: 0f).toDouble() }.toFloat()
        val fat = meals.sumOf { (it.fat100g ?: 0f).toDouble() }.toFloat()
        val carbs = meals.sumOf { (it.carbohydrates100g ?: 0f).toDouble() }.toFloat()
        val remaining = (nutritionGoals.caloriesTarget - calories).coerceAtLeast(0)

        tvCaloriesSummary.text = "$calories / ${nutritionGoals.caloriesTarget} kcal"
        tvCaloriesRemaining.text = "$remaining kcal remaining"
        progressCalories.progress = percent(calories.toFloat(), nutritionGoals.caloriesTarget.toFloat())

        tvProtein.text = "Protein\n${protein.clean()} / ${nutritionGoals.proteinTarget.clean()}g"
        tvFat.text = "Fat\n${fat.clean()} / ${nutritionGoals.fatTarget.clean()}g"
        tvCarbs.text = "Carbs\n${carbs.clean()} / ${nutritionGoals.carbsTarget.clean()}g"
        progressProtein.progress = percent(protein, nutritionGoals.proteinTarget)
        progressFat.progress = percent(fat, nutritionGoals.fatTarget)
        progressCarbs.progress = percent(carbs, nutritionGoals.carbsTarget)
    }

    private fun showEditGoalsSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = ModalNutritionGoalsBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        dialog.setOnShowListener {
            dialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundColor(Color.TRANSPARENT)
        }

        with(sheetBinding) {
            etGoalCalories.setText(nutritionGoals.caloriesTarget.toString())
            etGoalProtein.setText(nutritionGoals.proteinTarget.clean())
            etGoalFat.setText(nutritionGoals.fatTarget.clean())
            etGoalCarbs.setText(nutritionGoals.carbsTarget.clean())

            btnCancelGoals.setOnClickListener { dialog.dismiss() }
            btnSaveGoals.setOnClickListener {
                val next = NutritionGoals(
                    caloriesTarget = etGoalCalories.text.toString().toIntOrNull() ?: 0,
                    proteinTarget = etGoalProtein.text.toString().toFloatOrNull() ?: 0f,
                    fatTarget = etGoalFat.text.toString().toFloatOrNull() ?: 0f,
                    carbsTarget = etGoalCarbs.text.toString().toFloatOrNull() ?: 0f
                )

                val valid = validateGoals(next, sheetBinding)
                if (!valid) return@setOnClickListener

                nutritionGoals = next
                saveGoals(next)
                renderSummary(viewModel.todayMealsState.value)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun validateGoals(goals: NutritionGoals, sheet: ModalNutritionGoalsBinding): Boolean {
        val caloriesValid = goals.caloriesTarget > 0
        val proteinValid = goals.proteinTarget > 0f
        val fatValid = goals.fatTarget > 0f
        val carbsValid = goals.carbsTarget > 0f

        sheet.tilGoalCalories.error = if (caloriesValid) null else "Enter calories"
        sheet.tilGoalProtein.error = if (proteinValid) null else "Required"
        sheet.tilGoalFat.error = if (fatValid) null else "Required"
        sheet.tilGoalCarbs.error = if (carbsValid) null else "Required"

        return caloriesValid && proteinValid && fatValid && carbsValid
    }

    private fun renderMeals() {
        val raw = if (isHistoryMode) viewModel.historyMealsState.value else viewModel.todayMealsState.value
        val filtered = raw.filter {
            query.isBlank() || it.productName.orEmpty().contains(query, ignoreCase = true)
        }
        val items = if (isHistoryMode) buildHistoryItems(filtered) else buildTodayItems(filtered)
        mealsAdapter.submitList(items)

        binding.emptyState.isVisible = filtered.isEmpty()
        binding.rvTodayMeals.isVisible = filtered.isNotEmpty()
        binding.tvEmptyTitle.text = if (isHistoryMode) "No meals in history" else "No meals logged today"
        binding.tvEmptySubtitle.text = if (isHistoryMode) {
            "Saved meals will appear here grouped by date."
        } else {
            "Add your first meal or scan a product to start tracking."
        }
    }

    private fun buildTodayItems(meals: List<MealInfoEntity>): List<NutritionListItem> {
        val order = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Other")
        return meals
            .groupBy { it.mealType }
            .toSortedMap(compareBy { order.indexOf(it).let { index -> if (index == -1) 99 else index } })
            .flatMap { (type, list) ->
                val totals = list.totalsText()
                listOf(NutritionListItem.Section(type, totals)) +
                    list.sortedByDescending { it.date }.map { NutritionListItem.Meal(it) }
            }
    }

    private fun buildHistoryItems(meals: List<MealInfoEntity>): List<NutritionListItem> =
        meals
            .sortedByDescending { it.date }
            .groupBy { dayLabel(it.date) }
            .flatMap { (day, list) ->
                listOf(NutritionListItem.Section(day, list.totalsText())) +
                    list.map { NutritionListItem.Meal(it) }
            }

    private fun clearForm() = with(binding) {
        actMealType.setText("")
        actProductName.setText("")
        etCalories.setText("")
        etProtein.setText("")
        etFat.setText("")
        etCarbs.setText("")
    }

    private fun renderFormVisibility(show: Boolean, animate: Boolean) = with(binding.addMealCard) {
        if (show == isVisible) return
        if (!animate) {
            isVisible = show
            alpha = 1f
            translationY = 0f
            return
        }
        if (show) {
            alpha = 0f
            translationY = -14f
            isVisible = true
            animate().alpha(1f).translationY(0f).setDuration(220L).setInterpolator(AccelerateDecelerateInterpolator()).start()
        } else {
            animate().alpha(0f).translationY(-14f).setDuration(160L).withEndAction {
                isVisible = false
            }.start()
        }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.nutritionScroll) { view, insets ->
            // HomeActivity already applies status bar padding to the NavHost root.
            // Keeping this listener prevents double top padding while still participating in edge-to-edge dispatch.
            insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(view.paddingLeft, 0, view.paddingRight, view.paddingBottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.nutritionScroll)
    }

    private fun playIntro() {
        binding.content.alpha = 0f
        binding.content.translationY = 18f
        binding.content.animate().alpha(1f).translationY(0f).setDuration(280L).start()
    }

    private fun List<MealInfoEntity>.totalsText(): String {
        val calories = sumOf { (it.energyKcal100g ?: 0f).toDouble() }.roundToInt()
        val protein = sumOf { (it.proteins100g ?: 0f).toDouble() }.toFloat()
        val fat = sumOf { (it.fat100g ?: 0f).toDouble() }.toFloat()
        val carbs = sumOf { (it.carbohydrates100g ?: 0f).toDouble() }.toFloat()
        return "$calories kcal - P ${protein.clean()}g - F ${fat.clean()}g - C ${carbs.clean()}g"
    }

    private fun dayLabel(time: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return when {
            cal.isSameDay(today) -> "Today"
            cal.isSameDay(yesterday) -> "Yesterday"
            else -> SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(time))
        }
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    private fun percent(value: Float, target: Float): Int =
        if (target <= 0f) 0 else ((value / target) * 100f).roundToInt().coerceIn(0, 100)

    private fun Float.clean(): String =
        if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)

    private fun loadGoals(): NutritionGoals {
        val prefs = requireContext().getSharedPreferences(NUTRITION_PREFS, android.content.Context.MODE_PRIVATE)
        return NutritionGoals(
            caloriesTarget = prefs.getInt(KEY_CALORIES_TARGET, NutritionGoals.default().caloriesTarget),
            proteinTarget = prefs.getFloat(KEY_PROTEIN_TARGET, NutritionGoals.default().proteinTarget),
            fatTarget = prefs.getFloat(KEY_FAT_TARGET, NutritionGoals.default().fatTarget),
            carbsTarget = prefs.getFloat(KEY_CARBS_TARGET, NutritionGoals.default().carbsTarget)
        )
    }

    private fun saveGoals(goals: NutritionGoals) {
        requireContext().getSharedPreferences(NUTRITION_PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CALORIES_TARGET, goals.caloriesTarget)
            .putFloat(KEY_PROTEIN_TARGET, goals.proteinTarget)
            .putFloat(KEY_FAT_TARGET, goals.fatTarget)
            .putFloat(KEY_CARBS_TARGET, goals.carbsTarget)
            .apply()
    }

    data class NutritionGoals(
        val caloriesTarget: Int,
        val proteinTarget: Float,
        val fatTarget: Float,
        val carbsTarget: Float
    ) {
        companion object {
            // Customize default daily targets here.
            fun default(): NutritionGoals = NutritionGoals(
                caloriesTarget = 2200,
                proteinTarget = 160f,
                fatTarget = 60f,
                carbsTarget = 220f
            )
        }
    }

    private companion object {
        const val NUTRITION_PREFS = "nutrition_goals"
        const val KEY_CALORIES_TARGET = "calories_target"
        const val KEY_PROTEIN_TARGET = "protein_target"
        const val KEY_FAT_TARGET = "fat_target"
        const val KEY_CARBS_TARGET = "carbs_target"
    }
}
