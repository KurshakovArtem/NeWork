package ru.netology.nework.model

data class ErrorReport (
    val postIdError: Long,
    val feedErrorMassage: FeedErrorMassage
)

enum class FeedErrorMassage{
    LIKE_ERROR, DISLIKE_ERROR, REMOVE_ERROR, SAVE_ERROR,
}