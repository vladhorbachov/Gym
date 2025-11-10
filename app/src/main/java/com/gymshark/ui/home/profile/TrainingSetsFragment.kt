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
import com.gymshark.data.models.DaySlot
import com.gymshark.databinding.FragmentTrainingSetsBinding
import com.gymshark.utils.view.SetsDayView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
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
            vm.baseDays.collectLatest { vm.syncWithBase() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.displayedSlots.collectLatest { renderSlots(it) }
        }

        binding.ivAdd.setOnClickListener {
            vm.addNext()
            Log.d("TSF", "add clicked; baseDays=${vm.baseDays.value} slots=${vm.displayedSlots.value}")
        }
    }

    private fun renderSlots(slots: List<DaySlot>) {
        binding.linearLayout.removeAllViews()
        slots.forEach { addSlotView(it) }
    }

    private fun addSlotView(slot: DaySlot) {
        val label = dayOfWeekLabel(slot.day)
        val item = SetsDayView(requireContext()).apply {
            setDay(label)
            setTypesText(
                if (slot.types.isEmpty()) getString(R.string.choose_types_placeholder)
                else slot.types.joinToString(", ")
            )
            setOnAddTypeClick {
                val all = vm.categories.value
                val pre = slot.types.toSet()
                showCheckboxAlertDialog(all, pre) { chosen ->
                    vm.setSlotTypes(slot.id, chosen)
                    setTypesText(
                        if (chosen.isEmpty()) getString(R.string.choose_types_placeholder)
                        else chosen.joinToString(", ")
                    )
                }
            }
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

    private fun showCheckboxAlertDialog(
        items: List<String>,
        preselected: Set<String>,
        onSelected: (selectedItems: List<String>) -> Unit
    ) {
        val checked = BooleanArray(items.size) { i -> items[i] in preselected }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_types_title))
            .setMultiChoiceItems(items.toTypedArray(), checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                val result = items.filterIndexed { index, _ -> checked[index] }
                onSelected(result)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun dayOfWeekLabel(day: DayOfWeek): String =
        day.getDisplayName(TextStyle.SHORT, locale)

    private fun Int.dp(context: Context) =
        (this * context.resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
