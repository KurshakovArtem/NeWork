package ru.netology.nework.dto

data class EventRequest(
    val id: Long,
    val content: String,
    val coords: Coordinates? = null,
    val link: String? = null,
    val attachment: Attachment? = null,
    val participantsIds: List<Long>? = emptyList(),
    val datetime: String,
    val type: String,
)