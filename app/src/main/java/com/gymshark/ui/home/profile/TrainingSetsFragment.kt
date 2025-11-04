package com.gymshark.ui.home.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gymshark.R
import com.gymshark.databinding.FragmentTrainingSetsBinding
import com.gymshark.utils.view.SetsDayView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class TrainingSetsFragment : Fragment(R.layout.fragment_training_sets) {

    private var _binding: FragmentTrainingSetsBinding? = null
    private val binding get() = _binding!!

    private val vm: TrainingSetsViewModel by viewModel()
    private val locale: Locale get() = Locale.getDefault()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrainingSetsBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            vm.baseDays.collect { vm.syncWithBase() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.displayedDays.collect { list ->
                renderDays(list)
            }
        }

        binding.ivAdd.setOnClickListener { vm.addNext() }

    }

    private fun renderDays(days: List<Int>) {
        binding.linearLayout.removeAllViews()
        days.forEach { addDayView(it) }
    }

    fun showCheckboxAlertDialog(
        items: List<String>,
        onSelected: (selectedItems: List<String>) -> Unit
    ) {
        val selected = BooleanArray(items.size) { false }

        AlertDialog.Builder(requireContext())
            .setTitle("Виберіть опції")
            .setMultiChoiceItems(items.toTypedArray(), selected) { _, which, isChecked ->
                selected[which] = isChecked
            }
            .setPositiveButton("OK") { dialog, _ ->
                val result = items.filterIndexed { index, _ -> selected[index] }
                onSelected(result)
                dialog.dismiss()
            }
            .setNegativeButton("Відміна") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private val selectedItems =
        mutableListOf<String>("Груди", "Плечі", "Ноги", "Спина", "Біцепс", "Тріцепс")

    private fun addDayView(calendarValue: Int) {
        val label = calendarToLabel(calendarValue)
        val item = SetsDayView(requireContext()).apply {
            binding.ivAddType.setOnClickListener {
                showCheckboxAlertDialog(selectedItems){
                    binding.tvDayType.text = it.joinToString(", ")
                }
            }
            setDay(label)
        }
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val m = 6.dp(requireContext())
            setMargins(m, m, m, m)
        }
        binding.linearLayout.addView(item, lp)
    }

    private fun calendarToLabel(calendarValue: Int): String =
        when (calendarValue) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> null
        }?.getDisplayName(TextStyle.SHORT, locale) ?: "?"

    private fun Int.dp(context: Context) =
        (this * context.resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
