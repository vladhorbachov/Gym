package com.gymshark.ui.auth

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.gymshark.R
import com.gymshark.databinding.FragmentLoginBinding
import com.gymshark.utils.BaseFragment
import com.gymshark.utils.visibleIf
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {
    private val vm: AuthViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        vm.loading.observe(viewLifecycleOwner) {
            binding.progress.visibleIf(it)
        }
        vm.error.observe(viewLifecycleOwner) { binding.tvError.text = it.orEmpty() }
        vm.loggedIn.observe(viewLifecycleOwner) { if (it == true) goHome() }
    }

    private fun goHome() {
        val intent =
            android.content.Intent(requireContext(), com.gymshark.ui.home.HomeActivity::class.java)
                .addFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                            android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
        startActivity(intent)
        requireActivity().finish()
    }


    private fun initView() {

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPass.text.toString()
            if (email.isEmpty() || pass.length < 6) {
                binding.tvError.text = getString(R.string.email_6)
            } else vm.login(email, pass)
        }
        binding.tvToRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }
}
