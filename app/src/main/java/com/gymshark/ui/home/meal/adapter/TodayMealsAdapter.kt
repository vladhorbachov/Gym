package com.gymshark.ui.home.meal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.data.db.entity.MealInfoEntity
import com.gymshark.databinding.ItemNutritionSectionBinding
import com.gymshark.databinding.ItemTodayMealBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class NutritionListItem {
    data class Section(
        val title: String,
        val meta: String
    ) : NutritionListItem()

    data class Meal(
        val meal: MealInfoEntity
    ) : NutritionListItem()
}

class TodayMealsAdapter(
    private val onDeleteClick: (MealInfoEntity) -> Unit
) : ListAdapter<NutritionListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is NutritionListItem.Section -> VIEW_SECTION
            is NutritionListItem.Meal -> VIEW_MEAL
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_SECTION) {
            SectionViewHolder(ItemNutritionSectionBinding.inflate(inflater, parent, false))
        } else {
            MealViewHolder(ItemTodayMealBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is NutritionListItem.Section -> (holder as SectionViewHolder).bind(item)
            is NutritionListItem.Meal -> (holder as MealViewHolder).bind(item.meal)
        }
    }

    inner class SectionViewHolder(
        private val binding: ItemNutritionSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NutritionListItem.Section) = with(binding) {
            tvSectionTitle.text = item.title
            tvSectionMeta.text = item.meta
        }
    }

    inner class MealViewHolder(
        private val binding: ItemTodayMealBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MealInfoEntity) = with(binding) {
            val calories = item.energyKcal100g ?: 0f
            val protein = item.proteins100g ?: 0f
            val fat = item.fat100g ?: 0f
            val carbs = item.carbohydrates100g ?: 0f

            tvMealName.text = item.productName.orEmpty().ifBlank { "Unnamed meal" }
            tvMealType.text = "${item.mealType} - ${timeFormat.format(Date(item.date))}"
            tvMealCalories.text = "${calories.clean()} kcal"
            tvMealMacros.text = "P ${protein.clean()}g - F ${fat.clean()}g - C ${carbs.clean()}g"

            btnDeleteMeal.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NutritionListItem>() {
        override fun areItemsTheSame(oldItem: NutritionListItem, newItem: NutritionListItem): Boolean =
            when {
                oldItem is NutritionListItem.Section && newItem is NutritionListItem.Section -> oldItem.title == newItem.title
                oldItem is NutritionListItem.Meal && newItem is NutritionListItem.Meal -> oldItem.meal.id == newItem.meal.id
                else -> false
            }

        override fun areContentsTheSame(oldItem: NutritionListItem, newItem: NutritionListItem): Boolean =
            oldItem == newItem
    }

    private fun Float.clean(): String =
        if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)

    private companion object {
        const val VIEW_SECTION = 0
        const val VIEW_MEAL = 1
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
}
