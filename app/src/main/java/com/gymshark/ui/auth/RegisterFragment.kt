package com.gymshark.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gymshark.R
import com.gymshark.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val vm: AuthViewModel by viewModel()
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                signInWithGoogleToFirebase(account)
            } catch (e: Exception) {
                binding.tvError.text = e.message ?: "Google Sign-In failed"
                vm.loading.value = false
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBinding.bind(view)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPass.text.toString()

            if (email.isEmpty() || pass.length < 6) {
                binding.tvError.text = "Перевір email і пароль (мін. 6 символів)"
                return@setOnClickListener
            }

            vm.register(email, pass)
        }

        binding.btnGoogleSignIn?.setOnClickListener {
            vm.loading.value = true
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        vm.loading.observe(viewLifecycleOwner) {
            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
        }
        vm.error.observe(viewLifecycleOwner) { binding.tvError.text = it.orEmpty() }
        vm.loggedIn.observe(viewLifecycleOwner) { if (it == true) goHome() }
    }

    private fun signInWithGoogleToFirebase(account: GoogleSignInAccount?) {
        if (account == null) {
            binding.tvError.text = "Google Sign-In failed"
            vm.loading.value = false
            return
        }
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                val uid = result.user?.uid ?: error("Google Sign-In: no UID returned")
                val email = result.user?.email ?: ""
                // Delegate to the ViewModel so it can create the local UserEntity
                // and persist the uid — identical to the email-registration path.
                vm.handleGoogleSignIn(uid, email)
            } catch (e: Exception) {
                binding.tvError.text = e.message ?: "Google Sign-In failed"
                vm.loading.value = false
            }
        }
    }

    private fun goHome() {
        startActivity(
            android.content.Intent(requireContext(), com.gymshark.ui.home.HomeActivity::class.java)
                .addFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                            android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
        )
        requireActivity().finish()
    }
}
