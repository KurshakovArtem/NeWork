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
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.supportingFunctions.convertResponseToCardPost
import ru.netology.nework.supportingFunctions.converterNumToString
import ru.netology.nework.supportingFunctions.loadAttachmentImage
import ru.netology.nework.supportingFunctions.loadAvatar
import androidx.core.net.toUri

interface OnEventListener {
    fun onLike(event: Event) {}
    fun onParticipants(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onShare(event: Event) {}
    fun onMoveToSinglePost(event: Event) {}
}

class EventAdapter
    (
    private val onEventListener: OnEventListener
) : ListAdapter<Event, EventViewHolder>(EventDiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventViewHolder {
        val binding = CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onEventListener)
    }

    override fun onBindViewHolder(
        holder: EventViewHolder,
        position: Int
    ) {
        val event = getItem(position)
        holder.bind(event)
    }

}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventListener: OnEventListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) = with(binding) {
        cardAuthor.text = event.author
        cardPublished.text = convertResponseToCardPost(event.published)
        cardContent.text = event.content

        if (!event.link.isNullOrBlank()) {
            cardLink.visibility = View.VISIBLE
            cardLink.text = event.link
        } else {
            cardLink.visibility = View.GONE
        }

        if (event.authorAvatar.isNullOrBlank()) {
            cardAvatar.setImageResource(R.drawable.ic_empty_avatar_24)
        } else {
            cardAvatar.loadAvatar(event.authorAvatar)
        }

        if (event.likedByMe) {
            cardLikeButton.setIconResource(R.drawable.ic_liked_24)
        } else {
            cardLikeButton.setIconResource(R.drawable.ic_like_24)
        }

        if (event.participatedByMe) {
            cardParticipantsButton.setIconResource(R.drawable.ic_people_24)
        }else{
            cardParticipantsButton.setIconResource(R.drawable.ic_people_outline_24)
        }

        cardLikeButton.text = converterNumToString(event.likeOwnerIds?.size ?: 0)

        cardTypeEvent.text = event.type.toString()

        cardDateEvent.text = convertResponseToCardPost(event.published)

        if (!event.participantsIds.isNullOrEmpty()) {
            cardParticipantsButton.text = event.participantsIds.size.toString()
        } else {
            cardParticipantsButton.text = "0"
        }

        cardLikeButton.setOnClickListener {
            onEventListener.onLike(event)
        }

        cardShareButton.setOnClickListener {
            onEventListener.onShare(event)
        }

        cardEvent.setOnClickListener {
            onEventListener.onMoveToSinglePost(event)
        }

        cardParticipantsButton.setOnClickListener {
            onEventListener.onParticipants(event)
        }

        cardMenu.isVisible = event.ownedByMe
        cardMenu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.option_post)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.remove -> {
                            onEventListener.onRemove(event)
                            true
                        }

                        R.id.edit -> {
                            onEventListener.onEdit(event)
                            true
                        }

                        else -> false
                    }
                }
            }.show()
        }

        when (event.attachment?.type) {

            AttachmentType.EMPTY, null -> {
                cardAttachment.visibility = View.GONE
            }

            AttachmentType.IMAGE -> {
                cardAttachment.visibility = View.VISIBLE
                cardAttachmentImage.visibility = View.VISIBLE
                cardAttachmentVideo.visibility = View.GONE
                cardAttachmentImage.loadAttachmentImage(event.attachment.url)
            }

            AttachmentType.VIDEO -> {
                cardAttachment.visibility = View.VISIBLE
                cardAttachmentImage.visibility = View.GONE
                cardAttachmentVideo.apply {
                    visibility = View.VISIBLE
                    setMediaController(MediaController(binding.root.context))
                    setVideoURI(event.attachment.url.toUri())
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
                    setVideoURI(event.attachment.url.toUri())
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

object EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}


