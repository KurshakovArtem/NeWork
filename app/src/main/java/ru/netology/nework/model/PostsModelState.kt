package ru.netology.nework.model

data class PostsModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val errorReport: ErrorReport? = null,
)