package ru.netology.nework.model

data class AuthModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val success: Boolean = false,
    val needRefresh: Boolean = false
)