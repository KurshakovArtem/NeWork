package ru.netology.nework.model

import ru.netology.nework.dto.Post

data class PostFeedModel(
    val posts: List<Post> = emptyList(),
) {
    val empty: Boolean
        get() = posts.isEmpty()
}