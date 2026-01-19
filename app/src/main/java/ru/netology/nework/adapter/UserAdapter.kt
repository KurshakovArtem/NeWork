package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.dto.User
import ru.netology.nework.supportingFunctions.loadAvatar


interface OnUserListener {
    fun onUser(user: User) {}
}

class UserAdapter(
    private val onUserListener: OnUserListener
) : ListAdapter<User, UserViewHolder>(UserDiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val binding =
            CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onUserListener)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUserListener: OnUserListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) = with(binding) {
        userCardAuthor.text = user.name
        if (user.avatar.isNullOrBlank()) {
            userCardAvatar.setImageResource(R.drawable.ic_empty_avatar_24)
        } else {
            userCardAvatar.loadAvatar(user.avatar)
        }
        userCardLogin.text = user.login
        cardUser.setOnClickListener {
            onUserListener.onUser(user)
        }
    }
}

object UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}