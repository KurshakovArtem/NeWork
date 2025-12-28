package ru.netology.nework.dto

data class ResponsePost(
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
    val users: Map<Long, UserPreview>? = emptyMap()
)

fun ResponsePost.toPost(): Post {

    fun usersToList(): List<UserPreview>? =
        likeOwnerIds?.let { ids ->
            val usersPreview = mutableListOf<UserPreview>()
            for (id in ids) {
                val userPreview = users?.get(id) ?: UserPreview("Unknown", null)
                usersPreview.add(userPreview)
            }
            usersPreview
        }



    return Post(
        id = id,
        authorId = authorId,
        author = author,
        authorJob = authorJob,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        coords = coords,
        link = link,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likeOwnerIds = likeOwnerIds,
        likeByMe = likeByMe,
        attachment = attachment,
        users = usersToList()
    )
}