package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumeration.toAttachmentType
import ru.netology.nework.enumeration.toEventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Boolean = false,
    val coordsLat: Double = 0.0,
    val coordsLong: Double = 0.0,
    val type: String = "",
    @param:TypeConverters(Converter::class)
    val likeOwnerIds: List<Long>? = emptyList(),
    val likedByMe: Boolean = false,
    @param:TypeConverters(Converter::class)
    val speakerIds: List<Long>? = emptyList(),
    @param:TypeConverters(Converter::class)
    val participantsIds: List<Long>? = emptyList(),
    val participatedByMe: Boolean = false,
    val attachment: Boolean = false,
    val attachmentUrl: String = "",
    val attachmentType: String = "",
    val link: String? = null,
    @param:TypeConverters(Converter::class)
    val users: Map<Long, UserPreview>? = emptyMap(),
    val ownedByMe: Boolean = false
) {
    fun toEvent() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        datetime = datetime,
        published = published,
        coords = if (!coords) null else Coordinates(
            lat = coordsLat, long = coordsLong
        ),
        type = type.toEventType(),
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        speakerIds = speakerIds,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        attachment = if (!attachment) null else Attachment(
            url = attachmentUrl, type = attachmentType.toAttachmentType()
        ),
        link = link,
        users = users,
        ownedByMe = ownedByMe,
    )

    companion object {
        fun fromEvent(event: Event) = EventEntity(
            id = event.id,
            authorId = event.authorId,
            author = event.author,
            authorJob = event.authorJob,
            authorAvatar = event.authorAvatar,
            content = event.content,
            datetime = event.datetime,
            published = event.published,
            coords = event.coords != null,
            coordsLat = if (event.coords != null) event.coords.lat else 0.0,
            coordsLong = if (event.coords != null) event.coords.long else 0.0,
            type = event.type.toString(),
            likeOwnerIds = event.likeOwnerIds,
            likedByMe = event.likedByMe,
            speakerIds = event.speakerIds,
            participantsIds = event.participantsIds,
            participatedByMe = event.participatedByMe,
            attachment = event.attachment != null,
            attachmentUrl = if (event.attachment != null) event.attachment.url else "",
            attachmentType = if (event.attachment != null) event.attachment.type.toString() else "EMPTY",
            link = event.link,
            users = event.users,
            ownedByMe = event.ownedByMe
        )
    }
}

fun List<EventEntity>.toEvent(): List<Event> = map(EventEntity::toEvent)
fun List<Event>.toEventEntity(): List<EventEntity> = map(EventEntity::fromEvent)