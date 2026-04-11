package com.gymshark.ui.home.meal.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gymshark.data.db.entity.MealInfoEntity
import com.gymshark.databinding.ItemTodayMealBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayMealsAdapter(
    private val onDeleteClick: (MealInfoEntity) -> Unit
) : ListAdapter<MealInfoEntity, TodayMealsAdapter.MealViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemTodayMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MealViewHolder(
        private val binding: ItemTodayMealBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MealInfoEntity) {
            binding.tvMealName.text = item.productName
            binding.tvMealType.text = item.mealType
            binding.tvMealCalories.text = "${item.energyKcal100g ?: 0f} kcal"

            binding.tvMealTime.text = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date(item.date))

            binding.btnDeleteMeal.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MealInfoEntity>() {
        override fun areItemsTheSame(oldItem: MealInfoEntity, newItem: MealInfoEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MealInfoEntity, newItem: MealInfoEntity): Boolean {
            return oldItem == newItem
        }
    }
}