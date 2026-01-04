package ru.netology.nework.dto

data class AuthState(
    val id: Long = 0,
    val token: String? = null,
    val avatar: String? = null,
)
