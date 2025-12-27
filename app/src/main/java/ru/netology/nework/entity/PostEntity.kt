package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.UserPreview

@Entity
data class PostEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    val coords: Coordinates? = null,
    val link: String? = null,
    val mentionIds: List<Long>? = emptyList(),
    val mentionedMe: Boolean = false,
    val likeOwnerIds: List<Long>? = emptyList(),
    val likeByMe: Boolean = false,
    val attachment: Attachment? = null,
    val users: List<UserPreview>? = emptyList()
)