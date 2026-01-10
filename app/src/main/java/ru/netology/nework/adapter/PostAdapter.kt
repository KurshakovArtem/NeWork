package ru.netology.nework.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.supportingFunctions.convertResponseToCardPost
import ru.netology.nework.supportingFunctions.converterNumToString
import ru.netology.nework.supportingFunctions.loadAttachmentImage
import ru.netology.nework.supportingFunctions.loadAvatar


interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onVideo(post: Post) {}
    fun onMoveToSinglePost(post: Post) {}
    fun onSaveRefresh(post: Post) {}
    fun onMoveToSinglePhoto(post: Post) {}
}


class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int
    ) {
        val post = getItem(position)
        holder.bind(post)
    }

}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) = with(binding) {
        cardAuthor.text = post.author
        cardPublished.text = convertResponseToCardPost(post.published)
        cardContent.text = post.content

        if (!post.link.isNullOrBlank()) {
            cardLink.visibility = View.VISIBLE
            cardLink.text = post.link
        } else {
            cardLink.visibility = View.GONE
        }

        if (post.authorAvatar.isNullOrBlank()) {
            cardAvatar.setImageResource(R.drawable.ic_empty_avatar_24)
        } else {
            cardAvatar.loadAvatar(post.authorAvatar)
        }

        if (post.likedByMe) {
            cardLikeButton.setIconResource(R.drawable.ic_liked_24)
        } else {
            cardLikeButton.setIconResource(R.drawable.ic_like_24)
        }
        cardLikeButton.text = converterNumToString(post.likeOwnerIds?.size ?: 0)

        cardLikeButton.setOnClickListener {
            onInteractionListener.onLike(post)
        }

        cardPost.setOnClickListener {
            onInteractionListener.onMoveToSinglePost(post)
        }

        cardMenu.isVisible = post.ownedByMe
        cardMenu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.option_post)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.remove -> {
                            onInteractionListener.onRemove(post)
                            true
                        }

                        R.id.edit -> {
                            onInteractionListener.onEdit(post)
                            true
                        }

                        else -> false
                    }
                }
            }.show()
        }


        when (post.attachment?.type) {

            AttachmentType.EMPTY, null -> {
                cardAttachment.visibility = View.GONE
            }

            AttachmentType.IMAGE -> {
                cardAttachment.visibility = View.VISIBLE
                cardAttachmentImage.visibility = View.VISIBLE
                cardAttachmentVideo.visibility = View.GONE
                cardAttachmentImage.loadAttachmentImage(post.attachment.url)
            }

            AttachmentType.VIDEO -> {
                cardAttachment.visibility = View.VISIBLE
                cardAttachmentImage.visibility = View.GONE
                cardAttachmentVideo.apply {
                    visibility = View.VISIBLE
                    setMediaController(MediaController(binding.root.context))
                    setVideoURI(Uri.parse(post.attachment.url))
                    setOnPreparedListener {
                        animate().alpha(1F)
                        seekTo(0)
                        setZOrderOnTop(false)
                    }
                    setOnCompletionListener {
                        stopPlayback()
                    }
                }
            }

            AttachmentType.AUDIO -> {
                cardAttachment.visibility = View.VISIBLE
                cardAttachmentImage.visibility = View.GONE
                cardAttachmentVideo.apply {
                    visibility = View.VISIBLE
                    setMediaController(MediaController(binding.root.context))
                    setVideoURI(Uri.parse(post.attachment.url))
                    setBackgroundResource(R.drawable.audio)
                    setOnPreparedListener {
                        setZOrderOnTop(true)
                    }
                    setOnCompletionListener {
                        stopPlayback()
                    }
                }
            }

        }
    }

}


object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}