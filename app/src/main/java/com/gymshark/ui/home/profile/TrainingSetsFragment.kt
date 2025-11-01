package com.gymshark.ui.home.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.gymshark.R
import com.gymshark.databinding.FragmentTrainingSetsBinding
import com.gymshark.utils.view.SetsDayView


class TrainingSetsFragment : Fragment(R.layout.fragment_training_sets) {

    private var _binding: FragmentTrainingSetsBinding? = null
    private val binding get() = _binding!!
    private val days = listOf<Int>(2, 3, 5)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrainingSetsBinding.bind(view)
        addIntViews(days)

    }

    fun Int.dp(context: Context) = (this * context.resources.displayMetrics.density).toInt()

    fun addIntViews(ints: List<Int>) {
        binding.linearLayout.removeAllViews()

        ints.forEach { value ->
            val tv = SetsDayView(requireContext()).apply {
                setDay(
                    when (value) {
                        0 -> "Calendar.MONDAY"
                        1 -> "Calendar.TUESDAY"
                        2 -> "Calendar.WEDNESDAY"
                        3 -> "Calendar.THURSDAY"
                        4 -> "Calendar.FRIDAY"
                        5 -> "Calendar.SATURDAY"
                        6 -> "Calendar.SUNDAY"
                        else -> "error()"
                    }
                )
            }

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val m = 6.dp(requireContext())
                setMargins(m, m, m, m)
            }

            binding.linearLayout.addView(tv, lp)
        }
    }


}