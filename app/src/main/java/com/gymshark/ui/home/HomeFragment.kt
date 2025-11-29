package com.gymshark.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gymshark.R
import com.gymshark.databinding.FragmentHomeBinding
import com.gymshark.ui.home.training.TrainingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val trainingVm: TrainingViewModel by viewModel()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        with(binding) {

            cvCalendar.onDayClick = { date, train, types ->

                val effectiveTrain = train ?: run {
                    val today = LocalDate.now()
                    if (date == today) {
                        trainingVm.todayRecommendedTrainFlow.value
                    } else {
                        null
                    }
                }

                val title = effectiveTrain?.title ?: getString(R.string.no_train_selected)

                val muscles = if (types.isEmpty()) {
                    getString(R.string.no_muscles_planned)
                } else {
                    types.joinToString(", ")
                }

                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(
                        getString(
                            R.string.calendar_popup_title,
                            date.dayOfMonth,
                            date.monthValue,
                            date.year
                        )
                    )
                    .setMessage(
                        getString(
                            R.string.calendar_popup_message,
                            title,
                            if (muscles.isBlank()) getString(R.string.no_muscles_planned) else muscles
                        )
                    )
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    trainingVm.trainsFlow.collect { trains ->
                        binding.cvCalendar.setTrains(trains)
                    }
                }
                launch {
                    trainingVm.trainingDaysFlow.collect { calendarDays ->
                        binding.cvCalendar.setTrainingDays(calendarDays)
                    }
                }
                launch {
                    trainingVm.trainingSlotsFlow.collect { slots ->
                        binding.cvCalendar.setTrainingSlots(slots)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
