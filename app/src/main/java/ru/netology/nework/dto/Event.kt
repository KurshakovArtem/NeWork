package ru.netology.nework.dto

import ru.netology.nework.enumeration.EventType

data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates? = null,
    val type: EventType = EventType.ONLINE,
    val likeOwnerIds: List<Long>? = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Long>? = emptyList(),
    val participantsIds: List<Long>? = emptyList(),
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val users: Map<Long, UserPreview>? = emptyMap(),
    val ownedByMe: Boolean = false
)
