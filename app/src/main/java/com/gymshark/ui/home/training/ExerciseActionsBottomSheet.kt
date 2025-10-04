package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gymshark.R
import com.gymshark.data.models.Exercise
import com.gymshark.databinding.BsExerciseActionsBinding

class ExerciseActionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BsExerciseActionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var exercise: Exercise

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = BsExerciseActionsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        @Suppress("DEPRECATION")
        exercise = requireArguments().getParcelable(ARG_EXERCISE)!!
        binding.tvTitle.text = exercise.title

        if (binding.rowsContainer.isEmpty()) addRow()

        binding.btnPlus.setOnClickListener { addRow() }
        binding.btnMinus.setOnClickListener { removeRow() }
        binding.btnInfo.setOnClickListener { callbacks()?.onInfoClicked(exercise) }

        updateMinusEnabled()
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)

        val weights = collectWeights()
        val reps = collectReps()

        val maxWeight: Float? = weights.maxOrNull()
        val avgReps: Float? = if (reps.isNotEmpty()) reps.average().toFloat() else null
        val setsCount = binding.rowsContainer.childCount

        callbacks()?.onExerciseParamsChanged(
            exerciseId = exercise.id,
            maxWeight = maxWeight,
            avgReps = avgReps,
            sets = setsCount
        )
    }

    private fun collectWeights(): List<Float> {
        val res = mutableListOf<Float>()
        repeat(binding.rowsContainer.childCount) { i ->
            val row = binding.rowsContainer.getChildAt(i)
            val raw =
                row.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etRight)
                    ?.text?.toString().orEmpty()
            val normalized = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
            normalized.toFloatOrNull()?.let(res::add)
        }
        return res
    }

    private fun collectReps(): List<Int> {
        val res = mutableListOf<Int>()
        repeat(binding.rowsContainer.childCount) { i ->
            val row = binding.rowsContainer.getChildAt(i)
            val raw =
                row.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLeft)
                    ?.text?.toString().orEmpty()
            val onlyDigits = raw.filter { it.isDigit() }
            onlyDigits.toIntOrNull()?.let(res::add)
        }
        return res
    }

    private fun addRow() {
        val row = layoutInflater.inflate(R.layout.item_ex_params_row, binding.rowsContainer, false)
        binding.rowsContainer.addView(row)
        updateMinusEnabled()
    }

    private fun removeRow() {
        if (binding.rowsContainer.childCount > 1) {
            binding.rowsContainer.removeViewAt(binding.rowsContainer.childCount - 1)
        }
        updateMinusEnabled()
    }

    private fun updateMinusEnabled() {
        binding.btnMinus.isEnabled = binding.rowsContainer.childCount > 1
    }

    private fun callbacks(): Callbacks? = parentFragment as? Callbacks ?: activity as? Callbacks

    interface Callbacks {
        fun onInfoClicked(exercise: Exercise)

        fun onExerciseParamsChanged(
            exerciseId: Long,
            maxWeight: Float?,
            avgReps: Float?,
            sets: Int
        )
    }

    companion object {
        private const val ARG_EXERCISE = "arg_exercise"

        @JvmStatic
        fun newInstance(exercise: Exercise) = ExerciseActionsBottomSheet().apply {
            arguments = bundleOf(ARG_EXERCISE to exercise)
        }
    }
}
