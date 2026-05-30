package com.gymshark.ui.home.training

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.45f)
        }
        (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val ex = trainingVm.getExerciseDraft(exerciseId)

        binding.tvTitle.text = ex?.title ?: "Exercise"
        playIntro()

        val sets = ex?.sets.orEmpty()
        if (binding.rowsContainer.isEmpty()) {
            if (sets.isEmpty()) {
                addRow()
            } else {
                sets.forEach { addRowWithValues(it) }
            }
        }

        binding.btnPlus.setOnClickListener {
            it.pressPulse()
            addRow()
        }
        binding.btnMinus.setOnClickListener {
            it.pressPulse()
            removeRow()
        }
        binding.btnDone.setOnClickListener {
            it.pressPulse()
            dismiss()
        }

        binding.btnInfo.setOnClickListener {
            it.pressPulse()
            ExerciseInfoBottomSheet
                .newInstance(
                    title = ex?.title.orEmpty(),
                    category = ex?.baseCategory.orEmpty()
                )
                .show(parentFragmentManager, "exercise_info")
        }

        updateMinusEnabled()
        updateSummary()
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
        configureRow(row, binding.rowsContainer.childCount)
        animateRowIn(row)
        updateMinusEnabled()
        updateSummary()
    }

    private fun addRowWithValues(set: SetEntry) {
        val row = layoutInflater.inflate(R.layout.item_ex_params_row, binding.rowsContainer, false)
        row.findViewById<TextInputEditText>(R.id.etLeft)?.setText(set.reps?.toString().orEmpty())
        row.findViewById<TextInputEditText>(R.id.etRight)?.setText(formatWeight(set.weight))
        binding.rowsContainer.addView(row)
        configureRow(row, binding.rowsContainer.childCount)
        animateRowIn(row)
        updateMinusEnabled()
        updateSummary()
    }

    private fun removeRow() {
        if (binding.rowsContainer.childCount > 1) {
            binding.rowsContainer.removeViewAt(binding.rowsContainer.childCount - 1)
        }
        renumberRows()
        updateMinusEnabled()
        updateSummary()
    }

    private fun updateMinusEnabled() {
        binding.btnMinus.isEnabled = binding.rowsContainer.childCount > 1
        binding.btnMinus.alpha = if (binding.btnMinus.isEnabled) 1f else 0.45f
    }

    private fun configureRow(row: View, number: Int) {
        row.findViewById<TextView>(R.id.tvSetNumber)?.text = number.toString()
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSummary()
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        }
        row.findViewById<TextInputEditText>(R.id.etLeft)?.addTextChangedListener(watcher)
        row.findViewById<TextInputEditText>(R.id.etRight)?.addTextChangedListener(watcher)
    }

    private fun renumberRows() {
        repeat(binding.rowsContainer.childCount) { index ->
            binding.rowsContainer.getChildAt(index)
                .findViewById<TextView>(R.id.tvSetNumber)
                ?.text = (index + 1).toString()
        }
    }

    private fun updateSummary() {
        val sets = collectSets()
        val totalReps = sets.sumOf { it.reps ?: 0 }
        val topWeight = sets.mapNotNull { it.weight }.maxOrNull()

        binding.tvSetCount.text = binding.rowsContainer.childCount.toString()
        binding.tvTotalReps.text = totalReps.toString()
        binding.tvTopWeight.text = formatWeight(topWeight).ifBlank { "-" }
    }

    private fun formatWeight(value: Float?): String {
        val weight = value ?: return ""
        return if (weight % 1f == 0f) weight.toInt().toString() else "%.1f".format(weight)
    }

    private fun playIntro() = with(binding) {
        listOf(tvKicker, tvTitle, summaryRow, tableHeader, rowsContainer, btnPlus, btnDone)
            .forEachIndexed { index, target ->
                target.alpha = 0f
                target.translationY = 18f
                target.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(index * 35L)
                    .setDuration(220L)
                    .start()
            }
    }

    private fun animateRowIn(row: View) {
        row.alpha = 0f
        row.translationY = 18f
        row.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(180L)
            .start()
    }

    private fun View.pressPulse() {
        animate().scaleX(0.96f).scaleY(0.96f).setDuration(70L).withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(110L).start()
        }.start()
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
