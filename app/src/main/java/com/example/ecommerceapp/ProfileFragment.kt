package com.example.ecommerceapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ecommerceapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        loadUserDetails()
        setupButtons()
    }

    private fun loadUserDetails() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvProfileEmail.text = currentUser.email ?: "No email available"
            binding.tvProfileName.text = currentUser.displayName ?: "User"
        } else {
            binding.tvProfileName.text = "Guest"
            binding.tvProfileEmail.text = "Not logged in"
        }
    }

    private fun setupButtons() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.btnMyDashboard.setOnClickListener {
            val intent = Intent(requireActivity(), CustomerDashboard::class.java)
            startActivity(intent)
        }

        binding.btnMyOrders.setOnClickListener {
            val intent = Intent(requireActivity(), Orders::class.java)
            startActivity(intent)
        }

        binding.btnMyLaybys.setOnClickListener {
            val intent = Intent(requireActivity(), Laybys::class.java)
            startActivity(intent)
        }

        binding.btnLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Language settings clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotifications.setOnClickListener {
            val intent = Intent(requireActivity(), Notifications::class.java)
            startActivity(intent)
        }

        binding.btnPrivacy.setOnClickListener {
            val intent = Intent(requireActivity(), PrivacyPolicy::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
