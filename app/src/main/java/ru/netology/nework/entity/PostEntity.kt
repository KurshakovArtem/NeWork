package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.enumeration.toAttachmentType

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val coords: Boolean = false,
    val coordsLat: Double = 0.0,
    val coordsLong: Double = 0.0,
    val link: String? = null,
    @param:TypeConverters(Converter::class)
    val mentionIds: List<Long>? = emptyList(),
    val mentionedMe: Boolean = false,
    @param:TypeConverters(Converter::class)
    val likeOwnerIds: List<Long>? = emptyList(),
    val likedByMe: Boolean = false,
    val attachment: Boolean = false,
    val attachmentUrl: String = "",
    val attachmentType: String = "",
    @param:TypeConverters(Converter::class)
    val users: Map<Long, UserPreview>? = emptyMap(),
    val ownedByMe: Boolean = false,
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        coords = if (!coords) null else Coordinates(
            lat = coordsLat, long = coordsLong
        ),
        link = link,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        attachment = if (!attachment) null else Attachment(
            url = attachmentUrl, type = attachmentType.toAttachmentType()
        ),
        users = users,
        ownedByMe = ownedByMe,
    )

    companion object {
        fun fromDto(dto: Post) = PostEntity(
            id = dto.id,
            authorId = dto.authorId,
            author = dto.author,
            authorJob = dto.authorJob,
            authorAvatar = dto.authorAvatar,
            content = dto.content,
            published = dto.published,
            coords = dto.coords != null,
            coordsLat = if (dto.coords != null) dto.coords.lat else 0.0,
            coordsLong = if (dto.coords != null) dto.coords.long else 0.0,
            link = dto.link,
            mentionIds = dto.mentionIds,
            mentionedMe = dto.mentionedMe,
            likeOwnerIds = dto.likeOwnerIds,
            likedByMe = dto.likedByMe,
            attachment = dto.attachment != null,
            attachmentUrl = if (dto.attachment != null) dto.attachment.url else "",
            attachmentType = if (dto.attachment != null) dto.attachment.type.toString() else "EMPTY",
            users = dto.users,
            ownedByMe = dto.ownedByMe,
        )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)