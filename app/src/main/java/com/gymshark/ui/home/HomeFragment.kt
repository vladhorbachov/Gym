package com.gymshark.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.gymshark.R
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.data.models.Pentagon
import com.gymshark.data.models.Series
import com.gymshark.data.models.TrainingDay
import com.gymshark.databinding.FragmentHomeBinding
import com.gymshark.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val authVm: AuthViewModel by viewModel()
    private val vm: HomeViewModel by viewModel()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

//        prefill()
//        binding.btnLogout.setOnClickListener { logout() }
//        binding.btnSaveUser.setOnClickListener { save() }
//        observe()
    }

//    private fun observe() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                vm.user.collect { u ->
//                    val msg = if (u != null) {
//                        buildString {
//                            appendLine("Saved & loaded:")
//                            appendLine("ID: ${u.userId}")
//                            appendLine("Name: ${u.name}, Age: ${u.age}")
//                            appendLine("Sex: ${if (u.sex) "Male" else "Female"}")
//                            appendLine("WH: ${u.weight}kg / ${u.height}cm")
//                            appendLine("BPM min/avg/max: ${u.minBPM}/${u.avgBPM}/${u.maxBPM}")
//                            appendLine("Days: ${u.trainingDay.days.joinToString()}")
//                            appendLine("Pentagon: s=${u.pentagon.strength}, p=${u.pentagon.power}, a=${u.pentagon.agility}, e=${u.pentagon.endurance}, m=${u.pentagon.mobility}")
//                            appendLine("Series: cur=${u.series.current}/${u.series.maxSeries}, active=${u.series.isActive}")
//                        }
//                    } else "User not found"
//                    android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    private fun logout() {
//        authVm.signOut()
//        val opts = navOptions { popUpTo(R.id.homeFragment) { inclusive = true } }
//        findNavController().navigate(R.id.loginFragment, null, opts)
//    }
//
//    private fun save() {
//        vm.saveUser(buildUser())
//    }
//
//    private fun buildUser(): UserEntity = with(binding) {
//        UserEntity(
//            userId = etUserId.text.toString().trim(),
//            name = etName.text.toString().trim(),
//            age = etAge.text.toString().toIntOrZero(),
//            sex = switchSex.isChecked,
//            weight = etWeight.text.toString().toFloatOrZero(),
//            height = etHeight.text.toString().toIntOrZero(),
//            minBPM = etMinBPM.text.toString().toIntOrZero(),
//            maxBPM = etMaxBPM.text.toString().toIntOrZero(),
//            avgBPM = etAvgBPM.text.toString().toIntOrZero(),
//            pentagon = Pentagon(
//                strength = 6, power = 7, agility = 5, endurance = 6, mobility = 4
//            ),
//            trainingDay = TrainingDay(days = selectedDays()),
//            series = Series(current = 3, maxSeries = 10, isActive = true)
//        )
//    }
//
//    private fun selectedDays(): MutableList<Int> = with(binding) {
//        val result = mutableListOf<Int>()
//        if (chMon.isChecked) result += 1
//        if (chTue.isChecked) result += 2
//        if (chWed.isChecked) result += 3
//        if (chThu.isChecked) result += 4
//        if (chFri.isChecked) result += 5
//        if (chSat.isChecked) result += 6
//        if (chSun.isChecked) result += 7
//        result
//    }
//
//    private fun prefill() = with(binding) {
//        etUserId.text?.replace(0, etUserId.length(), "u_12345")
//        etName.text?.replace(0, etName.length(), "John Doe")
//        etAge.text?.replace(0, etAge.length(), "26")
//        switchSex.isChecked = true
//        etWeight.text?.replace(0, etWeight.length(), "78.5")
//        etHeight.text?.replace(0, etHeight.length(), "182")
//        etMinBPM.text?.replace(0, etMinBPM.length(), "48")
//        etMaxBPM.text?.replace(0, etMaxBPM.length(), "192")
//        etAvgBPM.text?.replace(0, etAvgBPM.length(), "76")
//        chMon.isChecked = true
//        chWed.isChecked = true
//        chFri.isChecked = true
//    }
//
//    private fun String.toIntOrZero(): Int = trim().toIntOrNull() ?: 0
//    private fun String.toFloatOrZero(): Float = trim().toFloatOrNull() ?: 0f

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
