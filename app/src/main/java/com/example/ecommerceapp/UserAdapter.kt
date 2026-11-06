package com.example.ecommerceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerceapp.databinding.ItemAdminUserBinding

data class UserAccount(
    val documentId: String = "",
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val role: String = "customer",
    val accountStatus: Boolean = true
)

class UserAdapter(
    private var users: List<UserAccount>,
    private val onToggleAccount: (UserAccount) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(
        val binding: ItemAdminUserBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemAdminUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val binding = holder.binding

        binding.tvUserName.text =
            "${user.firstName} ${user.lastName}".trim().ifBlank { "Unnamed User" }

        binding.tvUserEmail.text = user.email
        binding.tvUserRole.text = user.role.replaceFirstChar { it.uppercase() }

        if (user.accountStatus) {
            binding.tvAccountStatus.text = "Active"
            binding.tvAccountStatus.setTextColor(
                android.graphics.Color.rgb(22, 131, 74)
            )
        } else {
            binding.tvAccountStatus.text = "Inactive"
            binding.tvAccountStatus.setTextColor(
                android.graphics.Color.rgb(190, 50, 50)
            )
        }

        val isAdmin = user.role.equals("admin", ignoreCase = true)

        binding.btnToggleAccount.isEnabled = !isAdmin
        binding.btnToggleAccount.alpha = if (isAdmin) 0.5f else 1f

        binding.btnToggleAccount.text = when {
            isAdmin -> "Admin Account"
            user.accountStatus -> "Deactivate Account"
            else -> "Activate Account"
        }

        binding.btnToggleAccount.setOnClickListener {
            if (!isAdmin) {
                onToggleAccount(user)
            }
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<UserAccount>) {
        users = newUsers
        notifyDataSetChanged()
    }
}