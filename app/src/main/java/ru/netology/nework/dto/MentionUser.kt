package ru.netology.nework.dto

data class MentionUser(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null,
    val isSelected: Boolean = false,
)

fun User.toMentionUser() = MentionUser(
    id = id,
    login = login,
    name = name,
    avatar = avatar,
    isSelected = false,
)