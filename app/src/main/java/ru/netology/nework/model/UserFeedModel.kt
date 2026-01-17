package ru.netology.nework.model

import ru.netology.nework.dto.User

data class UserFeedModel(
    val users: List<User> = emptyList()
){
    val empty: Boolean
        get() = users.isEmpty()
}