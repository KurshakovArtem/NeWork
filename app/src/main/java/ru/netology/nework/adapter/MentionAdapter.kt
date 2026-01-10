package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUsersMentionedBinding
import ru.netology.nework.dto.MentionUser

import ru.netology.nework.supportingFunctions.loadAvatar

interface OnMentionListener {
    fun onUserSelected(user: MentionUser, isSelected: Boolean)
}


class MentionAdapter(
    private val onMentionListener: OnMentionListener
) : ListAdapter<MentionUser, MentionViewHolder>(UserDiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MentionViewHolder {
        val binding =
            CardUsersMentionedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MentionViewHolder(binding, onMentionListener)
    }

    override fun onBindViewHolder(
        holder: MentionViewHolder,
        position: Int
    ) {
        val user = getItem(position)
        holder.bind(user)
    }
}


class MentionViewHolder(
    private val binding: CardUsersMentionedBinding,
    private val onMentionListener: OnMentionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: MentionUser) = with(binding) {
        mentionedCardAuthor.text = user.name
        if (user.avatar.isNullOrBlank()) {
            mentionedCardAvatar.setImageResource(R.drawable.ic_empty_avatar_24)
        } else {
            mentionedCardAvatar.loadAvatar(user.avatar)
        }

        checkboxMentioned.isChecked = user.isSelected

        checkboxMentioned.setOnCheckedChangeListener(null)

        checkboxMentioned.setOnCheckedChangeListener { _ , isChecked ->
            onMentionListener.onUserSelected(user, isChecked)
        }
    }

}


object UserDiffCallback : DiffUtil.ItemCallback<MentionUser>() {
    override fun areItemsTheSame(oldItem: MentionUser, newItem: MentionUser): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MentionUser, newItem: MentionUser): Boolean {
        return oldItem == newItem
    }
}