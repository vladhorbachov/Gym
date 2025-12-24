package com.gymshark.ui.home.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.gymshark.R
import com.gymshark.databinding.BsExerciseActionsBinding
import com.gymshark.ui.home.training.drafts.SetEntry
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ExerciseActionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BsExerciseActionsBinding? = null
    private val binding get() = _binding!!

    private val trainingVm: TrainingViewModel by activityViewModel()

    private var exerciseId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseId = requireArguments().getLong(ARG_EXERCISE_ID)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = BsExerciseActionsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val ex = trainingVm.getExerciseDraft(exerciseId)

        binding.tvTitle.text = ex?.title ?: "Exercise"

        val sets = ex?.sets.orEmpty()
        if (binding.rowsContainer.isEmpty()) {
            if (sets.isEmpty()) {
                addRow()
            } else {
                sets.forEach { addRowWithValues(it) }
            }
        }

        binding.btnPlus.setOnClickListener { addRow() }
        binding.btnMinus.setOnClickListener { removeRow() }

        binding.btnInfo.setOnClickListener {}

        updateMinusEnabled()
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)

        val newSets = collectSets()
        trainingVm.updateExerciseSets(exerciseId, newSets)
    }

    private fun collectSets(): List<SetEntry> = buildList {
        repeat(binding.rowsContainer.childCount) { i ->
            val row = binding.rowsContainer.getChildAt(i)

            val repsRaw = row.findViewById<TextInputEditText>(R.id.etLeft)
                ?.text?.toString().orEmpty()
            val weightRaw = row.findViewById<TextInputEditText>(R.id.etRight)
                ?.text?.toString().orEmpty()

            val reps = repsRaw.filter { it.isDigit() }.toIntOrNull()
            val normalizedW = weightRaw.replace(',', '.').filter { it.isDigit() || it == '.' }
            val weight = normalizedW.toFloatOrNull()

            add(SetEntry(reps = reps, weight = weight))
        }
    }

    private fun addRow() {
        val row = layoutInflater.inflate(R.layout.item_ex_params_row, binding.rowsContainer, false)
        binding.rowsContainer.addView(row)
        updateMinusEnabled()
    }

    private fun addRowWithValues(set: SetEntry) {
        val row = layoutInflater.inflate(R.layout.item_ex_params_row, binding.rowsContainer, false)
        row.findViewById<TextInputEditText>(R.id.etLeft)?.setText(set.reps?.toString().orEmpty())
        row.findViewById<TextInputEditText>(R.id.etRight)?.setText(set.weight?.toString().orEmpty())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_EXERCISE_ID = "arg_exercise_id"

        fun newInstance(exerciseId: Long) = ExerciseActionsBottomSheet().apply {
            arguments = bundleOf(ARG_EXERCISE_ID to exerciseId)
        }
    }
}
