package com.gymshark.ui.home.profile.modal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.gymshark.data.db.entity.UserEntity
import com.gymshark.databinding.ModalProfileDetailsBinding
import com.gymshark.domain.models.Pentagon
import com.gymshark.domain.models.Series
import com.gymshark.ui.home.profile.ProfileViewModel
import com.gymshark.utils.view.hapticConfirm
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ProfileDetailsBottomSheet : BaseSettingsBottomSheet() {

    private val vm: ProfileViewModel by activityViewModel()
    private lateinit var binding: ModalProfileDetailsBinding
    private var currentUser: UserEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ModalProfileDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            currentUser = vm.loadUser()
            bindUser()
        }

        binding.btnSave.hapticConfirm()
        binding.btnSave.setOnClickListener {
            saveDetails()
        }
    }

    private fun bindUser() {
        val user = currentUser
        binding.header.tvTitle.text = "Personal details"
        binding.etName.setText(user?.name.orEmpty())
        binding.etAge.setText(user?.age?.takeIf { it > 0 }?.toString().orEmpty())
        binding.swSex.isChecked = user?.sex == true
        binding.etHeight.setText(user?.height?.takeIf { it > 0 }?.toString().orEmpty())
    }

    private fun saveDetails() {
        val user = currentUser ?: UserEntity(
            userId = "",
            name = "",
            age = 0,
            sex = false,
            weight = 0f,
            height = 0,
            minBPM = 0,
            maxBPM = 0,
            avgBPM = 0,
            pentagon = Pentagon(0, 0, 0, 0, 0),
            series = Series(0, 0, 0L)
        )

        val updated = user.copy(
            name = binding.etName.text?.toString().orEmpty(),
            age = binding.etAge.text?.toString()?.toIntOrNull() ?: 0,
            sex = binding.swSex.isChecked,
            height = binding.etHeight.text?.toString()?.toIntOrNull() ?: 0
        )

        vm.saveUser(updated)
        dismiss()
    }
}
